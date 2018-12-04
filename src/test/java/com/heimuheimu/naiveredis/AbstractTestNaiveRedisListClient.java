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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link NaiveRedisListClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisListClient {

    public abstract NaiveRedisListClient getClient();

    @Test
    public void testAddFirstToList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // addFirstToList(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList(null, ""));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList("", ""));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList(key, (String) null));

        // addFirstToList(String key, String member) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", i + 1,
                    client.addFirstToList(key, expectedMembers[i]));
        }

        List<String> actualMembers = client.getMembersFromList(key, 0, -1);
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addFirstToList` method failed: `invalid member`.",
                    expectedMembers[i], actualMembers.get(actualMembers.size() - 1 - i));
        }

        // addFirstToList(String key, String member, boolean isAutoCreate) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList(null, "", true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList("", "", true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList(key,  null, true));

        // addFirstToList(String key, String member, boolean isAutoCreate) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", 0,
                    client.addFirstToList(key, expectedMembers[i], false));
        }

        actualMembers = client.getMembersFromList(key, 0, -1);
        Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", 0, actualMembers.size());

        Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", 1,
                client.addFirstToList(key, expectedMembers[0], true));
        for (int i = 1; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", i + 1,
                    client.addFirstToList(key, expectedMembers[i], false));
        }

        actualMembers = client.getMembersFromList(key, 0, -1);
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addFirstToList` method failed: `invalid member`.",
                    expectedMembers[i], actualMembers.get(actualMembers.size() - 1 - i));
        }

        // addFirstToList(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList(null, Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList("", Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addFirstToList(key, Arrays.asList(expectedMembers[0], expectedMembers[1], null, expectedMembers[2])));

        // addFirstToList(String key, Collection<String> members) 功能测试
        Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", 0,
                client.addFirstToList(key, (Collection<String>) null));
        Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", 0,
                client.addFirstToList(key, Collections.emptyList()));

        Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", expectedMembers.length,
                client.addFirstToList(key, Arrays.asList(expectedMembers)));

        Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", 0,
                client.addFirstToList(key, (Collection<String>) null));
        Assert.assertEquals("Test `addFirstToList` method failed: `invalid list size`.", 0,
                client.addFirstToList(key, Collections.emptyList()));

        actualMembers = client.getMembersFromList(key, 0, -1);
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addFirstToList` method failed: `invalid member`.",
                    expectedMembers[i], actualMembers.get(actualMembers.size() - 1 - i));
        }
        client.delete(key);
    }

    @Test
    public void testAddLastToList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // addLastToList(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList(null, ""));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList("", ""));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList(key, (String) null));

        // addLastToList(String key, String member) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", i + 1,
                    client.addLastToList(key, expectedMembers[i]));
        }

        List<String> actualMembers = client.getMembersFromList(key, 0, -1);
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addLastToList` method failed: `invalid member`.",
                    expectedMembers[i], actualMembers.get(i));
        }

        // addLastToList(String key, String member, boolean isAutoCreate) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList(null, "", true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList("", "", true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList(key,  null, true));

        // addLastToList(String key, String member, boolean isAutoCreate) 功能测试
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", 0,
                    client.addLastToList(key, expectedMembers[i], false));
        }

        actualMembers = client.getMembersFromList(key, 0, -1);
        Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", 0, actualMembers.size());

        Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", 1,
                client.addLastToList(key, expectedMembers[0], true));
        for (int i = 1; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", i + 1,
                    client.addLastToList(key, expectedMembers[i], false));
        }

        actualMembers = client.getMembersFromList(key, 0, -1);
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addLastToList` method failed: `invalid member`.",
                    expectedMembers[i], actualMembers.get(i));
        }

        // addLastToList(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList(null, Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList("", Arrays.asList(expectedMembers)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addLastToList(key, Arrays.asList(expectedMembers[0], expectedMembers[1], null, expectedMembers[2])));

        // addLastToList(String key, Collection<String> members) 功能测试
        Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", 0,
                client.addLastToList(key, (Collection<String>) null));
        Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", 0,
                client.addLastToList(key, Collections.emptyList()));

        Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", expectedMembers.length,
                client.addLastToList(key, Arrays.asList(expectedMembers)));

        Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", 0,
                client.addLastToList(key, (Collection<String>) null));
        Assert.assertEquals("Test `addLastToList` method failed: `invalid list size`.", 0,
                client.addLastToList(key, Collections.emptyList()));

        actualMembers = client.getMembersFromList(key, 0, -1);
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `addLastToList` method failed: `invalid member`.",
                    expectedMembers[i], actualMembers.get(i));
        }
        client.delete(key);
    }

    @Test
    public void testPopFirstFromList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // popFirstFromList(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popFirstFromList(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popFirstFromList(""));

        // popFirstFromList(String key) 功能测试
        Assert.assertEquals("Test `popFirstFromList` method failed: `invalid member`.", null, client.popFirstFromList(key));

        client.addLastToList(key, Arrays.asList(expectedMembers));
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `popFirstFromList` method failed: `invalid member`.", expectedMembers[i], client.popFirstFromList(key));
        }

        Assert.assertEquals("Test `popFirstFromList` method failed: `invalid member`.", null, client.popFirstFromList(key));
        client.delete(key);
    }

    @Test
    public void testPopLastFromList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // popLastFromList(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popLastFromList(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.popLastFromList(""));

        // popLastFromList(String key) 功能测试
        Assert.assertEquals("Test `popLastFromList` method failed: `invalid member`.", null, client.popLastFromList(key));

        client.addFirstToList(key, Arrays.asList(expectedMembers));
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `popLastFromList` method failed: `invalid member`.", expectedMembers[i], client.popLastFromList(key));
        }

        Assert.assertEquals("Test `popLastFromList` method failed: `invalid member`.", null, client.popLastFromList(key));
        client.delete(key);
    }

    @Test
    public void testInsertIntoList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String notExistPivotalMember = "不存在的关键成员";
        int pivotalMemberIndex = 1;
        String pivotalMember = "关键成员";
        String insertBeforeMember = "前插入成员";
        String insertAfterMember = "后插入成员";
        String[] expectedMembers = new String[] {"", pivotalMember, pivotalMember, "尾部成员"};

        // insertIntoList(String key, String pivotalMember, String member, boolean isAfter) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.insertIntoList(null, pivotalMember, insertAfterMember, true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.insertIntoList("", pivotalMember, insertAfterMember, true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.insertIntoList(key, null, insertAfterMember, true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.insertIntoList(key, pivotalMember, null, true));

        // insertIntoList(String key, String pivotalMember, String member, boolean isAfter) 功能测试
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid return value`.", 0,
                client.insertIntoList(key, pivotalMember, insertAfterMember, true));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid list size`.", 0, client.getSizeOfList(key));

        client.addLastToList(key, Arrays.asList(expectedMembers));

        Assert.assertEquals("Test `insertIntoList` method failed: `invalid return value`.", -1,
                client.insertIntoList(key, notExistPivotalMember, insertAfterMember, true));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid list size`.", expectedMembers.length, client.getSizeOfList(key));

        Assert.assertEquals("Test `insertIntoList` method failed: `invalid return value`.", expectedMembers.length + 1,
                client.insertIntoList(key, pivotalMember, insertAfterMember, true));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid return value`.", expectedMembers.length + 2,
                client.insertIntoList(key, pivotalMember, insertAfterMember, true));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid member`.", insertAfterMember, client.getByIndexFromList(key, pivotalMemberIndex + 1));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid member`.", insertAfterMember, client.getByIndexFromList(key, pivotalMemberIndex + 2));

        Assert.assertEquals("Test `insertIntoList` method failed: `invalid return value`.", expectedMembers.length + 3,
                client.insertIntoList(key, pivotalMember, insertBeforeMember, false));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid return value`.", expectedMembers.length + 4,
                client.insertIntoList(key, pivotalMember, insertBeforeMember, false));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid member`.", insertBeforeMember, client.getByIndexFromList(key, pivotalMemberIndex));
        Assert.assertEquals("Test `insertIntoList` method failed: `invalid member`.", insertBeforeMember, client.getByIndexFromList(key, pivotalMemberIndex + 1));
        client.delete(key);
    }

    @Test
    public void testSetToList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String replaceMember = "替换的成员";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // setToList(String key, int index, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setToList(null, 0, replaceMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setToList("", 0, replaceMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setToList(key, 0, null));
        AssertUtil.assertThrowRedisException(() -> client.setToList(key, 0, replaceMember));

        // setToList(String key, int index, String member) 功能测试
        client.addLastToList(key, Arrays.asList(expectedMembers));
        AssertUtil.assertThrowRedisException(() -> client.setToList(key, expectedMembers.length, replaceMember));
        client.setToList(key, 0, replaceMember);
        Assert.assertEquals("Test `setToList` method failed: `invalid list size`.", expectedMembers.length, client.getSizeOfList(key));
        Assert.assertEquals("Test `setToList` method failed: `invalid member`.", replaceMember, client.getByIndexFromList(key, 0));

        client.setToList(key, -1, replaceMember);
        Assert.assertEquals("Test `setToList` method failed: `invalid list size`.", expectedMembers.length, client.getSizeOfList(key));
        Assert.assertEquals("Test `setToList` method failed: `invalid member`.", replaceMember, client.getByIndexFromList(key, expectedMembers.length - 1));
        client.delete(key);
    }

    @Test
    public void testRemoveFromList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        int removeMemberCount = 2;
        String removeMember = "移除的成员";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", removeMember, "成员 3", removeMember, "成员 4", "成员 5"};

        // removeFromList(String key, int count, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromList(null, 0, removeMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromList("", 0, removeMember));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromList(key, 0, null));

        // removeFromList(String key, int count, String member) 功能测试
        Assert.assertEquals("Test `setToList` method failed: `invalid return value`.", 0, client.removeFromList(key, 0, removeMember));

        client.addLastToList(key, Arrays.asList(expectedMembers));
        Assert.assertEquals("Test `setToList` method failed: `invalid return value`.", 1, client.removeFromList(key, 1, removeMember));

        client.delete(key);
        client.addLastToList(key, Arrays.asList(expectedMembers));
        Assert.assertEquals("Test `setToList` method failed: `invalid return value`.", 1, client.removeFromList(key, -1, removeMember));

        client.delete(key);
        client.addLastToList(key, Arrays.asList(expectedMembers));
        Assert.assertEquals("Test `setToList` method failed: `invalid return value`.", removeMemberCount, client.removeFromList(key, 0, removeMember));
        client.delete(key);
    }

    @Test
    public void testTrimList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // trimList(String key, int startIndex, int endIndex) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.trimList(null, 0, -1));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.trimList("", 0, -1));

        // trimList(String key, int startIndex, int endIndex) 功能测试
        client.trimList(key, 0, -1);

        client.addLastToList(key, Arrays.asList(expectedMembers));
        client.trimList(key, 1, expectedMembers.length - 2);
        List<String> actualMembers = client.getMembersFromList(key, 0 ,-1);
        Assert.assertEquals("Test `trimList` method failed: `invalid list size`.", expectedMembers.length - 2, actualMembers.size());
        for (int i = 1; i < expectedMembers.length - 1; i++) {
            Assert.assertEquals("Test `trimList` method failed: `invalid member`.", expectedMembers[i], client.popFirstFromList(key));
        }

        client.delete(key);
        client.addLastToList(key, Arrays.asList(expectedMembers));
        client.trimList(key, expectedMembers.length, -1);
        actualMembers = client.getMembersFromList(key, 0 ,-1);
        Assert.assertEquals("Test `trimList` method failed: `invalid list size`.", 0, actualMembers.size());

        client.delete(key);
        client.addLastToList(key, Arrays.asList(expectedMembers));
        client.trimList(key, 0, Integer.MAX_VALUE);
        actualMembers = client.getMembersFromList(key, 0 ,-1);
        Assert.assertEquals("Test `trimList` method failed: `invalid list size`.", expectedMembers.length, actualMembers.size());
        client.delete(key);
    }

    @Test
    public void testGetSizeOfList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // getSizeOfList(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfList(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfList(""));

        // getSizeOfList(String key) 功能测试
        Assert.assertEquals("Test `getSizeOfList` method failed: `invalid list size`.", 0, client.getSizeOfList(key));
        client.addLastToList(key, Arrays.asList(expectedMembers));
        Assert.assertEquals("Test `getSizeOfList` method failed: `invalid list size`.", expectedMembers.length, client.getSizeOfList(key));
        client.delete(key);
    }

    @Test
    public void testGetByIndexFromList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // getByIndexFromList(String key, int index) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getByIndexFromList(null, 0));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getByIndexFromList("", 0));

        // getByIndexFromList(String key, int index) 功能测试
        Assert.assertNull("Test `getByIndexFromList` method failed: `invalid member`.", client.getByIndexFromList(key, 0));
        client.addLastToList(key, Arrays.asList(expectedMembers));
        for (int i = 0; i < expectedMembers.length; i++) {
            Assert.assertEquals("Test `getByIndexFromList` method failed: `invalid member`.", expectedMembers[i],
                    client.getByIndexFromList(key, i));
        }
        Assert.assertNull("Test `getByIndexFromList` method failed: `invalid member`.", client.getByIndexFromList(key, expectedMembers.length));
        client.delete(key);
    }

    @Test
    public void testGetMembersFromList() {
        NaiveRedisListClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        String[] expectedMembers = new String[] {"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // getMembersFromList(String key, int startIndex, int endIndex) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersFromList(null, 0, -1));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersFromList("", 0, -1));

        // getMembersFromList(String key, int startIndex, int endIndex) 功能测试
        Assert.assertTrue("Test `getMembersFromList` method failed: `invalid list size`.", client.getMembersFromList(key, 0, -1).isEmpty());
        client.addLastToList(key, Arrays.asList(expectedMembers));
        List<String> actualMembers =  client.getMembersFromList(key, 1, expectedMembers.length - 2);
        Assert.assertEquals("Test `getMembersFromList` method failed: `invalid list size`.", expectedMembers.length - 2, actualMembers.size());
        for (int i = 1; i < expectedMembers.length - 1; i++) {
            Assert.assertEquals("Test `getMembersFromList` method failed: `invalid member`.", expectedMembers[i], actualMembers.get(i - 1));
        }

        actualMembers = client.getMembersFromList(key, expectedMembers.length, -1);
        Assert.assertEquals("Test `getMembersFromList` method failed: `invalid list size`.", 0, actualMembers.size());

        actualMembers = client.getMembersFromList(key, 0, Integer.MAX_VALUE);
        Assert.assertEquals("Test `getMembersFromList` method failed: `invalid list size`.", expectedMembers.length, actualMembers.size());
        client.delete(key);
    }
}
