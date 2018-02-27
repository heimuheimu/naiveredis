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

import com.heimuheimu.naivemonitor.monitor.CompressionMonitor;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.monitor.CompressionMonitorFactory;
import com.heimuheimu.naiveredis.transcoder.compression.LZFUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 使用 Java 序列化实现的 Java 对象与字节数组转换器。
 *
 * <p><strong>说明：</strong>{@code SimpleTranscoder} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SimpleTranscoder implements Transcoder {

    /**
     * 头字节长度
     */
    private static final int HEADER_LENGTH = 4;

    /**
     * NaiveRedis Java 对象与字节数组转换标示
     */
    private static final byte MAGIC_BYTE = 41;

    /**
     * 使用 LZF 算法进行压缩的字节标示
     */
    private static final byte LZF_COMPRESSION_BYTE = 1;

    /**
     * 当 Value 字节数小于或等于该值，不进行压缩
     */
    private final int compressionThreshold;

    /**
     * 压缩信息监控器
     */
    private final CompressionMonitor compressionMonitor;

    /**
     * 构造一个使用 Java 序列化实现的 Java 对象与字节数组转换器。
     *
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     */
    public SimpleTranscoder(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
        this.compressionMonitor = CompressionMonitorFactory.get();
    }

    @Override
    public byte[] encode(Object value) throws Exception {
        byte[] header = new byte[HEADER_LENGTH];
        header[0] = MAGIC_BYTE;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(value);
        byte[] valueBytes = bos.toByteArray();
        oos.close();
        int preCompressedLength = valueBytes.length;
        if (valueBytes.length > compressionThreshold) {
            valueBytes = LZFUtil.compress(valueBytes);
            compressionMonitor.onCompressed(preCompressedLength - valueBytes.length);
            header[2] = LZF_COMPRESSION_BYTE;
        }
        byte[] result = new byte[header.length + valueBytes.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(valueBytes, 0, result, header.length, valueBytes.length);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(byte[] src) throws Exception {
        if (src[0] == MAGIC_BYTE) {
            ByteArrayInputStream valueBis;
            if (src[2] == LZF_COMPRESSION_BYTE) {
                byte[] value = LZFUtil.decompress(src, HEADER_LENGTH, src.length - HEADER_LENGTH);
                valueBis = new ByteArrayInputStream(value);
            } else {
                valueBis = new ByteArrayInputStream(src, HEADER_LENGTH, src.length - HEADER_LENGTH);
            }
            ObjectInputStream ois = new ObjectInputStream(valueBis);
            return (T) ois.readObject();
        } else {
            throw new RedisException(RedisException.CODE_UNEXPECTED_ERROR,
                    "Decode java object failed: `unknown magic byte " + src[0] + "`.");
        }
    }
}
