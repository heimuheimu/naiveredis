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

package com.heimuheimu.naiveredis.cluster;

import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.NaiveRedisClient;
import com.heimuheimu.naiveredis.constant.RedisClientMethod;
import com.heimuheimu.naiveredis.constant.SortedSetAddMode;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.geo.GeoCoordinate;
import com.heimuheimu.naiveredis.geo.GeoDistanceUnit;
import com.heimuheimu.naiveredis.geo.GeoNeighbour;
import com.heimuheimu.naiveredis.geo.GeoSearchParameter;
import com.heimuheimu.naiveredis.monitor.ClusterMonitor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 集群客户端抽象类。
 *
 * @author heimuheimu
 */
public abstract class AbstractRedisClusterClient implements NaiveRedisClient {

    protected final ClusterMonitor clusterMonitor = ClusterMonitor.getInstance();

    @Override
    public void expire(String key, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("expiry", expiry);

        getClient(RedisClientMethod.EXPIRE, parameterMap).expire(key, expiry);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        getClient(RedisClientMethod.DELETE, parameterMap).delete(key);
    }

    @Override
    public <T> T get(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        return getClient(RedisClientMethod.GET, parameterMap).get(key);
    }

    @Override
    public void set(String key, Object value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("value", value);

        getClient(RedisClientMethod.SET, parameterMap).set(key, value);
    }

    @Override
    public void set(String key, Object value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("value", value);
        parameterMap.put("expiry", expiry);

        getClient(RedisClientMethod.SET_WITH_EXPIRE, parameterMap).set(key, value, expiry);
    }

    @Override
    public Long getCount(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        return getClient(RedisClientMethod.GET_COUNT, parameterMap).getCount(key);
    }

    @Override
    public long addAndGet(String key, long delta, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("delta", delta);
        parameterMap.put("expiry", expiry);

        return getClient(RedisClientMethod.ADD_AND_GET, parameterMap).addAndGet(key, delta, expiry);
    }

    @Override
    public int addToSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.ADD_TO_SET, parameterMap).addToSet(key, member);
    }

    @Override
    public int addToSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("members", members);

        return getClient(RedisClientMethod.MULTI_ADD_TO_SET, parameterMap).addToSet(key, members);
    }

    @Override
    public int removeFromSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.REMOVE_FROM_SET, parameterMap).removeFromSet(key, member);
    }

    @Override
    public int removeFromSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("members", members);

        return getClient(RedisClientMethod.MULTI_REMOVE_FROM_SET, parameterMap).removeFromSet(key, members);
    }

    @Override
    public boolean isMemberInSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.IS_MEMBER_IN_SET, parameterMap).isMemberInSet(key, member);
    }

    @Override
    public int getSizeOfSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        return getClient(RedisClientMethod.GET_SIZE_OF_SET, parameterMap).getSizeOfSet(key);
    }

    @Override
    public List<String> getMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("count", count);

        return getClient(RedisClientMethod.GET_MEMBERS_FROM_SET, parameterMap).getMembersFromSet(key, count);
    }

    @Override
    public List<String> popMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("count", count);

        return getClient(RedisClientMethod.POP_MEMBERS_FROM_SET, parameterMap).popMembersFromSet(key, count);
    }

    @Override
    public List<String> getAllMembersFromSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        return getClient(RedisClientMethod.GET_ALL_MEMBERS_FROM_SET, parameterMap).getAllMembersFromSet(key);
    }

    @Override
    public int addToSortedSet(String key, double score, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("score", score);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.ADD_TO_SORTED_SET, parameterMap).addToSortedSet(key, score, member);
    }

    @Override
    public int addToSortedSet(String key, double score, String member, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("score", score);
        parameterMap.put("member", member);
        parameterMap.put("mode", mode);

        return getClient(RedisClientMethod.ADD_TO_SORTED_SET_WITH_MODE, parameterMap).addToSortedSet(key, score, member, mode);
    }

    @Override
    public int addToSortedSet(String key, Map<String, Double> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("memberMap", memberMap);

        return getClient(RedisClientMethod.MULTI_ADD_TO_SORTED_SET, parameterMap).addToSortedSet(key, memberMap);
    }

    @Override
    public int addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("memberMap", memberMap);
        parameterMap.put("mode", mode);

        return getClient(RedisClientMethod.MULTI_ADD_TO_SORTED_SET_WITH_MODE, parameterMap).addToSortedSet(key, memberMap, mode);
    }

    @Override
    public double incrForSortedSet(String key, double increment, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("increment", increment);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.INCR_FOR_SORTED_SET, parameterMap).incrForSortedSet(key, increment, member);
    }

    @Override
    public int removeFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.REMOVE_FROM_SORTED_SET, parameterMap).removeFromSortedSet(key, member);
    }

    @Override
    public int removeFromSortedSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("members", members);

        return getClient(RedisClientMethod.MULTI_REMOVE_FROM_SORTED_SET, parameterMap).removeFromSortedSet(key, members);
    }

    @Override
    public int removeByRankFromSortedSet(String key, int start, int stop) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("start", start);
        parameterMap.put("stop", stop);

        return getClient(RedisClientMethod.REMOVE_BY_RANK_FROM_SORTED_SET, parameterMap).removeByRankFromSortedSet(key, start, stop);
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("maxScore", maxScore);

        return getClient(RedisClientMethod.REMOVE_BY_SCORE_FROM_SORTED_SET, parameterMap).removeByScoreFromSortedSet(key, minScore, maxScore);
    }

    @Override
    public int removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("includeMinScore", includeMinScore);
        parameterMap.put("maxScore", maxScore);
        parameterMap.put("includeMaxScore", includeMaxScore);

        return getClient(RedisClientMethod.EXTRA_REMOVE_BY_SCORE_FROM_SORTED_SET, parameterMap).removeByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore);
    }

    @Override
    public Double getScoreFromSortedSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.GET_SCORE_FROM_SORTED_SET, parameterMap).getScoreFromSortedSet(key, member);
    }

    @Override
    public Integer getRankFromSortedSet(String key, String member, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);
        parameterMap.put("reverse", reverse);

        return getClient(RedisClientMethod.GET_RANK_FROM_SORTED_SET, parameterMap).getRankFromSortedSet(key, member, reverse);
    }

    @Override
    public int getSizeOfSortedSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);

        return getClient(RedisClientMethod.GET_SIZE_OF_SORTED_SET, parameterMap).getSizeOfSortedSet(key);
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, double maxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("maxScore", maxScore);

        return getClient(RedisClientMethod.GET_COUNT_FROM_SORTED_SET, parameterMap).getCountFromSortedSet(key, minScore, maxScore);
    }

    @Override
    public int getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("includeMinScore", includeMinScore);
        parameterMap.put("maxScore", maxScore);
        parameterMap.put("includeMaxScore", includeMaxScore);

        return getClient(RedisClientMethod.EXTRA_GET_COUNT_FROM_SORTED_SET, parameterMap).getCountFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore);
    }

    @Override
    public List<String> getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("start", start);
        parameterMap.put("stop", stop);
        parameterMap.put("reverse", reverse);

        return getClient(RedisClientMethod.GET_MEMBERS_BY_RANK_FROM_SORTED_SET, parameterMap).getMembersByRankFromSortedSet(key, start, stop, reverse);
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("start", start);
        parameterMap.put("stop", stop);
        parameterMap.put("reverse", reverse);

        return getClient(RedisClientMethod.GET_MEMBERS_WITH_SCORES_BY_RANK_FROM_SORTED_SET, parameterMap).getMembersWithScoresByRankFromSortedSet(key, start, stop, reverse);
    }

    @Override
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("maxScore", maxScore);
        parameterMap.put("reverse", reverse);

        return getClient(RedisClientMethod.GET_MEMBERS_BY_SCORE_FROM_SORTED_SET, parameterMap).getMembersByScoreFromSortedSet(key, minScore, maxScore, reverse);
    }

    @Override
    public List<String> getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("includeMinScore", includeMinScore);
        parameterMap.put("maxScore", maxScore);
        parameterMap.put("includeMaxScore", includeMaxScore);
        parameterMap.put("reverse", reverse);
        parameterMap.put("offset", offset);
        parameterMap.put("count", count);

        return getClient(RedisClientMethod.EXTRA_GET_MEMBERS_BY_SCORE_FROM_SORTED_SET, parameterMap).getMembersByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore, reverse, offset, count);
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("maxScore", maxScore);
        parameterMap.put("reverse", reverse);

        return getClient(RedisClientMethod.GET_MEMBERS_WITH_SCORES_BY_SCORE_FROM_SORTED_SET, parameterMap).getMembersWithScoresByScoreFromSortedSet(key, minScore, maxScore, reverse);
    }

    @Override
    public LinkedHashMap<String, Double> getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("minScore", minScore);
        parameterMap.put("includeMinScore", includeMinScore);
        parameterMap.put("maxScore", maxScore);
        parameterMap.put("includeMaxScore", includeMaxScore);
        parameterMap.put("reverse", reverse);
        parameterMap.put("offset", offset);
        parameterMap.put("count", count);

        return getClient(RedisClientMethod.EXTRA_GET_MEMBERS_WITH_SCORES_BY_SCORE_FROM_SORTED_SET, parameterMap).getMembersWithScoresByScoreFromSortedSet(key, minScore, includeMinScore, maxScore, includeMaxScore, reverse, offset, count);
    }

    @Override
    public int addGeoCoordinate(String key, double longitude, double latitude, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("longitude", longitude);
        parameterMap.put("latitude", latitude);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.ADD_GEO_COORDINATE, parameterMap).addGeoCoordinate(key, longitude, latitude, member);
    }

    @Override
    public int addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("memberMap", memberMap);

        return getClient(RedisClientMethod.ADD_GEO_COORDINATES, parameterMap).addGeoCoordinates(key, memberMap);
    }

    @Override
    public Double getGeoDistance(String key, String member, String targetMember, GeoDistanceUnit unit) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);
        parameterMap.put("targetMember", targetMember);
        parameterMap.put("unit", unit);

        return getClient(RedisClientMethod.GET_GEO_DISTANCE, parameterMap).getGeoDistance(key, member, targetMember, unit);
    }

    @Override
    public GeoCoordinate getGeoCoordinate(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);

        return getClient(RedisClientMethod.GET_GEO_COORDINATE, parameterMap).getGeoCoordinate(key, member);
    }

    @Override
    public Map<String, GeoCoordinate> getGeoCoordinates(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("members", members);

        return getClient(RedisClientMethod.GET_GEO_COORDINATES, parameterMap).getGeoCoordinates(key, members);
    }

    @Override
    public List<GeoNeighbour> findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("center", center);
        parameterMap.put("geoSearchParameter", geoSearchParameter);

        return getClient(RedisClientMethod.FIND_GEO_NEIGHBOURS, parameterMap).findGeoNeighbours(key, center, geoSearchParameter);
    }

    @Override
    public List<GeoNeighbour> findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("key", key);
        parameterMap.put("member", member);
        parameterMap.put("geoSearchParameter", geoSearchParameter);

        return getClient(RedisClientMethod.FIND_GEO_NEIGHBOURS_BY_MEMBER, parameterMap).findGeoNeighboursByMember(key, member, geoSearchParameter);
    }

    protected abstract DirectRedisClient getClient(RedisClientMethod method, Map<String, Object> parameterMap);
}
