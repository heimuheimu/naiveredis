package com.heimuheimu.naiveredis.clients;


import com.heimuheimu.naiveredis.NaiveRedisKeysClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.storage.GetCommand;
import com.heimuheimu.naiveredis.command.storage.MGetCommand;
import com.heimuheimu.naiveredis.command.storage.SetCommand;
import com.heimuheimu.naiveredis.constant.SetMode;
import com.heimuheimu.naiveredis.data.RedisData;
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

    /**
     * 获得 Java 对象与字节数组转换器，不允许返回 {@code null}
     *
     * @return Java 对象与字节数组转换器
     */
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
                return getTranscoder().decode(response.getValueBytes());
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
                    Object value = getTranscoder().decode(redisData.getValueBytes());
                    result.put(key, value);
                }
            }
            return result;
        });
    }

    protected void set(String methodName, String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = checkParameterForSet(methodName, key, value, expiry);
        execute(methodName, parameterMap, () -> new SetCommand(key, getTranscoder().encode(value), expiry), null);
    }

    protected boolean setIfAbsent(String methodName, String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = checkParameterForSet(methodName, key, value, expiry);
        return (boolean) execute(methodName, parameterMap,
                () -> new SetCommand(key, getTranscoder().encode(value), expiry, SetMode.SET_IF_ABSENT),
                response -> response.getValueBytes() != null);
    }

    protected boolean setIfExist(String methodName, String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = checkParameterForSet(methodName, key, value, expiry);
        return (boolean) execute(methodName, parameterMap,
                () -> new SetCommand(key, getTranscoder().encode(value), expiry, SetMode.SET_IF_EXIST),
                response -> response.getValueBytes() != null);
    }

    /**
     * 执行 Set 相关方法执行参数有效性检查，如果有效，则返回执行参数 Map，否则将抛出 IllegalArgumentException 异常。
     *
     * @param methodName Set 相关方法名称
     * @param key Redis key，不允许 {@code null} 或空
     * @param value 字符串，不允许 {@code null}
     * @param expiry 过期时间，单位：秒，如果小于等于 0，则为永久保存
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @return 执行参数 Map，Key 为参数名称，Value 为参数值
     */
    private Map<String, Object> checkParameterForSet(String methodName, String key, Object value, int expiry) throws IllegalArgumentException {
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("value", value);
        parameterChecker.addParameter("expiry", expiry);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("value", "isNull", Parameters::isNull);
        return parameterChecker.getParameterMap();
    }
}
