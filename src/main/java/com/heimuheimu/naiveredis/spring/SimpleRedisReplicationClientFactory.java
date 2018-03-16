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

package com.heimuheimu.naiveredis.spring;

import com.heimuheimu.naiveredis.cluster.SimpleRedisReplicationClient;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientListListener;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import org.springframework.beans.factory.FactoryBean;

/**
 * {@link SimpleRedisReplicationClient} Spring 工厂类，兼容 Spring 4.0 以下版本不支持 lambda 语法问题。
 *
 * @author heimuheimu
 */
public class SimpleRedisReplicationClientFactory implements FactoryBean<SimpleRedisReplicationClient> {

    private final SimpleRedisReplicationClient client;

    /**
     * 构造一个 {@link SimpleRedisReplicationClient} Spring 工厂类，用于创建 {@link SimpleRedisReplicationClient} 实例。
     *
     * @param masterHost Redis master 地址，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param slaveHosts Redis slave 地址数组，不允许为 {@code null} 或空数组
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis master 地址为 {@code null} 或空字符串
     * @throws IllegalArgumentException 如果 Redis slave 地址数组为 {@code null} 或空数组
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public SimpleRedisReplicationClientFactory(String masterHost, String[] slaveHosts, DirectRedisClientListListener listener)
            throws IllegalArgumentException, IllegalStateException {
        this.client = new SimpleRedisReplicationClient(masterHost, slaveHosts, listener);
    }

    /**
     * 构造一个 {@link SimpleRedisReplicationClient} Spring 工厂类，用于创建 {@link SimpleRedisReplicationClient} 实例。
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
    public SimpleRedisReplicationClientFactory(String masterHost, String[] slaveHosts, SocketConfiguration configuration,
                                               int timeout, int compressionThreshold, int slowExecutionThreshold, int pingPeriod,
                                               DirectRedisClientListListener listener) throws IllegalArgumentException, IllegalStateException {
        this.client = new SimpleRedisReplicationClient(masterHost, slaveHosts, configuration, timeout, compressionThreshold,
                slowExecutionThreshold, pingPeriod, listener);
    }

    @Override
    public SimpleRedisReplicationClient getObject() throws Exception {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleRedisReplicationClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
