/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 heimuheimu
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

import com.heimuheimu.naivemonitor.falcon.support.AbstractThreadPoolDataCollector;
import com.heimuheimu.naivemonitor.monitor.ThreadPoolMonitor;
import com.heimuheimu.naiveredis.constant.FalconDataCollectorConstant;
import com.heimuheimu.naiveredis.monitor.ThreadPoolMonitorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 客户端使用的线程池信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 * 
 * <ul>
 *     <li>naiveredis_threadPool_rejected_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内所有线程池拒绝执行的任务总数</li>
 *     <li>naiveredis_threadPool_active_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 采集时刻所有线程池活跃线程数近似值总和</li>
 *     <li>naiveredis_threadPool_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 采集时刻所有线程池线程数总和</li>
 *     <li>naiveredis_threadPool_peak_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 所有线程池出现过的最大线程数总和</li>
 *     <li>naiveredis_threadPool_core_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 所有线程池配置的核心线程数总和</li>
 *     <li>naiveredis_threadPool_maximum_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 所有线程池配置的最大线程数总和</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class ThreadPoolDataCollector extends AbstractThreadPoolDataCollector {

    private final List<ThreadPoolMonitor> threadPoolMonitorList;

    public ThreadPoolDataCollector() {
        this.threadPoolMonitorList = new ArrayList<>();
        this.threadPoolMonitorList.add(ThreadPoolMonitorFactory.get());
    }

    @Override
    protected List<ThreadPoolMonitor> getThreadPoolMonitorList() {
        return threadPoolMonitorList;
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }
}
