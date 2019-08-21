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

import com.heimuheimu.naiveredis.channel.RedisChannel;

import java.io.InputStream;
import java.util.Properties;

/**
 * 提供用于单元测试使用的 {@link RedisChannel} 实例。
 *
 * @author heimuheimu
 */
public class TestRedisProvider {

    private static Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try {
            InputStream in = TestRedisProvider.class.getResourceAsStream("/redis-host.properties");
            PROPERTIES.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得用于单元测试使用的 Redis 地址。
     *
     * @return Redis 地址
     */
    public static String getRedisHost() {
        return PROPERTIES.getProperty("redis.host");
    }

    /**
     * 获得用于单元测试使用的 Redis 地址数组。
     *
     * @return Redis 地址数组
     */
    public static String[] getRedisHosts() {
        String hosts = PROPERTIES.getProperty("redis.cluster.hosts");
        if (hosts == null || hosts.isEmpty()) {
            return null;
        } else {
            return hosts.split(",");
        }
    }

    /**
     * 获得用于单元测试使用的处于 Master 模式 Redis 地址。
     *
     * @return 处于 Master 模式 Redis 地址
     */
    public static String getMasterRedisHost() {
        return PROPERTIES.getProperty("redis.master.host");
    }

    /**
     * 获得用于单元测试使用的处于 Slave 模式 Redis 地址数组。
     *
     * @return 处于 Slave 模式 Redis 地址数组
     */
    public static String[] getSlaveRedisHosts() {
        String slaveHosts = PROPERTIES.getProperty("redis.slave.hosts");
        if (slaveHosts == null || slaveHosts.isEmpty()) {
            return null;
        } else {
            return slaveHosts.split(",");
        }
    }

    /**
     * 获得用于单元测试使用的 Redis 集群启动主机地址数组。
     *
     * @returnRedis 集群启动主机地址数组
     */
    public static String[] getClusterBootstrapHosts() {
        String hosts = PROPERTIES.getProperty("redis.standard.cluster.bootstrap.hosts");
        if (hosts == null || hosts.isEmpty()) {
            return null;
        } else {
            return hosts.split(",");
        }
    }
}
