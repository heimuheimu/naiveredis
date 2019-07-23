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
import com.heimuheimu.naiveredis.facility.ConsistentHashLocator;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientList;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientListListener;
import com.heimuheimu.naiveredis.net.SocketConfiguration;

import java.util.Arrays;

/**
 * 一个简单的 Redis 集群客户端，连接多个 Redis 服务，根据 Key 进行 Hash 选择。
 *
 * <p><strong>说明：</strong>{@code SimpleRedisClusterClient} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SimpleRedisClusterClient extends AbstractRedisClusterClient {

    /**
     * Redis 地址数组
     */
    private final String[] hosts;

    /**
     * Redis 直连客户端列表
     */
    private final DirectRedisClientList directRedisClientList;

    /**
     * Redis 客户端定位器
     */
    private final ConsistentHashLocator consistentHashLocator = new ConsistentHashLocator();

    /**
     * 构造一个简单的 Redis 集群客户端，连接多个 Redis 服务，根据 Key 进行 Hash 选择。创建直连客户端的 {@link java.net.Socket}
     * 配置信息使用 {@link SocketConfiguration#DEFAULT}，Redis 操作超时时间设置为 5 秒，最小压缩字节数设置为 64 KB，
     * Redis 操作过慢最小时间设置为 50 毫秒，心跳检测时间设置为 30 秒。
     *
     * @param hosts Redis 地址数组，不允许为 {@code null} 或空数组
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public SimpleRedisClusterClient(String[] hosts, DirectRedisClientListListener listener) throws IllegalStateException {
        this(hosts, null, 5000, 64 * 1024, 50, 30, listener);
    }

    /**
     * 构造一个简单的 Redis 集群客户端，连接多个 Redis 服务，根据 Key 进行 Hash 选择。
     *
     * @param hosts Redis 地址数组，不允许为 {@code null} 或空数组
     * @param configuration 创建 Redis 直连客户端所使用的 Socket 配置信息，允许为 {@code null}
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public SimpleRedisClusterClient(String[] hosts, SocketConfiguration configuration, int timeout,
                                    int compressionThreshold, int slowExecutionThreshold, int pingPeriod,
                                    DirectRedisClientListListener listener) throws IllegalStateException {
        this.hosts = hosts;
        this.directRedisClientList = new DirectRedisClientList("SimpleRedisClusterClient", hosts, configuration,
                timeout, compressionThreshold, slowExecutionThreshold, pingPeriod, listener);
    }

    @Override
    public void close() {
        super.close();
        directRedisClientList.close();
    }

    @Override
    public String toString() {
        return "SimpleRedisClusterClient{" +
                "hosts=" + Arrays.toString(hosts) +
                ", directRedisClientList=" + directRedisClientList +
                '}';
    }

    @Override
    protected DirectRedisClient getClient(RedisClientMethod method, String key) {
        int clientIndex = consistentHashLocator.getIndex(key, hosts.length);
        return directRedisClientList.get(clientIndex);
    }
}
