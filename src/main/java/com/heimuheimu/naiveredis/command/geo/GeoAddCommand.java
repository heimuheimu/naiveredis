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

package com.heimuheimu.naiveredis.command.geo;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.geo.GeoCoordinate;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.Map;

/**
 * Redis GEOADD 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/geoadd">https://redis.io/commands/geoadd</a>
 *
 * <p><strong>说明：</strong>{@code GeoAddCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class GeoAddCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis GEOADD 命令。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param memberMap 成员 Map，Key 为成员，Value 为成员的经纬度信息，不允许为 {@code null} 或空，不允许包含 {@code null} 成员
     * @throws IllegalArgumentException 如果 Redis key 为 {@code null} 或空字符串，将抛出此异常
     * @throws IllegalArgumentException 如果成员 Map 为 {@code null} 或空，或包含 {@code null} 的成员，或经纬度值没有在有效区间范围内，将抛出此异常
     */
    public GeoAddCommand(String key, Map<String, GeoCoordinate> memberMap) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("GeoAddCommand", null);
        checker.addParameter("key", key);
        checker.addParameter("memberMap", memberMap);

        checker.check("key", "isEmpty", Parameters::isEmpty);
        checker.check("memberMap", "isEmpty", Parameters::isEmpty);

        int commandDataArrayLength = 2 + memberMap.size() * 3;
        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[commandDataArrayLength];
        commandDataArray[arrayIndex++] = new RedisBulkString("GEOADD".getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(key.getBytes(RedisData.UTF8));

        for (String member : memberMap.keySet()) {
            if (member == null) {
                throw new IllegalArgumentException("Create `GeoAddCommand` failed: `member could not be null`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            GeoCoordinate coordinate = memberMap.get(member);
            if (coordinate == null || !coordinate.isValid()) {
                throw new IllegalArgumentException("Create `GeoAddCommand` failed: `invalid geo coordinate " + coordinate + "`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            String longitude = String.valueOf(coordinate.getLongitude());
            String latitude = String.valueOf(coordinate.getLatitude());
            commandDataArray[arrayIndex++] = new RedisBulkString(longitude.getBytes(RedisData.UTF8));
            commandDataArray[arrayIndex++] = new RedisBulkString(latitude.getBytes(RedisData.UTF8));
            commandDataArray[arrayIndex++] = new RedisBulkString(member.getBytes(RedisData.UTF8));
        }

        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
