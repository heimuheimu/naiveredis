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
import com.heimuheimu.naiveredis.monitor.ClusterMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 集群客户端信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>naiveredis_cluster_unavailable_client_count 相邻两次采集周期内 Redis 集群客户端获取到不可用 Redis 客户端的次数</li>
 *     <li>naiveredis_cluster_multi_get_error_count 相邻两次采集周期内 Redis 集群客户端调用 multiGet 方法出现错误的次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.2
 */
public class RedisClusterPrometheusCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    private final DeltaCalculator deltaCalculator = new DeltaCalculator();

    @Override
    public List<PrometheusData> getList() {
        ClusterMonitor monitor = ClusterMonitor.getInstance();
        // create naiveredis_cluster_unavailable_client_count
        PrometheusData unavailableClientCountData = PrometheusData.buildGauge("naiveredis_cluster_unavailable_client_count", "");
        unavailableClientCountData.addSample(PrometheusSample.build(deltaCalculator.delta("unavailableClientCount", monitor.getUnavailableClientCount())));
        // create naiveredis_cluster_multi_get_error_count
        PrometheusData multiGetErrorCountData = PrometheusData.buildGauge("naiveredis_cluster_multi_get_error_count", "");
        multiGetErrorCountData.addSample(PrometheusSample.build(deltaCalculator.delta("multiGetErrorCount", monitor.getMultiGetErrorCount())));

        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(unavailableClientCountData);
        dataList.add(multiGetErrorCountData);
        return dataList;
    }
}
