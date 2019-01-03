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

import com.heimuheimu.naiveredis.constant.SortedSetAddMode;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Sorted SET 客户端，提供 Sorted SET 集合的相关操作。Sorted SET 操作更多信息：<a href="https://redis.io/commands#sorted_set">https://redis.io/commands#sorted_set</a>
 *
 * <p><strong>说明：</strong>{@code NaiveRedisSortedSetClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisSortedSetClient extends NaiveRedisKeysClient {

    /**
     * 将成员和对应的分值添加到指定的排序 Set 集合中，并返回成功添加的成员个数（不包括更新分值的成员），如果成员在 Set 集合中已存在，
     * 将会更新该成员的分值，如果 key 不存在，将会新创建一个 Set 集合后再执行添加操作。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zadd">ZADD key score member [score member ...]</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param score 成员对应的分值
     * @param member 成员，不允许为 {@code null}
     * @return 成功添加的成员个数（不包括更新分值的成员）
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     * @see SortedSetAddMode#REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER
     */
    int addToSortedSet(String key, double score, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员和对应的分值根据不同的添加模式添加到指定的排序 Set 集合中，如果添加模式为 {@code null}，默认使用 {@link SortedSetAddMode#REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER} 模式。
     * 如果 key 不存在，将会新创建一个 Set 集合后再执行添加操作。
     *
     * <p><strong>添加模式定义：</strong></p>
     * <ul>
     * <li>REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER：如果成员不存在，执行新增操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功添加的成员个数（不包括更新分值的成员）。</li>
     * <li>REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER：如果成员不存在，执行新增操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功添加或更新的成员个数。</li>
     * <li>ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER：如果成员不存在，不执行任何操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功更新的成员个数。</li>
     * <li>ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER：如果成员不存在，执行新增操作，如果成员已存在，不执行任何操作，操作完成后，返回成功添加的成员个数（不包括更新分值的成员）。</li>
     * </ul>
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zadd">ZADD key [NX|XX] [CH] score member [score member ...]</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param score 成员对应的分值
     * @param member 成员，不允许为 {@code null}
     * @param mode 添加模式，如果为 {@code null}，则默认为 {@link SortedSetAddMode#REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER} 模式
     * @return 根据 {@code mode} 返回不同的个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addToSortedSet(String key, double score, String member, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员 Map （Key 为成员，Value 为成员对应的分值）添加到指定的排序 Set 集合中，并返回成功添加的成员个数（不包括更新分值的成员），如果成员在 Set 集合中已存在，
     * 将会更新该成员的分值，如果 key 不存在，将会新创建一个 Set 集合后再执行添加操作，如果 memberMap 为{@code null} 或空，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(M * log(N))，M 为添加的成员个数，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zadd">ZADD key [NX|XX] [CH] score member [score member ...]</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param memberMap 成员 Map，Key 为成员，Value 为成员对应的分值，不允许含有 {@code null} 的成员
     * @return 成功添加的成员个数（不包括更新分值的成员）
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 memberMap 含有 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     * @see SortedSetAddMode#REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER
     */
    int addToSortedSet(String key, Map<String, Double> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将成员 Map 根据不同的添加模式添加到指定的排序 Set 集合中，Map 的 Key 为成员，Value 为成员对应的分值，如果添加模式为 {@code null}，
     * 默认使用 {@link SortedSetAddMode#REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER} 模式，如果 key 不存在，将会新创建一个 Set 集合后再执行添加操作，
     * 如果 memberMap 为{@code null} 或空，将会返回 0。
     *
     * <p><strong>添加模式定义：</strong></p>
     * <ul>
     * <li>REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER：如果成员不存在，执行新增操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功添加的成员个数（不包括更新分值的成员）。</li>
     * <li>REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER：如果成员不存在，执行新增操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功添加或更新的成员个数。</li>
     * <li>ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER：如果成员不存在，不执行任何操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功更新的成员个数。</li>
     * <li>ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER：如果成员不存在，执行新增操作，如果成员已存在，不执行任何操作，操作完成后，返回成功添加的成员个数（不包括更新分值的成员）。</li>
     * </ul>
     *
     * <p><strong>算法复杂度：</strong> O(M * log(N))，M 为添加的成员个数，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zadd">ZADD key [NX|XX] [CH] score member [score member ...]</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param memberMap 成员 Map，Key 为成员，Value 为成员对应的分值，不允许含有 {@code null} 的成员
     * @param mode 添加模式，如果为 {@code null}，则默认为 {@link SortedSetAddMode#REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER} 模式
     * @return 成功添加或更新的成员数量，由 {@code mode} 决定
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 memberMap 含有 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 给指定的成员增加分值，如果成员不存在，将会添加一个初始化分值为 0.0 的该成员，再执行增加分值操作。如果 key 不存在，
     * 将会新创建一个 Set 集合后再执行增加分值操作。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zincrby">ZINCRBY key increment member</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param increment 需要增加的分值
     * @param member 成员，不允许为 {@code null}
     * @return 成员增加后的分值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    double incrForSortedSet(String key, double increment, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将一个成员从指定的排序 Set 集合中移除，并返回成功移除的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zrem">ZREM key member [member ...]</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param member 需要移除的成员，不允许为 {@code null}
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将列表中的成员从指定的排序 Set 集合中移除，并返回成功移除的成员个数，如果 key 不存在，将会返回 0，如果 members 为 {@code null} 或空，
     * 将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(M * log(N))，M 为删除的成员个数，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zrem">ZREM key member [member ...]</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param members 需要移除的成员列表，成员不允许为 {@code null}
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 members 含有 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeFromSortedSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中移除排名区间内的成员，并返回成功移除的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p>排名定义：成员按分值从小到大排序（同样分值的成员按字母顺序排序），分值最小的成员排名为 0。</p>
     *
     * <p>起始排名、结束排名可以使用负数来代表分值最高的成员，例如：分值从大到小的成员排名依次为 -1，-2, -3...</p>
     *
     * <strong>代码示例：</strong>
     * <blockquote>
     * <pre>
     * naiveRedisSortedSetClient.removeByRankFromSortedSet("my_test_sorted_set", -3, -1); //删除分值最高的三个成员
     * </pre>
     * </blockquote>
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为删除的成员个数，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zremrangebyrank">ZREMRANGEBYRANK key start stop</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param start 起始排名（包含）
     * @param stop 结束排名（包含）
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeByRankFromSortedSet(String key, int start, int stop) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中移除分值区间内的成员，并返回成功移除的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为删除的成员个数，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zremrangebyscore">ZREMRANGEBYSCORE key min max</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值（包含），{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param maxScore 最大分值（包含），{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeByScoreFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中移除分值区间内的成员，并返回成功移除的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为删除的成员个数，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zremrangebyscore">ZREMRANGEBYSCORE key min max</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值，{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param includeMinScore 是否包含最小分值
     * @param maxScore 最大分值，{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @param includeMaxScore 是否包含最大分值
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得排序 Set 集合中指定成员的分值，如果该成员不存在或 key 不存在，将会返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(1)。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zscore">ZSCORE key member</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 成员的分值，可能为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Double getScoreFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得排序 Set 集合中指定成员的排名，排名从 0 开始（第一名为 0，第二名为 1，依次类推），如果该成员不存在或 key 不存在，则返回 {@code null}。
     *
     * <p>
     *     <strong>排名规则：</strong>reverse 为 {@code false}，按分值从低到高排序（同分值按字母顺序排序），reverse 为 {@code true}，
     *     按分值从高到低排序（同分值按字母顺序倒序排序）。
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>reverse 为 {@code false}：<a href="https://redis.io/commands/zrank">ZRANK key member</a></li>
     *     <li>reverse 为 {@code true}：<a href="https://redis.io/commands/zrevrank">ZREVRANK key member</a></li>
     * </ul>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param reverse 是否倒序排序
     * @return 成员的排名，可能为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 member 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Integer getRankFromSortedSet(String key, String member, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得指定的排序 Set 集合成员总数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(1)。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zcard">ZCARD key</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @return 排序 Set 集合成员总数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int getSizeOfSortedSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得排序 Set 集合中处于分值区间内的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zcount">ZCOUNT key min max</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值（包含），{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param maxScore 最大分值（包含），{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @return 处于分值区间内的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int getCountFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 获得排序 Set 集合中处于分值区间内的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zcount">ZCOUNT key min max</a></p>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值，{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param includeMinScore 是否包含最小分值
     * @param maxScore 最大分值，{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @param includeMaxScore 是否包含最大分值
     * @return 处于分值区间内的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中获得排名区间内的成员有序列表，排名从 0 开始（第一名为 0，第二名为 1，依次类推），如果 key 不存在，
     * 将会返回空列表，该方法不会返回 {@code null}。
     *
     * <p>
     *     <strong>排名规则：</strong>reverse 为 {@code false}，按分值从低到高排序（同分值按字母顺序排序），reverse 为 {@code true}，
     *     按分值从高到低排序（同分值按字母顺序倒序排序）。起始排名、结束排名可以使用负数来代表排序尾部成员，例如：当 reverse 为 {@code false} 时，
     *     分值从大到小的成员排名依次为 -1, -2, -3...
     * </p>
     *
     * <strong>代码示例：</strong>
     * <blockquote>
     * <pre>
     * naiveRedisSortedSetClient.getMembersByRankFromSortedSet("my_test_sorted_set", 0, -1, false); // 获取全部成员列表，按分值从低到高排序
     * </pre>
     * </blockquote>
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为返回的成员数量，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>reverse 为 {@code false}：<a href="https://redis.io/commands/zrange">ZRANGE key start stop [WITHSCORES]</a></li>
     *     <li>reverse 为 {@code true}：<a href="https://redis.io/commands/zrevrange">ZREVRANGE key start stop [WITHSCORES]</a></li>
     * </ul>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param start 起始排名（包含）
     * @param stop 结束排名（包含）
     * @param reverse 是否倒序排序
     * @return 排名区间内的成员有序列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中获得排名区间内的成员有序 Map，Key 为成员，Value 为成员对应的分值，排名从 0 开始（第一名为 0，第二名为 1，依次类推），
     * 如果 key 不存在，将会返回空 Map，该方法不会返回 {@code null}。
     *
     * <p>
     *     <strong>排名规则：</strong>reverse 为 {@code false}，按分值从低到高排序（同分值按字母顺序排序），reverse 为 {@code true}，
     *     按分值从高到低排序（同分值按字母顺序倒序排序）。起始排名、结束排名可以使用负数来代表排序尾部成员，例如：当 reverse 为 {@code false} 时，
     *     分值从大到小的成员排名依次为 -1, -2, -3...
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为返回的成员数量，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>reverse 为 {@code false}：<a href="https://redis.io/commands/zrange">ZRANGE key start stop [WITHSCORES]</a></li>
     *     <li>reverse 为 {@code true}：<a href="https://redis.io/commands/zrevrange">ZREVRANGE key start stop [WITHSCORES]</a></li>
     * </ul>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param start 起始排名（包含）
     * @param stop 结束排名（包含）
     * @param reverse 是否倒序排序
     * @return 成员有序 Map，Key 为成员，Value 为成员对应的分值，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    LinkedHashMap<String, Double> getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中获得分值区间内的成员有序列表，如果 key 不存在，将会返回空列表，该方法不会返回 {@code null}。
     *
     * <p>
     *     <strong>成员排序规则：</strong>reverse 为 {@code false}，全部成员按分值从低到高排序（同分值按字母顺序排序），
     *     reverse 为 {@code true}，全部成员按分值从高到低排序（同分值按字母顺序倒序排序）。
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为返回的成员数量，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>reverse 为 {@code false}：<a href="https://redis.io/commands/zrangebyscore">ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]</a></li>
     *     <li>reverse 为 {@code true}：<a href="https://redis.io/commands/zrevrangebyscore">ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]</a></li>
     * </ul>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值（包含），{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param maxScore 最大分值（包含），{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @param reverse 是否倒序排序
     * @return 分值区间内的成员有序列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中获得分值区间内的成员有序列表，如果 key 不存在，将会返回空列表，该方法不会返回 {@code null}。
     *
     * <p>
     *     <strong>成员排序规则：</strong>reverse 为 {@code false}，全部成员按分值从低到高排序（同分值按字母顺序排序），
     *     reverse 为 {@code true}，全部成员按分值从高到低排序（同分值按字母顺序倒序排序）。
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为返回的成员数量（如果 offset 大于 0，M 为 offset + 返回的成员数量），N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>reverse 为 {@code false}：<a href="https://redis.io/commands/zrangebyscore">ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]</a></li>
     *     <li>reverse 为 {@code true}：<a href="https://redis.io/commands/zrevrangebyscore">ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]</a></li>
     * </ul>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值，{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param includeMinScore 是否包含最小分值
     * @param maxScore 最大分值，{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @param includeMaxScore 是否包含最大分值
     * @param reverse 是否倒序排序
     * @param offset 起始索引，不允许小于 0，在分页获取时使用，与 MYSQL 的 LIMIT 语法类似
     * @param count 需要获取的成员数量，在分页获取时使用，与 MYSQL 的 LIMIT 语法类似，如果小于等于 0，将忽略 offset 的值，并获取全部成员列表
     * @return 分值区间内的成员有序列表，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 offset 小于 0，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<String> getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore,
                                                boolean includeMaxScore, boolean reverse, int offset, int count)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中获得分值区间内的成员有序 Map，Key 为成员，Value 为成员对应的分值，如果 key 不存在，
     * 将会返回空 Map，该方法不会返回 {@code null}。
     *
     * <p>
     *     <strong>成员排序规则：</strong>reverse 为 {@code false}，全部成员按分值从低到高排序（同分值按字母顺序排序），
     *     reverse 为 {@code true}，全部成员按分值从高到低排序（同分值按字母顺序倒序排序）。
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为返回的成员数量，N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>reverse 为 {@code false}：<a href="https://redis.io/commands/zrangebyscore">ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]</a></li>
     *     <li>reverse 为 {@code true}：<a href="https://redis.io/commands/zrevrangebyscore">ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]</a></li>
     * </ul>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值（包含），{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param maxScore 最大分值（包含），{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @param reverse 是否倒序排序
     * @return 分值区间内的成员有序 Map，Key 为成员，Value 为成员对应的分值，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 从指定的排序 Set 集合中获得分值区间内的成员有序 Map，Key 为成员，Value 为成员对应的分值，如果 key 不存在，
     * 将会返回空 Map，该方法不会返回 {@code null}。
     *
     * <p>
     *     <strong>成员排序规则：</strong>reverse 为 {@code false}，全部成员按分值从低到高排序（同分值按字母顺序排序），
     *     reverse 为 {@code true}，全部成员按分值从高到低排序（同分值按字母顺序倒序排序）。
     * </p>
     *
     * <p><strong>算法复杂度：</strong> O(log(N) + M)，M 为返回的成员数量（如果 offset 大于 0，M 为 offset + 返回的成员数量），N 为排序 Set 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong></p>
     * <ul>
     *     <li>reverse 为 {@code false}：<a href="https://redis.io/commands/zrangebyscore">ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]</a></li>
     *     <li>reverse 为 {@code true}：<a href="https://redis.io/commands/zrevrangebyscore">ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]</a></li>
     * </ul>
     *
     * @param key Sorted set key，不允许 {@code null} 或空
     * @param minScore 最小分值，{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param includeMinScore 是否包含最小分值
     * @param maxScore 最大分值，{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @param includeMaxScore 是否包含最大分值
     * @param reverse 是否倒序排序
     * @param offset 起始索引，不允许小于 0，在分页获取时使用，与 MYSQL 的 LIMIT 语法类似
     * @param count 需要获取的成员数量，在分页获取时使用，与 MYSQL 的 LIMIT 语法类似，如果小于等于 0，将忽略 offset 的值，并获取全部成员列表
     * @return 分值区间内的成员有序 Map，Key 为成员，Value 为成员对应的分值，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 offset 小于 0，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore,
                                                                           double maxScore, boolean includeMaxScore, boolean reverse,
                                                                           int offset, int count)
            throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
