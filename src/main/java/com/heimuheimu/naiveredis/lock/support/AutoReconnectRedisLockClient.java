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

package com.heimuheimu.naiveredis.lock.support;

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.keys.CustomDeleteCommand;
import com.heimuheimu.naiveredis.command.storage.SetCommand;
import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.constant.SetMode;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.Methods;
import com.heimuheimu.naiveredis.monitor.RedisLockClientMonitorFactory;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自动重连 Redis 分布式锁客户端，当运行过程中出现因网络原因或其它异常错误导致连接断开，会触发自动重连机制。
 *
 * <p><strong>说明：</strong>AutoReconnectRedisPublishClient 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class AutoReconnectRedisLockClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoReconnectRedisLockClient.class);

    private static final Logger REDIS_DISTRIBUTED_LOCK_LOG = LoggerFactory.getLogger("NAIVEREDIS_DISTRIBUTED_LOCK_LOG");

    /**
     * Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     */
    private final SocketConfiguration configuration;

    /**
     * PING 命令发送时间间隔，单位：秒
     */
    private final int pingPeriod;

    /**
     * 自动重连 Redis 分布式锁客户端事件监听器
     */
    private final AutoReconnectRedisLockClientListener listener;

    /**
     * Redis 分布式锁客户端使用的执行信息监控器
     */
    private final ExecutionMonitor monitor;

    /**
     * 与 Redis 服务进行数据交互的管道
     */
    private volatile RedisChannel channel;

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state;

    /**
     * 恢复任务使用的锁
     */
    private final Object rescueLock = new Object();

    /**
     * 当前实例使用的私有锁
     */
    private final Object lock = new Object();

    /**
     * 构造一个 AutoReconnectRedisLockClient 实例。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener 自动重连 Redis 分布式锁客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果 AutoReconnectRedisLockClient 创建过程中发生错误，将会抛出此异常
     */
    public AutoReconnectRedisLockClient(String host, SocketConfiguration configuration, int pingPeriod,
                                        AutoReconnectRedisLockClientListener listener) throws IllegalStateException {
        this.host = host;
        this.configuration = configuration;
        this.pingPeriod = pingPeriod;
        this.listener = listener;
        this.monitor = RedisLockClientMonitorFactory.get(host);
        synchronized (lock) {
            try {
                this.channel = new RedisChannel(this.host, this.configuration, this.pingPeriod, this::startRescueTask);
                this.channel.init();
            } catch (Exception e) {
                String errorMessage = "Fails to construct AutoReconnectRedisLockClient: `create RedisChannel failed`."
                        + LogBuildUtil.build(buildParameterMap());
                REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
                LOGGER.error(errorMessage, e);
                throw new IllegalStateException(errorMessage, e);
            }
            if (this.channel.isAvailable()) {
                state = BeanStatusEnum.NORMAL;
                Methods.invokeIfNotNull("AutoReconnectRedisLockClientListener#onCreated(String host)",
                        buildParameterMap(), this.listener, () -> this.listener.onCreated(this.host));
            } else {
                String errorMessage = "Fails to construct AutoReconnectRedisLockClient: `RedisChannel is not available`."
                        + LogBuildUtil.build(buildParameterMap());
                REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
                LOGGER.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }
    }

    /**
     * 提供可异步获取的 Redis 分布式锁，该方法不会返回 {@code null}。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @param token 锁对应的 token 值，不允许为 {@code null} 或空字符串
     * @param expiry 锁的过期时间，单位：秒，不允许小于等于 0
     * @return 可异步获取的 Redis 分布式锁
     * @throws IllegalArgumentException 如果 name 为 {@code null} 或空字符串，将会抛出此异常
     * @throws IllegalArgumentException 如果 token 为 {@code null} 或空字符串，将会抛出此异常
     * @throws IllegalArgumentException 如果 expiry 小于等于 0，将会抛出此异常
     * @throws IllegalStateException 如果与 Redis 服务进行数据交互的管道已关闭，将会抛出此异常
     * @throws RedisException 如果此方法执行出现未预期异常，将会抛出此异常
     */
    public FutureLock submit(String name, String token, int expiry) throws IllegalArgumentException,
            IllegalStateException, RedisException{
        long startNanoTime = System.nanoTime();
        if (name == null || name.isEmpty()) {
            monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT);
            monitor.onExecuted(startNanoTime);
            String errorMessage = buildLockErrorMessage("name could not be null or empty", name, token, expiry);
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (token == null || token.isEmpty()) {
            monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT);
            String errorMessage = buildLockErrorMessage("token could not be null or empty", name, token, expiry);
            monitor.onExecuted(startNanoTime);
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (expiry <= 0) {
            monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT);
            monitor.onExecuted(startNanoTime);
            String errorMessage = buildLockErrorMessage("expiry must greater than 0", name, token, expiry);
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            SetCommand command = new SetCommand(name, token.getBytes(RedisData.UTF8), expiry, SetMode.SET_IF_ABSENT);
            channel.asyncSend(command);
            return new FutureLock(startNanoTime, name, token, expiry, command, channel, monitor);
        } catch (IllegalStateException e) {
            monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_STATE);
            monitor.onExecuted(startNanoTime);
            String errorMessage = buildLockErrorMessage("channel has been closed", name, token, expiry);
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        } catch (Exception e) {
            monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            monitor.onExecuted(startNanoTime);
            String errorMessage = buildLockErrorMessage("unexpected error", name, token, expiry);
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new RedisException(RedisException.CODE_UNEXPECTED_ERROR, errorMessage, e);
        }
    }

    /**
     * 尝试获取指定名称的 Redis 分布式锁，如果获取成功，返回 {@code true}，否则返回 {@code false}。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @param token 锁对应的 token 值，不允许为 {@code null} 或空字符串
     * @param expiry 锁的过期时间，单位：秒，不允许小于等于 0
     * @param timeout 获取锁的超时时间，单位：毫秒，不允许小于等于 0
     * @return Redis 分布式锁获取的结果
     * @throws IllegalArgumentException 如果 name 为 {@code null} 或空字符串，将会抛出此异常
     * @throws IllegalArgumentException 如果 token 为 {@code null} 或空字符串，将会抛出此异常
     * @throws IllegalArgumentException 如果 expiry 小于等于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 timeout 小于等于 0，将会抛出此异常
     * @throws IllegalStateException 如果与 Redis 服务进行数据交互的管道已关闭，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 命令在等待 Redis 分布式锁获取结果时被关闭，将会抛出此异常
     * @throws TimeoutException 如果等待 Redis 分布式锁获取的结果超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错或出现其它未预期异常，将会抛出此异常
     */
    public boolean tryLock(String name, String token, int expiry, int timeout)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        FutureLock futureLock = submit(name, token, expiry);
        return futureLock.get(timeout);
    }

    /**
     * 释放指定名称的锁。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @param token 锁对应的 token 值，不允许为 {@code null} 或空字符串
     * @throws IllegalStateException 如果 RedisChannel 未初始化或已被关闭，将抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错或出现其它未预期异常，将会抛出此异常
     */
    public void unlock(String name, String token) throws IllegalStateException, RedisException {
        try {
            CustomDeleteCommand command = new CustomDeleteCommand(name, token.getBytes(RedisData.UTF8));
            channel.asyncSend(command);
        } catch (IllegalStateException e) {
            String errorMessage = "Fails to unlock: `illegal state`. `name`:`" + name + "`. `token`:`" + token + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Fails to unlock: `unexpected error`. `name`:`" + name + "`. `token`:`" + token + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new RedisException(RedisException.CODE_UNEXPECTED_ERROR, errorMessage, e);
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (state != BeanStatusEnum.CLOSED) {
                state = BeanStatusEnum.CLOSED;
                if (channel != null) {
                    channel.close();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "AutoReconnectRedisLockClient{" +
                "host='" + host + '\'' +
                ", configuration=" + configuration +
                ", pingPeriod=" + pingPeriod +
                ", channel=" + channel +
                ", state=" + state +
                '}';
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
        parameterMap.put("pingPeriod", pingPeriod);
        return parameterMap;
    }

    /**
     * 构造锁获取失败错误日志。
     *
     * @param reason 失败原因
     * @param name 锁名称
     * @param token 锁对应的 token 值
     * @param expiry 锁的过期时间，单位：秒
     * @return 锁获取失败错误日志
     */
    private String buildLockErrorMessage(String reason, String name, String token, int expiry) {
        LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("host", host);
        parameterMap.put("name", name);
        parameterMap.put("token", token);
        parameterMap.put("expiry", expiry + "s");
        return  "Acquire redis distributed lock failed: `" + reason + "`." + LogBuildUtil.build(parameterMap);
    }

    /**
     * 当 RedisChannel 不可用时，启动恢复线程。
     *
     * @param unavailableChannel 不可用的 RedisChannel 实例
     */
    private void startRescueTask(RedisChannel unavailableChannel) {
        synchronized (lock) {
            if (state == BeanStatusEnum.NORMAL && unavailableChannel == channel) {
                Thread rescueThread = new Thread(() -> {
                    synchronized (rescueLock) {
                        if (state == BeanStatusEnum.NORMAL && unavailableChannel == channel) {
                            Methods.invokeIfNotNull("AutoReconnectRedisLockClientListener#onClosed(String host)",
                                    buildParameterMap(), this.listener, () -> this.listener.onClosed(this.host));
                            long startTime = System.currentTimeMillis();
                            REDIS_DISTRIBUTED_LOCK_LOG.info("RedisLockClient rescue task has been started.{}",
                                    LogBuildUtil.build(buildParameterMap()));
                            while (state == BeanStatusEnum.NORMAL) {
                                RedisChannel channel;
                                try {
                                    channel = new RedisChannel(host, configuration, pingPeriod, this::startRescueTask);
                                    channel.init();
                                    synchronized (lock) {
                                        if (channel.isAvailable()) {
                                            if (state == BeanStatusEnum.NORMAL) {
                                                this.channel = channel;
                                            } else {
                                                channel.close();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    REDIS_DISTRIBUTED_LOCK_LOG.error("Fails to rescue `RedisLockClient`." +
                                            LogBuildUtil.build(buildParameterMap()), e);
                                }
                                if (this.channel.isAvailable()) {
                                    REDIS_DISTRIBUTED_LOCK_LOG.info("RedisLockClient rescue task has been finished. `cost`:`{}ms`.{}",
                                            System.currentTimeMillis() - startTime, LogBuildUtil.build(buildParameterMap()));
                                    Methods.invokeIfNotNull("AutoReconnectRedisLockClientListener#onRecovered(String host)",
                                            buildParameterMap(), this.listener, () -> this.listener.onRecovered(this.host));
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
                rescueThread.setName("naiveredis-lock-client-rescue-task");
                rescueThread.setDaemon(true);
                rescueThread.start();
            }
        }
    }
}
