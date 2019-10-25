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

package com.heimuheimu.naiveredis.cluster;

import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.NaiveRedisClient;
import com.heimuheimu.naiveredis.constant.RedisClientMethod;
import com.heimuheimu.naiveredis.constant.SortedSetAddMode;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.geo.GeoCoordinate;
import com.heimuheimu.naiveredis.geo.GeoDistanceUnit;
import com.heimuheimu.naiveredis.geo.GeoNeighbour;
import com.heimuheimu.naiveredis.geo.GeoSearchParameter;
import com.heimuheimu.naiveredis.monitor.ClusterMonitor;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Redis 集群客户端抽象类。
 *
 * @author heimuheimu
 */
public abstract class AbstractRedisClusterClient implements NaiveRedisClient, Closeable {

    /**
     * Redis 命令执行错误日志
     */
    protected static final Logger NAIVEREDIS_ERROR_LOG = LoggerFactory.getLogger("NAIVEREDIS_ERROR_LOG");

    /**
     * 错误日志
     */
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * Redis 集群客户端信息监控器
     */
    protected final ClusterMonitor clusterMonitor = ClusterMonitor.getInstance();

    /**
     * Redis 方法异步执行器
     */
    protected final RedisMethodAsyncExecutor asyncExecutor = new RedisMethodAsyncExecutor();

    /**
     * 根据调用的 Redis 方法和 Key 获得 Cluster 中对应的 Redis 直连客户端，如果该直连客户端当前不可用，允许返回 {@code null}。
     *
     * @param method 调用的 Redis 方法，不允许 {@code null}
     * @param key Redis key，不允许 {@code null} 或空
     * @return Redis 直连客户端，可能为 {@code null}
     */
    protected abstract DirectRedisClient getClient(RedisClientMethod method, String key);

    /**
     * 检查 Key 是否为 {@code null} 或空，如果满足，则抛出 IllegalArgumentException 异常。
     *
     * @param method 调用的 Redis 方法
     * @param key Redis key
     * @throws IllegalArgumentException 如果 Key 为 {@code null} 或空，将会抛出此异常
     */
    protected void checkKey(RedisClientMethod method, String key) throws IllegalArgumentException {
        if (key == null || key.isEmpty()) {
            Map<String, Object> errorParameterMap = new LinkedHashMap<>();
            errorParameterMap.put("key", key);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog(getClass().getSimpleName() + method.getMethodName(),
                    "key could not be empty", errorParameterMap);
            NAIVEREDIS_ERROR_LOG.error(errorMessage);
            LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * 调用 {@link #getClient(RedisClientMethod, String)} 方法获取可用的 Redis 直连客户端，如果获取过程中发生错误，
     * 或者当前没有可用的 Redis 直连客户端，将会抛出 IllegalArgumentException 异常，该方法不会返回 {@code null}。
     *
     * @param method 调用的 Redis 方法
     * @param key Redis key
     * @return Redis 直连客户端，不会为 {@code null}
     * @throws IllegalStateException 如果 Redis 直连客户端为 {@code null} 或不可用，将会抛出此异常
     */
    protected DirectRedisClient getAvailableClient(RedisClientMethod method, String key) throws IllegalStateException {
        DirectRedisClient client = null;
        Exception exception = null;
        try {
            client = getClient(method, key);
        } catch (Exception e) {
            exception = e;
        }
        if (client == null || !client.isAvailable()) {
            clusterMonitor.onUnavailable();
            Map<String, Object> errorParameterMap = new LinkedHashMap<>();
            errorParameterMap.put("key", key);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog(getClass().getSimpleName() + method.getMethodName(),
                    "DirectRedisClient is not available", errorParameterMap);
            NAIVEREDIS_ERROR_LOG.error(errorMessage);
            if (exception == null) {
                LOG.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            } else {
                LOG.error(errorMessage, exception);
                throw new IllegalStateException(errorMessage, exception);
            }
        }
        return client;
    }

    /**
     * 根据调用的 Redis 方法和 Key 获得 Cluster 中对应的 Redis 直连客户端，并使用该直连客户端执行 Redis 方法代理，返回执行结果。
     *
     * @param method 调用的 Redis 方法
     * @param key Redis Key
     * @param delegate Redis 方法执行代理
     * @param <T> Redis Value 类型
     * @return Redis 方法执行结果
     * @throws IllegalArgumentException 如果 Key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 直连客户端为 {@code null} 或不可用，将会抛出此异常
     */
    @SuppressWarnings("unchecked")
    protected <T> T execute(RedisClientMethod method, String key, RedisMethodDelegate delegate)
            throws IllegalArgumentException, IllegalStateException {
        checkKey(method, key);
        DirectRedisClient client = getAvailableClient(method, key);
        return (T) delegate.delegate(client);
    }

    /**
     * 构造一个 Redis Multi Get 方法执行代理，该方法不会返回 {@code null}。
     *
     * @param method 调用的 Redis Multi Get 方法
     * @param keySet Key 列表
     * @return Redis Multi Get 方法执行代理
     * @throws UnsupportedOperationException 如果 Redis 方法不属于 Multi Get 方法中的一种，将会抛出此异常
     */
    protected RedisMethodDelegate buildMultiGetDelegate(RedisClientMethod method, Set<String> keySet) throws UnsupportedOperationException {
        if (method == RedisClientMethod.MULTI_GET) {
            return client -> client.multiGet(keySet);
        } else if (method == RedisClientMethod.MULTI_GET_STRING) {
            return client -> client.multiGetString(keySet);
        } else if (method == RedisClientMethod.MULTI_GET_COUNT) {
            return client -> client.multiGetCount(keySet);
        } else { // should not happen, just for bug detection
            String errorMessage = getClass().getSimpleName() + method.getMethodName() + " is not supported.";
            LOG.error(errorMessage);
            throw new UnsupportedOperationException(errorMessage);
        }
    }

    /**
     * 执行 Redis Multi Get 方法失败后，统一调用此方法进行日志打印及监控。
     *
     * @param method 调用的 Redis Multi Get 方法
     * @param keySet Key 列表
     * @param errorMessage 错误描述信息
     * @param error 异常信息，允许为 {@code null}
     */
    protected void onMultiGetFailed(RedisClientMethod method, Set<String> keySet, String errorMessage, Exception error) {
        Map<String, Object> errorParameterMap = new LinkedHashMap<>();
        errorParameterMap.put("keySet", keySet);
        String log = LogBuildUtil.buildMethodExecuteFailedLog(getClass().getSimpleName() + method.getMethodName(),
                errorMessage, errorParameterMap);
        NAIVEREDIS_ERROR_LOG.error(errorMessage);
        if (error != null) {
            LOG.error(log, error);
        } else {
            LOG.error(log);
        }
        clusterMonitor.onMultiGetError();
    }

    /**
     * 合并 Redis Multi Get 方法返回的结果。
     *
     * @param method 调用的 Redis Multi Get 方法
     * @param result 合并后的结果 Map
     * @param futureMap Redis Multi Get 方法执行任务 Map，Key 为执行中的任务，Value 为该 Multi Get 方法使用的 Key 列表
     * @param <T> Redis Value 类型
     */
    protected <T> void mergeMultiGetResult(RedisClientMethod method, Map<String, T> result,
                                           Map<Future<Map<String, T>>, Set<String>> futureMap) {
        for (Future<Map<String, T>> future : futureMap.keySet()) {
            try {
                result.putAll(future.get());
            } catch (Exception e) {
                onMultiGetFailed(method, futureMap.get(future), "unexpected error", e);
            }
        }
    }

    /**
     * 执行指定的 Redis Multi Get 方法。
     *
     * @param method 调用的 Redis Multi Get 方法
     * @param keySet Key 列表
     * @param <T> Redis Value 类型
     * @return Key 列表对应的 Redis Value Map，不会为 {@code null}
     * @throws IllegalArgumentException 如果 Key 列表中包含的 Key 为 {@code null} 或空，将会抛出此异常
     */
    protected <T> Map<String, T> internalMultiGet(RedisClientMethod method, Set<String> keySet) throws IllegalArgumentException {
        Map<String, T> result = new HashMap<>();
        if (keySet == null || keySet.isEmpty()) {
            return result;
        }
        Map<DirectRedisClient, Set<String>> keysMap = new HashMap<>();
        for (String key : keySet) {
            try {
                checkKey(method, key);
            } catch (IllegalArgumentException e) {
                onMultiGetFailed(method, keySet, "keySet could not contain empty key", e);
                throw e;
            }
            DirectRedisClient client = null;
            try {
                client = getAvailableClient(method, key);
            } catch (Exception ignored) {}
            if (client != null) {
                Set<String> thisClientKeySet = keysMap.computeIfAbsent(client, k -> new HashSet<>());
                thisClientKeySet.add(key);
            }
        }
        Map<Future<Map<String, T>>, Set<String>> futureMap = new HashMap<>();
        for (DirectRedisClient client : keysMap.keySet()) {
            Set<String> subKeySet = keysMap.get(client);
            try {
                RedisMethodDelegate delegate = buildMultiGetDelegate(method, subKeySet);
                futureMap.put(asyncExecutor.submit(client, delegate), subKeySet);
            } catch (RejectedExecutionException e) {
                onMultiGetFailed(method, subKeySet, "thread pool is too busy", e);
            }
        }
        mergeMultiGetResult(method, result, futureMap);
        return result;
    }

    @Override
    public void close() {
        asyncExecutor.close();
    }

    @Override
    public void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.EXPIRE, key, client -> {
            client.expire(key, expiry);
            return null;
        });
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.DELETE, key, client -> {
            client.delete(key);
            return null;
        });
    }

    @Override
    public int getTimeToLive(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_TIME_TO_LIVE, key, client -> client.getTimeToLive(key));
    }

    @Override
    public <T> T get(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET, key, client -> client.get(key));
    }

    @Override
    public <T> Map<String, T> multiGet(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return internalMultiGet(RedisClientMethod.MULTI_GET, keySet);
    }

    @Override
    public void set(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.SET, key, client -> {
            client.set(key, value);
            return null;
        });
    }

    @Override
    public void set(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.SET_WITH_EXPIRE, key, client -> {
            client.set(key, value, expiry);
            return null;
        });
    }

    @Override
    public boolean setIfAbsent(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_IF_ABSENT, key, client -> client.setIfAbsent(key, value));
    }

    @Override
    public boolean setIfAbsent(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_IF_ABSENT_WITH_EXPIRE, key, client -> client.setIfAbsent(key, value, expiry));
    }

    @Override
    public boolean setIfExist(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_IF_EXIST, key, client -> client.setIfExist(key, value));
    }

    @Override
    public boolean setIfExist(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_IF_EXIST_WITH_EXPIRE, key, client -> client.setIfExist(key, value, expiry));
    }

    @Override
    public String getString(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_STRING, key, client -> client.getString(key));
    }

    @Override
    public Map<String, String> multiGetString(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return internalMultiGet(RedisClientMethod.MULTI_GET_STRING, keySet);
    }

    @Override
    public void setString(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.SET_STRING, key, client -> {
            client.setString(key, value);
            return null;
        });
    }

    @Override
    public void setString(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.SET_STRING_WITH_EXPIRE, key, client -> {
            client.setString(key, value, expiry);
            return null;
        });
    }

    @Override
    public boolean setStringIfAbsent(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_STRING_IF_ABSENT, key, client -> client.setStringIfAbsent(key, value));
    }

    @Override
    public boolean setStringIfAbsent(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_STRING_IF_ABSENT_WITH_EXPIRE, key, client -> client.setStringIfAbsent(key, value, expiry));
    }

    @Override
    public boolean setStringIfExist(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_STRING_IF_EXIST, key, client -> client.setStringIfExist(key, value));
    }

    @Override
    public boolean setStringIfExist(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.SET_STRING_IF_EXIST_WITH_EXPIRE, key, client -> client.setStringIfExist(key, value, expiry));
    }

    @Override
    public Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_COUNT, key, client -> client.getCount(key));
    }

    @Override
    public Map<String, Long> multiGetCount(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return internalMultiGet(RedisClientMethod.MULTI_GET_COUNT, keySet);
    }

    @Override
    public long addAndGet(String key, long delta, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_AND_GET, key, client -> client.addAndGet(key, delta, expiry));
    }

    @Override
    public int addToSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_TO_SET, key, client -> client.addToSet(key, member));
    }

    @Override
    public int addToSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_ADD_TO_SET, key, client -> client.addToSet(key, members));
    }

    @Override
    public int removeFromSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.REMOVE_FROM_SET, key, client -> client.removeFromSet(key, member));
    }

    @Override
    public int removeFromSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_REMOVE_FROM_SET, key, client -> client.removeFromSet(key, members));
    }

    @Override
    public boolean isMemberInSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.IS_MEMBER_IN_SET, key, client -> client.isMemberInSet(key, member));
    }

    @Override
    public int getSizeOfSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_SIZE_OF_SET, key, client -> client.getSizeOfSet(key));
    }

    @Override
    public List<String> getMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBERS_FROM_SET, key, client -> client.getMembersFromSet(key, count));
    }

    @Override
    public List<String> popMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.POP_MEMBERS_FROM_SET, key, client -> client.popMembersFromSet(key, count));
    }

    @Override
    public List<String> getAllMembersFromSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_ALL_MEMBERS_FROM_SET, key, client -> client.getAllMembersFromSet(key));
    }

    @Override
    public int addToSortedSet(String key, double score, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_TO_SORTED_SET, key, client -> client.addToSortedSet(key, score, member));
    }

    @Override
    public int addToSortedSet(String key, double score, String member, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_TO_SORTED_SET_WITH_MODE, key, client -> client.addToSortedSet(key, score, member, mode));
    }

    @Override
    public int addToSortedSet(String key, Map<String, Double> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_ADD_TO_SORTED_SET, key, client -> client.addToSortedSet(key, memberMap));
    }

    @Override
    public int addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_ADD_TO_SORTED_SET_WITH_MODE, key, client -> client.addToSortedSet(key, memberMap, mode));
    }

    @Override
    public double incrForSortedSet(String key, double increment, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.INCR_FOR_SORTED_SET, key, client -> client.incrForSortedSet(key, increment, member));
    }

    @Override
    public int removeFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.REMOVE_FROM_SORTED_SET, key, client -> client.removeFromSortedSet(key, member));
    }

    @Override
    public int removeFromSortedSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_REMOVE_FROM_SORTED_SET, key, client -> client.removeFromSortedSet(key, members));
    }

    @Override
    public int removeByRankFromSortedSet(String key, int start, int stop) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.REMOVE_BY_RANK_FROM_SORTED_SET, key, client -> client.removeByRankFromSortedSet(key, start, stop));
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.REMOVE_BY_SCORE_FROM_SORTED_SET, key, client -> client.removeByScoreFromSortedSet(key, minScore, maxScore));
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.EXTRA_REMOVE_BY_SCORE_FROM_SORTED_SET, key, client -> client.removeByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore));
    }

    @Override
    public Double getScoreFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_SCORE_FROM_SORTED_SET, key, client -> client.getScoreFromSortedSet(key, member));
    }

    @Override
    public Integer getRankFromSortedSet(String key, String member, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_RANK_FROM_SORTED_SET, key, client -> client.getRankFromSortedSet(key, member, reverse));
    }

    @Override
    public int getSizeOfSortedSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_SIZE_OF_SORTED_SET, key, client -> client.getSizeOfSortedSet(key));
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_COUNT_FROM_SORTED_SET, key, client -> client.getCountFromSortedSet(key, minScore, maxScore));
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.EXTRA_GET_COUNT_FROM_SORTED_SET, key, client -> client.getCountFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore));
    }

    @Override
    public List<String> getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBERS_BY_RANK_FROM_SORTED_SET, key, client -> client.getMembersByRankFromSortedSet(key, start, stop, reverse));
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBERS_WITH_SCORES_BY_RANK_FROM_SORTED_SET, key, client -> client.getMembersWithScoresByRankFromSortedSet(key, start, stop, reverse));
    }

    @Override
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBERS_BY_SCORE_FROM_SORTED_SET, key, client -> client.getMembersByScoreFromSortedSet(key, minScore, maxScore, reverse));
    }

    @Override
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.EXTRA_GET_MEMBERS_BY_SCORE_FROM_SORTED_SET, key, client -> client.getMembersByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore, reverse, offset, count));
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBERS_WITH_SCORES_BY_SCORE_FROM_SORTED_SET, key, client -> client.getMembersWithScoresByScoreFromSortedSet(key, minScore, maxScore, reverse));
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.EXTRA_GET_MEMBERS_WITH_SCORES_BY_SCORE_FROM_SORTED_SET, key, client -> client.getMembersWithScoresByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore, reverse, offset, count));
    }

    @Override
    public int addGeoCoordinate(String key, double longitude, double latitude, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_GEO_COORDINATE, key, client -> client.addGeoCoordinate(key, longitude, latitude, member));
    }

    @Override
    public int addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_GEO_COORDINATES, key, client -> client.addGeoCoordinates(key, memberMap));
    }

    @Override
    public int removeGeoMember(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.REMOVE_GEO_MEMBER, key, client -> client.removeGeoMember(key, member));
    }

    @Override
    public int removeGeoMembers(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_REMOVE_GEO_MEMBERS, key, client -> client.removeGeoMembers(key, members));
    }

    @Override
    public Double getGeoDistance(String key, String member, String targetMember, GeoDistanceUnit unit) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_GEO_DISTANCE, key, client -> client.getGeoDistance(key, member, targetMember, unit));
    }

    @Override
    public GeoCoordinate getGeoCoordinate(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_GEO_COORDINATE, key, client -> client.getGeoCoordinate(key, member));
    }

    @Override
    public Map<String, GeoCoordinate> getGeoCoordinates(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_GEO_COORDINATES, key, client -> client.getGeoCoordinates(key, members));
    }

    @Override
    public List<GeoNeighbour> findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.FIND_GEO_NEIGHBOURS, key, client -> client.findGeoNeighbours(key, center, geoSearchParameter));
    }

    @Override
    public List<GeoNeighbour> findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.FIND_GEO_NEIGHBOURS_BY_MEMBER, key, client -> client.findGeoNeighboursByMember(key, member, geoSearchParameter));
    }

    @Override
    public int addFirstToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_FIRST_TO_LIST, key, client -> client.addFirstToList(key, member));
    }

    @Override
    public int addFirstToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_FIRST_TO_LIST_WITH_MODE, key, client -> client.addFirstToList(key, member, isAutoCreate));
    }

    @Override
    public int addFirstToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_ADD_FIRST_TO_LIST, key, client -> client.addFirstToList(key, members));
    }

    @Override
    public int addLastToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_LAST_TO_LIST, key, client -> client.addLastToList(key, member));
    }

    @Override
    public int addLastToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.ADD_LAST_TO_LIST_WITH_MODE, key, client -> client.addLastToList(key, member, isAutoCreate));
    }

    @Override
    public int addLastToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_ADD_LAST_TO_LIST, key, client -> client.addLastToList(key, members));
    }

    @Override
    public String popFirstFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.POP_FIRST_FROM_LIST, key, client -> client.popFirstFromList(key));
    }

    @Override
    public String popLastFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.POP_LAST_FROM_LIST, key, client -> client.popLastFromList(key));
    }

    @Override
    public int insertIntoList(String key, String pivotalMember, String member, boolean isAfter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.INSERT_INTO_LIST, key, client -> client.insertIntoList(key, pivotalMember, member, isAfter));
    }

    @Override
    public void setToList(String key, int index, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.SET_TO_LIST, key, client -> {
            client.setToList(key, index, member);
            return null;
        });
    }

    @Override
    public int removeFromList(String key, int count, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.REMOVE_FROM_LIST, key, client -> client.removeFromList(key, count, member));
    }

    @Override
    public void trimList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.TRIM_LIST, key, client -> {
            client.trimList(key, startIndex, endIndex);
            return null;
        });
    }

    @Override
    public int getSizeOfList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_SIZE_OF_LIST, key, client -> client.getSizeOfList(key));
    }

    @Override
    public String getByIndexFromList(String key, int index) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_BY_INDEX_FROM_LIST, key, client -> client.getByIndexFromList(key, index));
    }

    @Override
    public List<String> getMembersFromList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBERS_FROM_LIST, key, client -> client.getMembersFromList(key, startIndex, endIndex));
    }

    @Override
    public int putToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.PUT_TO_HASHES, key, client -> client.putToHashes(key, member, value));
    }

    @Override
    public void putToHashes(String key, Map<String, String> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        execute(RedisClientMethod.MULTI_PUT_TO_HASHES, key, client -> {
            client.putToHashes(key, memberMap);
            return null;
        });
    }

    @Override
    public int putIfAbsentToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.PUT_IF_ABSENT_TO_HASHES, key, client -> client.putIfAbsentToHashes(key, member, value));
    }

    @Override
    public long incrForHashes(String key, String member, long increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.INCR_FOR_HASHES, key, client -> client.incrForHashes(key, member, increment));
    }

    @Override
    public double incrByFloatForHashes(String key, String member, double increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.INCR_BY_FLOAT_FOR_HASHES, key, client -> client.incrByFloatForHashes(key, member, increment));
    }

    @Override
    public int removeFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.REMOVE_FROM_HASHES, key, client -> client.removeFromHashes(key, member));
    }

    @Override
    public int removeFromHashes(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.MULTI_REMOVE_FROM_HASHES, key, client -> client.removeFromHashes(key, members));
    }

    @Override
    public boolean isExistInHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.IS_EXIST_IN_HASHES, key, client -> client.isExistInHashes(key, member));
    }

    @Override
    public int getSizeOfHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_SIZE_OF_HASHES, key, client -> client.getSizeOfHashes(key));
    }

    @Override
    public String getValueFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_VALUE_FROM_HASHES, key, client -> client.getValueFromHashes(key, member));
    }

    @Override
    public int getValueLengthFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_VALUE_LENGTH_FROM_HASHES, key, client -> client.getValueLengthFromHashes(key, member));
    }

    @Override
    public Map<String, String> getMemberMapFromHashes(String key, List<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBER_MAP_FROM_HASHES, key, client -> client.getMemberMapFromHashes(key, members));
    }

    @Override
    public Map<String, String> getAllFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_ALL_FROM_HASHES, key, client -> client.getAllFromHashes(key));
    }

    @Override
    public List<String> getMembersFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_MEMBERS_FROM_HASHES, key, client -> client.getMembersFromHashes(key));
    }

    @Override
    public List<String> getValuesFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return execute(RedisClientMethod.GET_VALUES_FROM_HASHES, key, client -> client.getValuesFromHashes(key));
    }
}
