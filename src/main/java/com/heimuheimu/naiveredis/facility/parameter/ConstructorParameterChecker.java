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

package com.heimuheimu.naiveredis.facility.parameter;

import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 构造函数参数有效性检查器。
 *
 * <p><strong>说明：</strong>{@code ConstructorParameterChecker} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ConstructorParameterChecker {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstructorParameterChecker.class);

    private final String className;

    private final Logger targetLogger;

    private final Map<String, Object> parameterMap = new LinkedHashMap<>();

    /**
     * 构造一个构造函数参数有效性检查器。
     *
     * @param className 类名称
     * @param targetLogger 日志输出器，允许为 {@code null}
     */
    public ConstructorParameterChecker(String className, Logger targetLogger) {
        this.className = className != null ? className : "";
        this.targetLogger = targetLogger;
    }

    /**
     * 添加一个构造函数参数。
     *
     * @param parameterName 参数名称
     * @param parameterValue 参数值，允许为 {@code null}
     */
    public void addParameter(String parameterName, Object parameterValue) {
        parameterMap.put(parameterName, parameterValue);
    }

    /**
     * 对指定构造函数参数进行有效性检查，参数在检查前应调用 {@link #addParameter(String, Object)} 方法进行添加。
     *
     * @param parameterName 参数名称
     * @param errorMessage 参数错误提示信息
     * @param predicate 参数检查器
     * @param <T> 参数值类型
     */
    @SuppressWarnings("unchecked")
    public <T> void check(String parameterName, String errorMessage, Predicate<T> predicate) {
        boolean isIllegalArgument;
        try {
            T parameterValue = (T) parameterMap.get(parameterName);
            isIllegalArgument = predicate.test(parameterValue);
        } catch (Exception e) {
            LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("className", className);
            parameterMap.put("parameterName", parameterName);
            parameterMap.put("parameterMap", this.parameterMap);
            LOGGER.error(LogBuildUtil.buildMethodExecuteFailedLog("ConstructorParameterChecker#check(String parameterName, String errorMessage, Predicate<T> predicate)",
                    "check constructor parameter error", parameterMap), e);
            isIllegalArgument = true;
        }
        if ( isIllegalArgument ) {
            errorMessage = Parameters.getErrorMessage(parameterName, errorMessage);
            String errorLog = "Create `" + className + "` failed: `" + errorMessage +  "`." + LogBuildUtil.build(parameterMap);
            if (targetLogger != null) {
                targetLogger.error(errorLog);
            }
            throw new IllegalArgumentException(errorLog);
        }
    }

    @Override
    public String toString() {
        return "ConstructorParameterChecker{" +
                "className='" + className + '\'' +
                ", parameterMap=" + parameterMap +
                '}';
    }
}
