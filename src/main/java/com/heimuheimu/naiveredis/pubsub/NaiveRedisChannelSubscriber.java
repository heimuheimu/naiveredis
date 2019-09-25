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

package com.heimuheimu.naiveredis.pubsub;

import java.util.List;

/**
 * Redis Channel 消息订阅者，更多信息请参考：<a href="https://redis.io/topics/pubsub"> Redis Pub/Sub </a>
 *
 * <p><strong>说明：</strong>NaiveRedisChannelSubscriber 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisChannelSubscriber {

    /**
     * 获得当前订阅者订阅的 Channel 列表，不允许返回 {@code null} 或空。
     *
     * @return 订阅的 Channel 列表，不允许返回 {@code null} 或空
     */
    List<String> getChannelList();

    /**
     * 当收到已订阅的 Channel 消息时，将会触发此方法。
     *
     * @param channel 消息所属的 channel
     * @param message 消息
     * @param <T> 消息类型
     */
    <T> void consume(String channel, T message);
}
