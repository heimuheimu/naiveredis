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
import com.heimuheimu.naiveredis.geo.GeoCoordinate;
import com.heimuheimu.naiveredis.geo.GeoDistanceUnit;
import com.heimuheimu.naiveredis.geo.GeoNeighbour;
import com.heimuheimu.naiveredis.geo.GeoSearchParameter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Redis GEO 客户端，提供经纬度相关操作。GEO 操作更多信息：<a href="https://redis.io/commands#geo">https://redis.io/commands#geo</a>
 *
 * <p><strong>说明：</strong>{@code NaiveRedisGeoClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisGeoClient extends NaiveRedisKeysClient {

    /**
     * 将一个成员的经纬度信息添加到指定的 GEO 成员集合中，并返回成功添加的成员个数（不包括更新经纬度信息的成员），如果成员在 Set 集合中已存在，
     * 将会更新该成员的经纬度信息，如果 key 不存在，将会新创建一个 Set 集合后再执行添加操作。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为 GEO 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/geoadd">GEOADD key longitude latitude member [longitude latitude member ...]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param longitude 经度，有效区间范围：-180 至 180
     * @param latitude 纬度，有效区间范围：-85.05112878 至 85.05112878
     * @param member 成员，不允许为 {@code null}
     * @return 成功添加的成员个数（不包括更新经纬度信息的成员）
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果成员为 {@code null} 或经纬度值没有在有效区间范围内，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addGeoCoordinate(String key, double longitude, double latitude, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将多个成员的经纬度信息添加到指定的 GEO 成员集合中，并返回成功添加的成员个数（不包括更新经纬度信息的成员），如果成员在 Set 集合中已存在，
     * 将会更新该成员的经纬度信息，如果 key 不存在，将会新创建一个 Set 集合后再执行添加操作，如果 memberMap 为 {@code null} 或空 Map，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(M * log(N))，M 为添加的成员个数，N 为 GEO 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/geoadd">GEOADD key longitude latitude member [longitude latitude member ...]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param memberMap 成员 Map，Key 为成员，Value 为成员对应的经纬度信息，允许为 {@code null} 或空，但不允许含有 {@code null} 的成员
     * @return 成功添加的成员个数（不包括更新经纬度信息的成员）
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code geoCoordinateMap} 含有 {@code null} 的成员或经纬度值没有在有效区间范围内，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将一个成员从 GEO 成员集合中移除，并返回成功移除的成员个数，如果 key 不存在，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为 GEO 成员集合总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zrem">ZREM key member [member ...]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 成功移除的成员个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果成员为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeGeoMember(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将列表中的成员从指定的 GEO 成员集合中移除，并返回成功移除的成员个数，如果 key 不存在，将会返回 0，如果 members 为
     * {@code null} 或空列表，将会返回 0。
     *
     * <p><strong>算法复杂度：</strong> O(M * log(N))，M 为删除的成员个数，N 为 GEO 成员集合总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/zrem">ZREM key member [member ...]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param members 需要移除的成员列表，允许为 {@code null} 或空，但不允许含有 {@code null} 的成员
     * @return 成功移除的个数
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 members 含有 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    int removeGeoMembers(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 计算两个成员之间的距离，如果有成员不存在或 key 不存在，则返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为 GEO 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/geodist">GEODIST key member1 member2 [unit]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param targetMember 目标成员，不允许为 {@code null}
     * @param unit 距离单位，不允许为 {@code null}
     * @return 两个成员之间的距离，可能为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code member} 或 {@code targetMember} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code unit} 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Double getGeoDistance(String key, String member, String targetMember, GeoDistanceUnit unit) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 查找成员的经纬度信息，如果成员不存在或 key 不存在，则返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(log(N))，N 为 GEO 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/geopos">GEOPOS key member [member ...]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @return 经纬度信息，可能为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code member} 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    GeoCoordinate getGeoCoordinate(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 查找成员的经纬度信息 Map，Key 为成员，Value 为经纬度信息，如果成员不存在，将不会在 Map 中存在，如果 key 不存在，将会返回空 Map，
     * 如果 members 为 {@code null} 或空列表，将会返回空 Map，该方法不会返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(M * log(N))，M 为查找的成员个数，N 为 GEO 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/geopos">GEOPOS key member [member ...]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param members 需要获取经纬度信息的成员列表，允许为 {@code null} 或空，但不允许含有 {@code null} 的成员
     * @return 成员的经纬度信息 Map，Key 为成员，Value 为经纬度信息，不会为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code members} 包含 {@code null} 的成员，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Map<String, GeoCoordinate> getGeoCoordinates(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 在指定的 GEO 成员集合中查找 {@code center} 附近的成员列表，如果 key 不存在，将会返回空列表，该方法不会返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(M + log(N)) ，M 为位于半径以内的成员数量，N 为 GEO 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/georadius">GEORADIUS key longitude latitude radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param center 圆心经纬度信息，不允许为 {@code null}
     * @param geoSearchParameter GEO 附近成员查询参数，不允许为 {@code null}
     * @return 附近的成员列表，不会返回 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code center} 为 {@code null} 或经纬度值没有在有效区间范围内，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code geoSearchParameter} 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<GeoNeighbour> findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 在指定的 GEO 成员集合中查找 {@code member} 附近的成员列表，如果成员不存在，将会抛出 {@link RedisException} 异常，
     * 如果 key 不存在，将会返回空列表，该方法不会返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(M + log(N)) ，M 为位于半径以内的成员数量，N 为 GEO 成员总数。</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/georadiusbymember">GEORADIUSBYMEMBER key member radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]</a></p>
     *
     * @param key GEO 成员集合 Key，不允许 {@code null} 或空
     * @param member 成员，不允许为 {@code null}
     * @param geoSearchParameter GEO 附近成员查询参数，不允许为 {@code null}
     * @return 附近的成员列表，不会返回 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code member} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code geoSearchParameter} 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    List<GeoNeighbour> findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
