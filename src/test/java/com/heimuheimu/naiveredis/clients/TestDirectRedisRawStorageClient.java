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


import com.heimuheimu.naiveredis.AbstractTestNaiveRedisRawStorageClient;
import com.heimuheimu.naiveredis.NaiveRedisRawStorageClient;
import com.heimuheimu.naiveredis.TestRedisProvider;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;

/**
 * {@link DirectRedisRawStorageClient} 单元测试类。
 *
 * <p><strong>注意：</strong>如果没有提供测试 Redis 地址，即 {@link TestRedisProvider#getRedisHost()} 返回空或 {@code null}，测试用例不会执行。</p>
 *
 * @author heimuheimu
 */
public class TestDirectRedisRawStorageClient extends AbstractTestNaiveRedisRawStorageClient {

    private static DirectRedisRawStorageClient CLIENT;

    @BeforeClass
    public static void init() {
        String redisHost = TestRedisProvider.getRedisHost();
        if (redisHost == null || redisHost.isEmpty()) {
            Assume.assumeTrue("TestDirectRedisRawStorageClient will be ignored: `empty redis host`.", false);
        } else {
            RedisChannel channel = new RedisChannel(redisHost, null, 30, null);
            channel.init();
            CLIENT = new DirectRedisRawStorageClient(channel, 5000, 1000);
        }
    }

    @AfterClass
    public static void clean() {
        if (CLIENT != null) {
            CLIENT.channel.close();
        }
    }

    @Override
    public NaiveRedisRawStorageClient getClient() {
        return CLIENT;
    }
}
