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

import com.heimuheimu.naiveredis.NaiveRedisSortedSetClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.sortedset.ZAddCommand;
import com.heimuheimu.naiveredis.command.sortedset.ZIncrByCommand;
import com.heimuheimu.naiveredis.constant.SortedSetAddMode;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

import java.util.*;

/**
 * Redis Sorted SET 直连客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisSortedSetClient extends AbstractDirectRedisClient implements NaiveRedisSortedSetClient {

    /**
     * 构造一个 Redis Sorted SET 直连客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public DirectRedisSortedSetClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
    }

    @Override
    public int addToSortedSet(String key, double score, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return addToSortedSet(key, score, member, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER);
    }

    @Override
    public int addToSortedSet(String key, double score, String member, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Double> memberMap = new HashMap<>();
        memberMap.put(key, score);
        return addToSortedSet(key, memberMap, mode);
    }

    @Override
    public int addToSortedSet(String key, Map<String, Double> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return addToSortedSet(key, memberMap, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER);
    }

    @Override
    public int addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (memberMap == null || memberMap.isEmpty()) {
            return 0;
        }

        String methodName = methodNamePrefix + "addToSortedSet(String key, Map<String, Double> memberMap, AddMode mode)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("memberMap", memberMap);
        parameterChecker.addParameter("mode", mode);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new ZAddCommand(key, memberMap, mode),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    public double incrForSortedSet(String key, double increment, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "incrForSortedSet(String key, double increment, String member)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("increment", increment);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (double) execute(methodName, parameterChecker.getParameterMap(), () -> new ZIncrByCommand(key, increment, member),
                response -> Double.valueOf(response.getText()));
    }

    @Override
    public int removeFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public int removeFromSortedSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public int removeByRankFromSortedSet(String key, int start, int stop) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public Double getScoreFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }

    @Override
    public Integer getRankFromSortedSet(String key, String member, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }

    @Override
    public int getSizeOfSortedSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return 0;
    }

    @Override
    public List<String> getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }

    @Override
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }

    @Override
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return null;
    }
}
