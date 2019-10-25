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
 * {@link NaiveRedisRawStorageClient} 接口实现类单元测试基类。
 *
 * @author heimuheimu
 */
public abstract class AbstractTestNaiveRedisRawStorageClient {

    public abstract NaiveRedisRawStorageClient getClient();

    @Test
    public void testGetString() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";

        // getString(String key) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getString(null));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.getString(""));

        // getString(String key) 功能测试
        Assert.assertNull("Test `getString` method failed: `invalid value`.", client.getString(key));
        client.setString(key, "字符串测试");
        Assert.assertNotNull("Test `getString` method failed: `invalid value`.", client.getString(key));
        client.delete(key);
    }

    @Test
    public void testMultiGetString() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        int validKeyCount = 8;
        String[] expectedKeys = new String[] {"测试 Key 1", "测试 Key 2", "测试 Key 3", "测试 Key 4", "测试 Key 5", "测试 Key 6",
                "测试 Key 7", "测试 Key 8", "测试 Key 9", "测试 Key 10"};

        // multiGet(Set<String> keySet) 参数异常测试
        deleteKeys(expectedKeys);
        AssertUtil.assertThrowException(() -> client.multiGetString(new HashSet<>(Arrays.asList(expectedKeys[0], null, expectedKeys[1]))));
        AssertUtil.assertThrowException(() -> client.multiGetString(new HashSet<>(Arrays.asList(expectedKeys[0], "", expectedKeys[1]))));

        // multiGet(Set<String> keySet) 功能测试
        Assert.assertTrue("Test `multiGetString` method failed: `invalid return value`.", client.multiGetString(null).isEmpty());
        Assert.assertTrue("Test `multiGetString` method failed: `invalid return value`.", client.multiGetString(new HashSet<>()).isEmpty());

        for (int i = 0; i < validKeyCount; i++) {
            client.setString(expectedKeys[i], "字符串测试 " + i);
        }

        Map<String, String> valueMap = client.multiGetString(new HashSet<>(Arrays.asList(expectedKeys)));
        Assert.assertEquals("Test `multiGetString` method failed: `invalid map size`.", validKeyCount, valueMap.size());
        for (int i = 0; i < validKeyCount; i++) {
            Assert.assertEquals("Test `multiGetString` method failed: `invalid value`.", "字符串测试 " + i, valueMap.get(expectedKeys[i]));
        }
        deleteKeys(expectedKeys);
    }

    @Test
    public void testSetString() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String value = "字符串测试 1";
        String newValue = "字符串测试 2";

        // set(String key, String value) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setString(null, value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setString("", value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setString(key, null));

        // setString(String key, String value) 功能测试
        client.setString(key, value);
        Assert.assertEquals("Test `setString` method failed: `invalid return value`.", value, client.getString(key));
        Assert.assertEquals("Test `setString` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.setString(key, newValue);
        Assert.assertEquals("Test `setString` method failed: `invalid return value`.", newValue, client.getString(key));
        Assert.assertEquals("Test `setString` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
    }

    @Test
    public void testSetStringWithExpiry() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String value = "字符串测试 1";
        String newValue = "字符串测试 2";
        int expiry = 30;

        // setString(String key, String value, int expiry) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setString(null, value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setString("", value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setString(key, null, expiry));

        // setString(String key, String value, int expiry) 功能测试
        client.setString(key, value, 0);
        Assert.assertEquals("Test `setString` method failed: `invalid return value`.", value, client.getString(key));
        Assert.assertEquals("Test `setString` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.setString(key, newValue, -1);
        Assert.assertEquals("Test `setString` method failed: `invalid return value`.", newValue, client.getString(key));
        Assert.assertEquals("Test `setString` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.setString(key, value, expiry);
        Assert.assertEquals("Test `setString` method failed: `invalid return value`.", value, client.getString(key));
        Assert.assertEquals("Test `setString` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        client.setString(key, newValue, expiry);
        Assert.assertEquals("Test `setString` method failed: `invalid return value`.", newValue, client.getString(key));
        Assert.assertEquals("Test `setString` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        client.delete(key);
    }

    @Test
    public void testSetStringIfAbsent() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String value = "字符串测试 1";
        String newValue = "字符串测试 2";

        // setStringIfAbsent(String key, String value) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfAbsent(null, value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfAbsent("", value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfAbsent(key, null));

        // setStringIfAbsent(String key, String value) 功能测试
        Assert.assertTrue("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, value));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        Assert.assertFalse("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, newValue));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
    }

    @Test
    public void testSetIfAbsentWithExpiry() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String value = "字符串测试 1";
        String newValue = "字符串测试 2";
        int expiry = 30;

        // setStringIfAbsent(String key, String value, int expiry) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfAbsent(null, value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfAbsent("", value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfAbsent(key, null, expiry));

        // setStringIfAbsent(String key, String value, int expiry) 功能测试
        Assert.assertTrue("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, value, 0));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        Assert.assertFalse("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, newValue, 0));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
        Assert.assertTrue("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, value, -1));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        Assert.assertFalse("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, newValue, -1));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
        Assert.assertTrue("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, value, expiry));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        Assert.assertFalse("Test `setStringIfAbsent` method failed: `invalid return value`.", client.setStringIfAbsent(key, newValue, expiry));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfAbsent` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 4);

        client.delete(key);
    }

    @Test
    public void testSetStringIfExist() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String value = "字符串测试 1";
        String newValue = "字符串测试 2";

        // setStringIfExist(String key, String value) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfExist(null, value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfExist("", value));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfExist(key, null));

        // setStringIfExist(String key, String value) 功能测试
        Assert.assertFalse("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, value));
        Assert.assertNull("Test `setStringIfExist` method failed: `invalid value`.", client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", -2, client.getTimeToLive(key));

        client.setString(key, value);
        Assert.assertTrue("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, newValue));
        Assert.assertEquals("Test `setIfExist` method failed: `invalid value`.", newValue, client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
    }

    @Test
    public void testSetIfExistWithExpiry() {
        NaiveRedisRawStorageClient client = getClient();

        // 测试数据准备
        String key = "测试 Key";
        String value = "字符串测试 1";
        String newValue = "字符串测试 2";
        int expiry = 30;

        // setStringIfExist(String key, Object value, int expiry) 参数异常测试
        client.delete(key);
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfExist(null, value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfExist("", value, expiry));
        AssertUtil.assertThrowIllegalArgumentException(() -> client.setStringIfExist(key, null, expiry));

        // setStringIfExist(String key, Object value, int expiry) 功能测试
        client.setString(key, value);
        Assert.assertTrue("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, newValue, 0));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid value`.", newValue, client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
        Assert.assertFalse("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, newValue, 0));
        Assert.assertNull("Test `setStringIfExist` method failed: `invalid value`.", client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", -2, client.getTimeToLive(key));

        client.setString(key, value);
        Assert.assertTrue("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, newValue, -1));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid value`.", newValue, client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", -1, client.getTimeToLive(key));

        client.delete(key);
        Assert.assertFalse("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, newValue, -1));
        Assert.assertNull("Test `setStringIfExist` method failed: `invalid value`.", client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", -2, client.getTimeToLive(key));

        client.setString(key, value);
        Assert.assertTrue("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, newValue, expiry));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid value`.", newValue, client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        Assert.assertTrue("Test `setStringIfExist` method failed: `invalid return value`.", client.setStringIfExist(key, value, expiry));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid value`.", value, client.getString(key));
        Assert.assertEquals("Test `setStringIfExist` method failed: `invalid return expiry`.", expiry, client.getTimeToLive(key), 2);

        client.delete(key);
    }

    private void deleteKeys(String[] keys) {
        NaiveRedisRawStorageClient client = getClient();
        for (String key : keys) {
            client.delete(key);
        }
    }
}
