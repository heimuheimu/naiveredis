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

import com.heimuheimu.naiveredis.NaiveRedisListClient;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.Collection;
import java.util.List;

/**
 * Redis LIST 客户端代理接口。
 *
 * @author heimuheimu
 */
public interface NaiveRedisListClientDelegate extends NaiveRedisListClient {

    NaiveRedisListClient getNaiveRedisListClient();

    @Override
    default int addFirstToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().addFirstToList(key, member);
    }

    @Override
    default int addFirstToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().addFirstToList(key, member, isAutoCreate);
    }

    @Override
    default int addFirstToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().addFirstToList(key, members);
    }

    @Override
    default int addLastToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().addLastToList(key, member);
    }

    @Override
    default int addLastToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().addLastToList(key, member, isAutoCreate);
    }

    @Override
    default int addLastToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().addLastToList(key, members);
    }

    @Override
    default String popFirstFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().popFirstFromList(key);
    }

    @Override
    default String popLastFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().popLastFromList(key);
    }

    @Override
    default int insertIntoList(String key, String pivotalMember, String member, boolean isAfter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().insertIntoList(key, pivotalMember, member, isAfter);
    }

    @Override
    default void setToList(String key, int index, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        getNaiveRedisListClient().setToList(key, index, member);
    }

    @Override
    default int removeFromList(String key, int count, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().removeFromList(key, count, member);
    }

    @Override
    default void trimList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        getNaiveRedisListClient().trimList(key, startIndex, endIndex);
    }

    @Override
    default int getSizeOfList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().getSizeOfList(key);
    }

    @Override
    default String getByIndexFromList(String key, int index) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().getByIndexFromList(key, index);
    }

    @Override
    default List<String> getMembersFromList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisListClient().getMembersFromList(key, startIndex, endIndex);
    }
}
