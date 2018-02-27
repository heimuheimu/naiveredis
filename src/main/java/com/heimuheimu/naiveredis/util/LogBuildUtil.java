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

package com.heimuheimu.naiveredis.util;

import java.util.Map;

/**
 * 日志信息输出构造器。
 *
 * @author heimuheimu
 */
public class LogBuildUtil {

    /**
     * 根据 {@code Map} 信息构造一个供日志输出使用的文本信息。
     *
     * @param data {@code Map} 信息
     * @return 供日志输出使用的文本信息
     */
    public static String build(Map<String, Object> data) {
        StringBuilder buffer = new StringBuilder();
        if (data != null && !data.isEmpty()) {
            for (String key : data.keySet()) {
                buffer.append(" `").append(key).append("`:`").append(data.get(key)).append("`.");
            }
        }
        return buffer.toString();
    }
}
