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

import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;
import com.heimuheimu.naiveredis.monitor.RedisDistributedLockMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 分布式锁信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>naiveredis_distributed_lock_success_count 相邻两次采集周期内 Redis 分布式锁获取成功的次数</li>
 *     <li>naiveredis_distributed_lock_fail_count 相邻两次采集周期内 Redis 分布式锁获取失败的次数</li>
 *     <li>naiveredis_distributed_lock_error_count 相邻两次采集周期内 Redis 分布式锁获取时出现异常的次数</li>
 *     <li>naiveredis_distributed_lock_unlock_success_count 相邻两次采集周期内 Redis 分布式锁释放成功的次数</li>
 *     <li>naiveredis_distributed_lock_avg_holding_time_millisecond 相邻两次采集周期内释放成功的 Redis 分布式锁的平均被持有时间，单位：毫秒</li>
 *     <li>naiveredis_distributed_lock_max_holding_time_millisecond 相邻两次采集周期内释放成功的 Redis 分布式锁的最大持有时间，单位：毫秒</li>
 *     <li>naiveredis_distributed_lock_unlock_error_count 相邻两次采集周期内 Redis 分布式锁释放异常的次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.2
 */
public class RedisDistributedLockPrometheusCollector implements PrometheusCollector {

    private final DeltaCalculator calculator = new DeltaCalculator();

    @Override
    public List<PrometheusData> getList() {
        RedisDistributedLockMonitor monitor = RedisDistributedLockMonitor.getInstance();
        List<PrometheusData> dataList = new ArrayList<>();
        // create naiveredis_distributed_lock_success_count
        dataList.add(PrometheusData.buildGauge("naiveredis_distributed_lock_success_count", "")
                .addSample(PrometheusSample.build(calculator.delta("successCount", monitor.getSuccessCount()))));
        // create naiveredis_distributed_lock_fail_count
        dataList.add(PrometheusData.buildGauge("naiveredis_distributed_lock_fail_count", "")
                .addSample(PrometheusSample.build(calculator.delta("failCount", monitor.getFailCount()))));
        // create naiveredis_distributed_lock_error_count
        dataList.add(PrometheusData.buildGauge("naiveredis_distributed_lock_error_count", "")
                .addSample(PrometheusSample.build(calculator.delta("errorCount", monitor.getErrorCount()))));
        // create naiveredis_distributed_lock_unlock_success_count
        double unlockSuccessCount = calculator.delta("unlockSuccessCount", monitor.getUnlockSuccessCount());
        dataList.add(PrometheusData.buildGauge("naiveredis_distributed_lock_unlock_success_count", "")
                .addSample(PrometheusSample.build(unlockSuccessCount)));
        // create naiveredis_distributed_lock_avg_holding_time_millisecond
        double holdingTimeMillisecond = calculator.delta("holdingTimeMillisecond", monitor.getTotalHoldingTime());
        double avgHoldingTimeMillisecond = unlockSuccessCount > 0 ? (holdingTimeMillisecond / unlockSuccessCount) : 0;
        dataList.add(PrometheusData.buildGauge("naiveredis_distributed_lock_avg_holding_time_millisecond", "")
                .addSample(PrometheusSample.build(avgHoldingTimeMillisecond)));
        // create naiveredis_distributed_lock_max_holding_time_millisecond
        dataList.add(PrometheusData.buildGauge("naiveredis_distributed_lock_max_holding_time_millisecond", "")
                .addSample(PrometheusSample.build(monitor.getMaxHoldingTime())));
        monitor.resetMaxHoldingTime();
        // create naiveredis_distributed_lock_unlock_error_count
        dataList.add(PrometheusData.buildGauge("naiveredis_distributed_lock_unlock_error_count", "")
                .addSample(PrometheusSample.build(calculator.delta("unlockErrorCount", monitor.getUnlockErrorCount()))));
        return dataList;
    }
}