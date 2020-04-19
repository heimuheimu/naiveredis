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
import com.heimuheimu.naiveredis.monitor.SubscriberMonitorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 消息订阅客户端使用的执行信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>naiveredis_subscriber_exec_count{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内消费消息总数</li>
 *     <li>naiveredis_subscriber_exec_peak_tps_count{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内每秒最大消费消息数量</li>
 *     <li>naiveredis_subscriber_avg_exec_time_millisecond{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内消费单条消息平均执行时间</li>
 *     <li>naiveredis_subscriber_max_exec_time_millisecond{name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内消费单条消息最大执行时间</li>
 *     <li>naiveredis_subscriber_exec_error_count{errorCode="-1",errorType="DecodeError",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发生的消息解码失败次数</li>
 *     <li>naiveredis_subscriber_exec_error_count{errorCode="-2",errorType="ConsumeError",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发生的消息消费失败次数</li>
 *     <li>naiveredis_subscriber_exec_error_count{errorCode="-3",errorType="UnexpectedError",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发生的消息消费出现预期外异常的次数</li>
 *     <li>naiveredis_subscriber_exec_error_count{errorCode="-4",errorType="SlowConsumption",name="$redisName",remoteAddress="$remoteAddress"} 相邻两次采集周期内发生的消息消费过慢的次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.2
 */
public class RedisSubscriberPrometheusCollector extends AbstractExecutionPrometheusCollector {

    /**
     * Redis 消息订阅客户端使用的操作执行信息监控器列表，不会为 {@code null} 或空，仅允许在构造函数中修改
     */
    private final List<RedisPrometheusCollectorConfiguration> configurationList;

    /**
     * 构造一个 RedisSubscriberPrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 configurationList 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 RedisPrometheusCollectorConfiguration 中的 hostList 长度不为 1，将会抛出此异常
     */
    public RedisSubscriberPrometheusCollector(List<RedisPrometheusCollectorConfiguration> configurationList) throws IllegalArgumentException {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `RedisSubscriberPrometheusCollector` failed: `configurationList could not be empty`.");
        }
        for (RedisPrometheusCollectorConfiguration configuration : configurationList) {
            if (configuration.getHostList().size() != 1) {
                throw new IllegalArgumentException("Create `RedisSubscriberPrometheusCollector` failed: `invalid host list size.`. " + configuration);
            }
        }
        this.configurationList = configurationList;
    }

    @Override
    protected String getMetricPrefix() {
        return "naiveredis_subscriber";
    }

    @Override
    protected Map<Integer, String> getErrorTypeMap() {
        Map<Integer, String> errorTypeMap = new HashMap<>();
        errorTypeMap.put(SubscriberMonitorFactory.ERROR_CODE_DECODE_ERROR, "DecodeError");
        errorTypeMap.put(SubscriberMonitorFactory.ERROR_CODE_CONSUME_ERROR, "ConsumeError");
        errorTypeMap.put(SubscriberMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR, "UnexpectedError");
        errorTypeMap.put(SubscriberMonitorFactory.ERROR_CODE_SLOW_CONSUMPTION, "SlowConsumption");
        return errorTypeMap;
    }

    @Override
    protected List<ExecutionMonitor> getMonitorList() {
        List<ExecutionMonitor> monitorList = new ArrayList<>();
        for (RedisPrometheusCollectorConfiguration configuration : configurationList) {
            monitorList.add(SubscriberMonitorFactory.get(configuration.getHostList().get(0)));
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
