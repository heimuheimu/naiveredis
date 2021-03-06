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

import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

/**
 * Redis 客户端代理接口。
 *
 * @author heimuheimu
 */
public interface NaiveRedisClientDelegate extends NaiveRedisStorageClientDelegate, NaiveRedisRawStorageClientDelegate,
        NaiveRedisCountClientDelegate, NaiveRedisSetClientDelegate, NaiveRedisSortedSetClientDelegate,
        NaiveRedisGeoClientDelegate, NaiveRedisListClientDelegate, NaiveRedisHashesClientDelegate {

    @Override
    default void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        getNaiveRedisStorageClient().expire(key, expiry);
    }

    @Override
    default void delete(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        getNaiveRedisStorageClient().delete(key);
    }

    @Override
    default int getTimeToLive(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisStorageClient().getTimeToLive(key);
    }
}
