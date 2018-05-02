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
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.Set;

/**
 * Redis MGET 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/mget">https://redis.io/commands/mget</a>
 *
 * <p><strong>说明：</strong>{@code MGetCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MGetCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis MGET 命令。
     *
     * @param keySet Redis key 列表，不允许为 {@code null} 或空，也不允许包含为 {@code null} 或空字符串的 Key
     * @throws IllegalArgumentException 如果 Redis key 列表为 {@code null} 或空，或者列表中包含 {@code null} 或空字符串的 Key，将抛出此异常
     */
    public MGetCommand(Set<String> keySet) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("MGetCommand", null);
        checker.addParameter("keySet", keySet);

        checker.check("keySet", "isEmpty", Parameters::isEmpty);

        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[1 + keySet.size()];
        commandDataArray[arrayIndex++] = new RedisBulkString("MGET".getBytes(RedisData.UTF8));

        for(String key : keySet) {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("Create `MGetCommand` failed: `keySet could not contain empty key`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            commandDataArray[arrayIndex++] = new RedisBulkString(key.getBytes(RedisData.UTF8));
        }

        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
