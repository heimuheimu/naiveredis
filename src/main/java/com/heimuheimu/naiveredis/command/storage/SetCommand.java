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

package com.heimuheimu.naiveredis.command.storage;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.constant.SetMode;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

/**
 * Redis SET 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/set">https://redis.io/commands/set</a>
 *
 * <p><strong>说明：</strong>{@code SetCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SetCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis SET 命令，SET 模式为 {@link SetMode#SET_ANYWAY}。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param value Redis value
     * @param seconds 有效时间，单位：秒，如果小于等于 0，则不进行设置
     * @throws IllegalArgumentException 如果 Redis key 为 {@code null} 或空字符串，将抛出此异常
     */
    public SetCommand(String key, byte[] value, int seconds) throws IllegalArgumentException {
        this(key, value, seconds, SetMode.SET_ANYWAY);
    }

    /**
     * 构造一个 Redis SET 命令。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param value Redis value
     * @param seconds 有效时间，单位：秒，如果小于等于 0，则不进行设置
     * @param mode SET 命令使用的模式，如果为 {@code null}，则默认为 {@link SetMode#SET_ANYWAY}
     * @throws IllegalArgumentException 如果 Redis key 为 {@code null} 或空字符串，将抛出此异常
     */
    public SetCommand(String key, byte[] value, int seconds, SetMode mode) {
        if (mode == null) {
            mode = SetMode.SET_ANYWAY;
        }

        ConstructorParameterChecker checker = new ConstructorParameterChecker("SetCommand", null);
        checker.addParameter("key", key);

        checker.check("key", "isEmpty", Parameters::isEmpty);
        int commandDataArrayLength = seconds > 0 ? 5 : 3;
        if (mode != SetMode.SET_ANYWAY) {
            commandDataArrayLength += 1;
        }
        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[commandDataArrayLength];
        commandDataArray[arrayIndex++] = new RedisBulkString("SET".getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(key.getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(value);
        if (seconds > 0) {
            commandDataArray[arrayIndex++] = new RedisBulkString("EX".getBytes(RedisData.UTF8));
            commandDataArray[arrayIndex++] = new RedisBulkString(String.valueOf(seconds).getBytes(RedisData.UTF8));
        }
        if (mode != SetMode.SET_ANYWAY) {
            if (mode == SetMode.SET_IF_ABSENT) {
                commandDataArray[arrayIndex] = new RedisBulkString("NX".getBytes(RedisData.UTF8));
            } else if (mode == SetMode.SET_IF_EXIST) {
                commandDataArray[arrayIndex] = new RedisBulkString("XX".getBytes(RedisData.UTF8));
            }
        }
        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
