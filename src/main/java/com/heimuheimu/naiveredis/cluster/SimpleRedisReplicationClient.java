/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 heimuheimu
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

package com.heimuheimu.naiveredis.cluster;

import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.constant.RedisClientMethod;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientList;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientListListener;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.net.SocketConfiguration;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 支持 Redis master-slave 复制模式的客户端，由 Master 服务执行变更操作，所有的 Slave 服务执行读取操作（轮询策略）。
 * <p>更多 Replication 信息请参考文档：
 * <a href="https://redis.io/topics/replication">https://redis.io/topics/replication</a>
 * </p>
 *
 * @author heimuheimu
 */
public class SimpleRedisReplicationClient extends AbstractRedisClusterClient {

    /**
     * 记录已获取 Slave {@code DirectRedisClient} 的次数，用于做负载均衡
     */
    private final AtomicLong count = new AtomicLong(0);

    /**
     * Redis master 地址，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String masterHost;

    /**
     * Redis slave 地址数组
     */
    private final String[] slaveHosts;

    /**
     * Redis 直连客户端列表，第一个为 Master 直连客户端，后续为直连 Slave 客户端列表
     */
    private final DirectRedisClientList directRedisClientList;

    /**
     * 构造一个支持 Redis master-slave 复制模式的客户端。创建直连客户端的 {@link java.net.Socket}配置信息使用
     * {@link SocketConfiguration#DEFAULT}，Redis 操作超时时间设置为 5 秒，最小压缩字节数设置为 64 KB，
     * Redis 操作过慢最小时间设置为 50 毫秒，心跳检测时间设置为 30 秒。
     *
     * @param masterHost Redis master 地址，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param slaveHosts Redis slave 地址数组，不允许为 {@code null} 或空数组
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis master 地址为 {@code null} 或空字符串
     * @throws IllegalArgumentException 如果 Redis slave 地址数组为 {@code null} 或空数组
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public SimpleRedisReplicationClient(String masterHost, String[] slaveHosts, DirectRedisClientListListener listener)
            throws IllegalArgumentException, IllegalStateException {
        this(masterHost, slaveHosts, null, 5000, 64 * 1024, 50, 30, listener);
    }

    /**
     * 构造一个支持 Redis master-slave 复制模式的客户端。
     *
     * @param masterHost Redis master 地址，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param slaveHosts Redis slave 地址数组，不允许为 {@code null} 或空数组
     * @param configuration 创建 Redis 直连客户端所使用的 Socket 配置信息，允许为 {@code null}
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis master 地址为 {@code null} 或空字符串
     * @throws IllegalArgumentException 如果 Redis slave 地址数组为 {@code null} 或空数组
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public SimpleRedisReplicationClient(String masterHost, String[] slaveHosts, SocketConfiguration configuration,
                                        int timeout, int compressionThreshold, int slowExecutionThreshold, int pingPeriod,
                                        DirectRedisClientListListener listener) throws IllegalArgumentException, IllegalStateException {
        ConstructorParameterChecker parameterChecker = new ConstructorParameterChecker("SimpleRedisReplicationClient", LOG);
        parameterChecker.addParameter("masterHost", masterHost);
        parameterChecker.addParameter("slaveHosts", slaveHosts);
        parameterChecker.addParameter("socketConfiguration", configuration);
        parameterChecker.addParameter("timeout", timeout);
        parameterChecker.addParameter("compressionThreshold", compressionThreshold);
        parameterChecker.addParameter("slowExecutionThreshold", slowExecutionThreshold);
        parameterChecker.addParameter("pingPeriod", pingPeriod);
        parameterChecker.addParameter("listener", listener);

        parameterChecker.check("masterHost", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("slaveHosts", "isEmpty", Parameters::isEmpty);

        this.masterHost = masterHost;
        this.slaveHosts = slaveHosts;

        String[] hosts = new String[slaveHosts.length + 1];
        hosts[0] = masterHost;
        System.arraycopy(slaveHosts, 0, hosts, 1, slaveHosts.length);
        this.directRedisClientList = new DirectRedisClientList("SimpleRedisReplicationClient", hosts, configuration,
                timeout, compressionThreshold, slowExecutionThreshold, pingPeriod, listener);
    }

    @Override
    public void close() {
        super.close();
        directRedisClientList.close();
    }

    @Override
    public String toString() {
        return "SimpleRedisReplicationClient{" +
                "count=" + count +
                ", masterHost='" + masterHost + '\'' +
                ", slaveHosts=" + Arrays.toString(slaveHosts) +
                ", directRedisClientList=" + directRedisClientList +
                '}';
    }

    @Override
    protected DirectRedisClient getClient(RedisClientMethod method, String key) {
        boolean useSlave = method.isReadOnly();
        DirectRedisClient client;
        if (useSlave) { // 获取 Slave 直连客户端
            int clientIndex = (int) (Math.abs(count.incrementAndGet()) % slaveHosts.length) + 1;
            client = directRedisClientList.orAvailableClient(clientIndex, 0);
        } else { // 获取 Master 直连客户端
            client = directRedisClientList.get(0);
        }
        return client;
    }
}
