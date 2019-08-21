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

import com.heimuheimu.naiveredis.AbstractTestNaiveRedisHashesClient;
import com.heimuheimu.naiveredis.NaiveRedisHashesClient;
import com.heimuheimu.naiveredis.TestRedisProvider;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;

/**
 * {@link StandardRedisClusterClient} 单元测试类，仅测试 Redis Hashes 相关方法。
 *
 * <p><strong>注意：</strong>如果没有提供测试 Redis 集群启动主机地址数组，即 {@link TestRedisProvider#getClusterBootstrapHosts()}
 * 返回空数组或 {@code null}，测试用例不会执行。</p>
 *
 * @author heimuheimu
 */
public class TestStandardRedisClusterClientForHashes extends AbstractTestNaiveRedisHashesClient {

    private static StandardRedisClusterClient CLIENT;

    @BeforeClass
    public static void init() {
        String[] bootstrapHosts = TestRedisProvider.getClusterBootstrapHosts();
        if (bootstrapHosts == null || bootstrapHosts.length == 0) {
            Assume.assumeTrue("TestStandardRedisClusterClientForHashes will be ignored: `empty cluster bootstrap hosts`.", false);
        } else {
            CLIENT = new StandardRedisClusterClient(bootstrapHosts, null);
        }
    }

    @AfterClass
    public static void clean() {
        if (CLIENT != null) {
            CLIENT.close();
        }
    }

    @Override
    public NaiveRedisHashesClient getClient() {
        return CLIENT;
    }
}
