/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 heimuheimu
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

package com.heimuheimu.naiveredis.command.keys;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

/**
 * Redis 定制删除命令，当 Key 在 Redis 中存在，并且存储的 Value 与指定的 Value 一致，才执行删除操作，否则不执行任何操作。
 * 该命令通过以下 Lua 脚本实现：
 * <blockquote>
 * <pre>
 *     if redis.call("get",KEYS[1]) == ARGV[1] then
 *         return redis.call("del",KEYS[1])
 *     else
 *         return 0
 *     end
 * </pre>
 * </blockquote>
 *
 * <p><strong>说明：</strong>CustomDeleteCommand 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class CustomDeleteCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis 定制删除命令，当 Key 在 Redis 中存在，并且存储的 Value 与指定的 Value 一致，才执行删除操作，否则不执行任何操作。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param value Redis value，不允许为 {@code null} 或空数组
     * @throws IllegalArgumentException 如果 Redis key 为 {@code null} 或空字符串，将抛出此异常
     * @throws IllegalArgumentException 如果 Redis value 为 {@code null} 或空数组，将抛出此异常
     */
    public CustomDeleteCommand(String key, byte[] value) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("CustomDeleteCommand", null);
        checker.addParameter("key", key);
        checker.addParameter("value", value);
        checker.check("key", "isEmpty", Parameters::isEmpty);
        checker.check("value", "isEmpty", Parameters::isEmpty);

        RedisData[] commandDataArray = new RedisData[5];
        commandDataArray[0] = new RedisBulkString("EVAL".getBytes(RedisData.UTF8));
        commandDataArray[1] = new RedisBulkString("if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end".getBytes(RedisData.UTF8));
        commandDataArray[2] = new RedisBulkString("1".getBytes(RedisData.UTF8));
        commandDataArray[3] = new RedisBulkString(key.getBytes(RedisData.UTF8));
        commandDataArray[4] = new RedisBulkString(value);
        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
