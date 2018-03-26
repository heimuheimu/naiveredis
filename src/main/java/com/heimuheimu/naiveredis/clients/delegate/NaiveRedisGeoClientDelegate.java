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

package com.heimuheimu.naiveredis.clients.delegate;

import com.heimuheimu.naiveredis.NaiveRedisGeoClient;
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
 * Redis GEO 客户端代理接口。
 *
 * @author heimuheimu
 */
public interface NaiveRedisGeoClientDelegate extends NaiveRedisGeoClient {

    NaiveRedisGeoClient getNaiveRedisGeoClient();

    @Override
    default int addGeoCoordinate(String key, double longitude, double latitude, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().addGeoCoordinate(key, longitude, latitude, member);
    }

    @Override
    default int addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().addGeoCoordinates(key, memberMap);
    }

    @Override
    default int removeGeoMember(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().removeGeoMember(key, member);
    }

    @Override
    default int removeGeoMembers(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().removeGeoMembers(key, members);
    }

    @Override
    default Double getGeoDistance(String key, String member, String targetMember, GeoDistanceUnit unit) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().getGeoDistance(key, member, targetMember, unit);
    }

    @Override
    default GeoCoordinate getGeoCoordinate(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().getGeoCoordinate(key, member);
    }

    @Override
    default Map<String, GeoCoordinate> getGeoCoordinates(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().getGeoCoordinates(key, members);
    }

    @Override
    default List<GeoNeighbour> findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().findGeoNeighbours(key, center, geoSearchParameter);
    }

    @Override
    default List<GeoNeighbour> findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getNaiveRedisGeoClient().findGeoNeighboursByMember(key, member, geoSearchParameter);
    }
}
