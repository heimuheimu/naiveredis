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

import com.heimuheimu.naiveredis.NaiveRedisHashesClient;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Redis Hashes 客户端代理接口。
 *
 * @author heimuheimu
 */
public interface NaiveRedisHashesClientDelegate extends NaiveRedisHashesClient {

    NaiveRedisHashesClient getNaiveRedisHashesClient();

    @Override
    default int putToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().putToHashes(key, member, value);
    }

    @Override
    default void putToHashes(String key, Map<String, String> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        getNaiveRedisHashesClient().putToHashes(key, memberMap);
    }

    @Override
    default int putIfAbsentToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().putIfAbsentToHashes(key, member, value);
    }

    @Override
    default long incrForHashes(String key, String member, long increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().incrForHashes(key, member, increment);
    }

    @Override
    default double incrByFloatForHashes(String key, String member, double increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().incrByFloatForHashes(key, member, increment);
    }

    @Override
    default int removeFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().removeFromHashes(key, member);
    }

    @Override
    default int removeFromHashes(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().removeFromHashes(key, members);
    }

    @Override
    default boolean isExistInHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().isExistInHashes(key, member);
    }

    @Override
    default int getSizeOfHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().getSizeOfHashes(key);
    }

    @Override
    default String getValueFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().getValueFromHashes(key, member);
    }

    @Override
    default int getValueLengthFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().getValueLengthFromHashes(key, member);
    }

    @Override
    default Map<String, String> getMemberMapFromHashes(String key, List<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().getMemberMapFromHashes(key, members);
    }

    @Override
    default Map<String, String> getAllFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().getAllFromHashes(key);
    }

    @Override
    default List<String> getMembersFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().getMembersFromHashes(key);
    }

    @Override
    default List<String> getValuesFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisHashesClient().getValuesFromHashes(key);
    }
}
