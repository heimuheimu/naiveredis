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
 * Redis 客户端信息复合采集器，该采集器将会收集以下采集器的信息：
 * <ul>
 *     <li>{@link RedisExecutionPrometheusCollector} Redis 客户端使用的执行信息采集器</li>
 *     <li>{@link RedisSocketPrometheusCollector} Redis 客户端使用的 Socket 读、写信息采集器</li>
 *     <li>{@link RedisCompressionPrometheusCollector} Redis 客户端使用的压缩信息采集器</li>
 *     <li>{@link RedisThreadPoolPrometheusCollector} Redis 客户端使用的线程池信息采集器</li>
 *     <li>{@link RedisClusterPrometheusCollector} Redis 集群客户端信息采集器</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.2
 */
public class RedisCompositePrometheusCollector implements PrometheusCollector {

    /**
     * Redis 客户端使用的执行信息采集器
     */
    private final RedisExecutionPrometheusCollector executionCollector;

    /**
     * Redis 客户端使用的 Socket 读、写信息采集器
     */
    private final RedisSocketPrometheusCollector socketCollector;

    /**
     * Redis 客户端使用的压缩信息采集器
     */
    private final RedisCompressionPrometheusCollector compressionCollector;

    /**
     * Redis 客户端使用的线程池信息采集器
     */
    private final RedisThreadPoolPrometheusCollector threadPoolCollector;

    /**
     * Redis 集群客户端信息采集器
     */
    private final RedisClusterPrometheusCollector clusterCollector;

    /**
     * 构造一个 RedisClientCompositePrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     */
    public RedisCompositePrometheusCollector(List<RedisPrometheusCollectorConfiguration> configurationList) {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `RedisClientCompositePrometheusCollector` failed: `configurationList could not be empty`.");
        }
        this.executionCollector = new RedisExecutionPrometheusCollector(configurationList);
        this.socketCollector = new RedisSocketPrometheusCollector(configurationList);
        this.compressionCollector = new RedisCompressionPrometheusCollector();
        this.threadPoolCollector = new RedisThreadPoolPrometheusCollector();
        this.clusterCollector = new RedisClusterPrometheusCollector();
    }

    @Override
    public List<PrometheusData> getList() {
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.addAll(executionCollector.getList());
        dataList.addAll(socketCollector.getList());
        dataList.addAll(compressionCollector.getList());
        dataList.addAll(threadPoolCollector.getList());
        dataList.addAll(clusterCollector.getList());
        return dataList;
    }
}
