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

import com.heimuheimu.naivemonitor.falcon.support.AbstractCompressionDataCollector;
import com.heimuheimu.naivemonitor.monitor.CompressionMonitor;
import com.heimuheimu.naiveredis.constant.FalconDataCollectorConstant;
import com.heimuheimu.naiveredis.monitor.CompressionMonitorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 客户端使用的压缩信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 * <ul>
 *     <li>naiveredis_compression_reduce_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内压缩操作已节省的字节数</li>
 *     <li>naiveredis_compression_avg_reduce_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内平均每次压缩操作节省的字节数</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class CompressionDataCollector extends AbstractCompressionDataCollector {

    private final List<CompressionMonitor> compressionMonitorList;

    /**
     * 构造一个 Redis 客户端使用的压缩信息采集器。
     */
    public CompressionDataCollector() {
        this.compressionMonitorList = new ArrayList<>();
        this.compressionMonitorList.add(CompressionMonitorFactory.get());
    }

    @Override
    protected List<CompressionMonitor> getCompressionMonitorList() {
        return compressionMonitorList;
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
