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

package com.heimuheimu.naiveredis.data;

import com.heimuheimu.naivemonitor.monitor.SocketMonitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Redis 数据读取器，从字节流中读取 {@link com.heimuheimu.naiveredis.data.RedisData}。
 *
 * <p><strong>说明：</strong>{@code RedisDataReader} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class RedisDataReader {

    private final SocketMonitor socketMonitor;

    private final BufferedInputStream bis;

    /**
     * 构造一个 Redis 数据读取器。
     *
     * @param socketMonitor Socket 读、写信息监控器
     * @param inputStream 字节流
     * @param bufferSize 批量读取的字节大小
     */
    public RedisDataReader(SocketMonitor socketMonitor, InputStream inputStream, int bufferSize) {
        this.socketMonitor = socketMonitor;
        this.bis = new BufferedInputStream(new MonitoredInputStream(inputStream), bufferSize);
    }

    /**
     * 从字节流中读取一个完整的 Redis 数据，如果字节流关闭，将会返回 {@code null}。
     *
     * @return Redis 数据
     * @throws IOException 当读取时发生 IO 错误，将抛出此异常
     */
    public RedisData read() throws IOException {
        int firstByte = bis.read();
        switch (firstByte) {
            case RedisSimpleString.FIRST_BYTE:
                return readSimpleString();
            case RedisError.FIRST_BYTE:
                return readError();
            case RedisInteger.FIRST_BYTE:
                return readInteger();
            case RedisBulkString.FIRST_BYTE:
                return readBulkString();
            case RedisArray.FIRST_BYTE:
                return readArray();
            case -1: //end of the stream is reached
                return null;
            default:
                throw new IOException("Unknown first byte: `" + firstByte + "`.");
        }
    }

    private RedisSimpleString readSimpleString() throws IOException {
        byte[] valueBytes = readLine();
        if (valueBytes != null) {
            return new RedisSimpleString(valueBytes);
        } else {
            return null;
        }
    }

    private RedisError readError() throws IOException {
        byte[] valueBytes = readLine();
        if (valueBytes != null) {
            return new RedisError(valueBytes);
        } else {
            return null;
        }
    }

    private RedisInteger readInteger() throws IOException {
        byte[] valueBytes = readLine();
        if (valueBytes != null) {
            return new RedisInteger(valueBytes);
        } else {
            return null;
        }
    }

    private RedisBulkString readBulkString() throws IOException {
        byte[] lengthBytes = readLine();
        if (lengthBytes != null) {
            int length = Integer.parseInt(new String(lengthBytes, RedisData.UTF8));
            if (length == -1) {
                return new RedisBulkString(null);
            } else {
                byte[] valueBytes = new byte[length];
                int valuePos = 0;
                while (valuePos < length) {
                    int readBytes = bis.read(valueBytes, valuePos, length - valuePos);
                    if (readBytes >= 0) {
                        valuePos += readBytes;
                    } else {
                        //end of the stream is reached
                        return null;
                    }
                }
                bis.read();
                bis.read();
                return new RedisBulkString(valueBytes);
            }
        } else {
            return null;
        }
    }

    private RedisArray readArray() throws IOException {
        byte[] lengthBytes = readLine();
        if (lengthBytes != null) {
            int length = Integer.parseInt(new String(lengthBytes, RedisData.UTF8));
            if (length == -1) {
                return new RedisArray(null);
            } else if (length == 0) {
                return new RedisArray(new RedisData[0]);
            } else {
                RedisData[] dataArray = new RedisData[length];
                for (int i = 0; i < length; i++) {
                    RedisData data = read();
                    if (data != null) {
                        dataArray[i] = data;
                    } else { //end of the stream is reached
                        return null;
                    }
                }
                return new RedisArray(dataArray);
            }
        } else {
            return null;
        }
    }

    private byte[] readLine() throws IOException {
        bis.mark(Integer.MAX_VALUE);
        int length = 0;
        int readByte;
        while ((readByte = bis.read()) != RedisData.CR) {
            if (readByte == -1) { //end of the stream is reached
                return null;
            } else {
                length++;
            }
        }
        bis.reset();
        byte[] valueBytes = new byte[length];
        if (length > 0) {
            bis.read(valueBytes);
        }
        bis.read();
        bis.read();
        return valueBytes;
    }

    private class MonitoredInputStream extends InputStream {

        private final InputStream in;

        private MonitoredInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int readBytes = in.read(b);
            if (readBytes >= 0) {
                socketMonitor.onRead(readBytes);
            }
            return readBytes;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int readBytes = in.read(b, off, len);
            if (readBytes >= 0) {
                socketMonitor.onRead(readBytes);
            }
            return readBytes;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipBytes = in.skip(n);
            if (skipBytes >= 0) {
                socketMonitor.onRead(skipBytes);
            }
            return skipBytes;
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        @Override
        public synchronized void mark(int readLimit) {
            in.mark(readLimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }

        @Override
        public int read() throws IOException {
            int value = in.read();
            if (value >= 0) {
                socketMonitor.onRead(1);
            }
            return value;
        }
    }
}
