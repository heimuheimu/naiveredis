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

import java.util.Objects;

/**
 * 经纬度坐标信息。
 *
 * @author heimuheimu
 */
public class GeoCoordinate {

    /**
     * 经度，有效区间范围：-180 至 180
     */
    private final double longitude;

    /**
     * 纬度，有效区间范围：-85.05112878 至 85.05112878
     */
    private final double latitude;

    /**
     * 构造一个经纬度坐标信息。
     *
     * @param longitude 经度，有效区间范围：-180 至 180
     * @param latitude 纬度，有效区间范围：-85.05112878 至 85.05112878
     */
    public GeoCoordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * 获得经度信息。
     *
     * @return 经度信息
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * 获得纬度信息。
     *
     * @return 纬度信息
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * 判断当前已设置的经纬度是否在有效区间范围。
     *
     * @return 当前经纬度是否在有效区间范围
     */
    public boolean isValid() {
        return longitude >= -180d && longitude <= 180d && latitude >= -85.05112878d && latitude <= 85.05112878;
    }

    @Override
    public String toString() {
        return "GeoCoordinate{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoCoordinate that = (GeoCoordinate) o;
        return Double.compare(that.longitude, longitude) == 0 &&
                Double.compare(that.latitude, latitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }
}
