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

import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.facility.RedisClusterHashSlotLocator;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientList;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientListListener;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 集群客户端路由器，可根据 Key 选择相应的 Redis 集群节点直连客户端，用于执行该 Key 相关的 Redis 方法，Redis 集群相关资料可参考以下内容：
 * <ul>
 *     <li><a href="https://redis.io/topics/cluster-tutorial">Redis cluster tutorial</a></li>
 *     <li><a href="https://redis.io/topics/cluster-spec">Redis Cluster Specification</a></li>
 * </ul>
 *
 * <p><strong>说明：</strong>{@code StandardRedisNodeRouter} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class StandardRedisNodeRouter implements Closeable {
    
    private static final Logger LOG = LoggerFactory.getLogger(StandardRedisNodeRouter.class);

    /**
     * 创建 Redis 直连客户端所使用的 Socket 配置信息
     */
    private final SocketConfiguration configuration;

    /**
     * Redis 操作超时时间，单位：毫秒
     */
    private final int timeout;

    /**
     * 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩
     */
    private final int compressionThreshold;

    /**
     * Redis 操作过慢最小时间，单位：毫秒
     */
    private final int slowExecutionThreshold;

    /**
     * PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     */
    private final int pingPeriod;

    /**
     * Redis 集群节点信息列表
     */
    private final List<StandardRedisNode> nodes;

    /**
     * Redis 集群中所有的主机地址所在索引位置 Map，Key 为 Redis 主机地址，Value 为该 Redis 主机在 {@link #directRedisClientList} 中的索引位置
     */
    private final Map<String, Integer> hostIndexMap;

    /**
     * Redis 集群使用的直连客户端列表，包含集群所有的 Redis 主机地址
     */
    private final DirectRedisClientList directRedisClientList;

    /**
     * Redis 集群使用的 Hash slot 值计算器
     */
    private final RedisClusterHashSlotLocator redisClusterHashSlotLocator;

    /**
     * 已迁移的 slot 和 Redis 主机地址映射 Map，Key 为已迁移的 slot 值，Value 为迁移后的 Redis 主机地址
     */
    private final Map<Integer, String> movedSlotHostMap = new ConcurrentHashMap<>();

    /**
     * 临时 Redis 直连客户端 Map，Key 为 Redis 主机地址，Value 为对应的 Redis 直连客户端，当前 Map 的 Key 与 {@link #hostIndexMap} 中的 Key 不会重叠
     */
    private final ConcurrentHashMap<String, DirectRedisClient> temporaryDirectRedisClientMap = new ConcurrentHashMap<>();

    /**
     * 在创建临时 Redis 直连客户端或执行关闭操作时使用的私有锁
     */
    private final Object lock = new Object();

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 StandardRedisNodeRouter 实例。
     *
     * @param bootstrapHosts Redis 集群启动主机地址列表，用于加载集群信息，不允许为 {@code null} 或空数组
     * @param configuration 创建 Redis 直连客户端所使用的 Socket 配置信息，允许为 {@code null}
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @param isSlaveActive 是否启用 Redis 集群中的 Slave 节点，用于执行已读操作
     * @throws IllegalStateException 如果 Redis 集群节点信息加载失败或所有的 Redis 直连客户端均不可用，将会抛出此异常
     */
    public StandardRedisNodeRouter(String[] bootstrapHosts, SocketConfiguration configuration, int timeout,
                                   int compressionThreshold, int slowExecutionThreshold, int pingPeriod,
                                   DirectRedisClientListListener listener, boolean isSlaveActive) throws IllegalStateException {
        try {
            this.configuration = configuration;
            this.timeout = timeout;
            this.compressionThreshold = compressionThreshold;
            this.slowExecutionThreshold = slowExecutionThreshold;
            this.pingPeriod = pingPeriod;
            StandardRedisNodesLoader nodesLoader = new StandardRedisNodesLoader();
            nodes = nodesLoader.load(bootstrapHosts);
            hostIndexMap = new HashMap<>();
            List<String> hostList = new ArrayList<>();
            for (StandardRedisNode node : nodes) {
                String nodeMasterHost = node.getMasterHost();
                if (!hostIndexMap.containsKey(nodeMasterHost)) {
                    hostIndexMap.put(nodeMasterHost, hostList.size());
                    hostList.add(nodeMasterHost);
                }
                if (isSlaveActive) {
                    for (String nodeSlaveHost : node.getSlaveHosts()) {
                        if (!hostIndexMap.containsKey(nodeSlaveHost)) {
                            hostIndexMap.put(nodeSlaveHost, hostList.size());
                            hostList.add(nodeSlaveHost);
                        }
                    }
                }
            }
            this.directRedisClientList = new DirectRedisClientList("StandardRedisNodeRouter",
                    hostList.toArray(new String[0]), configuration, timeout, compressionThreshold, slowExecutionThreshold,
                    pingPeriod, listener);
            this.redisClusterHashSlotLocator = new RedisClusterHashSlotLocator();
        } catch (Exception e) {
            LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("bootstrapHosts", bootstrapHosts);
            parameterMap.put("configuration", configuration);
            parameterMap.put("timeout", timeout);
            parameterMap.put("compressionThreshold", compressionThreshold);
            parameterMap.put("slowExecutionThreshold", slowExecutionThreshold);
            parameterMap.put("pingPeriod", pingPeriod);
            parameterMap.put("listener", listener);
            parameterMap.put("isSlaveActive", isSlaveActive);
            String errorMessage = "Fails to construct StandardRedisNodeRouter." + LogBuildUtil.build(parameterMap);
            LOG.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    /**
     * 根据调用的 Redis Key 获得在 Redis cluster 中对应 slot 值，Key 不允许为 {@code null} 或空。
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @return Key 对应的 slot 值
     * @throws IllegalArgumentException 如果 Key 为 {@code null} 或空，将会抛出此异常
     */
    public int getSlot(String key) throws IllegalArgumentException {
        return redisClusterHashSlotLocator.getSlot(key);
    }

    /**
     * 根据调用的 Redis Key 所在的 slot 值获得 Redis 集群中对应的 Redis 直连客户端，如果直连客户端不可用，可能会返回 {@code null}。
     *
     * <p><strong>说明：</strong>即使 allowSlave 为 {@code true}，如果该 Redis 节点没有对应的 Slave 节点或当前所有 Slave 节点均不可用，仍可能返回 Master 节点。</p>
     *
     * @param slot Key 所在的 slot 值
     * @param allowSlave 是否允许返回 Slave 节点
     * @return Redis 直连客户端，可能为 {@code null}
     * @throws IllegalArgumentException 如果无法找到该 slot 对应的集群节点，将会抛出此异常
     * @throws IllegalStateException 如果当前 Redis 集群客户端路由器已关闭，将会抛出此异常
     */
    public DirectRedisClient getClientBySlot(int slot, boolean allowSlave) throws IllegalArgumentException, IllegalStateException {
        if (state != BeanStatusEnum.NORMAL) {
            Map<String, Object> errorParameterMap = new LinkedHashMap<>();
            errorParameterMap.put("slot", slot);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("StandardRedisNodeRouter#getClientBySlot(int slot, boolean allowSlave)",
                    "StandardRedisNodeRouter has been closed", errorParameterMap);
            LOG.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        String redirectedHost = movedSlotHostMap.get(slot);
        if (redirectedHost != null) {
            return getClientByHost(redirectedHost);
        } else {
            StandardRedisNode matchedNode = null;
            for (StandardRedisNode node : nodes) {
                if (slot >= node.getStartSlot() && slot <= node.getEndSlot()) {
                    matchedNode = node;
                    break;
                }
            }
            if (matchedNode == null) { // should not happen
                Map<String, Object> errorParameterMap = new LinkedHashMap<>();
                errorParameterMap.put("slot", slot);
                errorParameterMap.put("redisNodes", nodes);
                String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("StandardRedisNodeRouter#getClientBySlot(int slot, boolean allowSlave)",
                        "no matched redis node", errorParameterMap);
                LOG.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            DirectRedisClient client = null;
            if (allowSlave && matchedNode.getSlaveHosts().length > 0) {
                int count = 0;
                while (count < matchedNode.getSlaveHosts().length) {
                    String slaveHost = matchedNode.getNextSlaveHost();
                    count++;
                    client = getClientByHost(slaveHost);
                    if (client != null && client.isAvailable()) {
                        break;
                    }
                }
            }
            if (client == null || !client.isAvailable()) {
                client = getClientByHost(matchedNode.getMasterHost());
            }
            return client;
        }
    }

    /**
     * 根据 Redis 主机地址获得对应的 Redis 直连客户端，如果该 Redis 主机不属于集群，将会新建一个直连客户端后返回，根据实现不同，
     * 该直连客户端可能会被缓存，如果直连客户端不可用，可能会返回 {@code null}。
     *
     * @param host Redis 地址，由主机名和端口组成，":" 符号分割，例如：localhost:6379
     * @return Redis 主机对应的直连客户端，可能为 {@code null}
     * @throws IllegalStateException 如果当前 Redis 集群客户端路由器已关闭，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 主机不属于集群，在新建该直连客户端过程中发生错误，将会抛出此异常
     */
    public DirectRedisClient getClientByHost(String host) throws IllegalStateException {
        if (state != BeanStatusEnum.NORMAL) {
            Map<String, Object> errorParameterMap = new LinkedHashMap<>();
            errorParameterMap.put("host", host);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("StandardRedisNodeRouter#getClientByHost(String host)",
                    "StandardRedisNodeRouter has been closed", errorParameterMap);
            LOG.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        Integer clientIndex = hostIndexMap.get(host);
        DirectRedisClient client;
        if (clientIndex != null) { // 如果该 Redis 主机属于集群
            client = directRedisClientList.get(clientIndex);
        } else { // 如果该 Redis 主机不属于集群
            client = temporaryDirectRedisClientMap.get(host);
            if (client != null && !client.isAvailable()) { // 如果直连客户端已经不可用，从 Map 中移除
                temporaryDirectRedisClientMap.remove(host);
                client = null;
            }
            if (client == null) {
                try {
                    synchronized (lock) {
                        if (state == BeanStatusEnum.NORMAL) {
                            client = new DirectRedisClient(host, configuration, timeout, compressionThreshold, slowExecutionThreshold, pingPeriod, null);
                            if (client.isAvailable()) {
                                DirectRedisClient existedClient = temporaryDirectRedisClientMap.putIfAbsent(host, client);
                                if (existedClient != null) { // 该主机对应的直连客户端已被其它线程创建，关闭新创建的直连客户端
                                    client.close();
                                    client = existedClient;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Map<String, Object> errorParameterMap = new LinkedHashMap<>();
                    errorParameterMap.put("host", host);
                    String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("StandardRedisNodeRouter#getClientByHost(String host)",
                            "create temporary DirectRedisClient failed", errorParameterMap);
                    LOG.error(errorMessage, e);
                    throw new IllegalStateException(errorMessage, e);
                }
            }
        }
        return client;
    }

    /**
     * 当 Redis 集群的某个 slot 被迁移后，可调用此方法进行配置更新，后续该 slot 相关的 Redis 操作将会直接在迁移后的 Redis 主机上执行。
     *
     * @param slot 迁移的 slot 值
     * @param host 迁移后的 Redis 主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    public void moved(int slot, String host) {
        movedSlotHostMap.put(slot, host);
    }

    /**
     * 获得 Redis 操作超时时间，单位：毫秒。
     *
     * @return  Redis 操作超时时间
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 获得 Redis 集群节点信息的只读列表。
     *
     * @return Redis 集群节点信息的只读列表
     */
    public List<StandardRedisNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * 获得 Redis 集群中所有的主机地址数组。
     *
     * @return Redis 集群中所有的主机地址数组
     */
    public String[] getHosts() {
        return hostIndexMap.keySet().toArray(new String[0]);
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (state != BeanStatusEnum.CLOSED) {
                state = BeanStatusEnum.CLOSED;
                directRedisClientList.close();
                for (DirectRedisClient client : temporaryDirectRedisClientMap.values()) {
                    client.close();
                }
            }
        }
    }
}
