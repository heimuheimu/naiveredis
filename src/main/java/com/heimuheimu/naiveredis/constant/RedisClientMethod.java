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

import java.util.Collection;

/**
 * Redis 客户端方法定义枚举类。
 *
 * @author heimuheimu
 */
public enum RedisClientMethod {

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#expire(String, int)
     */
    EXPIRE("#expire(String key, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#delete(String)
     */
    DELETE("#delete(String key)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#get(String)
     */
    GET("#get(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#set(String, Object)
     */
    SET("#set(String key, Object value)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#set(String, Object, int)
     */
    SET_WITH_EXPIRE("#set(String key, Object value, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getCount(String)
     */
    GET_COUNT("#getCount(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#addAndGet(String, long, int)
     */
    ADD_AND_GET("#addAndGet(String key, long delta, int expiry)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#addToSet(String, String)
     */
    ADD_TO_SET("#addToSet(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#addToSet(String, Collection)
     */
    MULTI_ADD_TO_SET("#addToSet(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#removeFromSet(String, String)
     */
    REMOVE_FROM_SET("#removeFromSet(String key, String member)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#removeFromSet(String, Collection)
     */
    MULTI_REMOVE_FROM_SET("#removeFromSet(String key, Collection<String> members)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#isMemberInSet(String, String)
     */
    IS_MEMBER_IN_SET("#isMemberInSet(String key, String member)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getSizeOfSet(String)
     */
    GET_SIZE_OF_SET("#getSizeOfSet(String key)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getMembersFromSet(String, int)
     */
    GET_MEMBERS_FROM_SET("#getMembersFromSet(String key, int count)", true),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#popMembersFromSet(String, int)
     */
    POP_MEMBERS_FROM_SET("#popMembersFromSet(String key, int count)", false),

    /**
     * @see com.heimuheimu.naiveredis.NaiveRedisClient#getAllMembersFromSet(String)
     */
    GET_ALL_MEMBERS_FROM_SET("#getAllMembersFromSet(String key)", true);

    private final String methodName;

    private final boolean isReadOnly;

    RedisClientMethod(String methodName, boolean isReadOnly) {
        this.methodName = methodName;
        this.isReadOnly = isReadOnly;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }
}
