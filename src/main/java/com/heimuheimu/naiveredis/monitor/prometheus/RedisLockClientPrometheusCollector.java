/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 heimuheimu
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

package com.heimuheimu.naiveredis.monitor.prometheus;

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.prometheus.support.AbstractExecutionPrometheusCollector;
import com.heimuheimu.naiveredis.monitor.RedisLockClientMonitorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 分布式锁客户端使用的执行信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>naiveredis_lock_client_exec_count{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端获取锁的次数</li>
 *     <li>naiveredis_lock_client_exec_peak_tps_count{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端每秒最大获取锁的次数</li>
 *     <li>naiveredis_lock_client_avg_exec_time_millisecond{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端单次获取锁的平均执行时间，单位：毫秒</li>
 *     <li>naiveredis_lock_client_max_exec_time_millisecond{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端单次获取锁的最大执行时间，单位：毫秒</li>
 *     <li>naiveredis_lock_client_exec_error_count{errorCode="-1",errorType="IllegalArgument",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端获取锁时出现参数不正确的错误次数</li>
 *     <li>naiveredis_lock_client_exec_error_count{errorCode="-2",errorType="IllegalState",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端获取锁时出现管道或命令已关闭的错误次数</li>
 *     <li>naiveredis_lock_client_exec_error_count{errorCode="-3",errorType="Timeout",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端获取锁时出现获取超时的错误次数</li>
 *     <li>naiveredis_lock_client_exec_error_count{errorCode="-4",errorType="RedisError",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端获取锁时出现 Redis 服务端执行异常的错误次数</li>
 *     <li>naiveredis_lock_client_exec_error_count{errorCode="-5",errorType="LockExist",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端获取锁时出现锁已被占用错误次数</li>
 *     <li>naiveredis_lock_client_exec_error_count{errorCode="-6",errorType="UnexpectedError",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Redis 分布式锁客户端获取锁时出现预期外异常的错误次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.2
 */
public class RedisLockClientPrometheusCollector extends AbstractExecutionPrometheusCollector {

    /**
     * Redis 分布式锁客户端使用的操作执行信息监控器列表，不会为 {@code null} 或空，仅允许在构造函数中修改
     */
    private final List<ExecutionMonitor> monitorList;

    /**
     * 操作执行信息监控器访问的 Redis 集群名称，与 {@link #monitorList} 一一对应， 不会为 {@code null} 或空，仅允许在构造函数中修改
     */
    private final List<String> nameList;

    /**
     * 操作执行信息监控器连接的 Redis 主机地址列表，与 {@link #monitorList} 一一对应， 不会为 {@code null} 或空，仅允许在构造函数中修改
     */
    private final List<String> hostList;

    /**
     * 构造一个 RedisLockClientPrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 configurationList 为 {@code null} 或空，将会抛出此异常
     */
    public RedisLockClientPrometheusCollector(List<RedisPrometheusCollectorConfiguration> configurationList) throws IllegalArgumentException {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `RedisLockClientPrometheusCollector` failed: `configurationList could not be empty`.");
        }
        monitorList = new ArrayList<>();
        nameList = new ArrayList<>();
        hostList = new ArrayList<>();
        for (RedisPrometheusCollectorConfiguration configuration : configurationList) {
            for (String host : configuration.getHostList()) {
                monitorList.add(RedisLockClientMonitorFactory.get(host));
                nameList.add(configuration.getName());
                hostList.add(host);
            }
        }
    }

    @Override
    protected String getMetricPrefix() {
        return "naiveredis_lock_client";
    }

    @Override
    protected Map<Integer, String> getErrorTypeMap() {
        Map<Integer, String> errorTypeMap = new HashMap<>();
        errorTypeMap.put(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT, "IllegalArgument");
        errorTypeMap.put(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_STATE, "IllegalState");
        errorTypeMap.put(RedisLockClientMonitorFactory.ERROR_CODE_TIMEOUT, "Timeout");
        errorTypeMap.put(RedisLockClientMonitorFactory.ERROR_CODE_REDIS_ERROR, "RedisError");
        errorTypeMap.put(RedisLockClientMonitorFactory.ERROR_CODE_LOCK_IS_EXIST, "LockExist");
        errorTypeMap.put(RedisLockClientMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR, "UnexpectedError");
        return errorTypeMap;
    }

    @Override
    protected List<ExecutionMonitor> getMonitorList() {
        return monitorList;
    }

    @Override
    protected String getMonitorId(ExecutionMonitor monitor, int index) {
        return String.valueOf(index);
    }

    @Override
    protected void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample) {
        sample.addSampleLabel("name", nameList.get(monitorIndex))
                .addSampleLabel("remoteAddress", hostList.get(monitorIndex));
    }
}
