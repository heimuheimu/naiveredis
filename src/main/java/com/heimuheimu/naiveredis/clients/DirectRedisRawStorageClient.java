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

import com.heimuheimu.naiveredis.NaiveRedisRawStorageClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.transcoder.StringTranscoder;
import com.heimuheimu.naiveredis.transcoder.Transcoder;

import java.util.Map;
import java.util.Set;

/**
 * Redis 字符串存储客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisRawStorageClient extends AbstractDirectRedisStorageClient implements NaiveRedisRawStorageClient {

    /**
     * Java 对象与字节数组转换器
     */
    private final Transcoder transcoder;

    /**
     * 构造一个 Redis 字符串存储客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public DirectRedisRawStorageClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
        this.transcoder = new StringTranscoder();
    }

    @Override
    public String getString(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getString(String key)";
        return get(methodName, key);
    }

    @Override
    public Map<String, String> multiGetString(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "multiGetString(Set<String> keySet)";
        return multiGet(methodName, keySet);
    }

    @Override
    public void setString(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        setString(key, value, -1);
    }

    @Override
    public void setString(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "setString(String key, String value, int expiry)";
        set(methodName, key, value, expiry);
    }

    @Override
    public boolean setStringIfAbsent(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return setStringIfAbsent(key, value, -1);
    }

    @Override
    public boolean setStringIfAbsent(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "setStringIfAbsent(String key, String value, int expiry)";
        return setIfAbsent(methodName, key, value, expiry);
    }

    @Override
    protected Transcoder getTranscoder() {
        return transcoder;
    }
}
