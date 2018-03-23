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

package com.heimuheimu.naiveredis.geo;

/**
 * 距离单位枚举类。
 *
 * @author heimuheimu
 */
public enum GeoDistanceUnit {

    /**
     * 米
     */
    M("m"),

    /**
     * 千米，1 KM = 1000 M
     */
    KM("km"),

    /**
     * 英尺，1 FT = 0.304794 M
     */
    FT("ft"),

    /**
     * 英里，1 MI = 1609.31 M
     */
    MI("mi");

    /**
     * 单位
     */
    private final String unit;

    GeoDistanceUnit(String unit) {
        this.unit = unit;
    }

    /**
     * 获得单位的字符串表达形式。
     *
     * @return 单位
     */
    public String getUnit() {
        return unit;
    }
}
