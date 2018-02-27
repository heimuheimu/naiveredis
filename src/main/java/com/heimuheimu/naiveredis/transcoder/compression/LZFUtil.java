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
package com.heimuheimu.naiveredis.transcoder.compression;

import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFEncoder;
import com.ning.compress.lzf.LZFException;

/**
 * LZF 压缩、解压工具。更多 LZF 信息可参考：
 * <p>
 *     <a href="https://github.com/ning/compress">https://github.com/ning/compress</a>
 * </p>
 *
 *
 * @author heimuheimu
 */
public class LZFUtil {

	/**
	 * 执行压缩操作。
	 *
	 * @param src 被压缩的字节数组
	 * @return 压缩后的字节数组
	 */
	public static byte[] compress(byte[] src) {
		return LZFEncoder.encode(src);
	}

	/**
	 * 执行解压操作。
	 *
	 * @param compressedBytes 被压缩的字节数组
	 * @param offset 解压起始索引
	 * @param length 解压的字节长度
	 * @return 解压后的字节数组
	 * @throws LZFException 如果解压过程众发生错误，将抛出此异常
	 */
	public static byte[] decompress(byte[] compressedBytes, int offset, int length) throws LZFException {
		return LZFDecoder.decode(compressedBytes, offset, length);
	}
}
