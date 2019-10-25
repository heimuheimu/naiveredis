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

import com.heimuheimu.naiveredis.NaiveRedisRawStorageClient;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.Map;
import java.util.Set;

/**
 * Redis 字符串存储客户端代理接口。
 *
 * @author heimuheimu
 */
public interface NaiveRedisRawStorageClientDelegate extends NaiveRedisRawStorageClient {

    NaiveRedisRawStorageClient getNaiveRedisRawStorageClient();

    @Override
    default String getString(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisRawStorageClient().getString(key);
    }

    @Override
    default Map<String, String> multiGetString(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisRawStorageClient().multiGetString(keySet);
    }

    @Override
    default void setString(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        getNaiveRedisRawStorageClient().setString(key, value);
    }

    @Override
    default void setString(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        getNaiveRedisRawStorageClient().setString(key, value, expiry);
    }

    @Override
    default boolean setStringIfAbsent(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisRawStorageClient().setStringIfAbsent(key, value);
    }

    @Override
    default boolean setStringIfAbsent(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisRawStorageClient().setStringIfAbsent(key, value, expiry);
    }

    @Override
    default boolean setStringIfExist(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisRawStorageClient().setStringIfExist(key, value);
    }

    @Override
    default boolean setStringIfExist(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisRawStorageClient().setStringIfExist(key, value, expiry);
    }
}
