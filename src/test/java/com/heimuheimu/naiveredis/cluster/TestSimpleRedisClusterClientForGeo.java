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

package com.heimuheimu.naiveredis.cluster;

import com.heimuheimu.naiveredis.AbstractTestNaiveRedisGeoClient;
import com.heimuheimu.naiveredis.NaiveRedisGeoClient;
import com.heimuheimu.naiveredis.TestRedisProvider;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;

/**
 * {@link SimpleRedisClusterClient} 单元测试类，仅测试 Redis GEO 相关方法。
 *
 * <p><strong>注意：</strong>如果没有提供测试 Redis 地址数组，即 {@link TestRedisProvider#getRedisHosts()} 返回空数组或 {@code null}，测试用例不会执行。</p>
 *
 * @author heimuheimu
 */
public class TestSimpleRedisClusterClientForGeo extends AbstractTestNaiveRedisGeoClient {

    private static SimpleRedisClusterClient CLIENT;

    @BeforeClass
    public static void init() {
        String[] redisHosts = TestRedisProvider.getRedisHosts();
        if (redisHosts == null || redisHosts.length == 0) {
            Assume.assumeTrue("TestSimpleRedisClusterClientForGeo will be ignored: `empty redis hosts`.", false);
        } else {
            CLIENT = new SimpleRedisClusterClient(redisHosts, null);
        }
    }

    @AfterClass
    public static void clean() {
        if (CLIENT != null) {
            CLIENT.close();
        }
    }

    @Override
    public NaiveRedisGeoClient getClient() {
        return CLIENT;
    }
}
