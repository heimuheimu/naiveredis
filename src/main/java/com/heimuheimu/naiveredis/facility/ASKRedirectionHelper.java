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

package com.heimuheimu.naiveredis.facility;

/**
 * ASK 重定向处理帮助类，关于更多信息请参考文档：<a href="https://redis.io/topics/cluster-spec#ask-redirection">ASK redirection</a>
 *
 * @author heimuheimu
 */
public class ASKRedirectionHelper {

    private static final ThreadLocal<Boolean> ASK_REDIRECTION_HOST_HOLDER = new ThreadLocal<>();

    /**
     * 调用此方法后，在相同线程中执行 Redis 方法前都会发起 ASKING 命令。
     */
    public static void onASKRedirection() {
        ASK_REDIRECTION_HOST_HOLDER.set(Boolean.TRUE);
    }

    /**
     * 判断在执行 Redis 方法前是否需要发起 ASKING 命令。
     *
     * @return 执行 Redis 方法前是否需要发起 ASKING 命令
     */
    public static boolean isASKRedirection() {
        return ASK_REDIRECTION_HOST_HOLDER.get() != null;
    }

    /**
     * 调用此方法后，在相同线程中执行 Redis 方法前不会发起 ASKING 命令。
     */
    public static void removeASKRedirection() {
        ASK_REDIRECTION_HOST_HOLDER.remove();
    }
}
