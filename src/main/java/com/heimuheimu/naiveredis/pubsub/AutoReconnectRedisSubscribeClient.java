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

package com.heimuheimu.naiveredis.pubsub;

import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.facility.Methods;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 订阅客户端，当运行过程中出现因网络原因或其它异常错误导致订阅中断，会触发自动重连机制。
 *
 * <p><strong>说明：</strong>AutoReconnectRedisSubscribeClient 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @see RedisSubscribeClient
 * @author heimuheimu
 */
public class AutoReconnectRedisSubscribeClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSubscribeClient.class);

    private static final Logger REDIS_SUBSCRIBER_LOG = LoggerFactory.getLogger("NAIVEREDIS_SUBSCRIBER_LOG");

    /**
     * Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * Socket 配置信息，如果为 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     */
    private final SocketConfiguration configuration;

    /**
     * PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     */
    private final int pingPeriod;

    /**
     * Java 对象与字节数组转换器，如果为 {@code null}，将会使用 {@link RedisSubscribeClient} 实现指定的默认转换器
     */
    private final Transcoder transcoder;

    /**
     * Redis channel 消息订阅者列表，允许为 {@code null} 或空
     */
    private final List<NaiveRedisChannelSubscriber> channelSubscriberList;

    /**
     * Redis pattern 消息订阅者列表，允许为 {@code null} 或空
     */
    private final List<NaiveRedisPatternSubscriber> patternSubscriberList;

    /**
     * 自动重连 Redis 订阅客户端事件监听器，允许为 {@code null}
     */
    private final AutoReconnectRedisSubscribeClientListener listener;

    /**
     * Redis 订阅客户端
     */
    private volatile RedisSubscribeClient redisSubscribeClient;

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state;

    /**
     * 恢复任务使用的锁
     */
    private final Object rescueLock = new Object();

    /**
     * 构造一个 AutoReconnectRedisSubscribeClient 实例，Socket 配置信息将会使用 {@link SocketConfiguration#DEFAULT}，
     * PING 命令发送时间间隔为 30 秒，Java 对象与字节数组转换器将会使用 {@link RedisSubscribeClient} 实现指定的默认转换器。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param channelSubscriberList Redis channel 消息订阅者列表，允许为 {@code null} 或空，但不允许与 patternSubscriberList 同时为空
     * @param patternSubscriberList Redis pattern 消息订阅者列表，允许为 {@code null} 或空，但不允许与 channelSubscriberList 同时为空
     * @param listener 自动重连 Redis 订阅客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果创建 AutoReconnectRedisSubscribeClient 过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisSubscribeClient(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList,
                                             List<NaiveRedisPatternSubscriber> patternSubscriberList,
                                             AutoReconnectRedisSubscribeClientListener listener) throws IllegalStateException {
        this(host, null, 30, null, channelSubscriberList, patternSubscriberList, listener);
    }

    /**
     * 构造一个 AutoReconnectRedisSubscribeClient 实例。
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
    public AutoReconnectRedisSubscribeClient(String host, SocketConfiguration configuration,
                                             int pingPeriod, Transcoder transcoder,
                                             List<NaiveRedisChannelSubscriber> channelSubscriberList,
                                             List<NaiveRedisPatternSubscriber> patternSubscriberList,
                                             AutoReconnectRedisSubscribeClientListener listener) throws IllegalStateException {
        this.host = host;
        this.configuration = configuration;
        this.pingPeriod = pingPeriod;
        this.transcoder = transcoder;
        this.channelSubscriberList = channelSubscriberList;
        this.patternSubscriberList = patternSubscriberList;
        this.listener = listener;
        try {
            this.redisSubscribeClient = new RedisSubscribeClient(this.host, this.configuration, this.pingPeriod,
                    this.transcoder, this.channelSubscriberList, this.patternSubscriberList, this::startRescueTask);
            this.redisSubscribeClient.init();
        } catch (Exception e) {
            String errorMessage = "Fails to construct AutoReconnectRedisSubscribeClient: `create RedisSubscribeClient failed`."
                    + LogBuildUtil.build(buildParameterMap());
            REDIS_SUBSCRIBER_LOG.error(errorMessage, e);
            LOGGER.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
        if (this.redisSubscribeClient.isAvailable()) {
            state = BeanStatusEnum.NORMAL;
            Methods.invokeIfNotNull("AutoReconnectRedisSubscribeClientListener#onCreated(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList, List<NaiveRedisPatternSubscriber> patternSubscriberList)",
                    buildParameterMap(), this.listener,
                    () -> this.listener.onCreated(this.host, this.channelSubscriberList, this.patternSubscriberList));
        } else {
            String errorMessage = "Fails to construct AutoReconnectRedisSubscribeClient: `RedisSubscribeClient is not available`."
                    + LogBuildUtil.build(buildParameterMap());
            REDIS_SUBSCRIBER_LOG.error(errorMessage);
            LOGGER.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * 构造一个参数 Map，供日志打印使用。
     *
     * @return 参数 Map
     */
    private Map<String, Object> buildParameterMap() {
        LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("host", host);
        parameterMap.put("pingPeriod", pingPeriod);
        parameterMap.put("configuration", configuration);
        parameterMap.put("transcoder", transcoder);
        parameterMap.put("channels", channelSubscriberList);
        parameterMap.put("patterns", patternSubscriberList);
        return parameterMap;
    }

    /**
     * 当前 Redis 订阅客户端不可用时，启动后台恢复线程。
     *
     * @param unavailableSubscribeClient 不可用的 Redis 订阅客户端
     */
    private void startRescueTask(RedisSubscribeClient unavailableSubscribeClient) {
        if (state == BeanStatusEnum.NORMAL && unavailableSubscribeClient == redisSubscribeClient) {
            Thread rescueThread = new Thread(() -> {
                synchronized (rescueLock) {
                    if (state == BeanStatusEnum.NORMAL && unavailableSubscribeClient == redisSubscribeClient) {
                        Methods.invokeIfNotNull("AutoReconnectRedisSubscribeClientListener#onClosed(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList, List<NaiveRedisPatternSubscriber> patternSubscriberList)",
                                buildParameterMap(), this.listener,
                                () -> this.listener.onClosed(this.host, this.channelSubscriberList, this.patternSubscriberList));
                        long startTime = System.currentTimeMillis();
                        REDIS_SUBSCRIBER_LOG.info("RedisSubscribeClient rescue task has been started.{}",
                                LogBuildUtil.build(buildParameterMap()));
                        while (state == BeanStatusEnum.NORMAL) {
                            RedisSubscribeClient client;
                            try {
                                client = new RedisSubscribeClient(this.host, this.configuration, this.pingPeriod,
                                        this.transcoder, this.channelSubscriberList, this.patternSubscriberList, this::startRescueTask);
                                client.init();
                                if (client.isAvailable()) {
                                    this.redisSubscribeClient = client;
                                }
                            } catch (Exception e) {
                                REDIS_SUBSCRIBER_LOG.error("Fails to rescue `RedisSubscribeClient`." +
                                        LogBuildUtil.build(buildParameterMap()), e);
                            }
                            if (this.redisSubscribeClient.isAvailable()) {
                                REDIS_SUBSCRIBER_LOG.info("RedisSubscribeClient rescue task has been finished. `cost`:`{}ms`.{}",
                                        System.currentTimeMillis() - startTime, LogBuildUtil.build(buildParameterMap()));
                                Methods.invokeIfNotNull("AutoReconnectRedisSubscribeClientListener#onRecovered(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList, List<NaiveRedisPatternSubscriber> patternSubscriberList)",
                                        buildParameterMap(), this.listener,
                                        () -> this.listener.onRecovered(this.host, this.channelSubscriberList, this.patternSubscriberList));
                                break;
                            } else {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {}
                            }
                        }
                    }
                }
            });
            rescueThread.setName("naiveredis-subscribe-client-rescue-task");
            rescueThread.setDaemon(true);
            rescueThread.start();
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            state = BeanStatusEnum.CLOSED;
            if (redisSubscribeClient != null) {
                redisSubscribeClient.close();
            }
        }
    }
}
