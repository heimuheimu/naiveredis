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

/**
 * Redis 分布式锁信息，同一时刻仅允许一个持有者获得相同名称的锁。
 *
 * <p><strong>说明：</strong>LockInfo 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class LockInfo {

    /**
     * 锁名称，同一时刻仅允许一个持有者获得相同名称的锁
     */
    private final String name;

    /**
     * 锁对应的 token 值，每次锁获取成功时，都会生成一个唯一的 token 值
     */
    private final String token;

    /**
     * 锁的最大持有时间，单位：秒
     */
    private final int validity;

    /**
     * 锁的创建时间戳
     */
    private final long createdTime;

    /**
     * 构造一个 LockInfo 对象。
     *
     * @param name 锁名称，同一时刻仅允许一个持有者获得相同名称的锁
     * @param token 锁对应的 token 值，每次锁获取成功时，都会生成一个唯一的 token 值
     * @param validity 锁的最大持有时间，单位：秒
     */
    public LockInfo(String name, String token, int validity) {
        this.name = name;
        this.token = token;
        this.validity = validity;
        this.createdTime = System.currentTimeMillis();
    }

    /**
     * 获得锁名称，同一时刻仅允许一个持有者获得相同名称的锁。
     *
     * @return 锁名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获得锁对应的 token 值，每次锁获取成功时，都会生成一个唯一的 token 值。
     *
     * @return 锁对应的 token 值
     */
    public String getToken() {
        return token;
    }

    /**
     * 获得锁的最大持有时间，单位：秒。
     *
     * @return 锁的最大持有时间，单位：秒
     */
    public int getValidity() {
        return validity;
    }

    /**
     * 获得锁的创建时间戳。
     *
     * @return 锁的创建时间戳
     */
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     * 通过锁的创建时间和最大持有时间判断当前时刻锁是否仍然有效，如果有效，返回 {@code true}，否则返回 {@code false}。
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return System.currentTimeMillis() - createdTime < validity * 1000L;
    }

    @Override
    public String toString() {
        return "LockInfo{" +
                "name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", validity=" + validity +
                ", createdTime=" + createdTime +
                '}';
    }
}
