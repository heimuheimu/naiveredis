package com.heimuheimu.naiveredis.clients;


import com.heimuheimu.naiveredis.NaiveRedisKeysClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.storage.GetCommand;
import com.heimuheimu.naiveredis.command.storage.MGetCommand;
import com.heimuheimu.naiveredis.command.storage.SetCommand;
import com.heimuheimu.naiveredis.command.storage.SetNXCommand;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.data.RedisDataParser;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naiveredis.transcoder.Transcoder;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis 直连存储客户端抽象类，并实现了 {@link NaiveRedisKeysClient} 接口中的所有方法。
 *
 * @author heimuheimu
 */
public abstract class AbstractDirectRedisStorageClient extends AbstractDirectRedisClient {

    public AbstractDirectRedisStorageClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
    }

    protected abstract Transcoder getTranscoder();

    @SuppressWarnings("unchecked")
    protected <T> T get(String methodName, String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (T) execute(methodName, parameterChecker.getParameterMap(), () -> new GetCommand(key), response -> {
            if (response.getValueBytes() == null) { // Key 不存在
                if (NAIVEREDIS_ERROR_LOG.isInfoEnabled()) {
                    NAIVEREDIS_ERROR_LOG.info(LogBuildUtil.buildMethodExecuteFailedLog(methodName, "key not found", parameterChecker.getParameterMap()));
                }
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_KEY_NOT_FOUND);
                return null;
            } else {
                Transcoder transcoder = getTranscoder();
                return transcoder != null ? transcoder.decode(response.getValueBytes()) : response.getText();
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected <T> Map<String, T> multiGet(String methodName, Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (keySet == null || keySet.isEmpty()) {
            return new HashMap<>();
        }

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("keySet", keySet);

        return (Map<String, T>) execute(methodName, parameterChecker.getParameterMap(), () -> new MGetCommand(keySet), response ->  {
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
                    Transcoder transcoder = getTranscoder();
                    Object value = transcoder != null ? transcoder.decode(redisData.getValueBytes()) : redisData.getText();
                    result.put(key, value);
                }
            }
            return result;
        });
    }

    protected void set(String methodName, String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("value", value);
        parameterChecker.addParameter("expiry", expiry);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("value", "isNull", Parameters::isNull);

        Transcoder transcoder = getTranscoder();
        execute(methodName, parameterChecker.getParameterMap(), () -> new SetCommand(key,
                transcoder != null ? transcoder.encode(value) : String.valueOf(value).getBytes(RedisData.UTF8), expiry), null);
    }

    protected boolean setIfAbsent(String methodName, String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("value", value);
        parameterChecker.addParameter("expiry", expiry);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("value", "isNull", Parameters::isNull);

        Transcoder transcoder = getTranscoder();
        boolean isSuccess = (boolean) execute(methodName, parameterChecker.getParameterMap(),
                () -> new SetNXCommand(key, transcoder != null ? transcoder.encode(value) : String.valueOf(value).getBytes(RedisData.UTF8)),
                RedisDataParser::parseBoolean);
        if (isSuccess && expiry > 0) {
            expire(key, expiry);
        }
        return isSuccess;
    }
}
