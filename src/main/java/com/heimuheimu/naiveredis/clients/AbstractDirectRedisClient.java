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

package com.heimuheimu.naiveredis.clients;

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naiveredis.NaiveRedisKeysClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.Command;
import com.heimuheimu.naiveredis.command.keys.DeleteCommand;
import com.heimuheimu.naiveredis.command.keys.ExpireCommand;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 直连客户端抽象类，并实现了 {@link NaiveRedisKeysClient} 接口中的所有方法。
 *
 * @author heimuheimu
 */
public abstract class AbstractDirectRedisClient implements NaiveRedisKeysClient {

    /**
     * Redis 命令执行错误日志
     */
    protected static final Logger NAIVEREDIS_ERROR_LOG = LoggerFactory.getLogger("NAIVEREDIS_ERROR_LOG");

    /**
     * Redis 命令慢执行日志
     */
    protected static final Logger NAIVEREDIS_SLOW_EXECUTION_LOG = LoggerFactory.getLogger("NAIVEREDIS_SLOW_EXECUTION_LOG");

    /**
     * 当前直连客户端使用的错误日志
     */
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 与 Redis 服务进行数据交互的管道
     */
    protected final RedisChannel channel;

    /**
     * Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    protected final String host;

    /**
     * Redis 操作超时时间，单位：毫秒，不能小于等于 0
     */
    protected final int timeout;

    /**
     * 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0，执行 Redis 命令时间大于该值时，将进行慢执行日志打印
     */
    protected final long slowExecutionThreshold;

    /**
     * 当前 Redis 客户端使用的操作执行信息监控器
     */
    protected final ExecutionMonitor executionMonitor;

    /**
     * 方法名前缀，用于日志打印
     */
    protected final String methodNamePrefix;

    /**
     * 构造一个 Redis 直连客户端抽象类。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public AbstractDirectRedisClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        this.channel = channel;
        this.host = channel.getHost();
        this.timeout = timeout;
        this.slowExecutionThreshold = slowExecutionThreshold;
        this.executionMonitor = ExecutionMonitorFactory.get(host);
        this.methodNamePrefix = getClass().getSimpleName() + "#";
    }

    @Override
    public void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "expire(String key, int expiry)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("expiry", expiry);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("expiry", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);

        execute(methodName, parameterChecker.getParameterMap(), () -> new ExpireCommand(key, expiry), null);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix +"delete(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        execute(methodName, parameterChecker.getParameterMap(), () -> new DeleteCommand(key), null);
    }

    protected interface CommandBuilder {

        Command build() throws Exception;
    }

    protected interface ResponseParser {

        Object parse(RedisData response) throws Exception;
    }

    protected MethodParameterChecker buildRedisCommandMethodParameterChecker(String methodName) {
        MethodParameterChecker checker = new MethodParameterChecker(methodName, NAIVEREDIS_ERROR_LOG, parameterName -> executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT));
        checker.addParameter("host", host);
        return checker;
    }

    protected Object execute(String methodName, Map<String, Object> paramMap, CommandBuilder builder, ResponseParser parser) {
        long startTime = System.nanoTime();
        try {
            Command command = builder.build();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Client will send following redis command:\n\r" + command.toString());
            }
            RedisData response = channel.send(command, timeout);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Client received following redis response:\n\r" + response.toString());
            }
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
        } catch (IllegalArgumentException e) {
            String errorLog = LogBuildUtil.buildMethodExecuteFailedLog(methodName, "illegal argument", paramMap);
            NAIVEREDIS_ERROR_LOG.error(errorLog);
            LOG.error(errorLog, e);
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT);
            throw e;
        }catch (IllegalStateException e) {
            NAIVEREDIS_ERROR_LOG.error(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "illegal state", paramMap));
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
