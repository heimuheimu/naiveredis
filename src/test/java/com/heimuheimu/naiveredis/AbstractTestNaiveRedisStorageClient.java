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
 * {@link NaiveRedisKeysClient} 以及 {@link NaiveRedisStorageClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisStorageClient {

    public abstract NaiveRedisStorageClient getClient();

    @Test
    public void testExpire() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        int expire = 30;

        // expire(String key, int expiry) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.expire(null, expire));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.expire("", expire));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.expire(key, 0));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.expire(key, -1));

        // expire(String key, int expiry) 功能测试
        client.expire(key, expire);
        Assert.assertEquals("Test `expire` method failed: `invalid return expiry`.", -2, client.getTimeToLive(key));
        client.set(key, 1);
        client.expire(key, expire);
        Assert.assertEquals("Test `expire` method failed: `invalid return expiry`.", expire, client.getTimeToLive(key), 2);
        client.delete(key);
    }

    @Test
    public void testDelete() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";

        // delete(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.delete(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.delete(""));

        // delete(String key) 功能测试
        client.delete(key);
        client.set(key, 1);
        Assert.assertNotNull("Test `delete` method failed: `invalid value`.", client.get(key));
        client.delete(key);
        Assert.assertNull("Test `delete` method failed: `invalid value`.", client.get(key));
    }

    @Test
    public void testGetTimeToLive() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        int expire = 30;

        // getTimeToLive(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getTimeToLive(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getTimeToLive(""));

        // getTimeToLive(String key) 功能测试
        Assert.assertEquals("Test `getTimeToLive` method failed: `invalid return expiry`.", -2, client.getTimeToLive(key));
        client.set(key, 1);
        Assert.assertEquals("Test `getTimeToLive` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));
        client.expire(key, expire);
        Assert.assertEquals("Test `getTimeToLive` method failed: `invalid return expiry`.", expire, client.getTimeToLive(key), 2);
    }

    @Test
    public void testGet() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";

        // get(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.get(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.get(""));

        // get(String key) 功能测试
        Assert.assertNull("Test `get` method failed: `invalid value`.", client.get(key));
        client.set(key, 1);
        Assert.assertNotNull("Test `get` method failed: `invalid value`.", client.get(key));
        client.delete(key);
    }

    @Test
    public void testMultiGet() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        int validKeyCount = 8;
        String[] expectedKeys = new String[] {"测试 Key 1", "测试 Key 2", "测试 Key 3", "测试 Key 4", "测试 Key 5", "测试 Key 6",
                "测试 Key 7", "测试 Key 8", "测试 Key 9", "测试 Key 10"};

        // multiGet(Set<String> keySet) 参数异常测试
        deleteKeys(expectedKeys);
        AssertUtil.assertThrowException(() -> client.multiGet(new HashSet<>(Arrays.asList(expectedKeys[0], null, expectedKeys[1]))));
        AssertUtil.assertThrowException(() -> client.multiGet(new HashSet<>(Arrays.asList(expectedKeys[0], "", expectedKeys[1]))));

        // multiGet(Set<String> keySet) 功能测试
        Assert.assertTrue("Test `multiGet` method failed: `invalid return value`.", client.multiGet(null).isEmpty());
        Assert.assertTrue("Test `multiGet` method failed: `invalid return value`.", client.multiGet(new HashSet<>()).isEmpty());

        for (int i = 0; i < validKeyCount; i++) {
            client.set(expectedKeys[i], 1);
        }

        Map<String, Integer> valueMap = client.multiGet(new HashSet<>(Arrays.asList(expectedKeys)));
        Assert.assertEquals("Test `multiGet` method failed: `invalid map size`.", validKeyCount, valueMap.size());
        for (int i = 0; i < validKeyCount; i++) {
            Assert.assertEquals("Test `multiGet` method failed: `invalid value`.", 1, (int) valueMap.get(expectedKeys[i]));
        }
        deleteKeys(expectedKeys);
    }

    @Test
    public void testSet() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        Integer value = 1;
        Integer newValue = 2;

        // set(String key, Object value) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.set(null, value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.set("", value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.set(key, null));

        // set(String key, Object value) 功能测试
        client.set(key, value);
        Assert.assertEquals("Test `set` method failed: `invalid return value`.", value, client.get(key));
        Assert.assertEquals("Test `set` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.set(key, newValue);
        Assert.assertEquals("Test `set` method failed: `invalid return value`.", newValue, client.get(key));
        Assert.assertEquals("Test `set` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
    }

    @Test
    public void testSetWithExpiry() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        Integer value = 1;
        Integer newValue = 2;
        int expiry = 30;

        // set(String key, Object value, int expiry) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.set(null, value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.set("", value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.set(key, null, expiry));

        // set(String key, Object value, int expiry) 功能测试
        client.set(key, value, 0);
        Assert.assertEquals("Test `set` method failed: `invalid return value`.", value, client.get(key));
        Assert.assertEquals("Test `set` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.set(key, newValue, -1);
        Assert.assertEquals("Test `set` method failed: `invalid return value`.", newValue, client.get(key));
        Assert.assertEquals("Test `set` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.set(key, value, expiry);
        Assert.assertEquals("Test `set` method failed: `invalid return value`.", value, client.get(key));
        Assert.assertEquals("Test `set` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        client.set(key, newValue, expiry);
        Assert.assertEquals("Test `set` method failed: `invalid return value`.", newValue, client.get(key));
        Assert.assertEquals("Test `set` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        client.delete(key);
    }

    @Test
    public void testSetIfAbsent() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        Integer value = 1;
        Integer newValue = 2;

        // setIfAbsent(String key, Object value) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setIfAbsent(null, value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setIfAbsent("", value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setIfAbsent(key, null));

        // setIfAbsent(String key, Object value) 功能测试
        Assert.assertTrue("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, value));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        Assert.assertFalse("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, newValue));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
    }

    @Test
    public void testSetIfAbsentWithExpiry() {
        NaiveRedisStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        Integer value = 1;
        Integer newValue = 2;
        int expiry = 30;

        // setIfAbsent(String key, Object value, int expiry) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setIfAbsent(null, value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setIfAbsent("", value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setIfAbsent(key, null, expiry));

        // setIfAbsent(String key, Object value, int expiry) 功能测试
        Assert.assertTrue("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, value, 0));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        Assert.assertFalse("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, newValue, 0));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
        Assert.assertTrue("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, value, -1));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        Assert.assertFalse("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, newValue, -1));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
        Assert.assertTrue("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, value, expiry));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        Assert.assertFalse("Test `setIfAbsent` method failed: `invalid return value`.", client.setIfAbsent(key, newValue, expiry));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid value`.", value, client.get(key));
        Assert.assertEquals("Test `setIfAbsent` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 4);

        client.delete(key);
    }

    private void deleteKeys(String[] keys) {
        NaiveRedisStorageClient client = getClient();
        for (String key : keys) {
            client.delete(key);
        }
    }
}
