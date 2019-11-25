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

package com.heimuheimu.naiveredis.lock;

import com.heimuheimu.naiveredis.exception.RedisDistributedLockException;

/**
 * Redis 分布式锁，基于 "SET lock_name token PX milliseconds NX" 命令实现。该分布式锁具备以下特点：
 * <ol>
 *     <li>在获取锁时，需指定最长持有时间来避免死锁。（最长持有时间通过 SET 命令的过期时间实现）</li>
 *     <li>当获取锁失败时，会随机延迟一段时间，避免多个端在同一时刻发起获取锁请求。</li>
 *     <li>当锁因过期被释放时，有可能存在两个端同时持有该锁的情况，业务方在使用 Redis 分布式锁时需考虑此场景。</li>
 * </ol>
 *
 * 在使用 Redis 分布式锁前，建议仔细阅读以下文档：<a href="https://redis.io/topics/distlock">Distributed locks with Redis</a>
 *
 * <p><strong>说明：</strong>RedisDistributedLock 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface RedisDistributedLock {

    /**
     * 尝试获取指定名称的锁，如果获取成功，将会返回对应的锁信息，如果锁已被占用，将会返回 {@code null}，
     * 通过此方法获取锁的配置信息为 {@link LockConfiguration#DEFAULT}。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @return 锁信息，可能为 {@code null}
     * @throws RedisDistributedLockException 如果获取锁时出错，将会抛出此异常
     */
    LockInfo tryLock(String name) throws RedisDistributedLockException;

    /**
     * 尝试获取指定名称的锁，如果获取成功，将会返回对应的锁信息，如果锁已被占用，将会返回 {@code null}，
     * 如果 configuration 为 {@code null}，将会默认使用 {@link LockConfiguration#DEFAULT}。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @param configuration 锁配置信息，允许为 {@code null}
     * @return 锁信息，可能为 {@code null}
     * @throws RedisDistributedLockException 如果获取锁时出错，将会抛出此异常
     */
    LockInfo tryLock(String name, LockConfiguration configuration) throws RedisDistributedLockException;

    /**
     * 尝试获取指定名称的锁，如果获取成功，将会返回对应的锁信息，如果锁已被占用，将会在 waitTime 指定的时间内不断重试，直至锁被获取成功后返回，
     * 如果超过 waitTime 指定的时间仍未获取成功，将会返回 {@code null}，通过此方法获取锁的配置信息为 {@link LockConfiguration#DEFAULT}。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @param waitTime 最大等待时间，单位：毫秒
     * @return 锁信息，可能为 {@code null}
     * @throws RedisDistributedLockException 如果获取锁时出错，将会抛出此异常
     */
    LockInfo tryLock(String name, int waitTime) throws RedisDistributedLockException;

    /**
     * 尝试获取指定名称的锁，如果获取成功，将会返回对应的锁信息，如果锁已被占用，将会在 waitTime 指定的时间内不断重试，直至锁被获取成功后返回，
     * 如果超过 waitTime 指定的时间仍未获取成功，将会返回 {@code null}，如果 configuration 为 {@code null}，
     * 将会默认使用 {@link LockConfiguration#DEFAULT}。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @param configuration 锁配置信息，允许为 {@code null}
     * @param waitTime 最大等待时间，单位：毫秒
     * @return 锁信息，可能为 {@code null}
     */
    LockInfo tryLock(String name, LockConfiguration configuration, int waitTime) throws RedisDistributedLockException;

    /**
     * 释放已获取的锁，如果 lockInfo 为 {@code null}，则不执行任何操作。
     *
     * @param lockInfo 锁信息，允许为 {@code null}
     * @throws RedisDistributedLockException 如果释放锁时出错，将会抛出此异常
     */
    void unlock(LockInfo lockInfo) throws RedisDistributedLockException;
}
