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

import java.util.Map;
import java.util.Set;

/**
 * Redis 计数器客户端，提供原子加（或减）方法。
 *
 * <p><strong>说明：</strong>{@code NaiveRedisCountClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisCountClient extends NaiveRedisKeysClient {

    /**
     * 获得 Key 对应的计数值，如果 Key 不存在，则返回 {@code null}。
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @return 计数值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 根据 Key 列表批量获取在 Redis 中存储的计数值，找到的 Key 将会把对应的 Key 和结果放入 Map 中，未找到或发生异常的 Key 不会出现在返回 Map 中。
     *
     * @param keySet Key 列表，列表中不允许包含为 {@code null} 或空字符串的 Key
     * @return Key 列表对应的计数值 Map，不会为 {@code null}
     * @throws IllegalArgumentException 如果 keySet 为 {@code null} 或空，或者 keySet 中包含 {@code null} 或空字符串的 Key，将抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Map<String, Long> multiGetCount(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 对 Key 对应的 long 数值执行原子加（或减）操作，并返回操作后的结果值。如果 Key 不存在，会初始化为 0 后再进行操作。
     *
     * @param key Redis key，不允许 {@code null} 或空
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
