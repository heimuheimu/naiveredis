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
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

/**
 * Redis ZRANGEBYSCORE 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/zrangebyscore">https://redis.io/commands/zrangebyscore</a>
 *
 * <p><strong>说明：</strong>{@code ZRangeByScoreCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ZRangeByScoreCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis ZRANGEBYSCORE 命令。
     *
     * @param key Redis key，不允许为 {@code null} 或空字符串
     * @param minScore 最小分值，{@link Double#NEGATIVE_INFINITY} 代表无穷小
     * @param includeMinScore 是否包含最小分值
     * @param maxScore 最大分值，{@link Double#POSITIVE_INFINITY} 代表无穷大
     * @param includeMaxScore 是否包含最大分值
     * @param offset 起始索引，在分页获取时使用，与 MYSQL 的 LIMIT 语法类似
     * @param count 需要获取的成员数量，在分页获取时使用，与 MYSQL 的 LIMIT 语法类似，如果小于等于 0，则获取全部成员列表
     * @param withScores 返回值是否包含成员的分值
     * @throws IllegalArgumentException 如果 Redis key 为 {@code null} 或空字符串，将抛出此异常
     */
    public ZRangeByScoreCommand(String key, double minScore, boolean includeMinScore, double maxScore,
                                boolean includeMaxScore, int offset, int count, boolean withScores) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("ZRangeByScoreCommand", null);
        checker.addParameter("key", key);
        checker.addParameter("minScore", minScore);
        checker.addParameter("includeMinScore", includeMinScore);
        checker.addParameter("maxScore", maxScore);
        checker.addParameter("includeMaxScore", includeMaxScore);
        checker.addParameter("offset", offset);
        checker.addParameter("count", count);
        checker.addParameter("withScores", withScores);

        checker.check("key", "isEmpty", Parameters::isEmpty);

        String minScoreStr = makeScoreToString(minScore, includeMinScore);

        String maxScoreStr = makeScoreToString(maxScore, includeMaxScore);

        int arrayLength = 4;
        if (withScores) {
            arrayLength += 1;
        }
        if (count > 0) {
            arrayLength += 3;
        }
        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[arrayLength];
        commandDataArray[arrayIndex++] = new RedisBulkString("ZRANGEBYSCORE".getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(key.getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(minScoreStr.getBytes(RedisData.UTF8));
        commandDataArray[arrayIndex++] = new RedisBulkString(maxScoreStr.getBytes(RedisData.UTF8));
        if (withScores) {
            commandDataArray[arrayIndex++] = new RedisBulkString("WITHSCORES".getBytes(RedisData.UTF8));
        }
        if (count > 0) {
            commandDataArray[arrayIndex++] = new RedisBulkString("LIMIT".getBytes(RedisData.UTF8));
            commandDataArray[arrayIndex++] = new RedisBulkString(String.valueOf(offset).getBytes(RedisData.UTF8));
            commandDataArray[arrayIndex++] = new RedisBulkString(String.valueOf(count).getBytes(RedisData.UTF8));
        }
        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
