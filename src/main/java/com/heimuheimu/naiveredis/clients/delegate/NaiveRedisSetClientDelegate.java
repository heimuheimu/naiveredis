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

import com.heimuheimu.naiveredis.NaiveRedisSetClient;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.Collection;
import java.util.List;

/**
 * Redis SET 客户端代理接口。
 *
 * @author heimuheimu
 */
public interface NaiveRedisSetClientDelegate extends NaiveRedisSetClient {

    NaiveRedisSetClient getNaiveRedisSetClient();

    @Override
    default int addToSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().addToSet(key, member);
    }

    @Override
    default int addToSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().addToSet(key, members);
    }

    @Override
    default int removeFromSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().removeFromSet(key, member);
    }

    @Override
    default int removeFromSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().removeFromSet(key, members);
    }

    @Override
    default boolean isMemberInSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().isMemberInSet(key, member);
    }

    @Override
    default int getSizeOfSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().getSizeOfSet(key);
    }

    @Override
    default List<String> getMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().getMembersFromSet(key, count);
    }

    @Override
    default List<String> popMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().popMembersFromSet(key, count);
    }

    @Override
    default List<String> getAllMembersFromSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisSetClient().getAllMembersFromSet(key);
    }
}
