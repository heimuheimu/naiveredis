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
import java.util.HashSet;
import java.util.Map;

/**
 * {@link NaiveRedisCountClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisCountClient {

    public abstract NaiveRedisCountClient getClient();

    @Test
    public void testGetCount() {
        NaiveRedisCountClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";

        // getCount(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getCount(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getCount(""));

        // getCount(String key) 功能测试
        Assert.assertNull("Test `getCount` method failed: `invalid return value`.", client.getCount(key));
        client.addAndGet(key, 100, 0);
        Assert.assertEquals("Test `getCount` method failed: `invalid return value`.", 100L, (long) client.getCount(key));
        client.delete(key);
    }

    @Test
    public void testMultiGetCount() {
        NaiveRedisCountClient client = getClient();
        // 测试数据准备
        int validKeyCount = 8;
        String[] expectedKeys = new String[] {"测试 Key 1", "测试 Key 2", "测试 Key 3", "测试 Key 4", "测试 Key 5", "测试 Key 6",
                "测试 Key 7", "测试 Key 8", "测试 Key 9", "测试 Key 10"};

        // multiGetCount(Set<String> keySet) 参数异常测试
        deleteKeys(expectedKeys);
        AssertUtil.assertThrowException(() -> client.multiGetCount(new HashSet<>(Arrays.asList(expectedKeys[0], null, expectedKeys[1]))));
        AssertUtil.assertThrowException(() -> client.multiGetCount(new HashSet<>(Arrays.asList(expectedKeys[0], "", expectedKeys[1]))));

        // multiGetCount(Set<String> keySet) 功能测试
        Assert.assertTrue("Test `multiGetCount` method failed: `invalid return value`.", client.multiGetCount(null).isEmpty());
        Assert.assertTrue("Test `multiGetCount` method failed: `invalid return value`.", client.multiGetCount(new HashSet<>()).isEmpty());

        for (int i = 0; i < validKeyCount; i++) {
            client.addAndGet(expectedKeys[i], 100, 0);
        }

        Map<String, Long> countMap = client.multiGetCount(new HashSet<>(Arrays.asList(expectedKeys)));
        Assert.assertEquals("Test `multiGetCount` method failed: `invalid map size`.", validKeyCount, countMap.size());
        for (int i = 0; i < validKeyCount; i++) {
            Assert.assertEquals("Test `multiGetCount` method failed: `invalid count`.", 100L, (long) countMap.get(expectedKeys[i]));
        }
        deleteKeys(expectedKeys);
    }

    @Test
    public void testAddAndGet() {
        NaiveRedisCountClient client = getClient();
        // 测试数据准备
        String key = "测试 Key";
        int increment = 100;

        // addAndGet(String key, long delta, int expiry) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addAndGet(null, 1, 0));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.addAndGet("", 1, 0));

        for (int i = 0; i < increment; i++) {
            Assert.assertEquals("Test `addAndGet` method failed: `invalid count`.", i + 1, client.addAndGet(key, 1, 0));
            Assert.assertTrue("Test `addAndGet` method failed: `invalid expiry`.", client.getTimeToLive(key) == -1);
        }

        client.delete(key);
        for (int i = 0; i < increment; i++) {
            Assert.assertEquals("Test `addAndGet` method failed: `invalid count`.", i + 1, client.addAndGet(key, 1, -1));
            Assert.assertTrue("Test `addAndGet` method failed: `invalid expiry`.", client.getTimeToLive(key) == -1);
        }

        client.delete(key);
        for (int i = 0; i < increment; i++) {
            Assert.assertEquals("Test `addAndGet` method failed: `invalid count`.", i + 1, client.addAndGet(key, 1, 30));
            int ttl = client.getTimeToLive(key);
            Assert.assertTrue("Test `addAndGet` method failed: `invalid expiry`.", ttl > 0 && ttl <= 30);
        }
        client.delete(key);
    }

    private void deleteKeys(String[] keys) {
        NaiveRedisCountClient client = getClient();
        for (String key : keys) {
            client.delete(key);
        }
    }
}
