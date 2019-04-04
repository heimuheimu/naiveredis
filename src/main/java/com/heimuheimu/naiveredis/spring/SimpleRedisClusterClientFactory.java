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

import com.heimuheimu.naiveredis.cluster.SimpleRedisClusterClient;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientListListener;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import org.springframework.beans.factory.FactoryBean;

/**
 * {@link SimpleRedisClusterClient} Spring 工厂类，兼容 Spring 4.0 以下版本不支持 lambda 语法问题。
 *
 * @author heimuheimu
 */
public class SimpleRedisClusterClientFactory implements FactoryBean<SimpleRedisClusterClient> {

    private final SimpleRedisClusterClient client;

    /**
     * 构造一个 {@link SimpleRedisClusterClient} Spring 工厂类，用于创建 {@link SimpleRedisClusterClient} 实例。
     *
     * @param hosts Redis 地址数组，不允许为 {@code null} 或空数组
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     * @see SimpleRedisClusterClient#SimpleRedisClusterClient(String[], DirectRedisClientListListener)
     */
    public SimpleRedisClusterClientFactory(String[] hosts, DirectRedisClientListListener listener) throws IllegalStateException {
        this.client = new SimpleRedisClusterClient(hosts, listener);
    }

    /**
     * 构造一个 {@link SimpleRedisClusterClient} Spring 工厂类，用于创建 {@link SimpleRedisClusterClient} 实例。
     *
     * @param hosts Redis 地址数组，不允许为 {@code null} 或空数组
     * @param configuration 创建 Redis 直连客户端所使用的 Socket 配置信息，允许为 {@code null}
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     * @see SimpleRedisClusterClient#SimpleRedisClusterClient(String[], SocketConfiguration, int, int, int, int, DirectRedisClientListListener)
     */
    public SimpleRedisClusterClientFactory(String[] hosts, SocketConfiguration configuration, int timeout,
                                           int compressionThreshold, int slowExecutionThreshold, int pingPeriod,
                                           DirectRedisClientListListener listener) throws IllegalStateException {
        this.client = new SimpleRedisClusterClient(hosts, configuration, timeout, compressionThreshold, slowExecutionThreshold,
                pingPeriod, listener);
    }

    @Override
    public SimpleRedisClusterClient getObject() {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleRedisClusterClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void close() {
        this.client.close();
    }
}
