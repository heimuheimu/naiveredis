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

package com.heimuheimu.naiveredis.monitor.falcon;


import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.falcon.support.AbstractFalconDataCollector;
import com.heimuheimu.naiveredis.constant.FalconDataCollectorConstant;
import com.heimuheimu.naiveredis.monitor.RedisDistributedLockMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 分布式锁信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 *
 * <ul>
 *     <li>naiveredis_distributed_lock_success_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 分布式锁获取成功的次数</li>
 *     <li>naiveredis_distributed_lock_fail_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 分布式锁获取失败的次数</li>
 *     <li>naiveredis_distributed_lock_error_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 分布式锁获取异常的次数</li>
 *     <li>naiveredis_distributed_unlock_success_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 分布式锁释放成功的次数</li>
 *     <li>naiveredis_distributed_avg_holding_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内释放成功的 Redis 分布式锁的平均被持有时间</li>
 *     <li>naiveredis_distributed_max_holding_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内释放成功的单个 Redis 分布式锁的最大持有时间</li>
 *     <li>naiveredis_distributed_unlock_error_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 分布式锁释放异常的次数</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class RedisDistributedLockDataCollector extends AbstractFalconDataCollector {

    /**
     * 上一次 Redis 分布式锁获取成功的次数
     */
    private volatile long lastSuccessCount = 0;

    /**
     * 上一次 Redis 分布式锁获取失败的次数
     */
    private volatile long lastFailCount = 0;

    /**
     * 上一次 Redis 分布式锁获取异常的次数
     */
    private volatile long lastErrorCount = 0;

    /**
     * 上一次 Redis 分布式锁释放成功的次数
     */
    private volatile long lastUnlockSuccessCount = 0;

    /**
     * 上一次 Redis 分布式锁被持有的总时间，单位：毫秒
     */
    private volatile long lastHoldingTime = 0;

    /**
     * 上一次 Redis 分布式锁释放成功的次数
     */
    private volatile long lastUnlockErrorCount = 0;

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>();
        RedisDistributedLockMonitor monitor = RedisDistributedLockMonitor.getInstance();
        long totalSuccessCount = monitor.getSuccessCount();
        falconDataList.add(create("_success_count", totalSuccessCount - lastSuccessCount));
        lastSuccessCount = totalSuccessCount;

        long totalFailCount = monitor.getFailCount();
        falconDataList.add(create("_fail_count", totalFailCount - lastFailCount));
        lastFailCount = totalFailCount;

        long totalErrorCount = monitor.getErrorCount();
        falconDataList.add(create("_error_count", totalErrorCount - lastErrorCount));
        lastErrorCount = totalErrorCount;

        long totalUnlockSuccessCount = monitor.getUnlockSuccessCount();
        long totalHoldingTime = monitor.getTotalHoldingTime();
        long currentUnlockSuccessCount = totalUnlockSuccessCount - lastUnlockSuccessCount;
        lastUnlockSuccessCount = totalUnlockSuccessCount;
        long currentHoldingTime = totalHoldingTime - lastHoldingTime;
        lastHoldingTime = totalHoldingTime;
        falconDataList.add(create("_unlock_success_count", currentUnlockSuccessCount));
        falconDataList.add(create("_avg_holding_time", currentUnlockSuccessCount > 0 ? (int) (currentHoldingTime / currentUnlockSuccessCount): 0));
        falconDataList.add(create("_max_holding_time", monitor.getMaxHoldingTime()));
        monitor.resetMaxHoldingTime();

        long totalUnlockErrorCount = monitor.getUnlockErrorCount();
        falconDataList.add(create("_unlock_error_count", totalUnlockErrorCount - lastUnlockErrorCount));
        lastUnlockErrorCount = totalUnlockErrorCount;

        return falconDataList;
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    protected String getCollectorName() {
        return "distributed_lock";
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }
}
