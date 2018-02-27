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

package com.heimuheimu.naiveredis.exception;

/**
 * Redis 命令执行出错时，将抛出此异常。出错原因包含以下两种可能性，可由错误码进行识别：
 * <ul>
 *     <li>接收到 Redis 服务返回的错误信息，错误码为 {@link #CODE_REDIS_SERVER}</li>
 *     <li>遇到预期外的异常，错误码为 {@link #CODE_UNEXPECTED_ERROR}，一般由 BUG 引起</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class RedisException extends RuntimeException {

    private static final long serialVersionUID = -1118221299966891736L;

    /**
     * 错误码常量：接收到 Redis 服务返回的错误信息
     */
    public static final int CODE_REDIS_SERVER = -1;

    /**
     * 错误码常量：遇到预期外的异常，一般由 BUG 引起
     */
    public static final int CODE_UNEXPECTED_ERROR = -2;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 构造一个 Redis 命令执行出错异常。
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public RedisException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造一个 Redis 命令执行出错异常。
     *
     * @param code 错误码
     * @param message 错误信息
     * @param cause 上层异常
     */
    public RedisException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获得错误码。
     *
     * @return 错误码
     * @see #CODE_REDIS_SERVER
     * @see #CODE_UNEXPECTED_ERROR
     */
    public int getCode() {
        return code;
    }
}
