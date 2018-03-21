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

package com.heimuheimu.naiveredis.clients.delegate;

import com.heimuheimu.naiveredis.NaiveRedisSortedSetClient;
import com.heimuheimu.naiveredis.constant.SortedSetAddMode;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Sorted SET 客户端代理接口。
 *
 * @author heimuheimu
 */
public interface NaiveRedisSortedSetClientDelegate extends NaiveRedisSortedSetClient {

    NaiveRedisSortedSetClient getNaiveRedisSortedSetClient();

    @Override
    default int addToSortedSet(String key, double score, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().addToSortedSet(key, score, member);
    }

    @Override
    default int addToSortedSet(String key, double score, String member, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().addToSortedSet(key, score, member, mode);
    }

    @Override
    default int addToSortedSet(String key, Map<String, Double> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().addToSortedSet(key, memberMap);
    }

    @Override
    default int addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().addToSortedSet(key, memberMap, mode);
    }

    @Override
    default double incrForSortedSet(String key, double increment, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().incrForSortedSet(key, increment, member);
    }

    @Override
    default int removeFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().removeFromSortedSet(key, member);
    }

    @Override
    default int removeFromSortedSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().removeFromSortedSet(key, members);
    }

    @Override
    default int removeByRankFromSortedSet(String key, int start, int stop) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().removeByRankFromSortedSet(key, start, stop);
    }

    @Override
    default int removeByScoreFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().removeByScoreFromSortedSet(key, minScore, maxScore);
    }

    @Override
    default int removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().removeByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore);
    }

    @Override
    default Double getScoreFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getScoreFromSortedSet(key, member);
    }

    @Override
    default Integer getRankFromSortedSet(String key, String member, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getRankFromSortedSet(key, member, reverse);
    }

    @Override
    default int getSizeOfSortedSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getSizeOfSortedSet(key);
    }

    @Override
    default int getCountFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getCountFromSortedSet(key, minScore, maxScore);
    }

    @Override
    default int getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getCountFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore);
    }

    @Override
    default List<String> getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getMembersByRankFromSortedSet(key, start, stop, reverse);
    }

    @Override
    default LinkedHashMap<String, Double> getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getMembersWithScoresByRankFromSortedSet(key, start, stop, reverse);
    }

    @Override
    default List<String> getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getMembersByScoreFromSortedSet(key, minScore, maxScore, reverse);
    }

    @Override
    default List<String> getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getMembersByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore, reverse, offset, count);
    }

    @Override
    default LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getMembersWithScoresByScoreFromSortedSet(key, minScore, maxScore, reverse);
    }

    @Override
    default LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSortedSetClient().getMembersWithScoresByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore, reverse, offset, count);
    }
}
