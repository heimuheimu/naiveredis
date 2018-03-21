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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Redis 命令抽象实现类。
 *
 * <p><strong>说明：</strong>{@code AbstractCommand} 的子类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public abstract class AbstractCommand implements Command {

    private final CountDownLatch latch = new CountDownLatch(1);

    private volatile RedisData responseData = null;

    @Override
    public void receiveResponseData(RedisData responseData) {
        this.responseData = responseData;
        latch.countDown();
    }

    @Override
    public RedisData getResponseData(long timeout) throws TimeoutException, IllegalStateException {
        boolean latchFlag;
        try {
            latchFlag = latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            latchFlag = false; //should not happen
        }
        if (latchFlag) {
            if (responseData != null) {
                return responseData;
            } else {
                throw new IllegalStateException("Get redis response data failed: `command has been closed`. Timeout: `"
                        + timeout + "ms`. Command: `" + this + "`.");
            }
        } else {
            throw new TimeoutException("Get redis response data failed: `wait response timeout`. Timeout: `"
                    + timeout + "ms`. Command: `" + this + "`.");
        }
    }

    @Override
    public void close() {
        latch.countDown();
    }

    protected String makeScoreToString(double score, boolean includeScore) {
        String scoreStr;
        if (score == Double.NEGATIVE_INFINITY) {
            scoreStr = "-inf";
        } else if (score == Double.POSITIVE_INFINITY) {
            scoreStr = "+inf";
        } else {
            scoreStr = String.valueOf(score);
            if (!includeScore) {
                scoreStr = "(" + scoreStr;
            }
        }
        return scoreStr;
    }
}
