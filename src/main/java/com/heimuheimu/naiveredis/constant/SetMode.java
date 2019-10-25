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

package com.heimuheimu.naiveredis.constant;

/**
 * Redis SET 命令可使用的模式，命令定义请参考文档：
 * <a href="https://redis.io/commands/set">https://redis.io/commands/set</a>
 *
 * @author heimuheimu
 */
public enum SetMode {

    /**
     * 如果 Key 不存在，执行新增操作，如果 Key 已存在，执行更新操作
     */
    SET_ANYWAY,

    /**
     * 如果 Key 不存在，执行新增操作，如果 Key 已存在，不执行任何操作
     */
    SET_IF_ABSENT,

    /**
     * 如果 Key 不存在，不执行任何操作，如果 Key 已存在，执行更新操作
     */
    SET_IF_EXIST
}
