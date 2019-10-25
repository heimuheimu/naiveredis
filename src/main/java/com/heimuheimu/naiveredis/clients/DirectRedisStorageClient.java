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

import com.heimuheimu.naiveredis.NaiveRedisStorageClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.transcoder.SimpleTranscoder;
import com.heimuheimu.naiveredis.transcoder.Transcoder;

import java.util.Map;
import java.util.Set;

/**
 * Redis 存储客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisStorageClient extends AbstractDirectRedisStorageClient implements NaiveRedisStorageClient {

    /**
     * Java 对象与字节数组转换器
     */
    private final Transcoder transcoder;

    /**
     * 构造一个 Redis 存储客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     */
    public DirectRedisStorageClient(RedisChannel channel, int timeout, long slowExecutionThreshold, int compressionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
        this.transcoder = new SimpleTranscoder(compressionThreshold);
    }

    @Override
    public <T> T get(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "get(String key)";
        return get(methodName, key);
    }

    @Override
    public <T> Map<String, T> multiGet(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "multiGet(Set<String> keySet)";
        return multiGet(methodName, keySet);
    }

    @Override
    public void set(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        set(key, value, -1);
    }

    @Override
    public void set(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "set(String key, Object value, int expiry)";
        set(methodName, key, value, expiry);
    }

    @Override
    public boolean setIfAbsent(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return setIfAbsent(key, value, -1);
    }

    @Override
    public boolean setIfAbsent(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "setIfAbsent(String key, Object value, int expiry)";
        return setIfAbsent(methodName, key, value, expiry);
    }

    @Override
    public boolean setIfExist(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return setIfExist(key, value, -1);
    }

    @Override
    public boolean setIfExist(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "setIfExist(String key, Object value, int expiry)";
        return setIfExist(methodName, key, value, expiry);
    }

    @Override
    protected Transcoder getTranscoder() {
        return transcoder;
    }
}
