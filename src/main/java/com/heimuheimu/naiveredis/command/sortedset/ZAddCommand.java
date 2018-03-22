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

package com.heimuheimu.naiveredis.command.sortedset;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.constant.SortedSetAddMode;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.Map;

/**
 * Redis ZADD 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/zadd">https://redis.io/commands/zadd</a>
 *
 * <p><strong>说明：</strong>{@code ZAddCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ZAddCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis ZADD 命令。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param memberMap Redis member Map，Key 为成员，Value 为成员对应的分值，不允许为 {@code null} 或空 Map
     * @param mode 成员新增模式，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis key 为 {@code null} 或空字符串，将抛出此异常
     * @throws IllegalArgumentException 如果 {@code memberMap} 为 {@code null} 或空 Map，将抛出此异常
     */
    public ZAddCommand(String key, Map<String, Double> memberMap, SortedSetAddMode mode) throws IllegalArgumentException {
        if (mode == null) {
            mode = SortedSetAddMode.REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER;
        }

        ConstructorParameterChecker checker = new ConstructorParameterChecker("ZAddCommand", null);
        checker.addParameter("key", key);
        checker.addParameter("memberMap", memberMap);
        checker.addParameter("mode", mode);

        checker.check("key", "isEmpty", Parameters::isEmpty);
        checker.check("memberMap", "isEmpty", Parameters::isEmpty);

        int commandDataArrayLength = 2 + memberMap.size() * 2;
        if (mode == SortedSetAddMode.ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER ||
                mode == SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER) {
            commandDataArrayLength += 1;
        }
        if (mode.isReturnUpdatedElementsNumber()) {
            commandDataArrayLength += 1;
        }

        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[commandDataArrayLength];
        commandDataArray[arrayIndex++] = new RedisBulkString("ZADD".getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(key.getBytes(RedisData.UTF8));

        if (mode == SortedSetAddMode.ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER) {
            commandDataArray[arrayIndex++] = new RedisBulkString("NX".getBytes(RedisData.UTF8));
        } else if (mode == SortedSetAddMode.ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER) {
            commandDataArray[arrayIndex++] = new RedisBulkString("XX".getBytes(RedisData.UTF8));
        }

        if (mode.isReturnUpdatedElementsNumber()) {
            commandDataArray[arrayIndex++] = new RedisBulkString("CH".getBytes(RedisData.UTF8));
        }

        for (String member : memberMap.keySet()) {
            if (member == null) {
                throw new IllegalArgumentException("Create `ZAddCommand` failed: `memberMap could not contain null key`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            Double score = memberMap.get(member);
            if (score == null) {
                score = 0d;
            }
            commandDataArray[arrayIndex++] = new RedisBulkString(score.toString().getBytes(RedisData.UTF8));
            commandDataArray[arrayIndex++] = new RedisBulkString(member.getBytes(RedisData.UTF8));
        }

        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
