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
 * GEO 根据圆心查找的附近成员信息。
 *
 * @author heimuheimu
 */
public class GeoNeighbour {

    /**
     * 附近的成员
     */
    private String member = "";

    /**
     * 附近成员与圆心的距离
     */
    private double distance = 0;

    /**
     * 附近成员与圆心的距离单位
     */
    private GeoDistanceUnit distanceUnit = GeoDistanceUnit.M;

    /**
     * 附近成员的经纬度信息，默认为 {@code null}
     */
    private GeoCoordinate coordinate = null;

    /**
     * 获得附近的成员。
     *
     * @return 附近的成员
     */
    public String getMember() {
        return member;
    }

    /**
     * 设置附近的成员。
     *
     * @param member 附近的成员
     */
    public void setMember(String member) {
        this.member = member;
    }

    /**
     * 获得附近成员与圆心的距离。
     *
     * @return 附近成员与圆心的距离
     */
    public double getDistance() {
        return distance;
    }

    /**
     * 设置附近成员与圆心的距离。
     *
     * @param distance 附近成员与圆心的距离
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * 获得附近成员与圆心的距离单位。
     *
     * @return 附近成员与圆心的距离单位
     */
    public GeoDistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    /**
     * 设置附近成员与圆心的距离单位。
     *
     * @param distanceUnit 附近成员与圆心的距离单位
     */
    public void setDistanceUnit(GeoDistanceUnit distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    /**
     * 获得附近成员的经纬度信息，默认为 {@code null}。
     *
     * @return 附近成员的经纬度信息
     */
    public GeoCoordinate getCoordinate() {
        return coordinate;
    }

    /**
     * 设置附近成员的经纬度信息。
     *
     * @param coordinate 附近成员的经纬度信息
     */
    public void setCoordinate(GeoCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public String toString() {
        return "GeoNeighbour{" +
                "member='" + member + '\'' +
                ", distance=" + distance +
                ", distanceUnit=" + distanceUnit +
                ", coordinate=" + coordinate +
                '}';
    }
}
