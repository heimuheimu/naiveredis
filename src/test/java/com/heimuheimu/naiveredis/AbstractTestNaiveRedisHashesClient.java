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

import com.heimuheimu.naiveredis.util.AssertUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * {@link NaiveRedisHashesClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisHashesClient {

    public abstract NaiveRedisHashesClient getClient();

    @Test
    public void testPutToHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        String[] expectedValues = new String[]{"值 1", "", "值 3", "值 4", "值 5"};
        String[] expectedNewValues = new String[]{"新值 1", "", "新值 3", "新值 4", "新值 5"};

        // putToHashes(String key, String member, String value) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putToHashes(null, expectedMembers[0], expectedValues[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putToHashes("", expectedMembers[0], expectedValues[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putToHashes(key, null, expectedValues[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putToHashes(key, expectedMembers[0], null));

        // putToHashes(String key, String member, String value) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putToHashes` method failed: `invalid return size`.", 1, client.putToHashes(key, expectedMembers[i], expectedValues[i]));
        }
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putToHashes` method failed: `invalid value`.", expectedValues[i], client.getValueFromHashes(key, expectedMembers[i]));
        }

        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putToHashes` method failed: `invalid return size`.", 0, client.putToHashes(key, expectedMembers[i], expectedNewValues[i]));
        }
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putToHashes` method failed: `invalid value`.", expectedNewValues[i], client.getValueFromHashes(key, expectedMembers[i]));
        }
        client.delete(key);
    }

    @Test
    public void testMultiPutToHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, String> expectedMembers = new HashMap<>();
        expectedMembers.put("", "值 1");
        expectedMembers.put("成员 2", "");
        expectedMembers.put("成员 3", "值 3");
        expectedMembers.put("成员 4", "值 4");
        expectedMembers.put("成员 5", "值 5");

        // putToHashes(String key, Map<String, String> memberMap) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putToHashes(null, expectedMembers));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putToHashes("", expectedMembers));
        Map<String, String> invalidMembers = new HashMap<>(expectedMembers);
        invalidMembers.put(null, "空值");
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putToHashes(key, invalidMembers));

        // putToHashes(String key, Map<String, String> memberMap) 功能测试
        client.putToHashes(key, null);
        Assert.assertEquals("Test `putToHashes` method failed: `invalid hashes size`.", 0, client.getSizeOfHashes(key));

        client.putToHashes(key, new HashMap<>());
        Assert.assertEquals("Test `putToHashes` method failed: `invalid hashes size`.", 0, client.getSizeOfHashes(key));

        client.putToHashes(key, expectedMembers);
        Assert.assertEquals("Test `putToHashes` method failed: `invalid hashes size`.", expectedMembers.size(), client.getSizeOfHashes(key));
        client.delete(key);
    }

    @Test
    public void testPutIfAbsentToHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        String[] expectedValues = new String[]{"值 1", "", "值 3", "值 4", "值 5"};
        String[] expectedNewValues = new String[]{"新值 1", "", "新值 3", "新值 4", "新值 5"};

        // putIfAbsentToHashes(String key, String member, String value) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putIfAbsentToHashes(null, expectedMembers[0], expectedValues[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putIfAbsentToHashes("", expectedMembers[0], expectedValues[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putIfAbsentToHashes(key, null, expectedValues[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.putIfAbsentToHashes(key, expectedMembers[0], null));

        // putIfAbsentToHashes(String key, String member, String value) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putIfAbsentToHashes` method failed: `invalid return size`.", 1, client.putIfAbsentToHashes(key, expectedMembers[i], expectedValues[i]));
        }
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putIfAbsentToHashes` method failed: `invalid value`.", expectedValues[i], client.getValueFromHashes(key, expectedMembers[i]));
        }

        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putIfAbsentToHashes` method failed: `invalid return size`.", 0, client.putIfAbsentToHashes(key, expectedMembers[i], expectedNewValues[i]));
        }
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `putIfAbsentToHashes` method failed: `invalid value`.", expectedValues[i], client.getValueFromHashes(key, expectedMembers[i]));
        }
        client.delete(key);
    }

    @Test
    public void testIncrForHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        long increment = 7;
        int count = 10;

        // incrForHashes(String key, String member, long increment) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrForHashes(null, expectedMembers[0], increment));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrForHashes("", expectedMembers[0], increment));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrForHashes(key, null, increment));
        client.putToHashes(key, expectedMembers[0], "invalid value");
        AssertUtil.assertThrowRedisException(() -> client.incrForHashes(key, expectedMembers[0], increment));

        // incrForHashes(String key, String member, long increment) 功能测试
        client.delete(key);
        for (int i = 0; i < count; i++) {
            for (String expectedMember : expectedMembers) {
                Assert.assertEquals("Test `incrForHashes` method failed: `invalid value`.", (i + 1) * increment,
                        client.incrForHashes(key, expectedMember, increment));
            }
        }

        for (String expectedMember : expectedMembers) {
            Assert.assertEquals("Test `incrForHashes` method failed: `invalid value`.", increment * count,
                    Integer.parseInt(client.getValueFromHashes(key, expectedMember)));
        }
        client.delete(key);
    }

    @Test
    public void testIncrByFloatForHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        double increment = 0.7;
        int count = 10;

        // incrByFloatForHashes(String key, String member, double increment) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrByFloatForHashes(null, expectedMembers[0], increment));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrByFloatForHashes("", expectedMembers[0], increment));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrByFloatForHashes(key, null, increment));
        client.putToHashes(key, expectedMembers[0], "invalid value");
        AssertUtil.assertThrowRedisException(() -> client.incrByFloatForHashes(key, expectedMembers[0], increment));

        // incrByFloatForHashes(String key, String member, double increment) 功能测试
        client.delete(key);
        for (int i = 0; i < count; i++) {
            for (String expectedMember : expectedMembers) {
                Assert.assertEquals("Test `incrByFloatForHashes` method failed: `invalid value`.", (i + 1) * increment,
                        client.incrByFloatForHashes(key, expectedMember, increment), 0.001);
            }
        }

        for (String expectedMember : expectedMembers) {
            Assert.assertEquals("Test `incrByFloatForHashes` method failed: `invalid value`.", increment * count,
                    Double.parseDouble(client.getValueFromHashes(key, expectedMember)), 0.001);
        }
        client.delete(key);
    }

    @Test
    public void testRemoveFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        String[] expectedValues = new String[]{"值 1", "", "值 3", "值 4", "值 5"};

        // removeFromHashes(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromHashes(null, expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromHashes("", expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromHashes(key, (String) null));

        // removeFromHashes(String key, String member) 功能测试
        Assert.assertEquals("Test `removeFromHashes` method failed: `invalid return size`.", 0, client.removeFromHashes(key, expectedMembers[0]));
        for (int i = 0; i < expectedMembers.length; i++) {
            client.putToHashes(key, expectedMembers[i], expectedValues[i]);
        }
        for (String expectedMember : expectedMembers) {
            Assert.assertEquals("Test `removeFromHashes` method failed: `invalid return size`.", 1, client.removeFromHashes(key, expectedMember));
            Assert.assertFalse("Test `removeFromHashes` method failed: `invalid member`.", client.isExistInHashes(key, expectedMember));
            Assert.assertEquals("Test `removeFromHashes` method failed: `invalid return size`.", 0, client.removeFromHashes(key, expectedMember));
            Assert.assertFalse("Test `removeFromHashes` method failed: `invalid member`.", client.isExistInHashes(key, expectedMember));
        }
        client.delete(key);
    }

    @Test
    public void testMultiRemoveFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, String> expectedMembers = new HashMap<>();
        expectedMembers.put("", "值 1");
        expectedMembers.put("成员 2", "");
        expectedMembers.put("成员 3", "值 3");
        expectedMembers.put("成员 4", "值 4");
        expectedMembers.put("成员 5", "值 5");

        // removeFromHashes(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromHashes(null, expectedMembers.keySet()));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromHashes("", expectedMembers.keySet()));
        List<String> invalidMembers = new ArrayList<>(expectedMembers.keySet());
        invalidMembers.add(null);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromHashes("", invalidMembers));

        // removeFromHashes(String key, Collection<String> members) 功能测试
        Assert.assertEquals("Test `removeFromHashes` method failed: `invalid return size`.", 0, client.removeFromHashes(key, (Collection<String>) null));
        Assert.assertEquals("Test `removeFromHashes` method failed: `invalid return size`.", 0, client.removeFromHashes(key, new ArrayList<>()));
        Assert.assertEquals("Test `removeFromHashes` method failed: `invalid return size`.", 0, client.removeFromHashes(key, expectedMembers.keySet()));

        client.putToHashes(key, expectedMembers);
        Assert.assertEquals("Test `removeFromHashes` method failed: `invalid return size`.", expectedMembers.size(), client.removeFromHashes(key, expectedMembers.keySet()));
        client.delete(key);
    }

    @Test
    public void testIsExistInHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String notExistMember = "不存在的成员 1";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        String[] expectedValues = new String[]{"值 1", "", "值 3", "值 4", "值 5"};

        // isExistInHashes(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.isExistInHashes(null, expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.isExistInHashes("", expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.isExistInHashes(key, null));

        // isExistInHashes(String key, String member) 功能测试
        Assert.assertFalse("Test `isExistInHashes` method failed: `invalid return value`.", client.isExistInHashes(key, expectedMembers[0]));
        for (int i = 0; i < expectedMembers.length; i++) {
            client.putToHashes(key, expectedMembers[i], expectedValues[i]);
        }
        for (int i = 0; i < expectedMembers.length; i++) {
            client.putToHashes(key, expectedMembers[i], expectedValues[i]);
        }
        for (String expectedMember : expectedMembers) {
            Assert.assertTrue("Test `isExistInHashes` method failed: `invalid return value`.", client.isExistInHashes(key, expectedMember));
        }
        Assert.assertFalse("Test `isExistInHashes` method failed: `invalid return value`.", client.isExistInHashes(key, notExistMember));
        client.delete(key);
    }

    @Test
    public void testGetSizeOfHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, String> expectedMembers = new HashMap<>();
        expectedMembers.put("", "值 1");
        expectedMembers.put("成员 2", "");
        expectedMembers.put("成员 3", "值 3");
        expectedMembers.put("成员 4", "值 4");
        expectedMembers.put("成员 5", "值 5");

        // getSizeOfHashes(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfHashes(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfHashes(""));

        // getSizeOfHashes(String key) 功能测试
        Assert.assertEquals("Test `getSizeOfHashes` method failed: `invalid hashes size`.", 0, client.getSizeOfHashes(key));
        client.putToHashes(key, expectedMembers);
        Assert.assertEquals("Test `getSizeOfHashes` method failed: `invalid hashes size`.", expectedMembers.size(), client.getSizeOfHashes(key));
        client.delete(key);
    }

    @Test
    public void testGetValueFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        String[] expectedValues = new String[]{"值 1", "", "值 3", "值 4", "值 5"};

        // getValueFromHashes(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValueFromHashes(null, expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValueFromHashes("", expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValueFromHashes(key, null));

        // getValueFromHashes(String key, String member) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertNull("Test `getValueFromHashes` method failed: `invalid value`.", client.getValueFromHashes(key, expectedMembers[i]));
        }
        for (int i = 0; i < expectedMembers.length; i++) {
            client.putToHashes(key, expectedMembers[i], expectedValues[i]);
        }

        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `getValueFromHashes` method failed: `invalid value`.", expectedValues[i],
                    client.getValueFromHashes(key, expectedMembers[i]));
        }
        client.delete(key);
    }

    @Test
    public void testGetValueLengthFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[]{"", "成员 2", "成员 3", "成员 4", "成员 5"};
        String[] expectedValues = new String[]{"", "值 2", "值 33", "值 444", "值 5555"};

        // getValueLengthFromHashes(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValueLengthFromHashes(null, expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValueLengthFromHashes("", expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValueLengthFromHashes(key, null));

        // getValueLengthFromHashes(String key, String member) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `getValueLengthFromHashes` method failed: `invalid value length`.", 0,
                    client.getValueLengthFromHashes(key, expectedMembers[i]));
        }

        for (int i = 0; i < expectedMembers.length; i++) {
            client.putToHashes(key, expectedMembers[i], expectedValues[i]);
        }

        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `getValueLengthFromHashes` method failed: `invalid value length [" + expectedValues[i] + "]`.",
                    expectedValues[i].getBytes(StandardCharsets.UTF_8).length, client.getValueLengthFromHashes(key, expectedMembers[i]));
        }
        client.delete(key);
    }

    @Test
    public void testGetMemberMapFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String notExistMember = "不存在的成员 1";
        Map<String, String> expectedMembers = new HashMap<>();
        expectedMembers.put("", "值 1");
        expectedMembers.put("成员 2", "");
        expectedMembers.put("成员 3", "值 3");
        expectedMembers.put("成员 4", "值 4");
        expectedMembers.put("成员 5", "值 5");

        // getMemberMapFromHashes(String key, List<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMemberMapFromHashes(null, new ArrayList<>(expectedMembers.keySet())));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMemberMapFromHashes("", new ArrayList<>(expectedMembers.keySet())));
        List<String> invalidMemberList = new ArrayList<>(expectedMembers.keySet());
        invalidMemberList.add(null);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMemberMapFromHashes(key, invalidMemberList));

        Assert.assertEquals("Test `getMemberMapFromHashes` method failed: `invalid map size`.", 0,
                client.getMemberMapFromHashes(key, new ArrayList<>(expectedMembers.keySet())).size());
        Assert.assertEquals("Test `getMemberMapFromHashes` method failed: `invalid map size`.", 0,
                client.getMemberMapFromHashes(key, null).size());
        Assert.assertEquals("Test `getMemberMapFromHashes` method failed: `invalid map size`.", 0,
                client.getMemberMapFromHashes(key, new ArrayList<>()).size());

        client.putToHashes(key, expectedMembers);
        List<String> members = new ArrayList<>(expectedMembers.keySet());
        members.add(notExistMember);
        Map<String, String> actualMembers = client.getMemberMapFromHashes(key, members);

        Assert.assertEquals("Test `getMemberMapFromHashes` method failed: `invalid map size`.", expectedMembers.size(),
                actualMembers.size());
        for (String expectedMember : expectedMembers.keySet()) {
            Assert.assertEquals("Test `getMemberMapFromHashes` method failed: `invalid value`.", expectedMembers.get(expectedMember),
                    actualMembers.get(expectedMember));
        }
        client.delete(key);
    }

    @Test
    public void testGetAllFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, String> expectedMembers = new HashMap<>();
        expectedMembers.put("", "值 1");
        expectedMembers.put("成员 2", "");
        expectedMembers.put("成员 3", "值 3");
        expectedMembers.put("成员 4", "值 4");
        expectedMembers.put("成员 5", "值 5");

        // getAllFromHashes(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getAllFromHashes(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getAllFromHashes(""));

        // getAllFromHashes(String key) 功能测试
        Assert.assertEquals("Test `getAllFromHashes` method failed: `invalid map size`.", 0,
                client.getAllFromHashes(key).size());
        client.putToHashes(key, expectedMembers);
        Map<String, String> actualMembers = client.getAllFromHashes(key);
        Assert.assertEquals("Test `getAllFromHashes` method failed: `invalid map size`.", expectedMembers.size(),
                actualMembers.size());
        for (String expectedMember : expectedMembers.keySet()) {
            Assert.assertEquals("Test `getAllFromHashes` method failed: `invalid value`.", expectedMembers.get(expectedMember),
                    actualMembers.get(expectedMember));
        }
        client.delete(key);
    }

    @Test
    public void testGetMembersFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, String> expectedMembers = new HashMap<>();
        expectedMembers.put("", "值 1");
        expectedMembers.put("成员 2", "");
        expectedMembers.put("成员 3", "值 3");
        expectedMembers.put("成员 4", "值 4");
        expectedMembers.put("成员 5", "值 5");

        // getMembersFromHashes(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersFromHashes(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersFromHashes(""));

        // getMembersFromHashes(String key) 功能测试
        Assert.assertEquals("Test `getMembersFromHashes` method failed: `invalid list size`.", 0,
                client.getMembersFromHashes(key).size());
        client.putToHashes(key, expectedMembers);

        List<String> actualMembers = client.getMembersFromHashes(key);
        Assert.assertEquals("Test `getMembersFromHashes` method failed: `invalid list size`.", expectedMembers.size(),
                actualMembers.size());

        for (String member : actualMembers) {
            Assert.assertTrue("Test `getMembersFromHashes` method failed: `invalid member[" + member + "]`.",
                    expectedMembers.containsKey(member));
        }
        client.delete(key);
    }

    public void testGetValuesFromHashes() {
        NaiveRedisHashesClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        Map<String, String> expectedMembers = new HashMap<>();
        expectedMembers.put("", "值 1");
        expectedMembers.put("成员 2", "");
        expectedMembers.put("成员 3", "值 3");
        expectedMembers.put("成员 4", "值 4");
        expectedMembers.put("成员 5", "值 5");

        // getValuesFromHashes(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValuesFromHashes(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getValuesFromHashes(""));

        // getValuesFromHashes(String key) 功能测试
        Assert.assertEquals("Test `getValuesFromHashes` method failed: `invalid list size`.", 0,
                client.getValuesFromHashes(key).size());
        client.putToHashes(key, expectedMembers);

        Collection<String> expectedValues = expectedMembers.values();
        List<String> actualValues = client.getValuesFromHashes(key);
        Assert.assertEquals("Test `getValuesFromHashes` method failed: `invalid list size`.", expectedValues.size(),
                actualValues.size());

        for (String actualValue : actualValues) {
            Assert.assertTrue("Test `getValuesFromHashes` method failed: `invalid value[" + actualValue + "]`.",
                    expectedValues.contains(actualValue));
        }
        client.delete(key);
    }
}
