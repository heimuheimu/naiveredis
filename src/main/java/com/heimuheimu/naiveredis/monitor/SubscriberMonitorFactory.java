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

/**
 * Redis 订阅客户端使用的执行信息监控工厂类。
 *
 * @author heimuheimu
 */
public class SubscriberMonitorFactory {

    private SubscriberMonitorFactory() {
        //private constructor
    }

    /**
     * Redis 订阅客户端使用的执行信息监控器。
     */
    private static final ExecutionMonitor MONITOR = new ExecutionMonitor();

    /**
     * Redis 订阅客户端消费信息错误码：消息解码失败
     */
    public static final int ERROR_CODE_DECODE_ERROR = -1;

    /**
     * Redis 订阅客户端消费信息错误码：消息消费失败
     */
    public static final int ERROR_CODE_CONSUME_ERROR = -2;

    /**
     * Redis 订阅客户端消费信息错误码：预期外异常
     */
    public static final int ERROR_CODE_UNEXPECTED_ERROR = -3;

    /**
     * Redis 订阅客户端消费信息错误码：单个 Redis 消息订阅者消费信息过慢
     */
    public static final int ERROR_CODE_SLOW_CONSUMPTION = -4;

    /**
     * 获得 Redis 订阅客户端使用的执行信息监控器，该方法不会返回 {@code null}。
     *
     * @return Redis 订阅客户端使用的执行信息监控器
     */
    public static ExecutionMonitor get() {
        return MONITOR;
    }
}
