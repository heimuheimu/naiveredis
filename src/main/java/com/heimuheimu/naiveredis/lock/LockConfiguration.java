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

package com.heimuheimu.naiveredis.lock;

import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Redis 分布式锁配置信息。
 *
 * <p><strong>说明：</strong>LockConfiguration 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class LockConfiguration {

    /**
     * 默认的 Redis 分布式锁配置信息，锁最大持有时间为 5 秒，延迟获取的最小时间为 1 毫秒，延迟获取的最大时间为 10 毫秒，超时时间为 500 毫秒。
     */
    public static LockConfiguration DEFAULT = new LockConfiguration(5, 1, 10, 500);

    private static final Logger LOGGER = LoggerFactory.getLogger(LockConfiguration.class);

    /**
     * 锁的最大持有时间，单位：秒，不允许小于等于 0
     */
    private final int validity;

    /**
     * 获取锁失败时，延迟获取的最小时间，单位：毫秒，不允许小于 0
     */
    private final int minDelay;

    /**
     * 获取锁失败时，延迟获取的最大时间，单位：毫秒，不允许小于 0
     */
    private final int maxDelay;

    /**
     * 获取锁的超时时间，单位：毫秒，不允许小于等于 0，通常超时时间应远小于锁的最大持有时间
     */
    private final int timeout;

    /**
     * 构造一个 LockConfiguration 实例。
     *
     * @param validity 锁的最大持有时间，单位：秒，不允许小于等于 0
     * @param minDelay 获取锁失败时，延迟获取的最小时间，单位：毫秒，不允许小于 0
     * @param maxDelay 获取锁失败时，延迟获取的最大时间，单位：毫秒，不允许小于 0
     * @param timeout 获取锁的超时时间，单位：毫秒，不允许小于等于 0，通常超时时间应远小于锁的最大持有时间
     * @throws IllegalArgumentException 如果 validity 小于等于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 minDelay 小于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 maxDelay 小于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 minDelay 大于 maxDelay，将会抛出此异常
     * @throws IllegalArgumentException 如果 timeout 小于等于 0，将会抛出此异常
     */
    public LockConfiguration(int validity, int minDelay, int maxDelay, int timeout) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("LockConfiguration", LOGGER);
        checker.addParameter("validity", validity);
        checker.addParameter("minDelay", minDelay);
        checker.addParameter("maxDelay", maxDelay);
        checker.addParameter("timeout", timeout);
        checker.check("validity", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);
        checker.check("minDelay", "isLessThanZero", Parameters::isLessThanZero);
        checker.check("maxDelay", "isLessThanZero", Parameters::isLessThanZero);
        checker.check("timeout", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);
        if (minDelay > maxDelay) {
            throw new IllegalArgumentException("Create `LockConfiguration` failed: `minDelay is greater than maxDelay`."
                + LogBuildUtil.build(checker.getParameterMap()));
        }

        this.validity = validity;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.timeout = timeout;
    }

    /**
     * 获得锁的最大持有时间，单位：秒，不会小于等于 0。
     *
     * @return 锁的最大持有时间，单位：秒，不会小于等于 0
     */
    public int getValidity() {
        return validity;
    }

    /**
     * 获得获取锁失败时，延迟获取的最小时间，单位：毫秒，不会小于 0。
     *
     * @return 延迟获取的最小时间，单位：毫秒，不会小于 0
     */
    public int getMinDelay() {
        return minDelay;
    }

    /**
     * 获得获取锁失败时，延迟获取的最大时间，单位：毫秒，不会小于 0。
     *
     * @return 延迟获取的最大时间，单位：毫秒，不会小于 0
     */
    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * 获得获取锁的超时时间，单位：毫秒，不会小于等于 0。
     *
     * @return 获取锁的超时时间，单位：毫秒，不会小于等于 0
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 获得获取锁失败时，延迟获取的随机时间，单位：毫秒，随机时间范围为：[minDelay, maxDelay]。
     *
     * @return 获取锁失败时，延迟获取的随机时间，单位：毫秒，范围为：[minDelay, maxDelay]
     */
    public int getRandomDelay() {
        return new Random().nextInt(maxDelay - minDelay + 1) + minDelay;
    }

    @Override
    public String toString() {
        return "LockConfiguration{" +
                "validity=" + validity +
                ", minDelay=" + minDelay +
                ", maxDelay=" + maxDelay +
                ", timeout=" + timeout +
                '}';
    }
}
