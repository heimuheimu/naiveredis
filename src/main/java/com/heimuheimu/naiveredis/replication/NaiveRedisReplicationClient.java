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

package com.heimuheimu.naiveredis.replication;

import com.heimuheimu.naiveredis.NaiveRedisClient;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

/**
 * 支持 Redis master-slave 复制模式的客户端，默认由 Master 服务执行变更操作，所有的 Slave 服务执行读取操作（轮询策略）。
 *
 * <p><strong>说明：</strong>{@code NaiveRedisReplicationClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface NaiveRedisReplicationClient extends NaiveRedisClient {

    /**
     * 获取 Key 对应的值，如果 Key 不存在，则返回 {@code null}。
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param useMaster 是否从 Master 服务获取
     * @param <T> Value 类型
     * @return Key 对应的值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    <T> T get(String key, boolean useMaster) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
