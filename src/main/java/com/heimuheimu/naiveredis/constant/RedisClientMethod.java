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

package com.heimuheimu.naiveredis.constant;

import com.heimuheimu.naiveredis.geo.GeoCoordinate;
import com.heimuheimu.naiveredis.geo.GeoDistanceUnit;
import com.heimuheimu.naiveredis.geo.GeoSearchParameter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Redis 客户端方法定义枚举类。
 *
 * @author heimuheimu
 */
public enum RedisClientMethod {

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#expire(String, int)
     */
    EXPIRE("#expire(String key, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#delete(String)
     */
    DELETE("#delete(String key)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#get(String)
     */
    GET("#get(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#multiGet(Set)
     */
    MULTI_GET("#multiGet(Set<String> keySet)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#set(String, Object)
     */
    SET("#set(String key, Object value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#set(String, Object, int)
     */
    SET_WITH_EXPIRE("#set(String key, Object value, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getCount(String)
     */
    GET_COUNT("#getCount(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#multiGetCount(Set)
     */
    MULTI_GET_COUNT("#multiGetCount(Set<String> keySet)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#addAndGet(String, long, int)
     */
    ADD_AND_GET("#addAndGet(String key, long delta, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#addToSet(String, String)
     */
    ADD_TO_SET("#addToSet(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#addToSet(String, Collection)
     */
    MULTI_ADD_TO_SET("#addToSet(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#removeFromSet(String, String)
     */
    REMOVE_FROM_SET("#removeFromSet(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#removeFromSet(String, Collection)
     */
    MULTI_REMOVE_FROM_SET("#removeFromSet(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#isMemberInSet(String, String)
     */
    IS_MEMBER_IN_SET("#isMemberInSet(String key, String member)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getSizeOfSet(String)
     */
    GET_SIZE_OF_SET("#getSizeOfSet(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getMembersFromSet(String, int)
     */
    GET_MEMBERS_FROM_SET("#getMembersFromSet(String key, int count)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#popMembersFromSet(String, int)
     */
    POP_MEMBERS_FROM_SET("#popMembersFromSet(String key, int count)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getAllMembersFromSet(String)
     */
    GET_ALL_MEMBERS_FROM_SET("#getAllMembersFromSet(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#addToSortedSet(String, double, String)
     */
    ADD_TO_SORTED_SET("#addToSortedSet(String key, double score, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#addToSortedSet(String, double, String, SortedSetAddMode)
     */
    ADD_TO_SORTED_SET_WITH_MODE("#addToSortedSet(String key, double score, String member, SortedSetAddMode mode)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#addToSortedSet(String, Map)
     */
    MULTI_ADD_TO_SORTED_SET("#addToSortedSet(String key, Map<String, Double> memberMap)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#addToSortedSet(String, Map, SortedSetAddMode)
     */
    MULTI_ADD_TO_SORTED_SET_WITH_MODE("#addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#incrForSortedSet(String, double, String)
     */
    INCR_FOR_SORTED_SET("#incrForSortedSet(String key, double increment, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#removeFromSortedSet(String, String)
     */
    REMOVE_FROM_SORTED_SET("#removeFromSortedSet(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#removeFromSortedSet(String, Collection)
     */
    MULTI_REMOVE_FROM_SORTED_SET("#removeFromSortedSet(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#removeByRankFromSortedSet(String, int, int)
     */
    REMOVE_BY_RANK_FROM_SORTED_SET("#removeByRankFromSortedSet(String key, int start, int stop)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#removeByScoreFromSortedSet(String, double, double)
     */
    REMOVE_BY_SCORE_FROM_SORTED_SET("#removeByScoreFromSortedSet(String key, double minScore, double maxScore)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#removeByScoreFromSortedSet(String, double, boolean, double, boolean)
     */
    EXTRA_REMOVE_BY_SCORE_FROM_SORTED_SET("#removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getScoreFromSortedSet(String, String)
     */
    GET_SCORE_FROM_SORTED_SET("#getScoreFromSortedSet(String key, String member)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getRankFromSortedSet(String, String, boolean)
     */
    GET_RANK_FROM_SORTED_SET("#getRankFromSortedSet(String key, String member, boolean reverse)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getSizeOfSortedSet(String)
     */
    GET_SIZE_OF_SORTED_SET("#getSizeOfSortedSet(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getCountFromSortedSet(String, double, double)
     */
    GET_COUNT_FROM_SORTED_SET("#getCountFromSortedSet(String key, double minScore, double maxScore)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getCountFromSortedSet(String, double, boolean, double, boolean)
     */
    EXTRA_GET_COUNT_FROM_SORTED_SET("#getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getMembersByRankFromSortedSet(String, int, int, boolean)
     */
    GET_MEMBERS_BY_RANK_FROM_SORTED_SET("#getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getMembersWithScoresByRankFromSortedSet(String, int, int, boolean)
     */
    GET_MEMBERS_WITH_SCORES_BY_RANK_FROM_SORTED_SET("#getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getMembersByScoreFromSortedSet(String, double, double, boolean)
     */
    GET_MEMBERS_BY_SCORE_FROM_SORTED_SET("#getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getMembersByScoreFromSortedSet(String, double, boolean, double, boolean, boolean, int, int)
     */
    EXTRA_GET_MEMBERS_BY_SCORE_FROM_SORTED_SET("#getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, " +
            "double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getMembersWithScoresByScoreFromSortedSet(String, double, double, boolean)
     */
    GET_MEMBERS_WITH_SCORES_BY_SCORE_FROM_SORTED_SET("#getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisSortedSetClient#getMembersWithScoresByScoreFromSortedSet(String, double, boolean, double, boolean, boolean, int, int)
     */
    EXTRA_GET_MEMBERS_WITH_SCORES_BY_SCORE_FROM_SORTED_SET("#getMembersWithScoresByScoreFromSortedSet(String key, double minScore, " +
            "boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#addGeoCoordinate(String, double, double, String)
     */
    ADD_GEO_COORDINATE("#addGeoCoordinate(String key, double longitude, double latitude, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#addGeoCoordinates(String, Map)
     */
    ADD_GEO_COORDINATES("#addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#removeGeoMember(String, String)
     */
    REMOVE_GEO_MEMBER("#removeGeoMember(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#removeGeoMembers(String, Collection)
     */
    MULTI_REMOVE_GEO_MEMBERS("#removeGeoMembers(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#getGeoDistance(String, String, String, GeoDistanceUnit)
     */
    GET_GEO_DISTANCE("#getGeoDistance(String key, String member, String targetMember, GeoDistanceUnit unit)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#getGeoCoordinate(String, String)
     */
    GET_GEO_COORDINATE("#getGeoCoordinate(String key, String member)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#getGeoCoordinates(String, Collection)
     */
    GET_GEO_COORDINATES("#getGeoCoordinates(String key, Collection<String> members)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#findGeoNeighbours(String, GeoCoordinate, GeoSearchParameter)
     */
    FIND_GEO_NEIGHBOURS("#findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisGeoClient#findGeoNeighboursByMember(String, String, GeoSearchParameter)
     */
    FIND_GEO_NEIGHBOURS_BY_MEMBER("#findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter)", true);

    private final String methodName;

    private final boolean isReadOnly;

    RedisClientMethod(String methodName, boolean isReadOnly) {
        this.methodName = methodName;
        this.isReadOnly = isReadOnly;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }
}
