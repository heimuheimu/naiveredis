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

/**
 * Redis Keys 客户端，提供对 Redis Key 进行操作的相关方法。
 *
 * <p><strong>说明：</strong>{@code NaiveRedisKeysClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisKeysClient {

    /**
     * 设置 key 对应的过期时间，如果 key 不存在，不执行任何操作。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/expire">EXPIRE key seconds</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param expiry 过期时间，单位：秒，不允许小于等于 0
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 expiry 小于等于 0，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从 Redis 中删除指定的 key，如果 key 不存在，不执行任何操作。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/del">DEL key</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void delete(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
