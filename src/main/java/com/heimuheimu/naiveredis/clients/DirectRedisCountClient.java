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

import com.heimuheimu.naiveredis.NaiveRedisCountClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.count.IncrByCommand;
import com.heimuheimu.naiveredis.command.storage.GetCommand;
import com.heimuheimu.naiveredis.command.storage.MGetCommand;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis 计数器直连客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisCountClient extends AbstractDirectRedisClient implements NaiveRedisCountClient {

    /**
     * 构造一个 Redis 计数器直连客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public DirectRedisCountClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
    }

    @Override
    public Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getCount(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (Long) execute(methodName, parameterChecker.getParameterMap(), () -> new GetCommand(key), response -> {
            if (response.getValueBytes() == null) { // Key 不存在
                if (NAIVEREDIS_ERROR_LOG.isInfoEnabled()) {
                    NAIVEREDIS_ERROR_LOG.info(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "key not found", parameterChecker.getParameterMap()));
                }
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_KEY_NOT_FOUND);
                return null;
            } else {
                return Long.valueOf(response.getText());
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Long> multiGetCount(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (keySet == null || keySet.isEmpty()) {
            return new HashMap<>();
        }

        String methodName = methodNamePrefix + "multiGetCount(Set<String> keySet)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("keySet", keySet);

        return (Map<String, Long>) execute(methodName, parameterChecker.getParameterMap(), () -> new MGetCommand(keySet), response ->  {
            HashMap<String, Object> result = new HashMap<>();
            int index = 0;
            for (String key : keySet) {
                RedisData redisData = response.get(index++);
                if (redisData.getValueBytes() == null) { // Key 不存在
                    if (NAIVEREDIS_ERROR_LOG.isInfoEnabled()) {
                        NAIVEREDIS_ERROR_LOG.info(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "key not found (" + key + ")", parameterChecker.getParameterMap()));
                    }
                    executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_KEY_NOT_FOUND);
                } else {
                    result.put(key, Long.valueOf(redisData.getText()));
                }
            }
            return result;
        });
    }

    @Override
    public long addAndGet(String key, long delta, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "addAndGet(String key, long delta, int expiry)";
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
}
