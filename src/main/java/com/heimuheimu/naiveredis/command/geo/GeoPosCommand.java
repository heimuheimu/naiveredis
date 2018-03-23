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
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.Collection;

/**
 * Redis GEOPOS 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/geopos">https://redis.io/commands/geopos</a>
 *
 * <p><strong>说明：</strong>{@code GeoPosCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class GeoPosCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis GEOPOS 命令。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param members 需要获取经纬度信息的成员列表，不允许为 {@code null} 或空，不允许包含 {@code null} 的成员
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code members} 包含 {@code null} 的成员，将会抛出此异常
     */
    public GeoPosCommand(String key, Collection<String> members) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("GeoPosCommand", null);
        checker.addParameter("key", key);
        checker.addParameter("members", members);

        checker.check("key", "isEmpty", Parameters::isEmpty);
        checker.check("members", "isEmpty", Parameters::isEmpty);

        int commandDataArrayLength = 2 + members.size();
        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[commandDataArrayLength];
        commandDataArray[arrayIndex++] = new RedisBulkString("GEOPOS".getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(key.getBytes(RedisData.UTF8));

        for (String member : members) {
            if (member == null) {
                throw new IllegalArgumentException("Create `GeoPosCommand` failed: `members could not contain null member`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            commandDataArray[arrayIndex++] = new RedisBulkString(member.getBytes(RedisData.UTF8));
        }

        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
