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

package com.heimuheimu.naiveredis.command.cluster;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.command.Command;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

/**
 * Redis ASKING 命令，在 Redis Cluster 中遇到 ASK 重定向错误时使用，关于更多信息请参考文档：
 * <a href="https://redis.io/topics/cluster-spec#ask-redirection">ASK redirection</a>
 *
 * @author heimuheimu
 */
public class AskingCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    private volatile boolean hasReceivedAskingCommandResponse = false;

    /**
     * 构造一个 Redis ASKING 命令。
     *
     * @param wrappedCommand 被封装的 Redis 命令，不允许为 {@code null}，不允许为 AskingCommand 实例
     * @throws IllegalArgumentException 如果 wrappedCommand 为 {@code null} 或为 AskingCommand 实例，将会抛出此异常
     */
    public AskingCommand(Command wrappedCommand) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("AskingCommand", null);
        checker.addParameter("wrappedCommand", wrappedCommand);
        checker.check("wrappedCommand", "isNull", Parameters::isNull);
        checker.check("wrappedCommand", "wrappedCommand could not be AskingCommand",
                checkedCommand -> checkedCommand instanceof AskingCommand);

        RedisData[] askingCommandDataArray = new RedisData[1];
        askingCommandDataArray[0] = new RedisBulkString("ASKING".getBytes(RedisData.UTF8));
        byte[] askingCommandRequestByteArray = new RedisArray(askingCommandDataArray).getRespByteArray();
        byte[] wrappedCommandRequestByteArray = wrappedCommand.getRequestByteArray();
        this.requestByteArray = new byte[askingCommandRequestByteArray.length + wrappedCommandRequestByteArray.length];
        System.arraycopy(askingCommandRequestByteArray, 0, this.requestByteArray, 0, askingCommandRequestByteArray.length);
        System.arraycopy(wrappedCommandRequestByteArray, 0, this.requestByteArray, askingCommandRequestByteArray.length, wrappedCommandRequestByteArray.length);
    }

    @Override
    public void receiveResponseData(RedisData responseData) {
        if (!hasReceivedAskingCommandResponse) {
            if (responseData.isError()) {
                throw new IllegalStateException("Fails to execute ASKING command: `" +responseData.getText() + "`.");
            } else {
                hasReceivedAskingCommandResponse = true;
            }
        } else {
            super.receiveResponseData(responseData);
        }
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
