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

import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

/**
 * GEO 附近成员查询参数。
 *
 * @author heimuheimu
 */
public class GeoSearchParameter {

    /**
     * 附近成员排序方式：不排序
     */
    public static final String ORDER_BY_EMPTY = "";

    /**
     * 附近成员排序方式：按距离从小到大排序
     */
    public static final String ORDER_BY_ASC = "ASC";

    /**
     * 附近成员排序方式：按距离从大到小排序
     */
    public static final String ORDER_BY_DESC = "DESC";

    /**
     * 需要查找的半径范围
     */
    private final double radius;

    /**
     * 半径距离单位
     */
    private final GeoDistanceUnit distanceUnit;

    /**
     * 需要获取的附近成员数量，如果小于等于 0，则全部获取，默认为 0
     */
    private int count = 0;

    /**
     * 是否需要附近成员的经纬度信息，默认为 {@code false}
     */
    private boolean needCoordinate = false;

    /**
     * 是否需要附近成员的距离信息，默认为 {@code true}
     */
    private boolean needDistance = true;

    /**
     * 附近成员排序方式，默认为没有排序
     */
    private String orderBy = ORDER_BY_EMPTY;

    /**
     * 构造一个 GEO 附近成员查询参数。
     *
     * @param radius 需要查找的半径范围，不允许小于等于 0
     * @param distanceUnit 半径距离单位，不允许为 {@code null}
     */
    public GeoSearchParameter(double radius, GeoDistanceUnit distanceUnit) {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("GeoSearchParameter", null);
        checker.addParameter("radius", radius);
        checker.addParameter("distanceUnit", distanceUnit);

        checker.check("radius", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);
        checker.check("distanceUnit", "isNull", Parameters::isNull);

        this.radius = radius;
        this.distanceUnit = distanceUnit;
    }

    /**
     * 获得需要查找的半径范围。
     *
     * @return 需要查找的半径范围
     */
    public double getRadius() {
        return radius;
    }

    /**
     * 获得半径距离单位，不会返回 {@code null}。
     *
     * @return 半径距离单位
     */
    public GeoDistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    /**
     * 获得需要获取的附近成员数量，如果小于等于 0，则全部获取，默认为 0。
     *
     * @return 需要获取的附近成员数量
     */
    public int getCount() {
        return count;
    }

    /**
     * 设置需要获取的附近成员数量，如果小于等于 0，则全部获取。
     *
     * @param count 需要获取的附近成员数量
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 是否需要附近成员的经纬度信息，默认为 {@code false}。
     *
     * @return 是否需要附近成员的经纬度信息
     */
    public boolean isNeedCoordinate() {
        return needCoordinate;
    }

    /**
     * 设置是否需要附近成员的经纬度信息。
     *
     * @param needCoordinate 是否需要附近成员的经纬度信息
     */
    public void setNeedCoordinate(boolean needCoordinate) {
        this.needCoordinate = needCoordinate;
    }

    /**
     * 是否需要附近成员的距离信息，默认为 {@code true}。
     *
     * @return 是否需要附近成员的距离信息
     */
    public boolean isNeedDistance() {
        return needDistance;
    }

    /**
     * 设置是否需要附近成员的距离信息。
     *
     * @param needDistance 是否需要附近成员的距离信息
     */
    public void setNeedDistance(boolean needDistance) {
        this.needDistance = needDistance;
    }

    /**
     * 获得附近成员排序方式，默认为没有排序：{@link #ORDER_BY_EMPTY}。
     *
     * @return 附近成员排序方式
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * 设置附近成员排序方式。
     *
     * @param orderBy 附近成员排序方式
     * @see #ORDER_BY_EMPTY
     * @see #ORDER_BY_ASC
     * @see #ORDER_BY_DESC
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public String toString() {
        return "GeoSearchParameter{" +
                "radius=" + radius +
                ", distanceUnit=" + distanceUnit +
                ", count=" + count +
                ", needCoordinate=" + needCoordinate +
                ", needDistance=" + needDistance +
                ", orderBy='" + orderBy + '\'' +
                '}';
    }
}
