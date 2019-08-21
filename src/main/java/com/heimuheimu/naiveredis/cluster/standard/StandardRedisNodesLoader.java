/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.naiveredis.cluster.standard;

import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.clients.AbstractDirectRedisClient;
import com.heimuheimu.naiveredis.command.cluster.ClusterSlotsCommand;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.RedisClusterHashSlotLocator;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 集群节点信息加载器。
 *
 * <p><strong>说明：</strong>StandardRedisNodesLoader 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class StandardRedisNodesLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardRedisNodesLoader.class);

    /**
     * 加载并返回 Redis 集群所有节点信息，所有节点必须完整覆盖从 0 至 16383 的 slots 区间。
     *
     * @param bootstrapHosts Redis 集群主机地址列表，不允许为 {@code null} 或空
     * @return 该 Redis 集群所有的节点信息
     * @throws IllegalArgumentException 如果 bootstrapHosts 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果从所有的 Redis 集群主机地址中均无法加载 Redis 集群所有节点信息，将会抛出此异常
     */
    public List<StandardRedisNode> load(String[] bootstrapHosts) throws IllegalArgumentException, IllegalStateException {
        if (bootstrapHosts == null || bootstrapHosts.length == 0) {
            String errorMessage = "Redis cluster slots fails to load: `empty bootstrap hosts`. hosts: `" + Arrays.toString(bootstrapHosts)+ "`.";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        SocketConfiguration socketConfiguration= new SocketConfiguration();
        socketConfiguration.setConnectionTimeout(5000); // 连接超时时间： 5 秒
        for (String bootstrapHost : bootstrapHosts) {
            try (RedisChannel channel = new RedisChannel(bootstrapHost, socketConfiguration, -1, null)) {
                channel.init();
                DirectClusterSlotsClient slotsClient = new DirectClusterSlotsClient(channel, 5000, TimeUnit.NANOSECONDS.convert(200, TimeUnit.MICROSECONDS));
                List<StandardRedisNode> nodes = slotsClient.getClusterSlots();
                nodes.sort(Comparator.comparingInt(StandardRedisNode::getStartSlot));
                if (isCoverAllSlots(nodes)) {
                    return nodes;
                } else {
                    LOGGER.error("Redis cluster slots fails to load: `all slots are not covered`. `host`:`" + bootstrapHost
                            + "`. `nodes`:`" + nodes + "`.");
                }
            } catch (Exception e) {
                LOGGER.error("Redis cluster slots fails to load: `unexpected error`. `host`:`" + bootstrapHost + "`.", e);
            }
        }
        String errorMessage = "Redis cluster slots fails to load: `no available cluster node`. hosts: `" + Arrays.toString(bootstrapHosts)+ "`.";
        LOGGER.error(errorMessage);
        throw new IllegalStateException(errorMessage);
    }

    /**
     * 判断传入的 Redis 集群节点信息列表是否已覆盖从 0 至 16383 的 slots 区间，传入的列表必须按节点负责的 startSlot 值从小到大排序。
     *
     * @param nodes Redis 集群节点信息列表
     * @return 是否已覆盖从 0 至 16383 的 slots 区间
     */
    private boolean isCoverAllSlots(List<StandardRedisNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            StandardRedisNode node = nodes.get(i);
            if (i == 0) {
                if (node.getStartSlot() != RedisClusterHashSlotLocator.MIN_SLOT) {
                    return false;
                }
            } else {
                if (node.getStartSlot() != nodes.get(i - 1).getEndSlot() + 1) {
                    return false;
                }
                if (i == nodes.size() -1 && node.getEndSlot() != RedisClusterHashSlotLocator.MAX_SLOT) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 使用 "CLUSTER SLOTS" 命令加载 Redis 集群节点信息客户端。
     */
    private static class DirectClusterSlotsClient extends AbstractDirectRedisClient {

        /**
         * 构造一个 Redis 直连客户端抽象类。
         *
         * @param channel 与 Redis 服务进行数据交互的管道
         * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
         * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
         */
        private DirectClusterSlotsClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
            super(channel, timeout, slowExecutionThreshold);
        }

        @SuppressWarnings("unchecked")
        private List<StandardRedisNode> getClusterSlots() throws IllegalStateException, TimeoutException, RedisException {
            String methodName = methodNamePrefix + "getClusterSlots()";
            return (List<StandardRedisNode>) execute(methodName, null, ClusterSlotsCommand::new, response -> {
                List<StandardRedisNode> nodes = new ArrayList<>();
                for (int i = 0; i < response.size(); i++) {
                    RedisData nodeData = response.get(i);
                    int startSlot = Integer.valueOf(nodeData.get(0).getText());
                    int endSlot = Integer.valueOf(nodeData.get(1).getText());
                    RedisData hostData = nodeData.get(2);
                    String masterHost = hostData.get(0).getText() + ":" + hostData.get(1).getText();
                    String[] slaveHosts = new String[nodeData.size() - 3];
                    for (int j = 3; j < nodeData.size(); j++) {
                        hostData = nodeData.get(j);
                        slaveHosts[j - 3] = hostData.get(0).getText() + ":" + hostData.get(1).getText();
                    }
                    nodes.add(new StandardRedisNode(startSlot, endSlot, masterHost, slaveHosts));
                }
                return nodes;
            });
        }
    }
}
