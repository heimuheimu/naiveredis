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
import com.heimuheimu.naiveredis.command.sortedset.*;
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
        return removeFromSortedSet(key, Collections.singleton(member));
    }

    @Override
    public int removeFromSortedSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (members == null || members.isEmpty()) {
            return 0;
        }
        String methodName = methodNamePrefix + "removeFromSortedSet(String key, Collection<String> members)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("members", members);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new ZRemCommand(key, members),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    public int removeByRankFromSortedSet(String key, int start, int stop) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "removeByRankFromSortedSet(String key, int start, int stop)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("start", start);
        parameterChecker.addParameter("stop", stop);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new ZRemRangeByRankCommand(key, start, stop),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return removeByScoreFromSortedSet(key, minScore, true, maxScore, true);
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("minScore", minScore);
        parameterChecker.addParameter("includeMinScore", includeMinScore);
        parameterChecker.addParameter("maxScore", maxScore);
        parameterChecker.addParameter("includeMaxScore", includeMaxScore);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(),
                () -> new ZRemRangeByScoreCommand(key, minScore, includeMinScore, maxScore, includeMaxScore),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    public Double getScoreFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getScoreFromSortedSet(String key, String member)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (Double) execute(methodName, parameterChecker.getParameterMap(),
                () -> new ZScoreCommand(key, member),
                response -> {
                    if (response.getValueBytes() != null) {
                        return Double.valueOf(response.getText());
                    } else {
                        return null;
                    }
                });
    }

    @Override
    public Integer getRankFromSortedSet(String key, String member, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getRankFromSortedSet(String key, String member, boolean reverse)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("reverse", reverse);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (Integer) execute(methodName, parameterChecker.getParameterMap(),
                () -> {
                    if (reverse) {
                        return new ZRevRankCommand(key, member);
                    } else {
                        return new ZRankCommand(key, member);
                    }
                },
                response -> {
                    if (response.getValueBytes() != null) {
                        return Integer.valueOf(response.getText());
                    } else {
                        return null;
                    }
                });
    }

    @Override
    public int getSizeOfSortedSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getSizeOfSortedSet(String key)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new ZCardCommand(key),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getCountFromSortedSet(key, minScore, true, maxScore, true);
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("minScore", minScore);
        parameterChecker.addParameter("includeMinScore", includeMinScore);
        parameterChecker.addParameter("maxScore", maxScore);
        parameterChecker.addParameter("includeMaxScore", includeMaxScore);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(),
                () -> new ZCountCommand(key, minScore, includeMinScore, maxScore, includeMaxScore),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("start", start);
        parameterChecker.addParameter("stop", stop);
        parameterChecker.addParameter("reverse", reverse);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(),
                () -> {
                    if (reverse) {
                        return new ZRevRangeCommand(key, start, stop, false);
                    } else {
                        return new ZRangeCommand(key, start, stop, false);
                    }
                },
                response -> {
                    List<String> members = new ArrayList<>();
                    for (int i = 0; i < response.size(); i++) {
                        members.add(response.get(i).getText());
                    }
                    return members;
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Double> getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("start", start);
        parameterChecker.addParameter("stop", stop);
        parameterChecker.addParameter("reverse", reverse);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (LinkedHashMap<String, Double>) execute(methodName, parameterChecker.getParameterMap(),
                () -> {
                    if (reverse) {
                        return new ZRevRangeCommand(key, start, stop, true);
                    } else {
                        return new ZRangeCommand(key, start, stop, true);
                    }
                },
                response -> {
                    LinkedHashMap<String, Double> memberMap = new LinkedHashMap<>();
                    for (int i = 0; i < response.size(); i += 2) {
                        String member = response.get(i).getText();
                        Double score = Double.valueOf(response.get(i + 1).getText());
                        memberMap.put(member, score);
                    }
                    return memberMap;
                });
    }

    @Override
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getMembersByScoreFromSortedSet(key, minScore, true, maxScore, true, reverse, 0, -1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore,
                                                       boolean includeMaxScore, boolean reverse, int offset, int count)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, " +
                "double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("minScore", minScore);
        parameterChecker.addParameter("includeMinScore", includeMinScore);
        parameterChecker.addParameter("maxScore", maxScore);
        parameterChecker.addParameter("includeMaxScore", includeMaxScore);
        parameterChecker.addParameter("reverse", reverse);
        parameterChecker.addParameter("offset", offset);
        parameterChecker.addParameter("count", count);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(),
                () -> {
                    if (reverse) {
                        return new ZRevRangeByScoreCommand(key, minScore, includeMinScore, maxScore, includeMaxScore, offset, count, false);
                    } else {
                        return new ZRangeByScoreCommand(key, minScore, includeMinScore, maxScore, includeMaxScore, offset, count, false);
                    }
                },
                response -> {
                    List<String> members = new ArrayList<>();
                    for (int i = 0; i < response.size(); i++) {
                        members.add(response.get(i).getText());
                    }
                    return members;
                });
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore,
                                                                                  boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getMembersWithScoresByScoreFromSortedSet(key, minScore, true, maxScore, true, reverse, 0, -1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore,
                                                                                  double maxScore, boolean includeMaxScore,
                                                                                  boolean reverse, int offset, int count)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, " +
                "double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("minScore", minScore);
        parameterChecker.addParameter("includeMinScore", includeMinScore);
        parameterChecker.addParameter("maxScore", maxScore);
        parameterChecker.addParameter("includeMaxScore", includeMaxScore);
        parameterChecker.addParameter("reverse", reverse);
        parameterChecker.addParameter("offset", offset);
        parameterChecker.addParameter("count", count);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (LinkedHashMap<String, Double>) execute(methodName, parameterChecker.getParameterMap(),
                () -> {
                    if (reverse) {
                        return new ZRevRangeByScoreCommand(key, minScore, includeMinScore, maxScore, includeMaxScore, offset, count, true);
                    } else {
                        return new ZRangeByScoreCommand(key, minScore, includeMinScore, maxScore, includeMaxScore, offset, count, true);
                    }
                },
                response -> {
                    LinkedHashMap<String, Double> memberMap = new LinkedHashMap<>();
                    for (int i = 0; i < response.size(); i += 2) {
                        String member = response.get(i).getText();
                        Double score = Double.valueOf(response.get(i + 1).getText());
                        memberMap.put(member, score);
                    }
                    return memberMap;
                });
    }
}
