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

import com.heimuheimu.naiveredis.geo.GeoCoordinate;
import com.heimuheimu.naiveredis.geo.GeoDistanceUnit;
import com.heimuheimu.naiveredis.geo.GeoNeighbour;
import com.heimuheimu.naiveredis.geo.GeoSearchParameter;
import com.heimuheimu.naiveredis.util.AssertUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link NaiveRedisGeoClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisGeoClient {

    public abstract NaiveRedisGeoClient getClient();

    @Test
    public void testAddGeoCoordinate() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String member = "经纬度成员";

        // addGeoCoordinate(String key, double longitude, double latitude, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinate(null, 50, 50, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinate("", 50, 50, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinate(key, 50, 50, null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinate(key, -181, 50, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinate(key, 181, 50, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinate(key, 50, -86, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinate(key, 50, 86, member));

        // addGeoCoordinate(String key, double longitude, double latitude, String member) 功能测试
        Assert.assertEquals("Test `addGeoCoordinate` method failed: `invalid return value`.", 1,
                client.addGeoCoordinate(key, 50, 50, member));
        GeoCoordinate coordinate = client.getGeoCoordinate(key, member);
        Assert.assertTrue("Test `addGeoCoordinate` method failed: `invalid coordinate`.",
                isSameDegree(coordinate.getLongitude(), 50) && isSameDegree(coordinate.getLatitude(), 50));

        Assert.assertEquals("Test `addGeoCoordinate` method failed: `invalid return value`.", 0,
                client.addGeoCoordinate(key, 60, 60, member));
        coordinate = client.getGeoCoordinate(key, member);
        Assert.assertTrue("Test `addGeoCoordinate` method failed: `invalid coordinate`.",
                isSameDegree(coordinate.getLongitude(), 60) && isSameDegree(coordinate.getLongitude(), 60));
        client.delete(key);
    }

    @Test
    public void testAddGeoCoordinates() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, GeoCoordinate> memberMap = new HashMap<>();
        memberMap.put("成员 1", new GeoCoordinate(10, 10));
        memberMap.put("成员 2", new GeoCoordinate(20, 20));
        memberMap.put("成员 3", new GeoCoordinate(30, 30));

        // addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinates(null, memberMap));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinates("", memberMap));
        Map<String, GeoCoordinate> invalidMemberMap = new HashMap<>(memberMap);
        invalidMemberMap.put(null, new GeoCoordinate(40, 40));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addGeoCoordinates(key, invalidMemberMap));

        // addGeoCoordinates(String key, Map<String, GeoCoordinate> memberMap) 功能测试
        Assert.assertEquals("Test `addGeoCoordinates` method failed: `invalid return value`.", 0,
                client.addGeoCoordinates(key, null));
        Assert.assertEquals("Test `addGeoCoordinates` method failed: `invalid return value`.", 0,
                client.addGeoCoordinates(key, new HashMap<>()));
        Assert.assertEquals("Test `addGeoCoordinates` method failed: `invalid return value`.", 3,
                client.addGeoCoordinates(key, memberMap));
        Assert.assertEquals("Test `addGeoCoordinates` method failed: `invalid return value`.", 0,
                client.addGeoCoordinates(key, memberMap));

        Map<String, GeoCoordinate> actualGeoCoordinateMap = client.getGeoCoordinates(key, memberMap.keySet());
        Assert.assertEquals("Test `addGeoCoordinates` method failed: `invalid map size`.", memberMap.size(),
                actualGeoCoordinateMap.size());
        for (String member : memberMap.keySet()) {
            GeoCoordinate expectedGeoCoordinate = memberMap.get(member);
            GeoCoordinate actualGeoCoordinate = actualGeoCoordinateMap.get(member);
            Assert.assertTrue("Test `addGeoCoordinate` method failed: `invalid coordinate`.",
                    isSameDegree(expectedGeoCoordinate.getLongitude(), actualGeoCoordinate.getLongitude())
                            && isSameDegree(expectedGeoCoordinate.getLatitude(), actualGeoCoordinate.getLatitude()));
        }
        client.delete(key);
    }

    @Test
    public void testRemoveGeoMember() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, GeoCoordinate> memberMap = new HashMap<>();
        memberMap.put("成员 1", new GeoCoordinate(10, 10));
        memberMap.put("成员 2", new GeoCoordinate(20, 20));
        memberMap.put("成员 3", new GeoCoordinate(30, 30));

        // removeGeoMember(String key, String member) 参数异常测试
        client.delete(key);
        String firstMember = memberMap.keySet().iterator().next();
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeGeoMember(null, firstMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeGeoMember("", firstMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeGeoMember(key, null));

        // removeGeoMember(String key, String member) 功能测试
        Assert.assertEquals("Test `removeGeoMember` method failed: `invalid return value`.", 0,
                client.removeGeoMember(key, firstMember));
        client.addGeoCoordinates(key, memberMap);
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `removeGeoMember` method failed: `invalid return value`.", 1,
                    client.removeGeoMember(key, member));
        }
        Map<String, GeoCoordinate> actualGeoCoordinateMap = client.getGeoCoordinates(key, memberMap.keySet());
        Assert.assertTrue("Test `removeGeoMember` method failed: `invalid map size`.", actualGeoCoordinateMap.isEmpty());
        client.delete(key);
    }

    @Test
    public void testRemoveGeoMembers() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, GeoCoordinate> memberMap = new HashMap<>();
        memberMap.put("成员 1", new GeoCoordinate(10, 10));
        memberMap.put("成员 2", new GeoCoordinate(20, 20));
        memberMap.put("成员 3", new GeoCoordinate(30, 30));

        // removeGeoMembers(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeGeoMembers(null, memberMap.keySet()));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeGeoMembers("", memberMap.keySet()));
        List<String> invalidMembers = new ArrayList<>(memberMap.keySet());
        invalidMembers.add(null);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeGeoMembers(key, invalidMembers));

        // removeGeoMembers(String key, Collection<String> members) 功能测试
        Assert.assertEquals("Test `removeGeoMembers` method failed: `invalid return value`.", 0,
                client.removeGeoMembers(key, null));
        Assert.assertEquals("Test `removeGeoMembers` method failed: `invalid return value`.", 0,
                client.removeGeoMembers(key, new ArrayList<>()));
        Assert.assertEquals("Test `removeGeoMembers` method failed: `invalid return value`.", 0,
                client.removeGeoMembers(key, memberMap.keySet()));
        client.addGeoCoordinates(key, memberMap);
        Assert.assertEquals("Test `removeGeoMembers` method failed: `invalid return value`.", memberMap.size(),
                client.removeGeoMembers(key, memberMap.keySet()));
        Map<String, GeoCoordinate> actualGeoCoordinateMap = client.getGeoCoordinates(key, memberMap.keySet());
        Assert.assertTrue("Test `removeGeoMembers` method failed: `invalid map size`.", actualGeoCoordinateMap.isEmpty());
        client.delete(key);
    }

    @Test
    public void testGetGeoCoordinate() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String notExistMember = "不存在的成员";
        Map<String, GeoCoordinate> memberMap = new HashMap<>();
        memberMap.put("成员 1", new GeoCoordinate(10, 10));
        memberMap.put("成员 2", new GeoCoordinate(20, 20));
        memberMap.put("成员 3", new GeoCoordinate(30, 30));

        // getGeoCoordinate(String key, String member) 参数异常测试
        client.delete(key);
        String firstMember = memberMap.keySet().iterator().next();
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getGeoCoordinate(null, firstMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getGeoCoordinate("", firstMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getGeoCoordinate(key, null));

        // getGeoCoordinate(String key, String member) 功能测试
        Assert.assertNull("Test `getGeoCoordinate` method failed: `invalid return value`.",
                client.getGeoCoordinate(key, firstMember));
        client.addGeoCoordinates(key, memberMap);
        Assert.assertNull("Test `getGeoCoordinate` method failed: `invalid return value`.",
                client.getGeoCoordinate(key, notExistMember));
        for (String member : memberMap.keySet()) {
            GeoCoordinate expectedGeoCoordinate = memberMap.get(member);
            GeoCoordinate actualGeoCoordinate = client.getGeoCoordinate(key, member);
            Assert.assertTrue("Test `getGeoCoordinate` method failed: `invalid coordinate`.",
                    isSameDegree(expectedGeoCoordinate.getLongitude(), actualGeoCoordinate.getLongitude())
                            && isSameDegree(expectedGeoCoordinate.getLatitude(), actualGeoCoordinate.getLatitude()));
        }
        client.delete(key);
    }

    @Test
    public void testGetGeoCoordinates() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String notExistMember = "不存在的成员";
        Map<String, GeoCoordinate> memberMap = new HashMap<>();
        memberMap.put("成员 1", new GeoCoordinate(10, 10));
        memberMap.put("成员 2", new GeoCoordinate(20, 20));
        memberMap.put("成员 3", new GeoCoordinate(30, 30));

        // getGeoCoordinates(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getGeoCoordinates(null, memberMap.keySet()));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getGeoCoordinates("", memberMap.keySet()));
        List<String> invalidMembers = new ArrayList<>(memberMap.keySet());
        invalidMembers.add(null);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getGeoCoordinates(key, invalidMembers));

        // getGeoCoordinates(String key, Collection<String> members) 功能测试
        Assert.assertTrue("Test `getGeoCoordinates` method failed: `invalid map size`.",
                client.getGeoCoordinates(key, null).isEmpty());
        Assert.assertTrue("Test `getGeoCoordinates` method failed: `invalid map size`.",
                client.getGeoCoordinates(key, new ArrayList<>()).isEmpty());
        Assert.assertTrue("Test `getGeoCoordinates` method failed: `invalid map size`.",
                client.getGeoCoordinates(key, memberMap.keySet()).isEmpty());
        client.addGeoCoordinates(key, memberMap);
        List<String> members = new ArrayList<>(memberMap.keySet());
        members.add(notExistMember);
        Map<String, GeoCoordinate> actualGeoCoordinateMap = client.getGeoCoordinates(key, members);
        Assert.assertEquals("Test `getGeoCoordinates` method failed: `invalid map size`.", memberMap.size(),
                actualGeoCoordinateMap.size());
        for (String member : memberMap.keySet()) {
            GeoCoordinate expectedGeoCoordinate = memberMap.get(member);
            GeoCoordinate actualGeoCoordinate = client.getGeoCoordinate(key, member);
            Assert.assertTrue("Test `getGeoCoordinates` method failed: `invalid coordinate`.",
                    isSameDegree(expectedGeoCoordinate.getLongitude(), actualGeoCoordinate.getLongitude())
                            && isSameDegree(expectedGeoCoordinate.getLatitude(), actualGeoCoordinate.getLatitude()));
        }
        client.delete(key);
    }

    @Test
    public void testFindGeoNeighbours() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        GeoCoordinate center = new GeoCoordinate(50, 50);
        Map<String, GeoCoordinate> memberMap = new HashMap<>();  // 0.0001 经度约等于 7.2 米
        memberMap.put("成员 1", new GeoCoordinate(50.0001, 50));
        memberMap.put("成员 2", new GeoCoordinate(50.0002, 50));
        memberMap.put("成员 3", new GeoCoordinate(50.0003, 50));
        memberMap.put("成员 4", new GeoCoordinate(50.0004, 50));
        memberMap.put("成员 5", new GeoCoordinate(50.0005, 50));

        GeoSearchParameter geoSearchParameter = new GeoSearchParameter(25, GeoDistanceUnit.M);
        String[] expectedMembers = new String[]{"成员 1", "成员 2", "成员 3"};

        // findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighbours(null, center, geoSearchParameter));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighbours("", center, geoSearchParameter));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighbours(key, null, geoSearchParameter));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighbours(key, center, null));

        // findGeoNeighbours(String key, GeoCoordinate center, GeoSearchParameter geoSearchParameter) 功能测试
        Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid list size`.",
                client.findGeoNeighbours(key, center, geoSearchParameter).isEmpty());
        client.addGeoCoordinates(key, memberMap);

        // 升序，不返回坐标和距离测试
        geoSearchParameter.setOrderBy(GeoSearchParameter.ORDER_BY_ASC);
        geoSearchParameter.setNeedCoordinate(false);
        geoSearchParameter.setNeedDistance(false);
        List<GeoNeighbour> geoNeighbours = client.findGeoNeighbours(key, center, geoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid list size`.",
                geoNeighbours.size() == expectedMembers.length);
        for (int i = 0; i < expectedMembers.length; i++) {
            GeoNeighbour neighbour = geoNeighbours.get(i);
            Assert.assertEquals("Test `findGeoNeighbours` method failed: `invalid member`.", expectedMembers[i], neighbour.getMember());
            Assert.assertEquals("Test `findGeoNeighbours` method failed: `invalid coordinate`.", null, neighbour.getCoordinate());
            Assert.assertEquals("Test `findGeoNeighbours` method failed: `invalid distance`.", 0.0d, neighbour.getDistance(), 0.000001);
        }

        // 降序，返回坐标和距离测试
        geoSearchParameter.setOrderBy(GeoSearchParameter.ORDER_BY_DESC);
        geoSearchParameter.setNeedCoordinate(true);
        geoSearchParameter.setNeedDistance(true);
        geoNeighbours = client.findGeoNeighbours(key, center, geoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);
        for (int i = 0; i < expectedMembers.length; i++) {
            GeoNeighbour neighbour = geoNeighbours.get(i);
            int expectedMemberIndex = expectedMembers.length - 1 - i;
            String expectedMember = expectedMembers[expectedMemberIndex];
            Assert.assertEquals("Test `findGeoNeighbours` method failed: `invalid member`.", expectedMember, neighbour.getMember());
            Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid coordinate`.", isSameCoordinate(memberMap.get(expectedMember), neighbour.getCoordinate()));
            Assert.assertEquals("Test `findGeoNeighbours` method failed: `invalid distance`.", 7.2d * (expectedMemberIndex + 1), neighbour.getDistance(), 1d);
        }

        // count 查询参数测试
        geoSearchParameter.setCount(2);
        geoNeighbours = client.findGeoNeighbours(key, center, geoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid list size`.", geoNeighbours.size() == 2);

        // 不同距离单位测试
        GeoSearchParameter kmGeoSearchParameter = new GeoSearchParameter(0.025, GeoDistanceUnit.KM);
        geoNeighbours = client.findGeoNeighbours(key, center, kmGeoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);

        GeoSearchParameter ftGeoSearchParameter = new GeoSearchParameter(82.0209974, GeoDistanceUnit.FT);
        geoNeighbours = client.findGeoNeighbours(key, center, ftGeoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);

        GeoSearchParameter miGeoSearchParameter = new GeoSearchParameter(0.0155343, GeoDistanceUnit.MI);
        geoNeighbours = client.findGeoNeighbours(key, center, miGeoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighbours` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);
        client.delete(key);
    }

    @Test
    public void testFindGeoNeighboursByMember() {
        NaiveRedisGeoClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String centerMember = "中心成员";
        GeoCoordinate centerCoordinate = new GeoCoordinate(50, 50);
        Map<String, GeoCoordinate> memberMap = new HashMap<>();  // 0.0001 经度约等于 7.2 米
        memberMap.put("成员 1", new GeoCoordinate(50.0001, 50));
        memberMap.put("成员 2", new GeoCoordinate(50.0002, 50));
        memberMap.put("成员 3", new GeoCoordinate(50.0003, 50));
        memberMap.put("成员 4", new GeoCoordinate(50.0004, 50));
        memberMap.put("成员 5", new GeoCoordinate(50.0005, 50));

        GeoSearchParameter geoSearchParameter = new GeoSearchParameter(25, GeoDistanceUnit.M);
        String[] expectedMembers = new String[]{"中心成员", "成员 1", "成员 2", "成员 3"};

        // findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighboursByMember(null, centerMember, geoSearchParameter));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighboursByMember("", centerMember, geoSearchParameter));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighboursByMember(key, null, geoSearchParameter));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.findGeoNeighboursByMember(key, centerMember, null));

        // findGeoNeighboursByMember(String key, String member, GeoSearchParameter geoSearchParameter) 功能测试
        Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid list size`.",
                client.findGeoNeighboursByMember(key, centerMember, geoSearchParameter).isEmpty());
        client.addGeoCoordinates(key, memberMap);

        AssertUtil.assertThrowRedisException(() -> client.findGeoNeighboursByMember(key, centerMember, geoSearchParameter));
        client.addGeoCoordinate(key, centerCoordinate.getLongitude(), centerCoordinate.getLatitude(), centerMember);

        // 升序，不返回坐标和距离测试
        geoSearchParameter.setOrderBy(GeoSearchParameter.ORDER_BY_ASC);
        geoSearchParameter.setNeedCoordinate(false);
        geoSearchParameter.setNeedDistance(false);
        List<GeoNeighbour> geoNeighbours = client.findGeoNeighboursByMember(key, centerMember, geoSearchParameter);
        Assert.assertEquals("Test `findGeoNeighboursByMember` method failed: `invalid list size`.", geoNeighbours.size(), expectedMembers.length);
        for (int i = 0; i < expectedMembers.length; i++) {
            GeoNeighbour neighbour = geoNeighbours.get(i);
            Assert.assertEquals("Test `findGeoNeighboursByMember` method failed: `invalid member`.", expectedMembers[i], neighbour.getMember());
            Assert.assertEquals("Test `findGeoNeighboursByMember` method failed: `invalid coordinate`.", null, neighbour.getCoordinate());
            Assert.assertEquals("Test `findGeoNeighboursByMember` method failed: `invalid distance`.", 0.0d, neighbour.getDistance(), 0.000001);
        }

        // 降序，返回坐标和距离测试
        geoSearchParameter.setOrderBy(GeoSearchParameter.ORDER_BY_DESC);
        geoSearchParameter.setNeedCoordinate(true);
        geoSearchParameter.setNeedDistance(true);
        geoNeighbours = client.findGeoNeighboursByMember(key, centerMember, geoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);
        for (int i = 0; i < expectedMembers.length; i++) {
            GeoNeighbour neighbour = geoNeighbours.get(i);
            int expectedMemberIndex = expectedMembers.length - 1 - i;
            String expectedMember = expectedMembers[expectedMemberIndex];
            Assert.assertEquals("Test `findGeoNeighboursByMember` method failed: `invalid member`.", expectedMember, neighbour.getMember());
            if (expectedMember.equals(centerMember)) {
                Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid coordinate`.", isSameCoordinate(centerCoordinate, neighbour.getCoordinate()));
            } else {
                Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid coordinate`.", isSameCoordinate(memberMap.get(expectedMember), neighbour.getCoordinate()));
            }
            Assert.assertEquals("Test `findGeoNeighboursByMember` method failed: `invalid distance`.", 7.2d * expectedMemberIndex, neighbour.getDistance(), 1d);
        }

        // count 查询参数测试
        geoSearchParameter.setCount(2);
        geoNeighbours = client.findGeoNeighboursByMember(key, centerMember, geoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid list size`.", geoNeighbours.size() == 2);

        // 不同距离单位测试
        GeoSearchParameter kmGeoSearchParameter = new GeoSearchParameter(0.025, GeoDistanceUnit.KM);
        geoNeighbours = client.findGeoNeighboursByMember(key, centerMember, kmGeoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);

        GeoSearchParameter ftGeoSearchParameter = new GeoSearchParameter(82.0209974, GeoDistanceUnit.FT);
        geoNeighbours = client.findGeoNeighboursByMember(key, centerMember, ftGeoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);

        GeoSearchParameter miGeoSearchParameter = new GeoSearchParameter(0.0155343, GeoDistanceUnit.MI);
        geoNeighbours = client.findGeoNeighboursByMember(key, centerMember, miGeoSearchParameter);
        Assert.assertTrue("Test `findGeoNeighboursByMember` method failed: `invalid list size`.", geoNeighbours.size() == expectedMembers.length);
        client.delete(key);
    }

    /**
     * 判断经度或纬度是否相等，在进行比较前，角度将会保留 5 位小数，舍入方法使用四舍五入方式。
     *
     * @param sourceDegree 目标角度
     * @param targetDegree 对比角度
     * @return 角度是否相等
     */
    private boolean isSameDegree(double sourceDegree, double targetDegree) {
        BigDecimal sourceDegreeBigDecimal = new BigDecimal(String.valueOf(sourceDegree)).setScale(5, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal targetDegreeBigDecimal = new BigDecimal(String.valueOf(targetDegree)).setScale(5, BigDecimal.ROUND_HALF_DOWN);
        return sourceDegreeBigDecimal.equals(targetDegreeBigDecimal);
    }

    /**
     * 判断经纬度坐标是否相等，在进行比较前，角度将会保留 5 位小数，舍入方法使用四舍五入方式。
     *
     * @param sourceCoordinate 目标经纬度
     * @param targetCoordinate 对比经纬度
     * @return 经纬度坐标是否相等
     */
    private boolean isSameCoordinate(GeoCoordinate sourceCoordinate, GeoCoordinate targetCoordinate) {
        return isSameDegree(sourceCoordinate.getLongitude(), targetCoordinate.getLongitude()) &&
                isSameDegree(sourceCoordinate.getLatitude(), targetCoordinate.getLatitude());
    }
}
