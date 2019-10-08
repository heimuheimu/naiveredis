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


package com.heimuheimu.naiveredis.command.pubsub;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.Set;

/**
 * Redis PSUBSCRIBE 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/psubscribe">https://redis.io/commands/psubscribe</a>
 *
 * <p><strong>说明：</strong>{@code PSubscribeCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class PSubscribeCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis PSUBSCRIBE 命令。
     *
     * @param patternSet Redis pattern 列表，不允许为 {@code null} 或空，也不允许包含为 {@code null} 或空字符串的 pattern
     * @throws IllegalArgumentException 如果 Redis pattern 列表为 {@code null} 或空，或者列表中包含 {@code null} 或空字符串的 pattern，将抛出此异常
     */
    public PSubscribeCommand(Set<String> patternSet) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("PSubscribeCommand", null);
        checker.addParameter("patternSet", patternSet);

        checker.check("patternSet", "isEmpty", Parameters::isEmpty);

        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[1 + patternSet.size()];
        commandDataArray[arrayIndex++] = new RedisBulkString("PSUBSCRIBE".getBytes(RedisData.UTF8));

        for(String pattern : patternSet) {
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("Create `PSubscribeCommand` failed: `patternSet could not contain empty pattern`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            commandDataArray[arrayIndex++] = new RedisBulkString(pattern.getBytes(RedisData.UTF8));
        }

        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
