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

package com.heimuheimu.naiveredis.constant;

/**
 * Redis 客户端方法定义枚举类。
 *
 * @author heimuheimu
 */
public enum RedisClientMethod {

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#get(String)
     */
    GET("#get(String key)"),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#set(String, Object)
     */
    SET("#set(String key, Object value)"),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#set(String, Object, int)
     */
    SET_WITH_EXPIRE("#set(String key, Object value, int expiry)"),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#expire(String, int)
     */
    EXPIRE("#expire(String key, int expiry)"),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getCount(String)
     */
    GET_COUNT("#getCount(String key)"),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#addAndGet(String, long, int)
     */
    ADD_AND_GET("#addAndGet(String key, long delta, int expiry)");

    private final String methodName;

    RedisClientMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
