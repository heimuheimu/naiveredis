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

package com.heimuheimu.naiveredis;

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.Command;
import com.heimuheimu.naiveredis.command.regular.ExpireCommand;
import com.heimuheimu.naiveredis.command.regular.GetCommand;
import com.heimuheimu.naiveredis.command.regular.IncrByCommand;
import com.heimuheimu.naiveredis.command.regular.SetCommand;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.UnusableServiceNotifier;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naiveredis.net.BuildSocketException;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.transcoder.SimpleTranscoder;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 直连客户端。可访问以下网站来获得更多 Redis 信息：<a href="https://redis.io">https://redis.io</a>
 *
 * <p><strong>说明：</strong>{@code DirectRedisClient} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class DirectRedisClient implements NaiveRedisClient {

    /**
     * Redis 命令执行错误日志
     */
    private static final Logger NAIVEREDIS_ERROR_LOG = LoggerFactory.getLogger("NAIVEREDIS_ERROR_LOG");

    /**
     * Redis 命令慢执行日志
     */
    private static final Logger NAIVEREDIS_SLOW_EXECUTION_LOG = LoggerFactory.getLogger("NAIVEREDIS_SLOW_EXECUTION_LOG");

    /**
     * {@code DirectRedisClient} 使用的错误日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(DirectRedisClient.class);

    /**
     * Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * Redis 操作超时时间，单位：毫秒，不能小于等于 0
     */
    private final int timeout;

    /**
     * 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于0，执行 Redis 命令时间大于该值时，将进行慢执行日志打印
     */
    private final long slowExecutionThreshold;

    /**
     * 与 Redis 服务进行数据交互的管道
     */
    private final RedisChannel redisChannel;

    /**
     * Java 对象与字节数组转换器
     */
    private final Transcoder transcoder;

    /**
     * 当前 Redis 客户端使用的操作执行信息监控器
     */
    private final ExecutionMonitor executionMonitor;

    /**
     * 构造一个 Redis 直连客户端，{@link java.net.Socket} 配置信息使用 {@link SocketConfiguration#DEFAULT}，Redis 操作超时时间设置为 5 秒，
     * 最小压缩字节数设置为 64 KB，Redis 操作过慢最小时间设置为 50 毫秒，心跳检测时间设置为 30 秒。
     *
     * @param host Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @throws IllegalArgumentException 如果 Redis 地址不符合规则，将会抛出此异常
     * @throws BuildSocketException 如果创建 {@link java.net.Socket} 过程中发生错误，将会抛出此异常
     */
    public DirectRedisClient(String host) throws IllegalArgumentException, BuildSocketException {
        this(host, null);
    }

    /**
     * 构造一个 Redis 直连客户端，{@link java.net.Socket} 配置信息使用 {@link SocketConfiguration#DEFAULT}，Redis 操作超时时间设置为 5 秒，
     * 最小压缩字节数设置为 64 KB，Redis 操作过慢最小时间设置为 50 毫秒，心跳检测时间设置为 30 秒。
     *
     * @param host Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param unusableServiceNotifier {@code DirectRedisClient} 不可用通知器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis 地址不符合规则，将会抛出此异常
     * @throws BuildSocketException 如果创建 {@link java.net.Socket} 过程中发生错误，将会抛出此异常
     */
    public DirectRedisClient(String host, UnusableServiceNotifier<DirectRedisClient> unusableServiceNotifier) throws IllegalArgumentException, BuildSocketException {
        this(host, null, 5000, 64 * 1024, 50, 30, unusableServiceNotifier);
    }

    /**
     * 构造一个 Redis 直连客户端。
     *
     * @param host Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration {@link java.net.Socket} 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param unusableServiceNotifier {@code DirectRedisClient} 不可用通知器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis 地址不符合规则，将会抛出此异常
     * @throws IllegalArgumentException 如果 Redis 操作超时时间小于等于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果最小压缩字节数小于等于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 Redis 操作过慢最小时间小于等于 0，将会抛出此异常
     * @throws BuildSocketException 如果创建 {@link java.net.Socket} 过程中发生错误，将会抛出此异常
     */
    public DirectRedisClient(String host, SocketConfiguration configuration, int timeout, int compressionThreshold, int slowExecutionThreshold,
        int pingPeriod, UnusableServiceNotifier<DirectRedisClient> unusableServiceNotifier) throws IllegalArgumentException, BuildSocketException {
        ConstructorParameterChecker parameterChecker = new ConstructorParameterChecker("DirectRedisClient", LOG);
        parameterChecker.addParameter("host", host);
        parameterChecker.addParameter("socketConfiguration", configuration);
        parameterChecker.addParameter("timeout", timeout);
        parameterChecker.addParameter("compressionThreshold", compressionThreshold);
        parameterChecker.addParameter("slowExecutionThreshold", slowExecutionThreshold);
        parameterChecker.addParameter("pingPeriod", pingPeriod);

        parameterChecker.check("timeout", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);
        parameterChecker.check("compressionThreshold", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);
        parameterChecker.check("slowExecutionThreshold", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);

        this.redisChannel = new RedisChannel(host, configuration, pingPeriod, channel -> {
            if (unusableServiceNotifier != null) {
                unusableServiceNotifier.onClosed(DirectRedisClient.this);
            }
        });
        this.redisChannel.init();
        this.host = host;
        this.timeout = timeout;
        this.transcoder = new SimpleTranscoder(compressionThreshold);
        this.slowExecutionThreshold = TimeUnit.NANOSECONDS.convert(slowExecutionThreshold, TimeUnit.MILLISECONDS); //将毫秒转换为纳秒
        this.executionMonitor = ExecutionMonitorFactory.get(host);
    }

    /**
     * 获得 Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     *
     * @return Redis 地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 判断当前 Redis 直连客户端是否可用。
     *
     * @return 直连客户端是否可用
     */
    public boolean isAvailable() {
        return redisChannel.isAvailable();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = "DirectRedisClient#get(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (T) execute(methodName, parameterChecker.getParameterMap(), () -> new GetCommand(key), response -> {
            if (response.getValueBytes() == null) { // Key 不存在
                NAIVEREDIS_ERROR_LOG.warn(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "key not found", parameterChecker.getParameterMap()));
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_KEY_NOT_FOUND);
                return null;
            } else {
                return transcoder.decode(response.getValueBytes());
            }
        });
    }

    @Override
    public void set(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        set(key, value, -1);
    }

    @Override
    public void set(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = "DirectRedisClient#set(String key, Object value, int expiry)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("value", value);
        parameterChecker.addParameter("expiry", expiry);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("value", "isNull", Parameters::isNull);


        execute(methodName, parameterChecker.getParameterMap(), () -> new SetCommand(key, transcoder.encode(value), expiry), null);
    }

    @Override
    public void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = "DirectRedisClient#expire(String key, int expiry)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("expiry", expiry);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("expiry", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);

        execute(methodName, parameterChecker.getParameterMap(), () -> new ExpireCommand(key, expiry), null);
    }

    @Override
    public Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = "DirectRedisClient#getCount(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (Long) execute(methodName, parameterChecker.getParameterMap(), () -> new GetCommand(key), response -> {
            if (response.getValueBytes() == null) { // Key 不存在
                NAIVEREDIS_ERROR_LOG.warn(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "key not found", parameterChecker.getParameterMap()));
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_KEY_NOT_FOUND);
                return null;
            } else {
                return Long.valueOf(response.getText());
            }
        });
    }

    @Override
    public long addAndGet(String key, long delta, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = "DirectRedisClient#addAndGet(String key, long delta, int expiry)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("delta", delta);
        parameterChecker.addParameter("expiry", expiry);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        //noinspection ConstantConditions
        return (Long) execute(methodName, parameterChecker.getParameterMap(), () -> new IncrByCommand(key, delta), response -> {
            long value = Long.valueOf(response.getText());
            if (expiry > 0 && value == delta) { // 如果是该 Key 的第一次操作，则进行过期时间设置
                expire(key, expiry);
            }
            return value;
        });
    }

    @Override
    public void close() {
        redisChannel.close();
    }

    @Override
    public String toString() {
        return "DirectRedisClient{" +
                "host='" + host + '\'' +
                ", timeout=" + timeout +
                ", slowExecutionThreshold=" + slowExecutionThreshold +
                ", redisChannel=" + redisChannel +
                ", transcoder=" + transcoder +
                '}';
    }

    private interface CommandBuilder {

        Command build() throws Exception;
    }

    private interface ResponseParser {

        Object parse(RedisData response) throws Exception;
    }

    private MethodParameterChecker buildRedisCommandMethodParameterChecker(String methodName) {
        MethodParameterChecker checker = new MethodParameterChecker(methodName, NAIVEREDIS_ERROR_LOG, parameterName -> executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_ARGUMENT));
        checker.addParameter("host", host);
        return checker;
    }

    private Object execute(String methodName, Map<String, Object> paramMap, CommandBuilder builder, ResponseParser parser) {
        long startTime = System.nanoTime();
        try {
            Command command = builder.build();
            RedisData response = redisChannel.send(command, timeout);
            if (response.isError()) { // 判断 Redis 服务是否返回错误信息
                String errorMessage = response.getText();
                NAIVEREDIS_ERROR_LOG.error(LogBuildUtil.buildMethodExecuteFailedLog(methodName, errorMessage, paramMap));
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_REDIS_ERROR);
                throw new RedisException(RedisException.CODE_REDIS_SERVER, errorMessage);
            }
            if (parser != null) {
                return parser.parse(response);
            } else {
                return null;
            }
        } catch (RedisException e) {
            throw e;
        } catch (IllegalStateException e) {
            NAIVEREDIS_ERROR_LOG.error(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "client has been closed", paramMap));
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_STATE);
            throw e;
        } catch (TimeoutException e) {
            NAIVEREDIS_ERROR_LOG.error(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "wait response timeout (" + timeout + "ms)", paramMap));
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_TIMEOUT);
            throw e;
        } catch (Exception e) { // should not happen, for bug detection
            String errorLog = LogBuildUtil.buildMethodExecuteFailedLog(methodName, "unexpected error", paramMap);
            NAIVEREDIS_ERROR_LOG.error(errorLog);
            LOG.error(errorLog, e);
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            throw new RedisException(RedisException.CODE_UNEXPECTED_ERROR, errorLog, e);
        } finally {
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                NAIVEREDIS_SLOW_EXECUTION_LOG.info("`Method`:`{}`. `Cost`:`{}ns ({}ms)`. `Host`:`{}`.{}", methodName, executedNanoTime,
                        TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), host, LogBuildUtil.build(paramMap));
            }
            executionMonitor.onExecuted(startTime);
        }
    }
}
