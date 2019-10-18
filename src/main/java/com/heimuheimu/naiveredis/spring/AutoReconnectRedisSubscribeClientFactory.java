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
import com.heimuheimu.naiveredis.pubsub.*;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import org.springframework.beans.factory.FactoryBean;

import java.io.Closeable;
import java.util.List;

/**
 * {@link AutoReconnectRedisSubscribeClient} Spring 工厂类，兼容 Spring 4.0 以下版本不支持 lambda 语法问题。
 *
 * @author heimuheimu
 */
public class AutoReconnectRedisSubscribeClientFactory implements FactoryBean<AutoReconnectRedisSubscribeClient>, Closeable {

    private final AutoReconnectRedisSubscribeClient client;

    /**
     * 构造一个 {@link AutoReconnectRedisSubscribeClient} Spring 工厂类，用于创建 {@link AutoReconnectRedisSubscribeClient} 实例，
     * Socket 配置信息将会使用 {@link SocketConfiguration#DEFAULT}，PING 命令发送时间间隔为 30 秒，
     * Java 对象与字节数组转换器将会使用 RedisSubscribeClient 实现指定的默认转换器。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param channelSubscriberList Redis channel 消息订阅者列表，允许为 {@code null} 或空，但不允许与 patternSubscriberList 同时为空
     * @param patternSubscriberList Redis pattern 消息订阅者列表，允许为 {@code null} 或空，但不允许与 channelSubscriberList 同时为空
     * @param listener 自动重连 Redis 订阅客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果创建 AutoReconnectRedisSubscribeClient 过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisSubscribeClientFactory(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList,
                                                    List<NaiveRedisPatternSubscriber> patternSubscriberList,
                                                    AutoReconnectRedisSubscribeClientListener listener) {
        this.client = new AutoReconnectRedisSubscribeClient(host, channelSubscriberList, patternSubscriberList, listener);
    }

    /**
     * 构造一个 {@link AutoReconnectRedisSubscribeClient} Spring 工厂类，用于创建 {@link AutoReconnectRedisSubscribeClient} 实例。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param transcoder Java 对象与字节数组转换器，如果传 {@code null}，将会使用 {@link RedisSubscribeClient} 实现指定的默认转换器
     * @param channelSubscriberList Redis channel 消息订阅者列表，允许为 {@code null} 或空，但不允许与 patternSubscriberList 同时为空
     * @param patternSubscriberList Redis pattern 消息订阅者列表，允许为 {@code null} 或空，但不允许与 channelSubscriberList 同时为空
     * @param listener 自动重连 Redis 订阅客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果创建 AutoReconnectRedisSubscribeClient 过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisSubscribeClientFactory(String host, SocketConfiguration configuration,
                                                    int pingPeriod, Transcoder transcoder,
                                                    List<NaiveRedisChannelSubscriber> channelSubscriberList,
                                                    List<NaiveRedisPatternSubscriber> patternSubscriberList,
                                                    AutoReconnectRedisSubscribeClientListener listener) {
        this.client = new AutoReconnectRedisSubscribeClient(host, configuration, pingPeriod, transcoder, channelSubscriberList, patternSubscriberList, listener);
    }

    @Override
    public AutoReconnectRedisSubscribeClient getObject() {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return AutoReconnectRedisSubscribeClient.class;
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
