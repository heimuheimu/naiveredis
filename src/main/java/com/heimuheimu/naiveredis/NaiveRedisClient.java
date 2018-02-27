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

package com.heimuheimu.naiveredis;

import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.io.Closeable;

/**
 * Redis 客户端。可访问以下网站来获得更多 Redis 信息：<a href="https://redis.io">https://redis.io</a>
 *
 * <p><strong>说明：</strong>{@code NaiveRedisClient} 的实现类必须是线程安全的。</p>
 */
public interface NaiveRedisClient extends Closeable {

    <T> T get(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    void set(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    void set(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 设置 Key 对应的过期时间。
     *
     * @param key Redis key
     * @param expiry 过期时间，单位：秒，不允许小于等于 0
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 expiry 小于等于 0，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 Key 对应的计数值，如果 Key 不存在，则返回 {@code null}。
     *
     * @param key Redis key
     * @return 计数值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 对 Key 对应的 long 数值执行原子加（或减）操作，并返回操作后的结果值。如果 Key 不存在，会初始化为 0 后再进行操作。
     *
     * @param key Redis key
     * @param delta 需要增加的值，如果为负数，则为减少的值
     * @param expiry 过期时间，单位：秒，如果小于等于 0，则为永久保存
     * @return 操作后的结果值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    long addAndGet(String key, long delta, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
