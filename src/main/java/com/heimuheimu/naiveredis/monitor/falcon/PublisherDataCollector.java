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
import com.heimuheimu.naiveredis.monitor.PublisherMonitorFactory;

import java.util.*;

/**
 * Redis 消息发布客户端使用的执行信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 * <ul>
 *     <li>naiveredis_publisher_publish_error/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生的消息发布失败次数</li>
 *     <li>naiveredis_publisher_no_client/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生的消息订阅者数量为 0 的次数</li>
 *     <li>naiveredis_publisher_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发布消息总数</li>
 *     <li>naiveredis_publisher_tps/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒平均发布消息数量</li>
 *     <li>naiveredis_publisher_peak_tps/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒最大发布消息数量</li>
 *     <li>naiveredis_publisher_avg_exec_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发布单条消息平均执行时间</li>
 *     <li>naiveredis_publisher_max_exec_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发布单条消息最大执行时间</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class PublisherDataCollector extends AbstractExecutionDataCollector {

    private volatile long lastExecutionCount = 0;

    private static final Map<Integer, String> ERROR_METRIC_SUFFIX_MAP;

    static {
        ERROR_METRIC_SUFFIX_MAP = new HashMap<>();
        ERROR_METRIC_SUFFIX_MAP.put(PublisherMonitorFactory.ERROR_CODE_PUBLISH_ERROR, "_publish_error");
        ERROR_METRIC_SUFFIX_MAP.put(PublisherMonitorFactory.ERROR_CODE_NO_CLIENT, "_no_client");
    }
    
    @Override
    protected List<ExecutionMonitor> getExecutionMonitorList() {
        return PublisherMonitorFactory.getAll();
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    protected String getCollectorName() {
        return "publisher";
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
