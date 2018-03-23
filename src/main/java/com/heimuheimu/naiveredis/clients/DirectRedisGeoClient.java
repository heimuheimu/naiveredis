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

package com.heimuheimu.naiveredis.clients;

import com.heimuheimu.naiveredis.NaiveRedisGeoClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.geo.*;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.data.RedisDataParser;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.geo.GeoCoordinate;
import com.heimuheimu.naiveredis.geo.GeoDistanceUnit;
import com.heimuheimu.naiveredis.geo.GeoNeighbour;
import com.heimuheimu.naiveredis.geo.GeoSearchParameter;

import java.util.*;

/**
 * Redis GEO 直连客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisGeoClient extends AbstractDirectRedisClient implements NaiveRedisGeoClient {

    /**
     * 构造一个 Redis 计数器直连客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public DirectRedisGeoClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
    }

    @Override
    public int addGeoCoordinate(String key, double longitude, double latitude, String member) throws IllegalArgumentException,
            IllegalStateException, TimeoutException, RedisException {
        Map<String, GeoCoordinate> memberMap = new HashMap<>();
        memberMap.put(member, new GeoCoordinate(longitude, latitude));
        return addGeoCoordinates(key, memberMap);
    }

    @Override
    public int addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (memberMap == null || memberMap.isEmpty()) {
            return 0;
        }
        String methodName = methodNamePrefix + "addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("memberMap", memberMap);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new GeoAddCommand(key, memberMap), RedisDataParser::parseInt);
    }

    @Override
    public Double getGeoDistance(String key, String member, String targetMember, GeoDistanceUnit unit) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getGeoDistance(String key, String member, String targetMember, GeoDistanceUnit unit)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("targetMember", targetMember);
        parameterChecker.addParameter("unit", unit);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);
        parameterChecker.check("targetMember", "isNull", Parameters::isNull);
        parameterChecker.check("unit", "isNull", Parameters::isNull);

        return (Double) execute(methodName, parameterChecker.getParameterMap(), () -> new GeoDistCommand(key, member, targetMember, unit),
                RedisDataParser::parseDouble);
    }

    @Override
    public GeoCoordinate getGeoCoordinate(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return getGeoCoordinates(key, Collections.singleton(member)).get(member);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, GeoCoordinate> getGeoCoordinates(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (members == null || members.isEmpty()) {
            return new HashMap<>();
        }
        String methodName = methodNamePrefix + "getGeoCoordinates(String key, Collection<String> members)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("members", members);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (Map<String, GeoCoordinate>) execute(methodName, parameterChecker.getParameterMap(), () -> new GeoPosCommand(key, members),
                response -> {
                    Map<String, GeoCoordinate> result = new HashMap<>();
                    if (response.size() > 0) {
                        int index = 0;
                        for (String member : members) {
                            RedisData data = response.get(index++);
                            if (data.size() > 0) {
                                double longitude = RedisDataParser.parseDouble(data.get(0));
                                double latitude = RedisDataParser.parseDouble(data.get(1));
                                result.put(member, new GeoCoordinate(longitude, latitude));
                            }
                        }
                    }
                    return result;
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<GeoNeighbour> findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("center", center);
        parameterChecker.addParameter("geoSearchParameter", geoSearchParameter);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("center", "isNull", Parameters::isNull);
        parameterChecker.check("geoSearchParameter", "isNull", Parameters::isNull);

        return (List<GeoNeighbour>) execute(methodName, parameterChecker.getParameterMap(), () -> new GeoRadiusCommand(key, center, geoSearchParameter),
                response -> parseNeighbours(response, geoSearchParameter));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<GeoNeighbour> findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("geoSearchParameter", geoSearchParameter);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);
        parameterChecker.check("geoSearchParameter", "isNull", Parameters::isNull);

        return (List<GeoNeighbour>) execute(methodName, parameterChecker.getParameterMap(), () -> new GeoRadiusByMemberCommand(key, member, geoSearchParameter),
                response -> parseNeighbours(response, geoSearchParameter));
    }

    private List<GeoNeighbour> parseNeighbours(RedisData data, GeoSearchParameter geoSearchParameter) {
        List<GeoNeighbour> neighbours = new ArrayList<>();
        if (!geoSearchParameter.isNeedDistance() && !geoSearchParameter.isNeedCoordinate()) {
            List<String> memberList = RedisDataParser.parseStringList(data);
            for (String member : memberList) {
                GeoNeighbour neighbour = new GeoNeighbour();
                neighbour.setMember(member);
                neighbour.setDistanceUnit(geoSearchParameter.getDistanceUnit());
                neighbours.add(neighbour);
            }
        } else {
            for (int i = 0; i < data.size(); i++) {
                int memberInfoIndex = 0;
                RedisData memberInfoArray = data.get(i);
                GeoNeighbour neighbour = new GeoNeighbour();
                neighbour.setMember(memberInfoArray.get(memberInfoIndex++).getText());
                neighbour.setDistanceUnit(geoSearchParameter.getDistanceUnit());
                if (geoSearchParameter.isNeedDistance()) {
                    neighbour.setDistance(RedisDataParser.parseDouble(memberInfoArray.get(memberInfoIndex++)));
                }
                if (geoSearchParameter.isNeedCoordinate()) {
                    RedisData coordinateArray = memberInfoArray.get(memberInfoIndex);
                    double longitude = RedisDataParser.parseDouble(coordinateArray.get(0));
                    double latitude = RedisDataParser.parseDouble(coordinateArray.get(1));
                    neighbour.setCoordinate(new GeoCoordinate(longitude, latitude));
                }
                neighbours.add(neighbour);
            }
        }
        return neighbours;
    }
}
