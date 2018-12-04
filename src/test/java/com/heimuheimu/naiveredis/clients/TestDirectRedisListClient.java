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


import com.heimuheimu.naiveredis.AbstractTestNaiveRedisListClient;
import com.heimuheimu.naiveredis.NaiveRedisListClient;
import com.heimuheimu.naiveredis.TestRedisProvider;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * {@link DirectRedisListClient} 单元测试类。
 *
 * <p><strong>说明：</strong>该单元测试需访问 Redis 才可执行，默认为 Ignore，需手动执行。</p>
 *
 * @author heimuheimu
 */
@Ignore
public class TestDirectRedisListClient extends AbstractTestNaiveRedisListClient {

    private static DirectRedisListClient CLIENT;

    @BeforeClass
    public static void init() {
        RedisChannel channel = new RedisChannel(TestRedisProvider.getRedisHost(), null, 30, null);
        channel.init();
        CLIENT = new DirectRedisListClient(channel, 5000, 1000);
    }

    @AfterClass
    public static void clean() {
        CLIENT.channel.close();
    }

    @Override
    public NaiveRedisListClient getClient() {
        return CLIENT;
    }
}
