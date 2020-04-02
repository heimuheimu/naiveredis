/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 heimuheimu
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

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.monitor.factory.NaiveExecutionMonitorFactory;

import java.util.List;

/**
 * Redis 消息发布客户端使用的执行信息监控工厂类。
 *
 * @author heimuheimu
 */
public class PublisherMonitorFactory {

    /**
     * Redis 消息发布客户端错误码：消息发布失败
     */
    public static final int ERROR_CODE_PUBLISH_ERROR = -1;

    /**
     * Redis 消息发布客户端错误码：消息订阅者数量为 0
     */
    public static final int ERROR_CODE_NO_CLIENT = -2;

    private static final String NAME_PREFIX = "NaiveRedis_PUB_";

    private PublisherMonitorFactory() {
        //private constructor
    }

    /**
     * 根据 Redis 主机地址获得对应的 Redis 消息发布客户端使用的执行信息监控器，该方法不会返回 {@code null}。
     *
     * @param host Redis 主机地址
     * @return Redis 消息发布客户端使用的执行信息监控器
     */
    public static ExecutionMonitor get(String host) {
        return NaiveExecutionMonitorFactory.get(NAME_PREFIX + host);
    }

    /**
     * 获得 Redis 消息发布客户端使用的执行信息监控器列表，该方法不会返回 {@code null}。
     *
     * @return Redis 消息发布客户端使用的执行信息监控器列表，不会为 {@code null}
     */
    public static List<ExecutionMonitor> getAll() {
        return NaiveExecutionMonitorFactory.getListByPrefix(NAME_PREFIX);
    }
}
