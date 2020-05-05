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

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 分布式锁信息复合采集器，该采集器将会收集以下采集器的信息：
 * <ul>
 *     <li>{@link RedisLockClientPrometheusCollector} Redis 分布式锁客户端使用的执行信息采集器</li>
 *     <li>{@link RedisDistributedLockPrometheusCollector} Redis 分布式锁信息采集器</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.2
 */
public class RedisLockCompositePrometheusCollector implements PrometheusCollector {

    /**
     * Redis 分布式锁客户端使用的执行信息采集器
     */
    private final RedisLockClientPrometheusCollector lockClientCollector;

    /**
     * Redis 分布式锁信息采集器
     */
    private final RedisDistributedLockPrometheusCollector distributedLockCollector;

    /**
     * 构造一个 RedisLockCompositePrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     */
    public RedisLockCompositePrometheusCollector(List<RedisPrometheusCollectorConfiguration> configurationList) {
        this.lockClientCollector = new RedisLockClientPrometheusCollector(configurationList);
        this.distributedLockCollector = new RedisDistributedLockPrometheusCollector();
    }

    @Override
    public List<PrometheusData> getList() {
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.addAll(lockClientCollector.getList());
        dataList.addAll(distributedLockCollector.getList());
        return dataList;
    }
}
