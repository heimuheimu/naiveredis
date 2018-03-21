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

package com.heimuheimu.naiveredis.command.set;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.Collection;

/**
 * Redis SREM 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/srem">https://redis.io/commands/srem</a>
 *
 * <p><strong>说明：</strong>{@code SRemCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SRemCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis SREM 命令。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param members Redis member 列表，不允许为 {@code null} 或空列表
     * @throws IllegalArgumentException 如果 Redis key 为 {@code null} 或空字符串，将抛出此异常
     * @throws IllegalArgumentException 如果 {@code members} 为 {@code null} 或空列表，将抛出此异常
     */
    public SRemCommand(String key, Collection<String> members) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("SRemCommand", null);
        checker.addParameter("key", key);
        checker.addParameter("members", members);

        checker.check("key", "isEmpty", Parameters::isEmpty);
        checker.check("members", "isEmpty", Parameters::isEmpty);

        int commandDataArrayLength = 2 + members.size();
        RedisData[] commandDataArray = new RedisData[commandDataArrayLength];
        commandDataArray[0] = new RedisBulkString("SREM".getBytes(RedisData.UTF8));
        commandDataArray[1] = new RedisBulkString(key.getBytes(RedisData.UTF8));
        int memberIndex = 2;
        for (String member : members) {
            if (member == null) {
                throw new IllegalArgumentException("Create `SRemCommand` failed: `members could not contain null element`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            commandDataArray[memberIndex++] = new RedisBulkString(member.getBytes(RedisData.UTF8));
        }
        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
