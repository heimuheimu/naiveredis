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
import com.heimuheimu.naiveredis.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.transcoder.SimpleTranscoder;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
     * Redis 操作超时时间，单位：毫秒，不能小于等于0
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

    public DirectRedisClient(String host, SocketConfiguration configuration, int timeout, int compressionThreshold,
                             int slowExecutionThreshold, int pingPeriod) {
        this.redisChannel = new RedisChannel(host, configuration, pingPeriod);
        this.redisChannel.init();
        this.host = host;
        this.timeout = timeout;
        this.transcoder = new SimpleTranscoder(compressionThreshold);
        this.slowExecutionThreshold = TimeUnit.NANOSECONDS.convert(slowExecutionThreshold, TimeUnit.MILLISECONDS); //将毫秒转换为纳秒
        this.executionMonitor = ExecutionMonitorFactory.get(host);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String opName = "get";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key", key);

        checkParameter(opName, "key", paramMap, paramValue -> paramValue == null || ((String) paramValue).isEmpty());

        return (T) execute(opName, paramMap, () -> new GetCommand(key), response -> transcoder.decode(response.getValueBytes()));
    }

    @Override
    public void set(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        set(key, value, -1);
    }

    @Override
    public void set(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String opName = "set";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key", key);
        paramMap.put("value", value);
        paramMap.put("expiry", expiry);

        checkParameter(opName, "key", paramMap, paramValue -> paramValue == null || ((String) paramValue).isEmpty());
        checkParameter(opName, "value", paramMap, Objects::isNull);

        execute(opName, paramMap, () -> new SetCommand(key, transcoder.encode(value), expiry), null);
    }

    @Override
    public void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String opName = "expire";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key", key);
        paramMap.put("expiry", expiry);

        checkParameter(opName, "key", paramMap, paramValue -> paramValue == null || ((String) paramValue).isEmpty());
        checkParameter(opName, "expiry", paramMap, paramValue -> (Integer) paramValue <= 0);

        execute(opName, paramMap, () -> new ExpireCommand(key, expiry), null);
    }

    @Override
    public Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String opName = "get-count";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key", key);

        checkParameter(opName, "key", paramMap, paramValue -> paramValue == null || ((String) paramValue).isEmpty());

        return (Long) execute(opName, paramMap, () -> new GetCommand(key), response -> {
            if (response.getValueBytes() == null) { // Key 不存在
                NAIVEREDIS_ERROR_LOG.warn("[{}] `Host`:`{}`. `Error`:`key not found`.{}",
                        opName, host, LogBuildUtil.build(paramMap));
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_KEY_NOT_FOUND);
                return null;
            } else {
                return Long.valueOf(response.getText());
            }
        });
    }

    @Override
    public long addAndGet(String key, long delta, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String opName = "add-and-get";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key", key);
        paramMap.put("delta", delta);
        paramMap.put("expiry", expiry);

        checkParameter(opName, "key", paramMap, paramValue -> paramValue == null || ((String) paramValue).isEmpty());

        //noinspection ConstantConditions
        return (Long) execute(opName, paramMap, () -> new IncrByCommand(key, delta), response -> {
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

    private interface ParameterChecker {

        boolean isIllegal(Object paramValue);
    }

    private interface CommandBuilder {

        Command build() throws Exception;
    }

    private interface ResponseParser {

        Object parse(RedisData response) throws Exception;
    }

    private void checkParameter(String opName, String paramName, Map<String, Object> paramMap, ParameterChecker checker) {
        Object paramValue = paramMap.get(paramName);
        if ( checker.isIllegal(paramValue) ) {
            String parametersLog = LogBuildUtil.build(paramMap);
            NAIVEREDIS_ERROR_LOG.error("[{}] `Host`:`{}`. `Error`:`invalid {}`.{}", opName, host, paramName, parametersLog);
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_ARGUMENT);
            throw new IllegalArgumentException("[" + opName + "] Redis command execute failed: `invalid " + paramName +
                    "`. `Host`:`" + host + "`." + parametersLog);
        }
    }

    private Object execute(String opName, Map<String, Object> paramMap, CommandBuilder builder, ResponseParser parser) {
        long startTime = System.nanoTime();
        try {
            Command command = builder.build();
            RedisData response = redisChannel.send(command, timeout);
            if (response.isError()) { // 判断 Redis 服务是否返回错误信息
                String errorMessage = response.getText();
                NAIVEREDIS_ERROR_LOG.error("[{}] `Host`:`{}`. `Error`:`{}`.{}", opName, host,
                        errorMessage, LogBuildUtil.build(paramMap));
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
            NAIVEREDIS_ERROR_LOG.error("[{}] `Host`:`{}`. `Error`:`illegal state`.{}", opName, host, LogBuildUtil.build(paramMap));
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_STATE);
            throw e;
        } catch (TimeoutException e) {
            NAIVEREDIS_ERROR_LOG.error("[{}] `Host`:`{}`. `Error`:`wait response timeout`. `Timeout`:`{}ms`.{}",
                    opName, host, timeout, LogBuildUtil.build(paramMap));
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_TIMEOUT);
            throw e;
        } catch (Exception e) { // should not happen, for bug detection
            String parametersLog = LogBuildUtil.build(paramMap);
            NAIVEREDIS_ERROR_LOG.error("[{}] `Host`:`{}`. `Error`:`{}`.{}", opName, host, e.getMessage(), parametersLog);
            LOG.error("[" + opName + "] Redis command execute failed: `" + e.getMessage() + "`. `Host`:`" + host + "`."
                    + parametersLog, e);
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            throw new RedisException(RedisException.CODE_UNEXPECTED_ERROR, "[" + opName + "] Redis command execute failed: `"
                    + e.getMessage() + "`. `Host`:`" + host + "`." + parametersLog, e);
        } finally {
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                NAIVEREDIS_SLOW_EXECUTION_LOG.info("[{}] `Cost`:`{}ns ({}ms)`. `Host`:`{}`.{}", opName, executedNanoTime,
                        TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), host, LogBuildUtil.build(paramMap));
            }
            executionMonitor.onExecuted(startTime);
        }
    }
}
