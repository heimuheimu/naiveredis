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

import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.pubsub.AutoReconnectRedisPublishClient;
import com.heimuheimu.naiveredis.pubsub.AutoReconnectRedisPublishClientListener;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import org.springframework.beans.factory.FactoryBean;

import java.io.Closeable;

/**
 * {@link AutoReconnectRedisPublishClient} Spring 工厂类，兼容 Spring 4.0 以下版本不支持 lambda 语法问题。
 *
 * @author heimuheimu
 */
public class AutoReconnectRedisPublishClientFactory implements FactoryBean<AutoReconnectRedisPublishClient>, Closeable {

    private final AutoReconnectRedisPublishClient client;

    /**
     * 构造一个 {@link AutoReconnectRedisPublishClient} Spring 工厂类，用于创建 {@link AutoReconnectRedisPublishClient} 实例，
     * Socket 配置信息将会使用 {@link SocketConfiguration#DEFAULT}，Redis 操作超时时间为 5 秒，执行过慢最小时间为 50 毫秒，
     * PING 命令发送时间间隔为 30 秒，Java 对象与字节数组转换器将会使用 RedisPublishClient 实现指定的默认转换器。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param listener 自动重连 Redis 消息发布客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果 AutoReconnectRedisPublishClient 创建过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisPublishClientFactory(String host, AutoReconnectRedisPublishClientListener listener)
            throws IllegalStateException {
        this.client = new AutoReconnectRedisPublishClient(host, listener);
    }

    /**
     * 构造一个 {@link AutoReconnectRedisPublishClient} Spring 工厂类，用于创建 {@link AutoReconnectRedisPublishClient} 实例。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param transcoder Java 对象与字节数组转换器，如果传 {@code null}，将会使用 RedisPublishClient 实现指定的默认转换器
     * @param listener 自动重连 Redis 消息发布客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果 AutoReconnectRedisPublishClient 创建过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisPublishClientFactory(String host, SocketConfiguration configuration, int timeout,
                                                  long slowExecutionThreshold, int pingPeriod, Transcoder transcoder,
                                                  AutoReconnectRedisPublishClientListener listener) {
        this.client = new AutoReconnectRedisPublishClient(host, configuration, timeout, slowExecutionThreshold,
                pingPeriod, transcoder, listener);
    }

    @Override
    public AutoReconnectRedisPublishClient getObject() {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return AutoReconnectRedisPublishClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void close() {
        client.close();
    }
}
