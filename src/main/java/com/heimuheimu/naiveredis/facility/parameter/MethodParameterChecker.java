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
 * 方法执行参数有效性检查器。
 *
 * <p><strong>说明：</strong>{@code MethodParameterChecker} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MethodParameterChecker {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodParameterChecker.class);

    private final String methodName;

    private final Logger targetLogger;

    private final IllegalMethodParameterCallback illegalMethodParameterCallback;

    private final Map<String, Object> parameterMap = new LinkedHashMap<>();

    /**
     * 构造一个方法执行参数有效性检查器。
     *
     * @param methodName 方法名称
     * @param targetLogger 日志输出器，允许为 {@code null}
     * @param illegalMethodParameterCallback 方法执行参数非法回调接口，允许为 {@code null}
     */
    public MethodParameterChecker(String methodName, Logger targetLogger, IllegalMethodParameterCallback illegalMethodParameterCallback) {
        this.methodName = methodName != null ? methodName : "";
        this.targetLogger = targetLogger;
        this.illegalMethodParameterCallback = illegalMethodParameterCallback;
    }

    /**
     * 添加一个方法执行参数。
     *
     * @param parameterName 参数名称
     * @param parameterValue 参数值，允许为 {@code null}
     */
    public void addParameter(String parameterName, Object parameterValue) {
        parameterMap.put(parameterName, parameterValue);
    }

    /**
     * 对指定方法执行参数进行有效性检查，参数在检查前应调用 {@link #addParameter(String, Object)} 方法进行添加。
     *
     * @param parameterName 参数名称
     * @param errorMessage 参数错误提示信息
     * @param predicate 参数检查器
     * @param <T> 参数值类型
     */
    public <T> void check(String parameterName, String errorMessage, Predicate<T> predicate) {
        boolean isIllegalArgument;
        try {
            T parameterValue = (T) parameterMap.get(parameterName);
            isIllegalArgument = predicate.test(parameterValue);
        } catch (Exception e) {
            LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("methodName", methodName);
            parameterMap.put("parameterName", parameterName);
            parameterMap.put("parameterMap", this.parameterMap);
            LOGGER.error(LogBuildUtil.buildMethodExecuteFailedLog("MethodParameterChecker#check(String parameterName, String errorMessage, Predicate<T> predicate)",
                    "check method parameter error", parameterMap), e);
            isIllegalArgument = true;
        }
        if ( isIllegalArgument ) {
            errorMessage = Parameters.getErrorMessage(parameterName, errorMessage);
            String errorLog = LogBuildUtil.buildMethodExecuteFailedLog(methodName, errorMessage, parameterMap);
            if (targetLogger != null) {
                targetLogger.error(errorLog);
            }
            if (illegalMethodParameterCallback != null) {
                illegalMethodParameterCallback.call(parameterName);
            }
            throw new IllegalArgumentException(errorLog);
        }
    }

    /**
     * 获得方法执行参数 {@code Map}。
     *
     * @return 方法执行参数 {@code Map}
     */
    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    @Override
    public String toString() {
        return "MethodParameterChecker{" +
                "methodName='" + methodName + '\'' +
                ", parameterMap=" + parameterMap +
                '}';
    }
}
