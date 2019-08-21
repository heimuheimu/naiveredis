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

package com.heimuheimu.naiveredis.command;

import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.exception.TimeoutException;

/**
 * Redis 命令，提供获取该命令的请求数据包、解析该命令的响应数据包等操作。
 *
 * <p><strong>说明：</strong>{@code Command} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface Command {

    /**
     * 获得该命令对应的请求数据包，不允许返回 {@code null}。
     *
     * @return 该命令对应的请求数据包
     */
    byte[] getRequestByteArray();

    /**
     * {@link com.heimuheimu.naiveredis.channel.RedisChannel}  在发送完命令对应的命令后，会通过该方法判断该命令是否需要继续接收响应数据。
     *
     * @return 该命令是否需要继续接收响应数据
     */
    boolean hasResponseData();

    /**
     * {@link com.heimuheimu.naiveredis.channel.RedisChannel} 接收到该命令的响应数据后，会调用此方法进行设置。
     *
     * @param responseData 响应数据
     */
    void receiveResponseData(RedisData responseData);

    /**
     * 获得该命令对应的响应数据，该方法不会返回 {@code null}。
     *
     * <p><strong>说明：</strong>该方法为阻塞式，调用后将会等待响应数据包到达。</p>
     *
     * @param timeout 超时时间，单位：毫秒
     * @return 该命令对应的响应数据，不会返回 {@code null}
     * @throws TimeoutException 等待响应数据超时，将抛出此异常
     * @throws IllegalStateException 等待响应数据过程中，命令被关闭，将抛出此异常
     */
    RedisData getResponseData(long timeout) throws TimeoutException, IllegalStateException;

    /**
     * 关闭该命令，如果该命令处于等待响应数据包状态，应立刻释放。
     */
    void close();
}
