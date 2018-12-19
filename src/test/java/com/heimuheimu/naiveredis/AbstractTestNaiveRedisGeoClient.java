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
import com.heimuheimu.naiveredis.util.AssertUtil;
import org.junit.Assert;
import org.junit.Test;

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
                Math.round(coordinate.getLongitude()) == 50 && Math.round(coordinate.getLatitude()) == 50);

        Assert.assertEquals("Test `addGeoCoordinate` method failed: `invalid return value`.", 0,
                client.addGeoCoordinate(key, 60, 60, member));
        coordinate = client.getGeoCoordinate(key, member);
        Assert.assertTrue("Test `addGeoCoordinate` method failed: `invalid coordinate`.",
                Math.round(coordinate.getLongitude()) == 60 && Math.round(coordinate.getLatitude()) == 60);
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
                    Math.round(expectedGeoCoordinate.getLongitude()) == Math.round(actualGeoCoordinate.getLongitude())
                            && Math.round(expectedGeoCoordinate.getLatitude()) == Math.round(actualGeoCoordinate.getLatitude()));
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
                    Math.round(expectedGeoCoordinate.getLongitude()) == Math.round(actualGeoCoordinate.getLongitude())
                            && Math.round(expectedGeoCoordinate.getLatitude()) == Math.round(actualGeoCoordinate.getLatitude()));
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
                    Math.round(expectedGeoCoordinate.getLongitude()) == Math.round(actualGeoCoordinate.getLongitude())
                            && Math.round(expectedGeoCoordinate.getLatitude()) == Math.round(actualGeoCoordinate.getLatitude()));
        }
        client.delete(key);
    }
}
