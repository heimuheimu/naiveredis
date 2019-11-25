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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 分布式锁客户端使用的执行信息监控工厂类。
 *
 * @author heimuheimu
 */
public class RedisLockClientMonitorFactory {

    private RedisLockClientMonitorFactory() {
        //private constructor
    }

    private static final ConcurrentHashMap<String, ExecutionMonitor> EXECUTION_MONITOR_MAP = new ConcurrentHashMap<>();

    private static final Object lock = new Object();

    /**
     * Redis 分布式锁客户端执行错误码：参数不正确
     */
    public static final int ERROR_CODE_ILLEGAL_ARGUMENT = -1;

    /**
     * Redis 分布式锁客户端执行错误码：管道或命令已关闭
     */
    public static final int ERROR_CODE_ILLEGAL_STATE = -2;

    /**
     * Redis 分布式锁客户端执行错误码：执行超时
     */
    public static final int ERROR_CODE_TIMEOUT = -3;

    /**
     * Redis 分布式锁客户端执行错误码：Redis 命令执行出错
     */
    public static final int ERROR_CODE_REDIS_ERROR = -4;

    /**
     * Redis 分布式锁客户端执行错误码：锁已被占用
     */
    public static final int ERROR_CODE_LOCK_IS_EXIST = -5;

    /**
     * Redis 客户端操作执行错误码：预期外异常
     */
    public static final int ERROR_CODE_UNEXPECTED_ERROR = -6;

    /**
     * 根据 Redis 服务主机地址获得对应的 Redis 分布式锁客户端使用的执行信息监控器，该方法不会返回 {@code null}。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @return Socket 连接目标地址获得对应的执行信息监控器，该方法不会返回 {@code null}
     */
    public static ExecutionMonitor get(String host) {
        ExecutionMonitor monitor = EXECUTION_MONITOR_MAP.get(host);
        if (monitor == null) {
            synchronized (lock) {
                monitor = EXECUTION_MONITOR_MAP.get(host);
                if (monitor == null) {
                    monitor = new ExecutionMonitor();
                    EXECUTION_MONITOR_MAP.put(host, monitor);
                }
            }
        }
        return monitor;
    }

    /**
     * 获得当前工厂类管理的所有 Redis 分布式锁客户端使用的执行信息监控器列表。
     *
     * @return 当前工厂类管理的所有 Redis 分布式锁客户端使用的执行信息监控器列表
     */
    public static List<ExecutionMonitor> getAll() {
        return new ArrayList<>(EXECUTION_MONITOR_MAP.values());
    }
}
