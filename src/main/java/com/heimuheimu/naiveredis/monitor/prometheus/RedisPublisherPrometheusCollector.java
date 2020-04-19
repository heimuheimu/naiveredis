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
import com.heimuheimu.naiveredis.monitor.PublisherMonitorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 消息发布客户端使用的执行信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>naiveredis_publisher_exec_count{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发布消息总数</li>
 *     <li>naiveredis_publisher_exec_peak_tps_count{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内每秒最大发布消息数量</li>
 *     <li>naiveredis_publisher_avg_exec_time_millisecond{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发布单条消息平均执行时间</li>
 *     <li>naiveredis_publisher_max_exec_time_millisecond{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发布单条消息最大执行时间</li>
 *     <li>naiveredis_publisher_exec_error_count{errorCode="-1",errorType="PublishError",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发生的消息发布失败次数</li>
 *     <li>naiveredis_publisher_exec_error_count{errorCode="-2",errorType="NoClient",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发生的消息订阅者数量为 0 的次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.2
 */
public class RedisPublisherPrometheusCollector extends AbstractExecutionPrometheusCollector {

    /**
     * Redis 消息发布客户端使用的操作执行信息监控器列表，不会为 {@code null} 或空，仅允许在构造函数中修改
     */
    private final List<RedisPrometheusCollectorConfiguration> configurationList;

    /**
     * 构造一个 RedisPublisherPrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 configurationList 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 RedisPrometheusCollectorConfiguration 中的 hostList 长度不为 1，将会抛出此异常
     */
    public RedisPublisherPrometheusCollector(List<RedisPrometheusCollectorConfiguration> configurationList) throws IllegalArgumentException {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `RedisPublisherPrometheusCollector` failed: `configurationList could not be empty`.");
        }
        for (RedisPrometheusCollectorConfiguration configuration : configurationList) {
            if (configuration.getHostList().size() != 1) {
                throw new IllegalArgumentException("Create `RedisPublisherPrometheusCollector` failed: `invalid host list size.`. " + configuration);
            }
        }
        this.configurationList = configurationList;
    }

    @Override
    protected String getMetricPrefix() {
        return "naiveredis_publisher";
    }

    @Override
    protected Map<Integer, String> getErrorTypeMap() {
        Map<Integer, String> errorTypeMap = new HashMap<>();
        errorTypeMap.put(PublisherMonitorFactory.ERROR_CODE_PUBLISH_ERROR, "PublishError");
        errorTypeMap.put(PublisherMonitorFactory.ERROR_CODE_NO_CLIENT, "NoClient");
        return errorTypeMap;
    }

    @Override
    protected List<ExecutionMonitor> getMonitorList() {
        List<ExecutionMonitor> monitorList = new ArrayList<>();
        for (RedisPrometheusCollectorConfiguration configuration : configurationList) {
            monitorList.add(PublisherMonitorFactory.get(configuration.getHostList().get(0)));
        }
        return monitorList;
    }

    @Override
    protected String getMonitorId(ExecutionMonitor monitor, int index) {
        return String.valueOf(index);
    }

    @Override
    protected void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample) {
        RedisPrometheusCollectorConfiguration configuration = configurationList.get(monitorIndex);
        sample.addSampleLabel("name", configuration.getName())
                .addSampleLabel("remoteAddress", configuration.getHostList().get(0));
    }
}
