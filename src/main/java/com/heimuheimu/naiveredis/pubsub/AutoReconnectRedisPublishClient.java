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
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.Methods;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自动重连 Redis 消息发布客户端，当运行过程中出现因网络原因或其它异常错误导致连接断开，会触发自动重连机制。
 *
 * <p><strong>说明：</strong>AutoReconnectRedisPublishClient 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @see RedisPublishClient
 * @author heimuheimu
 */
public class AutoReconnectRedisPublishClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPublishClient.class);

    private static final Logger REDIS_PUBLISHER_LOG = LoggerFactory.getLogger("NAIVEREDIS_PUBLISHER_LOG");

    /**
     * Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     */
    private final SocketConfiguration configuration;

    /**
     * Redis 操作超时时间，单位：毫秒，不能小于等于 0
     */
    private final int timeout;

    /**
     * Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     */
    private final long slowExecutionThreshold;

    /**
     * PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     */
    private final int pingPeriod;

    /**
     * Java 对象与字节数组转换器，如果传 {@code null}，将会使用 {@link RedisPublishClient} 实现指定的默认转换器
     */
    private final Transcoder transcoder;

    /**
     * 自动重连 Redis 消息发布客户端事件监听器，允许为 {@code null}
     */
    private final AutoReconnectRedisPublishClientListener listener;

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state;

    /**
     * Redis 消息发布客户端
     */
    private volatile RedisPublishClient redisPublishClient;

    /**
     * 恢复任务使用的锁
     */
    private final Object rescueLock = new Object();

    /**
     * 当前实例使用的私有锁
     */
    private final Object lock = new Object();

    /**
     * 构造一个 AutoReconnectRedisPublishClient 实例，Socket 配置信息将会使用 {@link SocketConfiguration#DEFAULT}，
     * Redis 操作超时时间为 5 秒，执行过慢最小时间为 50 毫秒，PING 命令发送时间间隔为 30 秒，
     * Java 对象与字节数组转换器将会使用 {@link RedisPublishClient} 实现指定的默认转换器。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param listener 自动重连 Redis 消息发布客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果 AutoReconnectRedisPublishClient 创建过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisPublishClient(String host, AutoReconnectRedisPublishClientListener listener)
            throws IllegalStateException {
        this(host, null, 5000, 50, 30, null, listener);
    }

    /**
     * 构造一个 AutoReconnectRedisPublishClient 实例。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param transcoder Java 对象与字节数组转换器，如果传 {@code null}，将会使用 {@link RedisPublishClient} 实现指定的默认转换器
     * @param listener 自动重连 Redis 消息发布客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果 AutoReconnectRedisPublishClient 创建过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisPublishClient(String host, SocketConfiguration configuration, int timeout,
                                           long slowExecutionThreshold, int pingPeriod, Transcoder transcoder,
                                           AutoReconnectRedisPublishClientListener listener) throws IllegalStateException {
        this.host = host;
        this.configuration = configuration;
        this.timeout = timeout;
        this.slowExecutionThreshold = slowExecutionThreshold;
        this.pingPeriod = pingPeriod;
        this.transcoder = transcoder;
        this.listener = listener;
        synchronized (lock) {
            try {
                this.redisPublishClient = new RedisPublishClient(this.host, this.configuration, this.timeout,
                        this.slowExecutionThreshold, this.pingPeriod, this.transcoder, this::startRescueTask);
            } catch (Exception e) {
                String errorMessage = "Fails to construct AutoReconnectRedisPublishClient: `create RedisPublishClient failed`."
                        + LogBuildUtil.build(buildParameterMap());
                REDIS_PUBLISHER_LOG.error(errorMessage, e);
                LOGGER.error(errorMessage, e);
                throw new IllegalStateException(errorMessage, e);
            }
            if (this.redisPublishClient.isAvailable()) {
                state = BeanStatusEnum.NORMAL;
                Methods.invokeIfNotNull("AutoReconnectRedisPublishClientListener#onCreated(String host)",
                        buildParameterMap(), this.listener, () -> this.listener.onCreated(this.host));
            } else {
                String errorMessage = "Fails to construct AutoReconnectRedisPublishClient: `RedisPublishClient is not available`."
                        + LogBuildUtil.build(buildParameterMap());
                REDIS_PUBLISHER_LOG.error(errorMessage);
                LOGGER.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }
    }

    /**
     * 发布一条 Redis 消息，并返回接收到该消息的 Redis 客户端数量。
     *
     * <p><strong>算法复杂度：</strong> O(N+M)，N 为订阅该 channel 的客户端数量，M 为已订阅的 patterns 总数</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/publish">PUBLISH channel message</a></p>
     *
     * @param channel Redis channel，不允许为 {@code null} 或空字符串
     * @param message Redis 消息，不允许为 {@code null}
     * @return 接收到该消息的 Redis 客户端数量
     * @throws IllegalArgumentException 如果 channel 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 message 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    public int publish(String channel, Object message) throws IllegalArgumentException, IllegalStateException,
            TimeoutException, RedisException {
        return redisPublishClient.publish(channel, message);
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (state != BeanStatusEnum.CLOSED) {
                state = BeanStatusEnum.CLOSED;
                if (redisPublishClient != null) {
                    redisPublishClient.close();
                }
            }
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
        parameterMap.put("configuration", configuration);
        parameterMap.put("timeout", timeout);
        parameterMap.put("slowExecutionThreshold", slowExecutionThreshold);
        parameterMap.put("pingPeriod", pingPeriod);
        parameterMap.put("transcoder", transcoder);
        return parameterMap;
    }

    /**
     * 当前 Redis 消息发布客户端不可用时，启动后台恢复线程。
     *
     * @param unavailablePublishClient 不可用的 Redis 消息发布客户端
     */
    private void startRescueTask(RedisPublishClient unavailablePublishClient) {
        synchronized (lock) {
            if (state == BeanStatusEnum.NORMAL && unavailablePublishClient == redisPublishClient) {
                Thread rescueThread = new Thread(() -> {
                    synchronized (rescueLock) {
                        if (state == BeanStatusEnum.NORMAL && unavailablePublishClient == redisPublishClient) {
                            Methods.invokeIfNotNull("AutoReconnectRedisPublishClientListener#onClosed(String host)",
                                    buildParameterMap(), this.listener,
                                    () -> this.listener.onClosed(this.host));
                            long startTime = System.currentTimeMillis();
                            REDIS_PUBLISHER_LOG.info("RedisPublishClient rescue task has been started.{}",
                                    LogBuildUtil.build(buildParameterMap()));
                            while (state == BeanStatusEnum.NORMAL) {
                                RedisPublishClient client;
                                try {
                                    client = new RedisPublishClient(this.host, this.configuration, this.timeout,
                                            this.slowExecutionThreshold, this.pingPeriod, this.transcoder, this::startRescueTask);
                                    synchronized (lock) {
                                        if (client.isAvailable()) {
                                            if (state == BeanStatusEnum.NORMAL) {
                                                this.redisPublishClient = client;
                                            } else {
                                                client.close();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    REDIS_PUBLISHER_LOG.error("Fails to rescue `RedisPublishClient`." +
                                            LogBuildUtil.build(buildParameterMap()), e);
                                }
                                if (this.redisPublishClient.isAvailable()) {
                                    REDIS_PUBLISHER_LOG.info("RedisPublishClient rescue task has been finished. `cost`:`{}ms`.{}",
                                            System.currentTimeMillis() - startTime, LogBuildUtil.build(buildParameterMap()));
                                    Methods.invokeIfNotNull("AutoReconnectRedisPublishClientListener#onRecovered(String host)",
                                            buildParameterMap(), this.listener, () -> this.listener.onRecovered(this.host));
                                    break;
                                } else {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ignored) {
                                    }
                                }
                            }
                        }
                    }
                });
                rescueThread.setName("naiveredis-publish-client-rescue-task");
                rescueThread.setDaemon(true);
                rescueThread.start();
            }
        }
    }
}
