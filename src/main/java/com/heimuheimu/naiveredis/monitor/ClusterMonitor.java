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

package com.heimuheimu.naiveredis.monitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 集群客户端信息监控器。
 *
 * @author heimuheimu
 */
public class ClusterMonitor {

    private static final ClusterMonitor INSTANCE = new ClusterMonitor();

    /**
     * 获取不可用客户端的次数
     */
    private final AtomicLong unavailableClientCount = new AtomicLong();

    /**
     * Redis 集群客户端调用 {@code #multiGet(Set<String> keySet)} 方法出现的错误次数
     */
    private final AtomicLong multiGetErrorCount = new AtomicLong();

    private ClusterMonitor() {
        //private constructor
    }

    /**
     * 对 Redis 集群客户端获取到不可用 Redis 客户端的次数进行监控。
     */
    public void onUnavailable() {
        unavailableClientCount.incrementAndGet();
    }

    /**
     * 获得获取不可用客户端的次数。
     *
     * @return 获取不可用客户端的次数
     */
    public long getUnavailableClientCount() {
        return unavailableClientCount.get();
    }

    /**
     * 对 Redis 集群客户端调用 {@code #multiGet(Set<String> keySet)} 方法出错的次数进行监控。
     */
    public void onMultiGetError() {
        multiGetErrorCount.incrementAndGet();
    }

    /**
     * 获得 Redis 集群客户端调用 {@code #multiGet(Set<String> keySet)} 方法出现的错误次数。
     *
     * @return Redis 集群客户端调用 {@code #multiGet(Set<String> keySet)} 方法出现的错误次数
     */
    public long getMultiGetErrorCount() {
        return multiGetErrorCount.get();
    }

    /**
     * 获得 Redis 集群客户端信息监控器。
     *
     * @return Redis 集群客户端信息监控器
     */
    public static ClusterMonitor getInstance() {
        return INSTANCE;
    }
}
