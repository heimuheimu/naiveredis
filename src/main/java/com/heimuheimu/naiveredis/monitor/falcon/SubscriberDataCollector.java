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
import com.heimuheimu.naivemonitor.falcon.support.AbstractExecutionDataCollector;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naiveredis.constant.FalconDataCollectorConstant;
import com.heimuheimu.naiveredis.monitor.SubscriberMonitorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 订阅客户端使用的执行信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 * <ul>
 *     <li>naiveredis_subscriber_decode_error/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生的消息解码失败次数</li>
 *     <li>naiveredis_subscriber_consume_error/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生的消息消费失败次数</li>
 *     <li>naiveredis_subscriber_unexpected_error/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内订阅期间发生的预期外异常次数</li>
 *     <li>naiveredis_subscriber_slow_consumption/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生的单个 Redis 消息订阅者消费信息过慢次数</li>
 *     <li>naiveredis_subscriber_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内消费消息总数</li>
 *     <li>naiveredis_subscriber_tps/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒平均消费消息数量</li>
 *     <li>naiveredis_subscriber_peak_tps/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒最大消费消息数量</li>
 *     <li>naiveredis_subscriber_avg_exec_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条消息消费平均执行时间</li>
 *     <li>naiveredis_subscriber_max_exec_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条消息消费最大执行时间</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class SubscriberDataCollector extends AbstractExecutionDataCollector {

    private volatile long lastExecutionCount = 0;

    private static final Map<Integer, String> ERROR_METRIC_SUFFIX_MAP;

    static {
        ERROR_METRIC_SUFFIX_MAP = new HashMap<>();
        ERROR_METRIC_SUFFIX_MAP.put(SubscriberMonitorFactory.ERROR_CODE_DECODE_ERROR, "_decode_error");
        ERROR_METRIC_SUFFIX_MAP.put(SubscriberMonitorFactory.ERROR_CODE_CONSUME_ERROR, "_consume_error");
        ERROR_METRIC_SUFFIX_MAP.put(SubscriberMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR, "_unexpected_error");
        ERROR_METRIC_SUFFIX_MAP.put(SubscriberMonitorFactory.ERROR_CODE_SLOW_CONSUMPTION, "_slow_consumption");
    }

    @Override
    protected List<ExecutionMonitor> getExecutionMonitorList() {
        return SubscriberMonitorFactory.getAll();
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    protected String getCollectorName() {
        return "subscriber";
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>(super.getList());
        long totalExecutionCount = 0;
        for (ExecutionMonitor executionMonitor : getExecutionMonitorList()) {
            totalExecutionCount += executionMonitor.getTotalCount();
        }
        falconDataList.add(create("_count", totalExecutionCount - lastExecutionCount));
        lastExecutionCount = totalExecutionCount;
        return falconDataList;
    }

    @Override
    protected Map<Integer, String> getErrorMetricSuffixMap() {
        return ERROR_METRIC_SUFFIX_MAP;
    }
}
