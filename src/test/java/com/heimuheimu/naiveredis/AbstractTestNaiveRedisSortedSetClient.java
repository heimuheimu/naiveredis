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

import com.heimuheimu.naiveredis.constant.SortedSetAddMode;
import com.heimuheimu.naiveredis.util.AssertUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * {@link NaiveRedisSortedSetClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisSortedSetClient {

    private static final double SCORE_DELTA = 0.00001d;

    public abstract NaiveRedisSortedSetClient getClient();

    @Test
    public void testAddToSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String member = "成员 1";
        Double score = 1d;
        Double newScore = 2d;

        // addToSortedSet(String key, double score, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(null, score, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet("", score, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(key, score, null));

        // addToSortedSet(String key, double score, String member) 功能测试
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 1, client.addToSortedSet(key, score, member));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);

        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0, client.addToSortedSet(key, newScore, member));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", newScore, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        client.delete(key);
    }

    @Test
    public void testAddToSortedSetWithMode() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String member = "成员 1";
        Double score = 1d;
        Double newScore = 2d;

        // addToSortedSet(String key, double score, String member, SortedSetAddMode mode) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(null, score, member, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet("", score, member, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(key, score, null, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));

        // addToSortedSet(String key, double score, String member, SortedSetAddMode mode) 功能测试
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 1, client.addToSortedSet(key, score, member, null));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0, client.addToSortedSet(key, newScore, member, null));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", newScore, client.getScoreFromSortedSet(key, member), SCORE_DELTA);

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 1, client.addToSortedSet(key, score, member, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0, client.addToSortedSet(key, score, member, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0, client.addToSortedSet(key, newScore, member, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", newScore, client.getScoreFromSortedSet(key, member), SCORE_DELTA);

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 1, client.addToSortedSet(key, score, member, SortedSetAddMode.REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 1, client.addToSortedSet(key, newScore, member, SortedSetAddMode.REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", newScore, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0, client.addToSortedSet(key, newScore, member, SortedSetAddMode.REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", newScore, client.getScoreFromSortedSet(key, member), SCORE_DELTA);

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 1, client.addToSortedSet(key, score, member, SortedSetAddMode.ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0, client.addToSortedSet(key, newScore, member, SortedSetAddMode.ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0, client.addToSortedSet(key, score, member, SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        Assert.assertNull("Test `addToSortedSet` method failed: `invalid score.", client.getScoreFromSortedSet(key, member));
        client.addToSortedSet(key, score, member, null);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, score, member, SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", score,
                client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 1,
                client.addToSortedSet(key, newScore, member, SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", newScore,
                client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        client.delete(key);
    }

    @Test
    public void testMultiAddToSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        Map<String, Double> memberMap = new HashMap<>();
        memberMap.put("成员 1", 1d);
        memberMap.put("成员 2", 2d);
        memberMap.put("成员 3", 3d);
        memberMap.put("成员 4", 4d);
        memberMap.put("成员 5", 5d);

        Map<String, Double> updatedMemberMap = new HashMap<>();
        updatedMemberMap.put("成员 1", 10d);
        updatedMemberMap.put("成员 2", 20d);
        updatedMemberMap.put("成员 3", 30d);
        updatedMemberMap.put("成员 4", 40d);
        updatedMemberMap.put("成员 5", 50d);

        // addToSortedSet(String key, Map<String, Double> memberMap) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(null, memberMap));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet("", memberMap));
        Map<String, Double> invalidMemberMap = new HashMap<>(memberMap);
        invalidMemberMap.put(null, 100d);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(key, invalidMemberMap));

        // addToSortedSet(String key, Map<String, Double> memberMap) 功能测试
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", memberMap.size(),
                client.addToSortedSet(key, memberMap));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }

        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, updatedMemberMap));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", updatedMemberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        client.delete(key);
    }

    @Test
    public void testMultiAddToSortedSetWithMode() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        Map<String, Double> memberMap = new HashMap<>();
        memberMap.put("成员 1", 1d);
        memberMap.put("成员 2", 2d);
        memberMap.put("成员 3", 3d);
        memberMap.put("成员 4", 4d);
        memberMap.put("成员 5", 5d);

        Map<String, Double> updatedMemberMap = new HashMap<>();
        updatedMemberMap.put("成员 1", 10d);
        updatedMemberMap.put("成员 2", 20d);
        updatedMemberMap.put("成员 3", 30d);
        updatedMemberMap.put("成员 4", 40d);
        updatedMemberMap.put("成员 5", 50d);

        // addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(null, memberMap, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet("", memberMap, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        Map<String, Double> invalidMemberMap = new HashMap<>(memberMap);
        invalidMemberMap.put(null, 100d);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addToSortedSet(key, invalidMemberMap, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));

        // addToSortedSet(String key, Map<String, Double> memberMap, SortedSetAddMode mode) 功能测试
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", memberMap.size(),
                client.addToSortedSet(key, memberMap, null));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, updatedMemberMap, null));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", updatedMemberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", memberMap.size(),
                client.addToSortedSet(key, memberMap, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, memberMap, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, updatedMemberMap, SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", updatedMemberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", memberMap.size(),
                client.addToSortedSet(key, memberMap, SortedSetAddMode.REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, memberMap, SortedSetAddMode.REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", memberMap.size(),
                client.addToSortedSet(key, updatedMemberMap, SortedSetAddMode.REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", updatedMemberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", memberMap.size(),
                client.addToSortedSet(key, memberMap, SortedSetAddMode.ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, updatedMemberMap, SortedSetAddMode.ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }

        client.delete(key);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, memberMap, SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertNull("Test `addToSortedSet` method failed: `invalid score.",
                    client.getScoreFromSortedSet(key, member));
        }
        client.addToSortedSet(key, memberMap, null);
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", 0,
                client.addToSortedSet(key, memberMap, SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", memberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        Assert.assertEquals("Test `addToSortedSet` method failed: `invalid return size`.", memberMap.size(),
                client.addToSortedSet(key, updatedMemberMap, SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER));
        for (String member : memberMap.keySet()) {
            Assert.assertEquals("Test `addToSortedSet` method failed: `invalid score.", updatedMemberMap.get(member),
                    client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        }
        client.delete(key);
    }

    @Test
    public void testIncrForSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String member = "成员 1";
        double increment = 1d;

        // incrForSortedSet(String key, double increment, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrForSortedSet(null, increment, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrForSortedSet("", increment, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.incrForSortedSet(key, increment, null));

        // incrForSortedSet(String key, double increment, String member) 功能测试
        Assert.assertEquals("Test `incrForSortedSet` method failed: `invalid return score`.", increment,
                client.incrForSortedSet(key, increment, member), SCORE_DELTA);
        Assert.assertEquals("Test `incrForSortedSet` method failed: `invalid return score`.", 2 * increment,
                client.incrForSortedSet(key, increment, member), SCORE_DELTA);
        Assert.assertEquals("Test `incrForSortedSet` method failed: `invalid return score`.", 3 * increment,
                client.incrForSortedSet(key, increment, member), SCORE_DELTA);
        Assert.assertEquals("Test `incrForSortedSet` method failed: `invalid return score`.", 2 * increment,
                client.incrForSortedSet(key, -increment, member), SCORE_DELTA);
        client.delete(key);
    }

    @Test
    public void testRemoveFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        double score = 1d;
        String[] members = new String[]{"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // removeFromSortedSet(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSortedSet(null, members[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSortedSet("", members[0]));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSortedSet(key, (String) null));

        // removeFromSortedSet(String key, String member) 功能测试
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeFromSortedSet(key, members[0]));
        for (int i = 0; i < members.length; i++) {
            client.addToSortedSet(key, score, members[i]);
        }
        for (int i = 0; i < members.length; i++) {
            Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", 1,
                    client.removeFromSortedSet(key, members[i]));
        }
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid sorted set size`.", 0,
                client.getSizeOfSortedSet(key));
        for (int i = 0; i < members.length; i++) {
            Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", 0,
                    client.removeFromSortedSet(key, members[i]));
        }
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid sorted set size`.", 0,
                client.getSizeOfSortedSet(key));
        client.delete(key);
    }

    @Test
    public void testMultiRemoveFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        double score = 1d;
        String[] members = new String[]{"成员 1", "成员 2", "成员 3", "成员 4", "成员 5"};

        // removeFromSortedSet(String key, Collection<String> members) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSortedSet(null, Arrays.asList(members)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSortedSet("", Arrays.asList(members)));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeFromSortedSet(key, Arrays.asList(members[0], null, members[1])));

        // removeFromSortedSet(String key, Collection<String> members) 功能测试
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeFromSortedSet(key, (Collection<String>) null));
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeFromSortedSet(key, new ArrayList<>()));
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeFromSortedSet(key, Arrays.asList(members)));
        for (int i = 0; i < members.length; i++) {
            client.addToSortedSet(key, score, members[i]);
        }
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", members.length,
                client.removeFromSortedSet(key, Arrays.asList(members)));
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid sorted set size`.", 0,
                client.getSizeOfSortedSet(key));
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeFromSortedSet(key, Arrays.asList(members)));
        Assert.assertEquals("Test `removeFromSortedSet` method failed: `invalid sorted set size`.", 0,
                client.getSizeOfSortedSet(key));
        client.delete(key);
    }

    @Test
    public void testRemoveByRankFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // removeByRankFromSortedSet(String key, int start, int stop) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeByRankFromSortedSet(null, 0, 1));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeByRankFromSortedSet("", 0, 1));

        // removeByRankFromSortedSet(String key, int start, int stop) 功能测试
        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid return value`.", 2,
                client.removeByRankFromSortedSet(key, 0, 1));
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid sorted set size`.",
                members.size() - 2, client.getSizeOfSortedSet(key));
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid return value`.",
                members.size() - 2, client.removeByRankFromSortedSet(key, 0, members.size()));
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid sorted set size`.",
                0, client.getSizeOfSortedSet(key));

        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid return value`.", 2,
                client.removeByRankFromSortedSet(key, -2, -1));
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid sorted set size`.",
                members.size() - 2, client.getSizeOfSortedSet(key));
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid return value`.",
                members.size() - 2, client.removeByRankFromSortedSet(key, 0, -1));
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid sorted set size`.",
                0, client.getSizeOfSortedSet(key));
        client.delete(key);
    }

    @Test
    public void testRemoveByScoreFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // removeByScoreFromSortedSet(String key, double minScore, double maxScore) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeByScoreFromSortedSet(null, 0, 10));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeByScoreFromSortedSet("", 0, 10));

        // removeByScoreFromSortedSet(String key, double minScore, double maxScore) 功能测试
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeByScoreFromSortedSet(key, 0, 10));
        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", 2,
                client.removeByScoreFromSortedSet(key, 1d, 2d));
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid sorted set size`.",
                members.size() - 2, client.getSizeOfSortedSet(key));

        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", members.size() - 2,
                client.removeByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assert.assertEquals("Test `removeByRankFromSortedSet` method failed: `invalid sorted set size`.",
                0, client.getSizeOfSortedSet(key));
        client.delete(key);
    }

    @Test
    public void testRemoveByScoreFromSortedSetWithMode() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeByScoreFromSortedSet(null, 0, true, 10, true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.removeByScoreFromSortedSet("", 0, true, 10, true));


        // removeByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) 功能测试
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeByScoreFromSortedSet(key, 0, true, 10, true));

        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", 0,
                client.removeByScoreFromSortedSet(key, 1, false, 2, false));
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid sorted set size`.",
                members.size(), client.getSizeOfSortedSet(key));

        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", 1,
                client.removeByScoreFromSortedSet(key, 1, true, 2, false));
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid sorted set size`.",
                members.size() - 1, client.getSizeOfSortedSet(key));

        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", 1,
                client.removeByScoreFromSortedSet(key, 1, false, 2, true));
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid sorted set size`.",
                members.size() - 1, client.getSizeOfSortedSet(key));

        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", 2,
                client.removeByScoreFromSortedSet(key, 1, true, 2, true));
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid sorted set size`.",
                members.size() - 2, client.getSizeOfSortedSet(key));

        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", members.size(),
                client.removeByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false));
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid sorted set size`.",
                0, client.getSizeOfSortedSet(key));

        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid return value`.", members.size(),
                client.removeByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, true, Double.POSITIVE_INFINITY, true));
        Assert.assertEquals("Test `removeByScoreFromSortedSet` method failed: `invalid sorted set size`.",
                0, client.getSizeOfSortedSet(key));
        client.delete(key);
    }

    @Test
    public void testGetScoreFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String member = "成员 1";
        double score = 1d;
        String notExistMember = "成员 2";

        // getScoreFromSortedSet(String key, String member) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getScoreFromSortedSet(null, member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getScoreFromSortedSet("", member));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getScoreFromSortedSet(key, null));

        // getScoreFromSortedSet(String key, String member) 功能测试
        Assert.assertNull("Test `getScoreFromSortedSet` method failed: `invalid score`.", client.getScoreFromSortedSet(key, member));
        client.addToSortedSet(key, score, member);
        Assert.assertEquals("Test `getScoreFromSortedSet` method failed: `invalid score`.", score, client.getScoreFromSortedSet(key, member), SCORE_DELTA);
        Assert.assertNull("Test `getScoreFromSortedSet` method failed: `invalid score`.", client.getScoreFromSortedSet(key, notExistMember));
        client.delete(key);
    }

    @Test
    public void testGetRankFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String existMember = "成员 1";
        String notExistMember = "不存在的成员";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put(existMember, 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getRankFromSortedSet(String key, String member, boolean reverse) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getRankFromSortedSet(null, existMember, false));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getRankFromSortedSet("", existMember, false));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getRankFromSortedSet(key, null, false));

        // getRankFromSortedSet(String key, String member, boolean reverse) 功能测试
        Assert.assertNull("Test `getRankFromSortedSet` method failed: `invalid rank`.", client.getRankFromSortedSet(key, existMember, false));
        client.addToSortedSet(key ,members);
        Assert.assertNull("Test `getRankFromSortedSet` method failed: `invalid rank`.", client.getRankFromSortedSet(key, notExistMember, false));
        int rank = 0;
        for (String member : members.keySet()) {
            Assert.assertEquals("Test `getRankFromSortedSet` method failed: `invalid rank`.", rank++,
                    (int) client.getRankFromSortedSet(key, member, false));
        }

        rank = members.size() - 1;
        for (String member : members.keySet()) {
            Assert.assertEquals("Test `getRankFromSortedSet` method failed: `invalid rank`.", rank--,
                    (int) client.getRankFromSortedSet(key, member, true));
        }
        client.delete(key);
    }

    @Test
    public void testGetSizeOfSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getSizeOfSortedSet(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfSortedSet(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getSizeOfSortedSet(""));

        // getSizeOfSortedSet(String key) 功能测试
        Assert.assertEquals("Test `getSizeOfSortedSet` method failed: `invalid size`.", 0,
                client.getSizeOfSortedSet(key));
        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `getSizeOfSortedSet` method failed: `invalid size`.", members.size(),
                client.getSizeOfSortedSet(key));
        client.delete(key);
    }

    @Test
    public void testGetCountFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getCountFromSortedSet(String key, double minScore, double maxScore) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getCountFromSortedSet(null, 0d, 10d));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getCountFromSortedSet("", 0d, 10d));

        // getCountFromSortedSet(String key, double minScore, double maxScore) 功能测试
        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", 0,
                client.getCountFromSortedSet(key, 0d, 10d));
        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", 2,
                client.getCountFromSortedSet(key, 1d, 2d));
        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", members.size(),
                client.getCountFromSortedSet(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        client.delete(key);
    }

    @Test
    public void testGetCountFromSortedSetWithMode() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getCountFromSortedSet(null, 0d, true, 10d, true));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getCountFromSortedSet("", 0d, true, 10d, true));

        // getCountFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore) 功能测试
        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", 0,
                client.getCountFromSortedSet(key, 0d, true, 10d, true));
        client.addToSortedSet(key, members);

        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", 0,
                client.getCountFromSortedSet(key, 1d, false, 2d, false));

        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", 1,
                client.getCountFromSortedSet(key, 1d, true, 2d, false));

        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", 1,
                client.getCountFromSortedSet(key, 1d, false, 2d, true));

        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", 2,
                client.getCountFromSortedSet(key, 1d, true, 2d, true));

        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", members.size(),
                client.getCountFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false));

        Assert.assertEquals("Test `getCountFromSortedSet` method failed: `invalid size`.", members.size(),
                client.getCountFromSortedSet(key, Double.NEGATIVE_INFINITY, true, Double.POSITIVE_INFINITY, true));
        client.delete(key);
    }

    @Test
    public void getMembersByRankFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersByRankFromSortedSet(null, 0, 1, false));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersByRankFromSortedSet("", 0, 1, false));

        // getMembersByRankFromSortedSet(String key, int start, int stop, boolean reverse) 功能测试
        Assert.assertEquals("Test `getMembersByRankFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersByRankFromSortedSet(key, 0, 1, false).size());
        client.addToSortedSet(key, members);

        Assert.assertEquals("Test `getMembersByRankFromSortedSet` method failed: `invalid size`.", 2,
                client.getMembersByRankFromSortedSet(key, 0, 1, false).size());
        Assert.assertEquals("Test `getMembersByRankFromSortedSet` method failed: `invalid size`.", 2,
                client.getMembersByRankFromSortedSet(key, 0, 1, true).size());

        List<String> actualMembers = client.getMembersByRankFromSortedSet(key, 0, members.size(), false);
        Assert.assertEquals("Test `getMembersByRankFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        int rank = 0;
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersByRankFromSortedSet` method failed: `invalid member`.", expectedMember,
                    actualMembers.get(rank++));
        }

        actualMembers = client.getMembersByRankFromSortedSet(key, 0, -1, true);
        Assert.assertEquals("Test `getMembersByRankFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        rank = members.size() - 1;
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersByRankFromSortedSet` method failed: `invalid member`.", expectedMember,
                    actualMembers.get(rank--));
        }
        client.delete(key);
    }

    @Test
    public void testGetMembersWithScoresByRankFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersWithScoresByRankFromSortedSet(null, 0, 1, false));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersWithScoresByRankFromSortedSet("", 0, 1, false));

        // getMembersWithScoresByRankFromSortedSet(String key, int start, int stop, boolean reverse) 功能测试
        Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersWithScoresByRankFromSortedSet(key, 0, 1, false).size());
        client.addToSortedSet(key, members);

        Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid size`.", 2,
                client.getMembersWithScoresByRankFromSortedSet(key, 0, 1, false).size());
        Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid size`.", 2,
                client.getMembersWithScoresByRankFromSortedSet(key, 0, 1, true).size());

        LinkedHashMap<String, Double> actualMembers = client.getMembersWithScoresByRankFromSortedSet(key, 0, members.size(), false);
        Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                new ArrayList<>(actualMembers.keySet()));
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid score`.", members.get(expectedMember),
                    actualMembers.get(expectedMember), SCORE_DELTA);
        }

        actualMembers = client.getMembersWithScoresByRankFromSortedSet(key, 0, -1, true);
        Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        List<String> actualMembersList = new ArrayList<>(members.keySet());
        Collections.sort(actualMembersList);
        Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid keySet`.",
                new ArrayList<>(members.keySet()), actualMembersList);
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersWithScoresByRankFromSortedSet` method failed: `invalid score`.", members.get(expectedMember),
                    actualMembers.get(expectedMember), SCORE_DELTA);
        }
        client.delete(key);
    }

    @Test
    public void testGetMembersByScoreFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersByScoreFromSortedSet(null, 0d, 10d, false));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersByScoreFromSortedSet("", 0d, 10d, false));

        // getMembersByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) 功能测试
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersByScoreFromSortedSet(key, 1d, 2d, false).size());
        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 2,
                client.getMembersByScoreFromSortedSet(key, 1d, 2d, false).size());

        List<String> actualMembers = client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                actualMembers);

        actualMembers = client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        Collections.reverse(actualMembers);
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                actualMembers);
        client.delete(key);
    }

    @Test
    public void testGetMembersByScoreFromSortedSetWithMode() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersByScoreFromSortedSet(null, 0d, true,
                10d, true, false, 0, 0));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersByScoreFromSortedSet("", 0d, true,
                10d, true, false, 0, 0));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersByScoreFromSortedSet(key, 0d, true,
                10d, true, false, -1, 0));

        // getMembersByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) 功能测试
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersByScoreFromSortedSet(key, 0d, true, 10d, true, false, 0, 0).size());
        client.addToSortedSet(key, members);

        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersByScoreFromSortedSet(key, 1d, false, 2d, false, false, 0, 0).size());
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersByScoreFromSortedSet(key, 1d, true, 2d, false, false, 0, 0).size());
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersByScoreFromSortedSet(key, 1d, false, 2d, true, false, 0, 0).size());

        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 0, 0).size());
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, Integer.MAX_VALUE, 0).size());

        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 0, 1).size());
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 1, 1).size());

        List<String> actualMembers = client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 0, 0);
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                actualMembers);

        actualMembers = client.getMembersByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, true, Double.POSITIVE_INFINITY, true, true, 0, 0);
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        Collections.reverse(actualMembers);
        Assert.assertEquals("Test `getMembersByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                actualMembers);
        client.delete(key);
    }

    @Test
    public void testGetMembersWithScoresByScoreFromSortedSet() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersWithScoresByScoreFromSortedSet(null, 0d, 10d, false));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersWithScoresByScoreFromSortedSet("", 0d, 10d, false));

        // getMembersWithScoresByScoreFromSortedSet(String key, double minScore, double maxScore, boolean reverse) 功能测试
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersWithScoresByScoreFromSortedSet(key, 1d, 2d, false).size());
        client.addToSortedSet(key, members);
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 2,
                client.getMembersWithScoresByScoreFromSortedSet(key, 1d, 2d, false).size());

        LinkedHashMap<String, Double> actualMembers = client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                new ArrayList<>(actualMembers.keySet()));
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid score`.", members.get(expectedMember),
                    actualMembers.get(expectedMember), SCORE_DELTA);
        }

        actualMembers = client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        List<String> actualMembersList = new ArrayList<>(actualMembers.keySet());
        Collections.reverse(actualMembersList);
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                actualMembersList);
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid score`.", members.get(expectedMember),
                    actualMembers.get(expectedMember), SCORE_DELTA);
        }
        client.delete(key);
    }

    @Test
    public void testGetMembersWithScoresByScoreFromSortedSetWithMode() {
        NaiveRedisSortedSetClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        LinkedHashMap<String, Double> members = new LinkedHashMap<>();
        members.put("成员 1", 1d);
        members.put("成员 2", 2d);
        members.put("成员 3", 3d);
        members.put("成员 4", 4d);
        members.put("成员 5", 5d);

        // getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersWithScoresByScoreFromSortedSet(null, 0d, true,
                10d, true, false, 0, 0));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersWithScoresByScoreFromSortedSet("", 0d, true,
                10d, true, false, 0, 0));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getMembersWithScoresByScoreFromSortedSet(key, 0d, true,
                10d, true, false, -1, 0));

        // getMembersWithScoresByScoreFromSortedSet(String key, double minScore, boolean includeMinScore, double maxScore, boolean includeMaxScore, boolean reverse, int offset, int count) 功能测试
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersWithScoresByScoreFromSortedSet(key, 0d, true, 10d, true, false, 0, 0).size());
        client.addToSortedSet(key, members);

        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 0,
                client.getMembersWithScoresByScoreFromSortedSet(key, 1d, false, 2d, false, false, 0, 0).size());
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersWithScoresByScoreFromSortedSet(key, 1d, true, 2d, false, false, 0, 0).size());
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersWithScoresByScoreFromSortedSet(key, 1d, false, 2d, true, false, 0, 0).size());

        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 0, 0).size());
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, Integer.MAX_VALUE, 0).size());

        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 0, 1).size());
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", 1,
                client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 1, 1).size());

        Map<String, Double> actualMembers = client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false, false, 0, 0);
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                new ArrayList<>(actualMembers.keySet()));
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid score`.", members.get(expectedMember),
                    actualMembers.get(expectedMember), SCORE_DELTA);
        }

        actualMembers = client.getMembersWithScoresByScoreFromSortedSet(key, Double.NEGATIVE_INFINITY, true, Double.POSITIVE_INFINITY, true, true, 0, 0);
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid size`.", members.size(),
                actualMembers.size());
        List<String> actualMembersList = new ArrayList<>(actualMembers.keySet());
        Collections.reverse(actualMembersList);
        Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid keySet`.", new ArrayList<>(members.keySet()),
                actualMembersList);
        for (String expectedMember : members.keySet()) {
            Assert.assertEquals("Test `getMembersWithScoresByScoreFromSortedSet` method failed: `invalid score`.", members.get(expectedMember),
                    actualMembers.get(expectedMember), SCORE_DELTA);
        }
        client.delete(key);
    }
}
