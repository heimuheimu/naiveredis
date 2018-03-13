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
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Redis 集群客户端抽象类。
 *
 * @author heimuheimu
 */
public abstract class AbstractRedisClusterClient implements NaiveRedisClient {

    @Override
    public <T> T get(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        return getClient(RedisClientMethod.GET, parameterMap).get(key);
    }

    @Override
    public void set(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("value", value);

        getClient(RedisClientMethod.SET, parameterMap).set(key, value);
    }

    @Override
    public void set(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("value", value);
        parameterMap.put("expiry", expiry);

        getClient(RedisClientMethod.SET_WITH_EXPIRE, parameterMap).set(key, value, expiry);
    }

    @Override
    public void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("expiry", expiry);

        getClient(RedisClientMethod.EXPIRE, parameterMap).expire(key, expiry);
    }

    @Override
    public Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        return getClient(RedisClientMethod.GET_COUNT, parameterMap).getCount(key);
    }

    @Override
    public long addAndGet(String key, long delta, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("delta", delta);
        parameterMap.put("expiry", expiry);

        return getClient(RedisClientMethod.ADD_AND_GET, parameterMap).addAndGet(key, delta, expiry);
    }

    protected abstract DirectRedisClient getClient(RedisClientMethod method, Map<String, Object> parameterMap);
}
