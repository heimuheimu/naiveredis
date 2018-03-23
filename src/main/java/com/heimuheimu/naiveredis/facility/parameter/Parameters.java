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

package com.heimuheimu.naiveredis.facility.parameter;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * 提供参数有效性检查的常用方法。
 *
 * @author heimuheimu
 */
public class Parameters {

    /**
     * 判断数字类型的参数值是否小于等于 0。
     *
     * @param parameterValue 参数值
     * @return 是否小于等于 0
     */
    public static boolean isEqualOrLessThanZero(Object parameterValue) {
        Number number = (Number) parameterValue;
        return number.doubleValue() <= 0d;
    }

    /**
     * 判断参数值是否为 {@code null}。
     *
     * @param parameterValue 参数值
     * @return 是否为 {@code null}
     */
    public static boolean isNull(Object parameterValue) {
        return parameterValue == null;
    }

    /**
     * 判断参数值是否为空。如果满足以下条件中的一条，将会返回 {@code true}：
     * <ul>
     *     <li>参数值为 {@code null}</li>
     *     <li>参数类型为 {@link String}，并且 {@link String#isEmpty()} 返回 {@code true}</li>
     *     <li>参数值为长度为 0 的数组</li>
     *     <li>参数类型为 {@link Collection}，并且 {@link Collection#isEmpty()} 返回 {@code true}</li>
     *     <li>参数类型为 {@link Map}，并且 {@link Map#isEmpty()} 返回 {@code true}</li>
     * </ul>
     *
     * @param parameterValue 参数值
     * @return 是否为空
     */
    public static boolean isEmpty(Object parameterValue) {
        if (parameterValue == null) {
            return true;
        } else if (parameterValue instanceof String) {
            return ((String) parameterValue).isEmpty();
        } else if (parameterValue.getClass().isArray()) {
            return Array.getLength(parameterValue) == 0;
        } else if (parameterValue instanceof Collection) {
            return ((Collection) parameterValue).isEmpty();
        } else if (parameterValue instanceof Map) {
            return ((Map) parameterValue).isEmpty();
        } else {
            return false;
        }
    }

    /**
     * 如果 {@code errorMessage} 为错误信息缩写，将返回完整错误信息，否则返回 {@code errorMessage}。
     *
     * @param parameterName 错误名称
     * @param errorMessage 错误信息
     * @return 转换后错误信息
     */
    public static String getErrorMessage(String parameterName, String errorMessage) {
        if ("isEqualOrLessThanZero".equals(errorMessage)) {
            return parameterName + " could not be equal or less than 0";
        } else if ("isNull".equals(errorMessage)) {
            return parameterName + " could not be null";
        } else if ("isEmpty".equals(errorMessage)) {
            return parameterName + " could not be empty";
        } else {
            return errorMessage;
        }
    }
}
