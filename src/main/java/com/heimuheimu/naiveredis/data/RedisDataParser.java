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

package com.heimuheimu.naiveredis.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 提供常用的 Redis 数据解析方法。
 *
 * @author heimuheimu
 */
public class RedisDataParser {

    /**
     * 将 Redis 数据转换成文本类型后返回，有可能返回 {@code null}。
     *
     * @param data Redis 数据
     * @return Redis 数据对应文本
     */
    public static String parseString(RedisData data) {
        return data.getText();
    }

    /**
     * 将 Redis 数据转换成整数类型后返回，有可能返回 {@code null}。
     *
     * @param data Redis 数据
     * @return Redis 数据对应的整数值
     * @throws NumberFormatException 如果整数转换失败，将会抛出异常
     */
    public static Integer parseInt(RedisData data) throws NumberFormatException {
        if (data.getValueBytes() != null && data.getValueBytes().length > 0) {
            return Integer.valueOf(data.getText());
        } else {
            return null;
        }
    }

    /**
     * 将 Redis 数据先进行整数类型转换，然后判断值是否为 1 并返回布尔类型。
     *
     * @param data Redis 数据
     * @return Redis 数据对应的布尔值
     * @throws NumberFormatException 如果整数转换失败，将会抛出异常
     */
    public static boolean parseBoolean(RedisData data) throws NumberFormatException {
        return parseInt(data) == 1;
    }

    /**
     * 将 Redis 数据转换成 String 类型的列表后返回，该方法不会返回 {@code null}。
     *
     * @param data Redis 数据
     * @return String 类型的列表
     * @throws UnsupportedOperationException 如果 Redis 数据类型不是数组类型，将会抛出此异常
     */
    public static List<String> parseStringList(RedisData data) throws UnsupportedOperationException {
        List<String> members = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            members.add(data.get(i).getText());
        }
        return members;
    }

    /**
     * 将 Redis 数据转换成 Double 类型的列表后返回，有可能返回 {@code null}。
     *
     * @param data Redis 数据
     * @return Redis 数据对应的 Double 值
     * @throws NumberFormatException 如果 Double 转换失败，将会抛出异常
     */
    public static Double parseDouble(RedisData data) throws NumberFormatException {
        if (data.getValueBytes() != null && data.getValueBytes().length > 0) {
            return toDouble(data.getText());
        } else {
            return null;
        }
    }

    /**
     * 将 Redis 数据转换成 Sorted SET 成员 Map，Key 为成员，Value 为成员对应的分值。
     *
     * @param data Redis 数据
     * @return Sorted SET 成员 Map，Key 为成员，Value 为成员对应的分值
     * @throws UnsupportedOperationException 如果 Redis 数据类型不是数组类型，将会抛出此异常
     * @throws NumberFormatException 如果分值转换失败，将会抛出异常
     */
    public static LinkedHashMap<String, Double> parseMemberMapForSortedSet(RedisData data)
            throws UnsupportedOperationException, NumberFormatException {
        LinkedHashMap<String, Double> memberMap = new LinkedHashMap<>();
        for (int i = 0; i < data.size(); i += 2) {
            String member = data.get(i).getText();
            Double score = toDouble(data.get(i + 1).getText());
            memberMap.put(member, score);
        }
        return memberMap;
    }

    private static Double toDouble(String doubleStr) throws NumberFormatException {
        if ("inf".equals(doubleStr)) {
            return Double.POSITIVE_INFINITY;
        } else if ("-inf".equals(doubleStr)) {
            return Double.NEGATIVE_INFINITY;
        } else {
            return Double.valueOf(doubleStr);
        }
    }
}
