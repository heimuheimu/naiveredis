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
import com.heimuheimu.naiveredis.geo.GeoSearchParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis GEORADIUSBYMEMBER 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/georadiusbymember">https://redis.io/commands/georadiusbymember</a>
 *
 * <p><strong>说明：</strong>{@code GeoRadiusByMemberCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class GeoRadiusByMemberCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis GEORADIUSBYMEMBER 命令。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param member 成员，不允许为 {@code null}
     * @param geoSearchParameter GEO 附近成员查询参数，不允许为 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code member} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code geoSearchParameter} 为 {@code null}，将会抛出此异常
     */
    public GeoRadiusByMemberCommand(String key, String member, GeoSearchParameter geoSearchParameter) {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("GeoRadiusByMemberCommand", null);
        checker.addParameter("key", key);
        checker.addParameter("member", member);
        checker.addParameter("geoSearchParameter", geoSearchParameter);

        checker.check("key", "isEmpty", Parameters::isEmpty);
        checker.check("member", "isNull", Parameters::isNull);
        checker.check("geoSearchParameter", "isNull", Parameters::isNull);

        List<RedisData> commandDataList = new ArrayList<>(11);
        commandDataList.add(new RedisBulkString("GEORADIUSBYMEMBER_RO".getBytes(RedisData.UTF8)));
        commandDataList.add(new RedisBulkString(key.getBytes(RedisData.UTF8)));
        commandDataList.add(new RedisBulkString(member.getBytes(RedisData.UTF8)));
        commandDataList.add(new RedisBulkString(String.valueOf(geoSearchParameter.getRadius()).getBytes(RedisData.UTF8)));
        commandDataList.add(new RedisBulkString(geoSearchParameter.getDistanceUnit().getUnit().getBytes(RedisData.UTF8)));
        if (geoSearchParameter.isNeedCoordinate()) {
            commandDataList.add(new RedisBulkString("WITHCOORD".getBytes(RedisData.UTF8)));
        }
        if (geoSearchParameter.isNeedDistance()) {
            commandDataList.add(new RedisBulkString("WITHDIST".getBytes(RedisData.UTF8)));
        }
        if (geoSearchParameter.getCount() > 0) {
            commandDataList.add(new RedisBulkString("COUNT".getBytes(RedisData.UTF8)));
            commandDataList.add(new RedisBulkString(String.valueOf(geoSearchParameter.getCount()).getBytes(RedisData.UTF8)));
        }
        if (GeoSearchParameter.ORDER_BY_ASC.equals(geoSearchParameter.getOrderBy())) {
            commandDataList.add(new RedisBulkString("ASC".getBytes(RedisData.UTF8)));
        } else if (GeoSearchParameter.ORDER_BY_DESC.equals(geoSearchParameter.getOrderBy())) {
            commandDataList.add(new RedisBulkString("DESC".getBytes(RedisData.UTF8)));
        }
        RedisData[] commandDataArray = commandDataList.toArray(new RedisData[commandDataList.size()]);

        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
