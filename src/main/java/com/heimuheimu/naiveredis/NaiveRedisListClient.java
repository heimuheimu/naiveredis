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
 * Redis LIST 客户端，提供 LIST 集合的相关操作。LIST 操作更多信息：<a href="https://redis.io/commands#list">https://redis.io/commands#list</a>
 *
 * <p><strong>说明：</strong>{@code NaiveRedisListClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisListClient extends NaiveRedisKeysClient {

    /**
     * 将成员添加到指定的 List 集合头部，并返回操作完成后 List 集合的大小，如果 key 不存在，将会新创建一个 List 集合，再进行添加操作。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/lpush">LPUSH key value [value ...]</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addFirstToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员添加到指定的 List 集合头部，并返回操作完成后 List 集合的大小，如果 isAutoCreate 为 {@code true}，并且 key 不存在，
     * 将会新创建一个 List 集合，再进行添加操作，如果 isAutoCreate 为 {@code false}，并且 key 不存在，将不会执行任何操作，返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>isAutoCreate 为 {@code true}：<a href="https://redis.io/commands/lpush">LPUSH key value [value ...]</a></li>
     *     <li>isAutoCreate 为 {@code false}：<a href="https://redis.io/commands/lpushx">LPUSHX key value</a></li>
     * </ul>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param isAutoCreate 当 {@code key} 不存在时，是否自动创建
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addFirstToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将列表中的成员均添加到指定的 List 集合头部，并返回操作完成后 List 集合的大小，如果 key 不存在，将会新创建一个 List 集合，再进行添加操作，
     * 如果 members 为 {@code null} 或空列表，将返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(N), N 为添加的成员数量。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/lpush">LPUSH key value [value ...]</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param members 需要添加的成员列表，成员不允许为 {@code null}
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 members 集合中包含 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addFirstToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员添加到指定的 List 集合尾部，并返回操作完成后 List 集合的大小，如果 key 不存在，将会新创建一个 List 集合，再进行添加操作。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/rpush">RPUSH key value [value ...]</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addLastToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员添加到指定的 List 集合尾部，并返回操作完成后 List 集合的大小，如果 isAutoCreate 为 {@code true}，并且 key 不存在，
     * 将会新创建一个 List 集合，再进行添加操作，如果 isAutoCreate 为 false，并且 key 不存在，将不会执行任何操作，返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>isAutoCreate 为 {@code true}：<a href="https://redis.io/commands/rpush">RPUSH key value [value ...]</a></li>
     *     <li>isAutoCreate 为 {@code false}：<a href="https://redis.io/commands/rpushx">RPUSHX key value</a></li>
     * </ul>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param isAutoCreate 当 key 不存在时，是否自动创建
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addLastToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将列表中的成员均添加到指定的 List 集合尾部，并返回操作完成后 List 集合的大小，如果 key 不存在，将会新创建一个 List 集合，再进行添加操作，
     * 如果 members 为 {@code null} 或空列表，将返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(N), N 为添加的成员数量。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/rpush">RPUSH key value [value ...]</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param members 需要添加的成员列表，成员不允许为 {@code null}
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 members 集合中包含 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addLastToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获取并移除指定的 List 集合头部成员，如果 key 不存在，将会返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/lpop">LPOP key</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @return List 集合头部成员，允许为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    String popFirstFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获取并移除指定的 List 集合尾部成员，如果 key 不存在，将会返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/rpop">RPOP key</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @return List 集合尾部成员，允许为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    String popLastFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员插入到 List 集合中 pivotalMember（关键成员）的前面或者后面，并返回操作完成后 List 集合的大小，如果 pivotalMember 不存在，
     * 将会返回 -1，如果 key 不存在，将不执行任何操作，并返回 0。
     *
     * <p><strong>注意：</strong>如果在 List 集合中存在多个 pivotalMember（关键成员），将只匹配 List 集合中最前面的 pivotalMember。</p>
     *
     * <p><strong>算法复杂度：</strong> O(N), N 为到达关键成员需要遍历的成员数量，如果关键成员是第一个成员，复杂度为 O(1)，
     * 如果关键成员是最后一个成员，复杂度为 O(N)，N 为列表所有的成员数量。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/linsert">LINSERT key BEFORE|AFTER pivot value</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param pivotalMember 关键成员，不允许为 {@code null}
     * @param member 插入的成员，不允许为 {@code null}
     * @param isAfter {@code true} 插入后面，{@code false} 插入前面
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 pivotalMember 或 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int insertIntoList(String key, String pivotalMember, String member, boolean isAfter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 覆盖 List 集合中指定索引位置的成员，如果索引越界或 key 不存在，将会抛出 {@link RedisException} 异常。
     *
     * <p>
     *     <strong>索引位置说明：</strong> 索引位置从 0 开始计算，即 List 集合第一个成员的索引位置为 0，第二个成员的索引位置为 1，以此类推...
     *     可以使用负数来代表尾部成员的索引位置，例如 -1 为最后一个成员的索引位置，-2 为倒数第二个成员的索引位置，以此类推...
     * </p>
     *
     * <p>
     *     <strong>算法复杂度：</strong> O(N), N 为列表所有的成员数量，如果设置的是第一个或最后一个成员，复杂度为 O(1)。
     * </p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/lset">LSET key index value</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param index 索引位置
     * @param member 成员，不允许为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void setToList(String key, int index, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员从指定的 List 集合中移除，并返回成功移除的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>count 参数说明：</strong></p>
     * <ul>
     *     <li>count &gt; 0：从 List 集合头部向尾部移除指定个数的符合条件成员，例如：<strong>LREM mylist 2 "hello"</strong> 将会移除 mylist 集合中最先出现的两个 "hello" 成员。</li>
     *     <li>count &lt; 0：从 List 集合尾部向头部移除指定个数的符合条件成员，例如：<strong>LREM mylist -2 "hello"</strong> 将会移除 mylist 集合中最后出现的两个 "hello" 成员。</li>
     *     <li>count = 0：移除 List 集合中所有符合条件的成员。</li>
     * </ul>
     *
     * <p><strong>算法复杂度：</strong> O(N), N 为列表所有的成员数量。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/lrem">LREM key count value</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param count 需要移除的成员个数
     * @param member 成员，不允许为 {@code null}
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeFromList(String key, int count, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 移除 List 集合指定范围外的成员，仅保留索引位置从 startIndex（包含） 至 endIndex（包含）的成员，如果 key 不存在，将不执行任何操作。
     *
     * <p>索引位置允许越界，如果 startIndex 大于 List 集合中最后一个成员的索引位置，将会移除 List 集合中的所有成员。如果 endIndex
     * 大于 List 集合中最后一个成员的索引位置，Redis 会将其处理成最后一个成员的索引位置。</p>
     *
     * <p>
     *     <strong>索引位置说明：</strong> 索引位置从 0 开始计算，即 List 集合第一个成员的索引位置为 0，第二个成员的索引位置为 1，以此类推...
     *     可以使用负数来代表尾部成员的索引位置，例如 -1 为最后一个成员的索引位置，-2 为倒数第二个成员的索引位置，以此类推...
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(N), N 为本次移除的成员数量。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/ltrim">LTRIM key start stop</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param startIndex 开始索引位置（包含）
     * @param endIndex 结束索引位置（包含）
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void trimList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 List 集合的大小，如果 key 不存在，将返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/llen">LLEN key</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @return List 集合的大小
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int getSizeOfList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获取 List 集合中指定索引位置的成员，如果索引位置越界或 key 不存在，将返回 {@code null}。
     *
     * <p>
     *     <strong>索引位置说明：</strong> 索引位置从 0 开始计算，即 List 集合第一个成员的索引位置为 0，第二个成员的索引位置为 1，以此类推...
     *     可以使用负数来代表尾部成员的索引位置，例如 -1 为最后一个成员的索引位置，-2 为倒数第二个成员的索引位置，以此类推...
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(N), N 为到达指定索引位置需要遍历的成员数量，如果是获取第一个或最后一个成员，复杂度为 O(1)。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/lindex">LINDEX key index</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param index 索引位置
     * @return 成员，不允许为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    String getByIndexFromList(String key, int index) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得 List 集合中索引位置从 startIndex（包含） 至 endIndex（包含）的成员列表，如果 key 不存在，将返回空列表，该方法不会返回 {@code null}。
     *
     * <p>索引位置允许越界，如果 startIndex 大于 List 集合中最后一个成员的索引位置，将会返回空列表。如果 endIndex 大于 List 集合中
     * 最后一个成员的索引位置，Redis 会将其处理成最后一个成员的索引位置。</p>
     *
     * <p>
     *     <strong>索引位置说明：</strong> 索引位置从 0 开始计算，即 List 集合第一个成员的索引位置为 0，第二个成员的索引位置为 1，以此类推...
     *     可以使用负数来代表尾部成员的索引位置，例如 -1 为最后一个成员的索引位置，-2 为倒数第二个成员的索引位置，以此类推...
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(S+N), 在 List 集合较小时，S 为 startIndex 距离头部索引位置的成员数量，在 List 集合很大时，
     * S 为 startIndex 距离头部或尾部索引位置的成员数量，取最小值，N 为需要获取的成员数量。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/lrange">LRANGE key start stop</a></p>
     *
     * @param key List key，不允许 {@code null} 或空
     * @param startIndex 开始索引位置（包含）
     * @param endIndex 结束索引位置（包含）
     * @return 成员列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getMembersFromList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
