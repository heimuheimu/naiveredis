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
 * Redis Sorted SET 成员新增模式。
 *
 * @author heimuheimu
 */
public enum SortedSetAddMode {

    /**
     * 如果成员不存在，执行新增操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功添加的成员个数（不包括更新分值的成员）
     */
    REPLACE_AND_RETURN_NEW_ELEMENTS_NUMBER(false),

    /**
     * 如果成员不存在，执行新增操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功添加或更新的成员个数
     */
    REPLACE_AND_RETURN_UPDATED_ELEMENTS_NUMBER(true),

    /**
     * 如果成员不存在，不执行任何操作，如果成员已存在，但分值不一致，则执行更新操作，操作完成后，返回成功更新的成员个数
     */
    ONLY_UPDATE_AND_RETURN_UPDATED_ELEMENTS_NUMBER(true),

    /**
     * 如果成员不存在，执行新增操作，如果成员已存在，不执行任何操作，操作完成后，返回成功添加的成员个数（不包括更新分值的成员）
     */
    ONLY_ADD_AND_RETURN_NEW_ELEMENTS_NUMBER(false);

    private final boolean isReturnUpdatedElementsNumber;

    SortedSetAddMode(boolean isReturnUpdatedElementsNumber) {
        this.isReturnUpdatedElementsNumber = isReturnUpdatedElementsNumber;
    }

    public boolean isReturnUpdatedElementsNumber() {
        return isReturnUpdatedElementsNumber;
    }
}
