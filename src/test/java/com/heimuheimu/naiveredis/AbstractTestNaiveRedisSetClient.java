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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * {@link NaiveRedisSetClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisSetClient {

    public abstract NaiveRedisSetClient getClient();

    @Test
    public void testAddToSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // addToSet(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSet(null, expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSet("", expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSet(key, (String) null));

        // addToSet(String key, String member) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addToSet` method failed: `invalid return size`.", 1,
                    client.addToSet(key, expectedMembers[i]));
            Assert.assertEquals("Test `addToSet` method failed: `invalid return size`.", 0,
                    client.addToSet(key, expectedMembers[i]));
        }

        Assert.assertEquals("Test `addToSet` method failed: `invalid set size`.", expectedMembers.length,
                client.getSizeOfSet(key));
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertTrue("Test `addToSet` method failed: `invalid member`.", client.isMemberInSet(key, expectedMembers[i]));
        }
        client.delete(key);
    }

    @Test
    public void testMultiAddToSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // addToSet(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSet(null, Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSet("", Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSet(key, Arrays.asList(expectedMembers[0], expectedMembers[1], null, expectedMembers[2])));

        // addToSet(String key, Collection<String> members) 功能测试
        Assert.assertEquals("Test `addToSet` method failed: `invalid return size`.", expectedMembers.length,
                client.addToSet(key, Arrays.asList(expectedMembers)));

        Assert.assertEquals("Test `addToSet` method failed: `invalid return size`.", 0,
                client.addToSet(key, (Collection<String>) null));
        Assert.assertEquals("Test `addToSet` method failed: `invalid return size`.", 0,
                client.addToSet(key, new ArrayList<>()));

        Assert.assertEquals("Test `addToSet` method failed: `invalid return size`.", 0,
                client.addToSet(key, Arrays.asList(expectedMembers)));
        Assert.assertEquals("Test `addToSet` method failed: `invalid set size`.", expectedMembers.length,
                client.getSizeOfSet(key));
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertTrue("Test `addToSet` method failed: `invalid member`.", client.isMemberInSet(key, expectedMembers[i]));
        }
        client.delete(key);
    }

    @Test
    public void testRemoveFromSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String notExistMember = "不存在的成员";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // removeFromSet(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSet(null, expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSet("", expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSet(key, (String) null));

        // removeFromSet(String key, String member) 参数异常测试
        Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", 0,
                client.removeFromSet(key, expectedMembers[0]));
        client.addToSet(key, Arrays.asList(expectedMembers));
        Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", 0,
                client.removeFromSet(key, notExistMember));
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", 1,
                    client.removeFromSet(key, expectedMembers[i]));
        }
        Assert.assertEquals("Test `removeFromSet` method failed: `invalid set size`.", 0,
                client.getSizeOfSet(key));
        client.delete(key);
    }

    @Test
    public void testMultiRemoveFromSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String[] notExistMembers = new String[]{"不存在的成员 1", "不存在的成员 2", "不存在的成员 3"};
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // removeFromSet(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSet(null, Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSet("", Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSet(key, Arrays.asList(expectedMembers[0], expectedMembers[1], null, expectedMembers[2])));

        // removeFromSet(String key, Collection<String> members) 功能测试
        Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", 0,
                client.removeFromSet(key, Arrays.asList(expectedMembers)));

        client.addToSet(key, Arrays.asList(expectedMembers));

        Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", 0,
                client.removeFromSet(key, Arrays.asList(notExistMembers)));
        Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", 0,
                client.removeFromSet(key, (Collection<String>) null));
        Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", 0,
                client.removeFromSet(key, new ArrayList<>()));

        Assert.assertEquals("Test `removeFromSet` method failed: `invalid return size`.", expectedMembers.length,
                client.removeFromSet(key, Arrays.asList(expectedMembers)));
        Assert.assertEquals("Test `removeFromSet` method failed: `invalid set size`.", 0,
                client.getSizeOfSet(key));
        client.delete(key);
    }

    @Test
    public void testIsMemberInSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String notExistMember = "不存在的成员";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // isMemberInSet(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.isMemberInSet(null, expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.isMemberInSet("", expectedMembers[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.isMemberInSet(key, null));

        // isMemberInSet(String key, String member) 功能测试
        Assert.assertFalse("Test `isMemberInSet` method failed: `invalid return value`.", client.isMemberInSet(key, expectedMembers[0]));

        client.addToSet(key, Arrays.asList(expectedMembers));
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertTrue("Test `isMemberInSet` method failed: `invalid return value`.",
                    client.isMemberInSet(key, expectedMembers[i]));
        }

        Assert.assertFalse("Test `isMemberInSet` method failed: `invalid return value`.", client.isMemberInSet(key, notExistMember));
        client.delete(key);
    }

    @Test
    public void testGetSizeOfSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // getSizeOfSet(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfSet(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfSet(""));

        // getSizeOfSet(String key) 功能测试
        Assert.assertEquals("Test `getSizeOfSet` method failed: `invalid return size`.", 0,
                client.getSizeOfSet(key));
        client.addToSet(key, Arrays.asList(expectedMembers));
        Assert.assertEquals("Test `getSizeOfSet` method failed: `invalid return size`.", expectedMembers.length,
                client.getSizeOfSet(key));
        client.delete(key);
    }

    @Test
    public void testGetMembersFromSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // getMembersFromSet(String key, int count) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersFromSet(null, 1));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersFromSet("", 1));

        // getMembersFromSet(String key, int count) 功能测试
        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", 0,
                client.getMembersFromSet(key, 1).size());

        client.addToSet(key, Arrays.asList(expectedMembers));

        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", 0,
                client.getMembersFromSet(key, 0).size());

        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", 2,
                client.getMembersFromSet(key, 2).size());
        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", expectedMembers.length,
                client.getMembersFromSet(key, expectedMembers.length).size());
        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", expectedMembers.length,
                client.getMembersFromSet(key, expectedMembers.length * 2).size());

        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", 2,
                client.getMembersFromSet(key, -2).size());
        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", expectedMembers.length,
                client.getMembersFromSet(key, -expectedMembers.length).size());
        Assert.assertEquals("Test `getMembersFromSet` method failed: `invalid return size`.", expectedMembers.length * 2,
                client.getMembersFromSet(key, -expectedMembers.length * 2).size());
        client.delete(key);
    }

    @Test
    public void testPopMembersFromSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // popMembersFromSet(String key, int count) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popMembersFromSet(null, 1));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popMembersFromSet("", 1));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popMembersFromSet(key, -1));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popMembersFromSet(key, 0));

        // popMembersFromSet(String key, int count) 功能测试
        Assert.assertEquals("Test `popMembersFromSet` method failed: `invalid return size`.", 0,
                client.popMembersFromSet(key, 1).size());

        client.addToSet(key, Arrays.asList(expectedMembers));

        Assert.assertEquals("Test `popMembersFromSet` method failed: `invalid return size`.", 2,
                client.popMembersFromSet(key, 2).size());
        Assert.assertEquals("Test `popMembersFromSet` method failed: `invalid set size`.", expectedMembers.length - 2,
                client.getSizeOfSet(key));

        Assert.assertEquals("Test `popMembersFromSet` method failed: `invalid return size`.", expectedMembers.length - 2,
                client.popMembersFromSet(key, expectedMembers.length).size());
        Assert.assertEquals("Test `popMembersFromSet` method failed: `invalid set size`.", 0,
                client.getSizeOfSet(key));
        client.delete(key);
    }

    @Test
    public void testGetAllMembersFromSet() {
        NaiveRedisSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // getAllMembersFromSet(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getAllMembersFromSet(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getAllMembersFromSet(""));

        Assert.assertEquals("Test `getAllMembersFromSet` method failed: `invalid return size`.", 0,
                client.getAllMembersFromSet(key).size());

        client.addToSet(key, Arrays.asList(expectedMembers));

        Assert.assertEquals("Test `getAllMembersFromSet` method failed: `invalid return size`.", expectedMembers.length,
                client.getAllMembersFromSet(key).size());
        client.delete(key);
    }
}
