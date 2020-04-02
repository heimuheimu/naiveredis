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

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.clients.AbstractDirectRedisClient;
import com.heimuheimu.naiveredis.command.pubsub.PublishCommand;
import com.heimuheimu.naiveredis.data.RedisDataParser;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.UnusableServiceNotifier;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naiveredis.monitor.PublisherMonitorFactory;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.transcoder.SimpleTranscoder;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 消息发布客户端，更多信息请参考：<a href="https://redis.io/topics/pubsub"> Redis Pub/Sub </a>
 *
 * <p><strong>说明：</strong>RedisPublishClient 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class RedisPublishClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPublishClient.class);

    private static final Logger REDIS_PUBLISHER_LOG = LoggerFactory.getLogger("NAIVEREDIS_PUBLISHER_LOG");

    /**
     * Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * Java 对象与字节数组转换器
     */
    private final Transcoder transcoder;

    /**
     * Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final DirectRedisPublishClient client;

    /**
     * Redis 消息发布客户端使用的执行信息监控器
     */
    private final ExecutionMonitor monitor;

    /**
     * 构造一个 RedisPublishClient 实例。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param transcoder Java 对象与字节数组转换器，如果传 {@code null}，将会使用 {@link SimpleTranscoder} 转换器，compressionThreshold 默认为 64 KB
     * @param unusableServiceNotifier RedisPublishClient 不可用通知器，允许为 {@code null}
     * @throws IllegalStateException 如果 RedisPublishClient 创建过程中发生错误，将会抛出此异常
     */
    @SuppressWarnings("Duplicates")
    public RedisPublishClient(String host, SocketConfiguration configuration, int timeout, long slowExecutionThreshold,
                              int pingPeriod, Transcoder transcoder,
                              UnusableServiceNotifier<RedisPublishClient> unusableServiceNotifier) throws IllegalStateException {
        LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("host", host);
        parameterMap.put("configuration", configuration);
        parameterMap.put("timeout", timeout);
        parameterMap.put("slowExecutionThreshold", slowExecutionThreshold);
        parameterMap.put("pingPeriod", pingPeriod);
        parameterMap.put("transcoder", transcoder);
        try {
            this.host = host;
            this.transcoder = transcoder == null ? new SimpleTranscoder(64 * 1024) : transcoder;
            RedisChannel channel = new RedisChannel(this.host, configuration, pingPeriod, unavailableChannel -> {
                REDIS_PUBLISHER_LOG.info("RedisPublishClient has been closed.{}", LogBuildUtil.build(parameterMap));
                if (unusableServiceNotifier != null) {
                    unusableServiceNotifier.onClosed(this);
                }
            });
            channel.init();
            client = new DirectRedisPublishClient(channel, timeout,
                    TimeUnit.NANOSECONDS.convert(slowExecutionThreshold, TimeUnit.MILLISECONDS));
            monitor = PublisherMonitorFactory.get(host);
            REDIS_PUBLISHER_LOG.info("RedisPublishClient init success.{}", LogBuildUtil.build(parameterMap));
        } catch (Exception e) {
            String errorMessage = "Fails to construct RedisPublishClient." + LogBuildUtil.build(parameterMap);
            REDIS_PUBLISHER_LOG.error(errorMessage, e);
            LOGGER.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
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
        long startNanoTime = System.nanoTime();
        try {
            int receivedClients = client.publish(channel, message);
            if (receivedClients > 0 && REDIS_PUBLISHER_LOG.isDebugEnabled()) //noinspection Duplicates
            {
                LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
                long cost = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startNanoTime, TimeUnit.NANOSECONDS);
                parameterMap.put("cost", cost + "ms");
                parameterMap.put("receivedClients", receivedClients);
                parameterMap.put("host", host);
                parameterMap.put("channel", channel);
                parameterMap.put("message", message);
                REDIS_PUBLISHER_LOG.debug("Publish message success.{}", LogBuildUtil.build(parameterMap));
            } else if (receivedClients == 0) {
                monitor.onError(PublisherMonitorFactory.ERROR_CODE_NO_CLIENT);
                REDIS_PUBLISHER_LOG.warn("There is no client for channel: `" + channel + "`. `message`: `" + message + "`.");
            }
            return receivedClients;
        } catch (Exception e) {
            monitor.onError(PublisherMonitorFactory.ERROR_CODE_PUBLISH_ERROR);
            LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("host", host);
            parameterMap.put("channel", channel);
            parameterMap.put("message", message);
            REDIS_PUBLISHER_LOG.error("Fails to publish message." + LogBuildUtil.build(parameterMap), e);
            throw e;
        } finally {
            monitor.onExecuted(startNanoTime);
        }
    }

    /**
     * 判断当前 Redis 消息发布直连客户端是否可用。
     *
     * @return 是否可用
     */
    public boolean isAvailable() {
        return client != null && client.isAvailable();
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Redis 消息发布直连客户端。
     */
    private class DirectRedisPublishClient extends AbstractDirectRedisClient {

        /**
         * 构造一个 DirectRedisPublishClient 实例。
         *
         * @param channel 与 Redis 服务进行数据交互的管道
         * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
         * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
         */
        public DirectRedisPublishClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
            super(channel, timeout, slowExecutionThreshold);
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
            String methodName = "RedisPublishClient#publish(String channel, Object message)";
            MethodParameterChecker parameterChecker = new MethodParameterChecker(methodName, NAIVEREDIS_ERROR_LOG,
                    parameterName -> executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT));
            parameterChecker.addParameter("channel", channel);
            parameterChecker.addParameter("message", message);
            parameterChecker.check("channel", "isEmpty", Parameters::isEmpty);
            parameterChecker.check("message", "isNull", Parameters::isNull);

            return (int) execute(methodName, parameterChecker.getParameterMap(),
                    () -> new PublishCommand(channel, transcoder.encode(message)), RedisDataParser::parseInt);
        }

        /**
         * 判断当前 Redis 消息发布直连客户端是否可用。
         *
         * @return 是否可用
         */
        public boolean isAvailable() {
            return channel.isAvailable();
        }

        /**
         * 关闭当前 Redis 消息发布直连客户端。
         */
        public void close() {
            channel.close();
        }
    }
}
