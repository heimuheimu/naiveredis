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

import com.heimuheimu.naivemonitor.facility.MonitoredSocketOutputStream;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.naiveredis.command.keys.PingCommand;
import com.heimuheimu.naiveredis.command.pubsub.PSubscribeCommand;
import com.heimuheimu.naiveredis.command.pubsub.SubscribeCommand;
import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.data.RedisDataReader;
import com.heimuheimu.naiveredis.facility.UnusableServiceNotifier;
import com.heimuheimu.naiveredis.monitor.SocketMonitorFactory;
import com.heimuheimu.naiveredis.net.SocketBuilder;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.transcoder.StringTranscoder;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Redis 订阅客户端，自动接收已订阅的 channel 或 pattern 消息，
 * 更多信息请参考：<a href="https://redis.io/topics/pubsub"> Redis Pub/Sub </a>
 *
 * <p><strong>说明：</strong>RedisSubscribeClient 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class RedisSubscribeClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSubscribeClient.class);

    private static final Logger REDIS_SUBSCRIBER_LOG = LoggerFactory.getLogger("NAIVEREDIS_SUBSCRIBER_LOG");

    /**
     * Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * 与 Redis 服务建立的 Socket 连接
     */
    private final Socket socket;

    /**
     * Redis 数据读取器
     */
    private final RedisDataReader reader;

    /**
     * 向 Redis 服务发送数据的可监控输出流
     */
    private final MonitoredSocketOutputStream outputStream;

    /**
     * PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     */
    private final int pingPeriod;

    /**
     * Java 对象与字节数组转换器
     */
    private final Transcoder transcoder;

    /**
     * Redis channel 消息订阅者 Map，Key 为 channel，Value 为该 channel 对应的订阅者列表，不会为 {@code null}
     */
    private final Map<String, List<NaiveRedisChannelSubscriber>> channelSubscribersMap;

    /**
     * Redis pattern 消息订阅者 Map，Key 为 pattern，Value 为该 pattern 对应的订阅者列表，不会为 {@code null}
     */
    private final Map<String, List<NaiveRedisPatternSubscriber>> patternSubscribersMap;

    /**
     * RedisSubscribeClient 不可用通知器，可能为 {@code null}
     */
    private final UnusableServiceNotifier<RedisSubscribeClient> unusableServiceNotifier;

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    /**
     * Redis 订阅 IO 线程
     */
    private RedisSubscribeIOTask ioTask = null;

    /**
     * 心跳检测任务执行器，访问该属性需使用当前实例的锁
     */
    private ScheduledExecutorService pingExecutorService = null;

    /**
     * 心跳检测任务使用的 CountDownLatch 实例
     */
    private volatile CountDownLatch pingLatch = null;

    /**
     * 构造一个 RedisSubscribeClient 实例。
     *
     * @param host Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration {@link Socket} 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param transcoder Java 对象与字节数组转换器，如果传 {@code null}，将会使用 {@link StringTranscoder} 转换器
     * @param channelSubscriberList Redis channel 消息订阅者列表，允许为 {@code null} 或空
     * @param patternSubscriberList Redis pattern 消息订阅者列表，允许为 {@code null} 或空
     * @param unusableServiceNotifier RedisSubscribeClient 不可用通知器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 host 不符合 Redis 地址规则，将会抛出此异常
     * @throws IllegalArgumentException 如果某个 Redis channel 消息订阅者返回的 channel 列表为 {@code null} 或空，或者包含 {@code null} 或空的 channel，将会抛出此异常
     * @throws IllegalArgumentException 如果某个 Redis pattern 消息订阅者返回的 pattern 列表为 {@code null} 或空，或者包含 {@code null} 或空的 pattern，将会抛出此异常
     * @throws IllegalArgumentException 如果 Redis channel 消息订阅者列表和 Redis pattern 消息订阅者列表同时为 {@code null} 或空，将会抛出此异常
     */
    public RedisSubscribeClient(String host, SocketConfiguration configuration,
                                int pingPeriod, Transcoder transcoder,
                                List<NaiveRedisChannelSubscriber> channelSubscriberList,
                                List<NaiveRedisPatternSubscriber> patternSubscriberList,
                                UnusableServiceNotifier<RedisSubscribeClient> unusableServiceNotifier) throws IllegalArgumentException {
        this.host = host;
        this.pingPeriod = pingPeriod;
        this.channelSubscribersMap = toChannelSubscribersMap(channelSubscriberList);
        this.patternSubscribersMap = toPatternSubscribersMap(patternSubscriberList);
        if (this.channelSubscribersMap.isEmpty() && this.patternSubscribersMap.isEmpty()) {
            String errorMessage = "Fails to construct RedisSubscribeClient: `there is no channels or patterns`."
                    + LogBuildUtil.build(buildParameterMap(-1));
            REDIS_SUBSCRIBER_LOG.error(errorMessage);
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            this.transcoder = transcoder == null ? new StringTranscoder() : transcoder;
            if (configuration == null) {
                configuration = SocketConfiguration.DEFAULT;
            }
            this.socket = SocketBuilder.create(host, configuration);
            SocketMonitor monitor = SocketMonitorFactory.get(host);
            this.reader = new RedisDataReader(monitor, socket.getInputStream(),
                    configuration.getReceiveBufferSize() != null ? configuration.getReceiveBufferSize() : 64 * 1024);
            this.outputStream = new MonitoredSocketOutputStream(socket.getOutputStream(), monitor);
            this.unusableServiceNotifier = unusableServiceNotifier;
        } catch (Exception e) {
            String errorMessage = "Fails to construct RedisSubscribeClient: `unexpected error`."
                    + LogBuildUtil.build(buildParameterMap(-1));
            REDIS_SUBSCRIBER_LOG.error(errorMessage, e);
            LOGGER.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    /**
     * 判断当前 Redis 订阅客户端是否可用。
     *
     * @return 是否可用
     */
    public boolean isAvailable() {
        return state == BeanStatusEnum.NORMAL;
    }

    /**
     * 执行 RedisSubscribeClient 初始化操作，初始化成功后，自动接收已订阅的 channel 或 pattern 消息，初始化操作仅允许调用一次，
     * 重复调用不会产生任何效果。
     */
    public synchronized void init() {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            long startTime = System.currentTimeMillis();
            try {
                state = BeanStatusEnum.NORMAL;
                String socketAddress = host + "/" + socket.getLocalPort();
                ioTask = new RedisSubscribeIOTask();
                ioTask.setName("naiveredis-subscribe-io-" + socketAddress);
                ioTask.start();
                ioTask.awaitInitialization();
                startPingTask();
            } catch (Exception e) {
                closeDueToError("fails to initialize", e);
            }
            if (isAvailable()) {
                StringBuilder buffer = new StringBuilder();
                for (String channel : channelSubscribersMap.keySet()) {
                    List<NaiveRedisChannelSubscriber> channelSubscriberList = channelSubscribersMap.get(channel);
                    for (NaiveRedisChannelSubscriber channelSubscriber : channelSubscriberList) {
                        buffer.append("[ChannelSubscriber] channel: `").append(channel)
                                .append("`. subscriber: ").append(channelSubscriber).append("\n\r");
                    }
                }
                for (String pattern : patternSubscribersMap.keySet()) {
                    List<NaiveRedisPatternSubscriber> patternSubscriberList = patternSubscribersMap.get(pattern);
                    for (NaiveRedisPatternSubscriber patternSubscriber : patternSubscriberList) {
                        buffer.append("[PatternSubscriber] pattern: `").append(pattern)
                                .append("`. subscriber: ").append(patternSubscriber).append("\n\r");
                    }
                }
                if (buffer.length() > 2) {
                    buffer.delete(buffer.length() - 2, buffer.length());
                }
                Map<String, Object> parameterMap = buildParameterMap(System.currentTimeMillis() - startTime);
                REDIS_SUBSCRIBER_LOG.info("RedisSubscribeClient init success.{}\n\r{}",
                        LogBuildUtil.build(parameterMap), buffer.toString());
            }
        }
    }

    /**
     * 执行 RedisSubscribeClient 关闭操作，如果当前 RedisSubscribeClient 已关闭，调用此方法不会产生任何效果。
     */
    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            try {
                if (socket != null) {
                    socket.close();
                }
                if (ioTask != null) {
                    ioTask.interrupt();
                }
                if (pingExecutorService != null) {
                    pingExecutorService.shutdown();
                }
                Map<String, Object> parameterMap = buildParameterMap(System.currentTimeMillis() - startTime);
                REDIS_SUBSCRIBER_LOG.info("RedisSubscribeClient has been closed." + LogBuildUtil.build(parameterMap));
            } catch (Exception e) {
                Map<String, Object> parameterMap = buildParameterMap(System.currentTimeMillis() - startTime);
                String errorMessage = "Fails to close RedisSubscribeClient: `unexpected error`."
                        + LogBuildUtil.build(parameterMap);
                REDIS_SUBSCRIBER_LOG.error(errorMessage, e);
            } finally {
                if (unusableServiceNotifier != null) {
                    unusableServiceNotifier.onClosed(this);
                }
            }
        }
    }

    /**
     * 将 Redis channel 消息订阅者列表转换为 Map，Key 为 channel，Value 为该 channel 对应的订阅者列表，该方法不会返回 {@code null}，
     * 但可能返回空 Map。
     *
     * <p><strong>注意：</strong>该方法仅供 RedisSubscribeClient 构造函数调用。</p>
     *
     * @param channelSubscriberList Redis channel 消息订阅者列表，允许为 {@code null} 或空
     * @return Redis channel 消息订阅者 Map，不会为 {@code null}
     * @throws IllegalArgumentException 如果某个 Redis channel 消息订阅者返回的 channel 列表为 {@code null} 或空，或者包含 {@code null} 或空的 channel，将会抛出此异常
     */
    private Map<String, List<NaiveRedisChannelSubscriber>> toChannelSubscribersMap(
            List<NaiveRedisChannelSubscriber> channelSubscriberList) throws IllegalArgumentException {
        Map<String, List<NaiveRedisChannelSubscriber>> channelSubscribersMap = new HashMap<>();
        if (channelSubscriberList != null) {
            for (NaiveRedisChannelSubscriber channelSubscriber : channelSubscriberList) {
                List<String> channelList = channelSubscriber.getChannelList();
                if (channelList == null || channelList.isEmpty()) {
                    LinkedHashMap<String, Object> errorParameterMap = new LinkedHashMap<>();
                    errorParameterMap.put("host", host);
                    errorParameterMap.put("channelSubscriber", channelSubscriber);
                    String errorMessage = "Fails to construct RedisSubscribeClient: `invalid NaiveRedisChannelSubscriber, channelList is empty`."
                            + LogBuildUtil.build(errorParameterMap);
                    REDIS_SUBSCRIBER_LOG.error(errorMessage);
                    LOGGER.error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
                for (String channel : channelList) {
                    if (channel == null || channel.isEmpty()) {
                        LinkedHashMap<String, Object> errorParameterMap = new LinkedHashMap<>();
                        errorParameterMap.put("host", host);
                        errorParameterMap.put("channelSubscriber", channelSubscriber);
                        String errorMessage = "Fails to construct RedisSubscribeClient: `invalid NaiveRedisChannelSubscriber, channelList contain empty channel`."
                                + LogBuildUtil.build(errorParameterMap);
                        REDIS_SUBSCRIBER_LOG.error(errorMessage);
                        LOGGER.error(errorMessage);
                        throw new IllegalArgumentException(errorMessage);
                    }
                    List<NaiveRedisChannelSubscriber> subscriberListForTheChannel = channelSubscribersMap.computeIfAbsent(
                            channel, k -> new ArrayList<>()
                    );
                    subscriberListForTheChannel.add(channelSubscriber);
                }
            }
        }
        return channelSubscribersMap;
    }

    /**
     * 将 Redis pattern 消息订阅者列表转换为 Map，Key 为 pattern，Value 为该 pattern 对应的订阅者列表，该方法不会返回 {@code null}，
     * 但可能返回空 Map。
     *
     * <p><strong>注意：</strong>该方法仅供 RedisSubscribeClient 构造函数调用。</p>
     *
     * @param patternSubscriberList Redis pattern 消息订阅者列表，允许为 {@code null} 或空
     * @return Redis pattern 消息订阅者 Map，不会为 {@code null}
     * @throws IllegalArgumentException 如果某个 Redis pattern 消息订阅者返回的 pattern 列表为 {@code null} 或空，或者包含 {@code null} 或空的 pattern，将会抛出此异常
     */
    private Map<String, List<NaiveRedisPatternSubscriber>> toPatternSubscribersMap(
            List<NaiveRedisPatternSubscriber> patternSubscriberList) throws IllegalArgumentException {
        Map<String, List<NaiveRedisPatternSubscriber>> patternSubscribersMap = new HashMap<>();
        if (patternSubscriberList != null) {
            for (NaiveRedisPatternSubscriber patternSubscriber : patternSubscriberList) {
                List<String> patternList = patternSubscriber.getPatternList();
                if (patternList == null || patternList.isEmpty()) {
                    LinkedHashMap<String, Object> errorParameterMap = new LinkedHashMap<>();
                    errorParameterMap.put("host", host);
                    errorParameterMap.put("patternSubscriber", patternSubscriber);
                    String errorMessage = "Fails to construct RedisSubscribeClient: `invalid NaiveRedisPatternSubscriber, patternList is empty`."
                            + LogBuildUtil.build(errorParameterMap);
                    REDIS_SUBSCRIBER_LOG.error(errorMessage);
                    LOGGER.error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
                for (String pattern : patternList) {
                    if (pattern == null || pattern.isEmpty()) {
                        LinkedHashMap<String, Object> errorParameterMap = new LinkedHashMap<>();
                        errorParameterMap.put("host", host);
                        errorParameterMap.put("patternSubscriber", patternSubscriber);
                        String errorMessage = "Fails to construct RedisSubscribeClient: `invalid NaiveRedisPatternSubscriber, patternList contain empty pattern`."
                                + LogBuildUtil.build(errorParameterMap);
                        REDIS_SUBSCRIBER_LOG.error(errorMessage);
                        LOGGER.error(errorMessage);
                        throw new IllegalArgumentException(errorMessage);
                    }
                    List<NaiveRedisPatternSubscriber> subscriberListForThePattern = patternSubscribersMap.computeIfAbsent(
                            pattern, k -> new ArrayList<>()
                    );
                    subscriberListForThePattern.add(patternSubscriber);
                }
            }
        }
        return patternSubscribersMap;
    }

    /**
     * 构造一个参数 Map，供日志打印使用。
     *
     * @param cost 程序执行耗时，单位：毫秒，如果该值小于 0，则参数 Map 中不会包含该字段
     * @return 参数 Map
     */
    private Map<String, Object> buildParameterMap(long cost) {
        LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
        if (cost >= 0) {
            parameterMap.put("cost", cost + "ms");
        }
        parameterMap.put("host", host);
        parameterMap.put("pingPeriod", pingPeriod);
        parameterMap.put("channels", channelSubscribersMap.keySet());
        parameterMap.put("patterns", patternSubscribersMap.keySet());
        return parameterMap;
    }

    /**
     * 启动心跳检测任务。
     */
    private synchronized void startPingTask() {
        if (pingPeriod > 0 && isAvailable()) {
            pingExecutorService = Executors.newSingleThreadScheduledExecutor();
            pingExecutorService.scheduleWithFixedDelay(() -> {
                if (isAvailable() && ioTask != null) {
                    long startTime = System.currentTimeMillis();
                    try {
                        if (System.currentTimeMillis() - ioTask.lastActiveTime > pingPeriod * 1000) {
                            if (isAvailable()) {
                                pingLatch = new CountDownLatch(1);
                                PingCommand pingCommand = new PingCommand();
                                outputStream.write(pingCommand.getRequestByteArray());
                                outputStream.flush();
                                boolean isSuccess = pingLatch.await(5, TimeUnit.SECONDS);
                                if (!isSuccess) {
                                    closeDueToError("no response for ping command", null);
                                } else {
                                    if (REDIS_SUBSCRIBER_LOG.isDebugEnabled()) {
                                        REDIS_SUBSCRIBER_LOG.debug("Execute ping command success.{}",
                                                buildParameterMap(System.currentTimeMillis() - startTime));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        closeDueToError("fails to send ping", e);
                    }
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * 打印日志记录错误原因后，执行关闭操作，如果当前 RedisSubscribeClient 已关闭，调用此方法不会产生任何效果。
     *
     * @param errorMessage 错误描述信息
     * @param exception 异常信息，允许为 {@code null}
     */
    private synchronized void closeDueToError(String errorMessage, Exception exception) {
        if (state != BeanStatusEnum.CLOSED) {
            Map<String, Object> parameter = buildParameterMap(-1);
            errorMessage = "RedisSubscribeClient need to be closed: `" + errorMessage + "`." + LogBuildUtil.build(parameter);
            if (exception == null) {
                REDIS_SUBSCRIBER_LOG.error(errorMessage);
                LOGGER.error(errorMessage);
            } else {
                REDIS_SUBSCRIBER_LOG.error(errorMessage, exception);
                LOGGER.error(errorMessage, exception);
            }
            close();
        }
    }

    /**
     * Redis 订阅客户端使用的 IO 线程。
     */
    private class RedisSubscribeIOTask extends Thread {

        /**
         * 用于判断 channel 订阅命令是否已响应
         *
         * @see #subscribeChannels()
         */
        private final CountDownLatch subscribeChannelLatch = new CountDownLatch(1);

        /**
         * 用于判读 pattern 订阅命令是否已响应
         */
        private final CountDownLatch subscribePatternLatch = new CountDownLatch(1);

        /**
         * 响应数据类型：接收 channel 消息
         */
        private final byte[] MESSAGE_TYPE_VALUE_BYTES = "message".getBytes(StandardCharsets.UTF_8);

        /**
         * 响应数据类型：接收 pattern 消息
         */
        private final byte[] PMESSAGE_TYPE_VALUE_BYTES = "pmessage".getBytes(StandardCharsets.UTF_8);

        /**
         * 响应数据类型：接收 ping 命令返回消息
         */
        private final byte[] PONG_TYPE_VALUE_BYTES = "pong".getBytes(StandardCharsets.UTF_8);

        /**
         * 响应数据类型：订阅 channel 成功响应消息
         */
        private final byte[] SUBSCRIBE_TYPE_VALUE_BYTES = "subscribe".getBytes(StandardCharsets.UTF_8);

        /**
         * 响应数据类型：订阅 pattern 成功响应消息
         */
        private final byte[] PSUBSCRIBE_TYPE_VALUE_BYTES = "psubscribe".getBytes(StandardCharsets.UTF_8);

        /**
         * 最后一次收到响应消息的时间
         */
        private volatile long lastActiveTime = 0;

        /**
         * 等待 channel 订阅和 pattern 订阅命令完成，如果等待超时，将会关闭当前 RedisSubscribeClient 实例。
         *
         * @throws InterruptedException 在等待过程中发生线程中断请求，将会抛出此异常
         */
        private void awaitInitialization() throws InterruptedException {
            long timeoutMills = 5000; // 等待超时时间 5000
            long startTime = System.currentTimeMillis();
            boolean isSubscribeChannelSuccess = subscribeChannelLatch.await(timeoutMills, TimeUnit.MILLISECONDS);
            if (isSubscribeChannelSuccess) {
                timeoutMills = timeoutMills - (System.currentTimeMillis() - startTime);
                boolean isSubscribePatternSuccess = subscribePatternLatch.await(timeoutMills, TimeUnit.MILLISECONDS);
                if (!isSubscribePatternSuccess) {
                    closeDueToError("subscribe patterns timeout", null);
                }
            } else {
                closeDueToError("subscribe channels timeout", null);
            }
        }

        @Override
        public void run() {
            lastActiveTime = System.currentTimeMillis();
            subscribeChannels();
            if (isAvailable()) {
                subscribePatterns();
            }
            try {
                while (state == BeanStatusEnum.NORMAL) {
                    RedisData response = reader.read();
                    lastActiveTime = System.currentTimeMillis();
                    if (response.isArray()) {
                        byte[] valueBytes = response.get(0).getValueBytes();
                        if (Arrays.equals(MESSAGE_TYPE_VALUE_BYTES, valueBytes)) {
                            onChannelMessageReceived(response);
                        } else if (Arrays.equals(PMESSAGE_TYPE_VALUE_BYTES, valueBytes)) {
                            onPatternMessageReceived(response);
                        } else if (Arrays.equals(PONG_TYPE_VALUE_BYTES, valueBytes)) {
                            if (pingLatch != null) {
                                pingLatch.countDown();
                            }
                        } else if (Arrays.equals(SUBSCRIBE_TYPE_VALUE_BYTES, valueBytes)) {
                            subscribeChannelLatch.countDown();
                        } else if (Arrays.equals(PSUBSCRIBE_TYPE_VALUE_BYTES, valueBytes)) {
                            subscribePatternLatch.countDown();
                        } else {
                            String errorMessage = "Unrecognized response: `" + response + "`."
                                    + LogBuildUtil.build(buildParameterMap(-1));
                            REDIS_SUBSCRIBER_LOG.error(errorMessage);
                        }
                    }
                }
            } catch (Exception e) {
                closeDueToError("unexpected error", e);
            }
            close();
            subscribeChannelLatch.countDown();
            subscribePatternLatch.countDown();
        }

        /**
         * 处理接收到的 Channel 消息，该方法不会抛出任何异常。
         *
         * @param response Redis 响应数据
         */
        private void onChannelMessageReceived(RedisData response) {
            long startTime = System.currentTimeMillis();
            String channel;
            Object message;
            try {
                channel = response.get(1).getText();
                message = transcoder.decode(response.get(2).getValueBytes());
            } catch (Exception e) {
                Map<String, Object> parameter = buildParameterMap(-1);
                parameter.put("response", response);
                String errorMessage = "Fails to receive channel message: `decode message failed`." + LogBuildUtil.build(parameter);
                REDIS_SUBSCRIBER_LOG.error(errorMessage, e);
                return;
            }
            List<NaiveRedisChannelSubscriber> subscriberList = channelSubscribersMap.get(channel);
            if (subscriberList == null || subscriberList.isEmpty()) { // should not happen, just for bug detection
                Map<String, Object> parameter = buildParameterMap(-1);
                parameter.put("channel", channel);
                parameter.put("message", message);
                parameter.put("response", response);
                String errorMessage = "Fails to receive channel message: `empty subscriber list`." + LogBuildUtil.build(parameter);
                REDIS_SUBSCRIBER_LOG.error(errorMessage);
                return;
            }
            for (NaiveRedisChannelSubscriber subscriber : subscriberList) {
                try {
                    subscriber.consume(channel, message);
                } catch (Exception e) {
                    Map<String, Object> parameter = new LinkedHashMap<>();
                    parameter.put("host", host);
                    parameter.put("channel", channel);
                    parameter.put("message", message);
                    parameter.put("subscriber", subscriber);
                    String errorMessage = "Fails to consume channel message." + LogBuildUtil.build(parameter);
                    REDIS_SUBSCRIBER_LOG.error(errorMessage , e);
                }
            }
            if (REDIS_SUBSCRIBER_LOG.isDebugEnabled()) {
                Map<String, Object> parameter = new LinkedHashMap<>();
                parameter.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                parameter.put("host", host);
                parameter.put("channel", channel);
                parameter.put("message", message);
                REDIS_SUBSCRIBER_LOG.debug("Consume channel message success." + LogBuildUtil.build(parameter));
            }
        }

        /**
         * 处理接收到的 Pattern 消息，该方法不会抛出任何异常。
         *
         * @param response Redis 响应数据
         */
        private void onPatternMessageReceived(RedisData response) {
            long startTime = System.currentTimeMillis();
            String pattern;
            String channel;
            Object message;
            try {
                pattern = response.get(1).getText();
                channel = response.get(2).getText();
                message = transcoder.decode(response.get(3).getValueBytes());
            } catch (Exception e) {
                Map<String, Object> parameter = buildParameterMap(-1);
                parameter.put("response", response);
                String errorMessage = "Fails to receive pattern message: `decode message failed`." + LogBuildUtil.build(parameter);
                REDIS_SUBSCRIBER_LOG.error(errorMessage, e);
                return;
            }
            List<NaiveRedisPatternSubscriber> patternSubscriberList = patternSubscribersMap.get(pattern);
            if (patternSubscriberList == null || patternSubscriberList.isEmpty()) { // should not happen, just for bug detection
                Map<String, Object> parameter = buildParameterMap(-1);
                parameter.put("pattern", pattern);
                parameter.put("channel", channel);
                parameter.put("message", message);
                parameter.put("response", response);
                String errorMessage = "Fails to receive pattern message: `empty subscriber list`." + LogBuildUtil.build(parameter);
                REDIS_SUBSCRIBER_LOG.error(errorMessage);
                return;
            }
            for (NaiveRedisPatternSubscriber subscriber : patternSubscriberList) {
                try {
                    subscriber.consume(pattern, channel, message);
                } catch (Exception e) {
                    Map<String, Object> parameter = new LinkedHashMap<>();
                    parameter.put("host", host);
                    parameter.put("pattern", pattern);
                    parameter.put("channel", channel);
                    parameter.put("message", message);
                    parameter.put("subscriber", subscriber);
                    String errorMessage = "Fails to consume pattern message." + LogBuildUtil.build(parameter);
                    REDIS_SUBSCRIBER_LOG.error(errorMessage , e);
                }
            }
            if (REDIS_SUBSCRIBER_LOG.isDebugEnabled()) {
                Map<String, Object> parameter = new LinkedHashMap<>();
                parameter.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                parameter.put("host", host);
                parameter.put("pattern", pattern);
                parameter.put("channel", channel);
                parameter.put("message", message);
                REDIS_SUBSCRIBER_LOG.debug("Consume pattern message success." + LogBuildUtil.build(parameter));
            }
        }

        /**
         * 发送 channel 订阅命令，如果 channel 列表为空，则不执行任何操作，该方法不会抛出任何异常。
         */
        private void subscribeChannels() {
            Set<String> channelSet = channelSubscribersMap.keySet();
            if (!channelSet.isEmpty()) {
                try {
                    SubscribeCommand command = new SubscribeCommand(channelSet);
                    outputStream.write(command.getRequestByteArray());
                    outputStream.flush();
                } catch (Exception e) {
                    closeDueToError("fails to subscribe channels", e);
                }
            } else {
                subscribeChannelLatch.countDown();
            }
        }

        /**
         * 发送 pattern 订阅命令，如果 pattern 列表为空，则不执行任何操作，该方法不会抛出任何异常。
         */
        private void subscribePatterns() {
            Set<String> patternSet = patternSubscribersMap.keySet();
            if (!patternSet.isEmpty()) {
                try {
                    PSubscribeCommand command = new PSubscribeCommand(patternSet);
                    outputStream.write(command.getRequestByteArray());
                    outputStream.flush();
                } catch (Exception e) {
                    closeDueToError("fails to subscribe patterns", e);
                }
            } else {
                subscribePatternLatch.countDown();
            }
        }
    }
}
