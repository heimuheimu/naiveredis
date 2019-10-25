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
import java.util.List;
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
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getTimeToLive(String)
     */
    GET_TIME_TO_LIVE("#getTimeToLive(String key)", true),

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
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setIfAbsent(String, Object)
     */
    SET_IF_ABSENT("#setIfAbsent(String key, Object value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setIfAbsent(String, Object, int)
     */
    SET_IF_ABSENT_WITH_EXPIRE("#setIfAbsent(String key, Object value, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setIfExist(String, Object)
     */
    SET_IF_EXIST("#setIfExist(String key, Object value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setIfExist(String, Object, int)
     */
    SET_IF_EXIST_WITH_EXPIRE("#setIfExist(String key, Object value, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getString(String)
     */
    GET_STRING("#getString(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#multiGetString(Set)
     */
    MULTI_GET_STRING("#multiGetString(Set<String> keySet)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setString(String, String)
     */
    SET_STRING("#setString(String key, String value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setString(String, String, int)
     */
    SET_STRING_WITH_EXPIRE("#setString(String key, String value, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setStringIfAbsent(String, String)
     */
    SET_STRING_IF_ABSENT("#setStringIfAbsent(String key, String value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#setStringIfAbsent(String, String, int)
     */
    SET_STRING_IF_ABSENT_WITH_EXPIRE("#setStringIfAbsent(String key, String value, int expiry)", false),

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
    FIND_GEO_NEIGHBOURS_BY_MEMBER("#findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#addFirstToList(String, String)
     */
    ADD_FIRST_TO_LIST("#addFirstToList(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#addFirstToList(String, String, boolean)
     */
    ADD_FIRST_TO_LIST_WITH_MODE("#addFirstToList(String key, String member, boolean isAutoCreate)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#addFirstToList(String, Collection)
     */
    MULTI_ADD_FIRST_TO_LIST("#addFirstToList(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#addLastToList(String, String)
     */
    ADD_LAST_TO_LIST("#addLastToList(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#addLastToList(String, String, boolean)
     */
    ADD_LAST_TO_LIST_WITH_MODE("#addLastToList(String key, String member, boolean isAutoCreate)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#addLastToList(String, Collection)
     */
    MULTI_ADD_LAST_TO_LIST("#addLastToList(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#popFirstFromList(String)
     */
    POP_FIRST_FROM_LIST("#popFirstFromList(String key)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#popLastFromList(String)
     */
    POP_LAST_FROM_LIST("#popLastFromList(String key)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#insertIntoList(String, String, String, boolean)
     */
    INSERT_INTO_LIST("#insertIntoList(String key, String pivotalMember, String member, boolean isAfter)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#setToList(String, int, String)
     */
    SET_TO_LIST("#setToList(String key, int index, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#removeFromList(String, int, String)
     */
    REMOVE_FROM_LIST("#removeFromList(String key, int count, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#trimList(String, int, int)
     */
    TRIM_LIST("#trimList(String key, int startIndex, int endIndex)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#getSizeOfList(String)
     */
    GET_SIZE_OF_LIST("#getSizeOfList(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#getByIndexFromList(String, int)
     */
    GET_BY_INDEX_FROM_LIST("#getByIndexFromList(String key, int index)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisListClient#getMembersFromList(String, int, int)
     */
    GET_MEMBERS_FROM_LIST("#getMembersFromList(String key, int startIndex, int endIndex)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#putToHashes(String, String, String)
     */
    PUT_TO_HASHES("#putToHashes(String key, String member, String value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#putToHashes(String, Map)
     */
    MULTI_PUT_TO_HASHES("#putToHashes(String key, Map<String, String> memberMap)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#putIfAbsentToHashes(String, String, String)
     */
    PUT_IF_ABSENT_TO_HASHES("#putIfAbsentToHashes(String key, String member, String value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#incrForHashes(String, String, long)
     */
    INCR_FOR_HASHES("#incrForHashes(String key, String member, long increment)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#incrByFloatForHashes(String, String, double)
     */
    INCR_BY_FLOAT_FOR_HASHES("#incrByFloatForHashes(String key, String member, double increment)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#removeFromHashes(String, String)
     */
    REMOVE_FROM_HASHES("#removeFromHashes(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#removeFromHashes(String, Collection)
     */
    MULTI_REMOVE_FROM_HASHES("#removeFromHashes(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#isExistInHashes(String, String)
     */
    IS_EXIST_IN_HASHES("#isExistInHashes(String key, String member)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#getSizeOfHashes(String)
     */
    GET_SIZE_OF_HASHES("#getSizeOfHashes(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#getValueFromHashes(String, String)
     */
    GET_VALUE_FROM_HASHES("#getValueFromHashes(String key, String member)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#getValueLengthFromHashes(String, String)
     */
    GET_VALUE_LENGTH_FROM_HASHES("#getValueLengthFromHashes(String key, String member)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#getMemberMapFromHashes(String, List)
     */
    GET_MEMBER_MAP_FROM_HASHES("#getMemberMapFromHashes(String key, List<String> members)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#getAllFromHashes(String)
     */
    GET_ALL_FROM_HASHES("#getAllFromHashes(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#getMembersFromHashes(String)
     */
    GET_MEMBERS_FROM_HASHES("#getMembersFromHashes(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisHashesClient#getValuesFromHashes(String)
     */
    GET_VALUES_FROM_HASHES("#getValuesFromHashes(String key)", true);

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
