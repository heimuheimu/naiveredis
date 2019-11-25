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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 分布式锁信息监控器。
 *
 * @author heimuheimu
 */
public class RedisDistributedLockMonitor {

    private static final RedisDistributedLockMonitor INSTANCE = new RedisDistributedLockMonitor();

    /**
     * Redis 分布式锁获取成功的次数
     */
    private final AtomicLong successCount = new AtomicLong(0);

    /**
     * Redis 分布式锁获取失败的次数
     */
    private final AtomicLong failCount = new AtomicLong(0);

    /**
     * Redis 分布式锁获取异常的次数
     */
    private final AtomicLong errorCount = new AtomicLong(0);

    /**
     * Redis 分布式锁释放成功的次数
     */
    private final AtomicLong unlockSuccessCount = new AtomicLong(0);

    /**
     * Redis 分布式锁被持有的总时间，单位：毫秒
     */
    private final AtomicLong totalHoldingTime = new AtomicLong(0);

    /**
     * Redis 分布式锁单次被持有的最大时间，单位：毫秒
     */
    private volatile long maxHoldingTime = 0;

    /**
     * Redis 分布式锁释放异常的次数
     */
    private final AtomicLong unlockErrorCount = new AtomicLong(0);


    private RedisDistributedLockMonitor() {
        // private constructor
    }

    /**
     * 对 Redis 分布式锁获取成功的次数进行监控。
     */
    public void onSuccess() {
        successCount.incrementAndGet();
    }

    /**
     * 对 Redis 分布式锁获取失败的次数进行监控。
     */
    public void onFail() {
        failCount.incrementAndGet();
    }

    /**
     * 对 Redis 分布式锁获取异常的次数进行监控。
     */
    public void onError() {
        errorCount.incrementAndGet();
    }

    /**
     * 对 Redis 分布式锁释放成功的次数进行监控。
     */
    public void onUnlockSuccess(long holdingTime) {
        unlockSuccessCount.incrementAndGet();
        totalHoldingTime.addAndGet(holdingTime);
        // 仅使用 volatile 来保证可见性，并没有保证操作的原子性，极端情况下，真正的最大值可能会被覆盖，但做统计影响不大
        if (holdingTime > maxHoldingTime) {
            maxHoldingTime = holdingTime;
        }
    }

    /**
     * 对 Redis 分布式锁释放异常的次数进行监控。
     */
    public void onUnlockErrorCount() {
        unlockErrorCount.incrementAndGet();
    }

    /**
     * 获得 Redis 分布式锁获取成功的次数。
     *
     * @return Redis 分布式锁获取成功的次数
     */
    public long getSuccessCount() {
        return successCount.get();
    }

    /**
     * 获得 Redis 分布式锁获取失败的次数。
     *
     * @return Redis 分布式锁获取失败的次数
     */
    public long getFailCount() {
        return failCount.get();
    }

    /**
     * 获得 Redis 分布式锁获取异常的次数。
     *
     * @return Redis 分布式锁获取异常的次数
     */
    public long getErrorCount() {
        return errorCount.get();
    }

    /**
     * 获得 Redis 分布式锁信息监控器，该方法不会返回 {@code null}。
     *
     * @return Redis 分布式锁信息监控器，不会为 {@code null}
     */
    public static RedisDistributedLockMonitor getInstance() {
        return INSTANCE;
    }
}
