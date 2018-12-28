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
import java.util.Map;

/**
 * Redis Hashes 客户端，提供 Hashes（类似 Java 的 HashMap 数据结构） 的相关操作。
 * Hashes 操作更多信息：<a href="https://redis.io/commands#hash">https://redis.io/commands#hash</a>
 *
 * <p><strong>说明：</strong>{@code NaiveRedisHashesClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisHashesClient {

    /**
     * 将成员和对应的值添加到指定的 Hashes 中，并返回成功添加的成员个数（不包括更新值的成员），如果成员在 Hashes 中已存在，将会更新该成员的值，
     * 如果 key 不存在，将会新创建一个 Hashes 后再执行添加操作。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/hset">HSET key field value</a></p>
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param value 成员的值，不允许为 {@code null}
     * @return 成功添加的成员个数（不包括更新值的成员）
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int putToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员 Map （Key 为成员，Value 为成员对应的值）添加到指定的 Hashes 中，如果成员在 Hashes 中已存在，将会更新该成员的值，
     * 如果 key 不存在，将会新创建一个 Hashes 后再执行添加操作，如果 memberMap 为{@code null} 或空，则不执行任何操作。
     *
     * <p><strong>算法复杂度：</strong> O(N)，N 为添加的成员数量。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/hmset">HMSET key field value [field value ...]</a></p>
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param memberMap 成员 Map，Key 为成员，Value 为成员对应的值，不允许含有 {@code null} 的成员
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 memberMap 含有 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void putToHashes(String key, Map<String, String> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员和对应的值添加到指定的 Hashes 中，并返回成功添加的成员个数，如果成员在 Hashes 中已存在，将不执行任何操作，如果 key 不存在，
     * 将会新创建一个 Hashes 后再执行添加操作。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/hsetnx">HSETNX key field value</a></p>
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param value 成员的值，不允许为 {@code null}
     * @return 成功添加的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int putIfAbsentToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 给指定成员增加计数值，如果成员不存在，将会添加一个初始化计数值为 0 的该成员，再执行增加计数值操作，如果 key 不存在，
     * 将会新创建一个 Hashes 后再执行增加计数值操作。
     *
     * <p><strong>注意：</strong>成员的值必须为整数类型的字符串，否则会抛出 {@link RedisException} 异常。</p>
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/hincrby">HINCRBY key field increment</a></p>
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param increment 需要增加的计数值
     * @return 成员计数值增加后的值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    long incrForHashes(String key, String member, long increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 给指定成员增加计数值，如果成员不存在，将会添加一个初始化计数值为 0 的该成员，再执行增加计数值操作，如果 key 不存在，
     * 将会新创建一个 Hashes 后再执行增加计数值操作。
     *
     * <p><strong>注意：</strong>成员的值必须为小数类型或整数类型的字符串，否则会抛出 {@link RedisException} 异常。</p>
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/hincrbyfloat">HINCRBYFLOAT key field increment</a></p>
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param increment 需要增加的计数值
     * @return 成员计数值增加后的值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    double incrByFloatForHashes(String key, String member, double increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将指定的成员从 Hashes 中移除，并返回成功移除的成员个数，如果 key 不存在，将会返回 0。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将列表中的所有成员从 Hashes 中移除，并返回成功移除的成员个数，如果 key 不存在，将会返回 0，如果 members 为 {@code null} 或空列表，
     * 将会返回 0。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param members 成员列表，不允许含有 {@code null} 的成员
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 members 中含有 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeFromHashes(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 判断成员是否在 Hashes 中存在，如果该成员不存在或 key 不存在，将会返回 {@code false}。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 成员是否在 Hashes 中存在
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    boolean isExistInHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 Hashes 中的成员总数，如果 key 不存在，将会返回 0。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @return Hashes 中的成员总数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int getSizeOfHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 Hashes 中指定成员的值，如果该成员不存在或 key 不存在，将会返回 {@code null}。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 成员的值，可能返回 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    String getValueFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 Hashes 中指定成员的值的长度，如果该成员不存在或 key 不存在，将会返回 0。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 成员的值的长度
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int getValueLengthFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 根据成员列表批量获取在 Hashes 中对应的值，并返回成员 Map，Key 为成员，Value 为成员的值，不存在的成员不会出现在返回的 Map 中，
     * 如果 key 不存在，将返回空 Map，如果 members 为 {@code null} 或空列表，将返回空 Map，该方法不会返回 {@code null}。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @param members 成员列表，不允许含有 {@code null} 的成员
     * @return 成员 Map，Key 为成员，Value 为成员的值，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 members 中含有 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Map<String, String> getMemberMapFromHashes(String key, List<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 Hashes 中所有的成员 Map，Key 为成员，Value 为成员的值，如果 key 不存在，将返回空 Map，该方法不会返回 {@code null}。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @return 成员 Map，Key 为成员，Value 为成员的值，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Map<String, String> getAllFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 Hashes 中所有的成员列表，如果 key 不存在，将返回空列表，该方法不会返回 {@code null}。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @return 成员列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getKeysFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 Hashes 中所有的值列表，如果 key 不存在，将返回空列表，该方法不会返回 {@code null}。
     *
     * @param key Hashes key，不允许 {@code null} 或空
     * @return 值列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getValuesFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
