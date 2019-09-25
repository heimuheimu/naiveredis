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

package com.heimuheimu.naiveredis.transcoder;

import java.nio.charset.StandardCharsets;

/**
 * 字符串与字节数组转换器。
 *
 * <p><strong>说明：</strong>{@code SimpleTranscoder} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class StringTranscoder implements Transcoder {

    /**
     * 通过 {@link String#valueOf(Object)} 方法将 Java 对象转换为字符串，然后返回该字符串对应的 UTF-8 编码字节数组。
     *
     * @param value Java 对象
     * @return 字符串对应的 UTF-8 编码字节数组
     */
    @Override
    public byte[] encode(Object value) {
        String stringValue = String.valueOf(value);
        return stringValue.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获得 UTF-8 编码的字节数组对应的字符串。
     *
     * @param src UTF-8 编码的字节数组
     * @return 字符串
     */
    @Override
    @SuppressWarnings("unchecked")
    public String decode(byte[] src) {
        return new String(src, StandardCharsets.UTF_8);
    }
}
