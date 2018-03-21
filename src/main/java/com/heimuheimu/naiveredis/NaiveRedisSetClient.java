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

import java.util.Collection;
import java.util.List;

/**
 * Redis SET 客户端，提供 SET 集合的相关操作。SET 操作更多信息：<a href="https://redis.io/commands#set">https://redis.io/commands#set</a>
 *
 * <p><strong>说明：</strong>{@code NaiveRedisSetClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisSetClient extends NaiveRedisKeysClient {

    /**
     * 将成员添加到指定的 Set 集合中，并返回成功（集合中尚不存在该成员）添加的个数。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @param member 需要添加的成员，不允许为 {@code null}
     * @return 成功（集合中尚不存在该成员）添加的个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addToSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将列表中的成员均添加到指定的 Set 集合中，并返回成功（集合中尚不存在该成员）添加的个数。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @param members 需要添加的成员列表，成员不允许为 {@code null}
     * @return 成功（集合中尚不存在该成员）添加的个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addToSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将一个成员从指定的 Set 集合中移除，并返回成功移除的个数。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @param member 需要移除的成员，不允许为 {@code null}
     * @return 成功移除的个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeFromSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将列表中的成员从指定的 Set 集合中移除，并返回成功移除的个数。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @param members 需要移除的成员列表，成员不允许为 {@code null}
     * @return 成功移除的个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeFromSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 判断成员是否在指定的 Set 集合中存在。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 是否在指定的 Set 集合中存在
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    boolean isMemberInSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得指定的 Set 集合成员总数。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @return Set 集合成员总数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int getSizeOfSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的 Set 集合中随机选择指定个数的成员，以列表的形式返回，该成员列表不会从 Set 中移除，该方法不会返回 {@code null}。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @param count Set 集合成员总数
     * @return 随机成员列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的 Set 集合中随机选择指定个数的成员，以列表的形式返回，并将该成员列表从 Set 中移除，该方法不会返回 {@code null}。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @param count Set 集合成员总数，不允许小于等于 0
     * @return 随机成员列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code count} 小于等于 0，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> popMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得指定的 Set 集合中所有的成员列表，该方法不会返回 {@code null}。
     *
     * @param key Set key，不允许 {@code null} 或空
     * @return Set 集合中所有的成员列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getAllMembersFromSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
