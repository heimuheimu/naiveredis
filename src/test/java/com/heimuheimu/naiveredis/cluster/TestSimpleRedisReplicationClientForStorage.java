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

import com.heimuheimu.naiveredis.AbstractTestNaiveRedisStorageClient;
import com.heimuheimu.naiveredis.NaiveRedisStorageClient;
import com.heimuheimu.naiveredis.TestRedisProvider;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;

/**
 * {@link SimpleRedisReplicationClient} 单元测试类，仅测试 Redis Storage 相关方法。
 *
 * <p><strong>注意：</strong>如果没有提供测试 Redis Master 地址或 测试 Redis Slave 地址数组，即 {@link TestRedisProvider#getMasterRedisHost()} 返回空或 {@code null}
 * 或者 {@link TestRedisProvider#getSlaveRedisHosts()} ()} 返回空数组或 {@code null}，测试用例不会执行。</p>
 *
 * @author heimuheimu
 */
public class TestSimpleRedisReplicationClientForStorage extends AbstractTestNaiveRedisStorageClient {

    private static SimpleRedisReplicationClient CLIENT;

    @BeforeClass
    public static void init() {
        String redisMasterHost = TestRedisProvider.getMasterRedisHost();
        String[] redisSlaveHosts = TestRedisProvider.getSlaveRedisHosts();
        if (redisMasterHost == null || redisMasterHost.isEmpty()
                || redisSlaveHosts == null || redisSlaveHosts.length == 0) {
            Assume.assumeTrue("TestSimpleRedisReplicationClientForStorage will be ignored: `empty redis master host or empty redis slave hosts`.", false);
        } else {
            CLIENT = new SimpleRedisReplicationClient(redisMasterHost, redisSlaveHosts, null);
        }
    }

    @AfterClass
    public static void clean() {
        if (CLIENT != null) {
            CLIENT.close();
        }
    }

    @Override
    public NaiveRedisStorageClient getClient() {
        return CLIENT;
    }
}
