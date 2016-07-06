/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.commons.util;

import sun.misc.BASE64Encoder;

/**
 * BASE64Encoder优化类
 * 
 * @author yanghe
 */
@SuppressWarnings("restriction")
public class BASE64 extends BASE64Encoder {

    private static BASE64 base = null;

    private static Object lock = new Object();

    private BASE64() {

    }

    public static final BASE64 getInstance() {
        synchronized (lock) {
            if (base == null) {
                base = new BASE64();
            }
        }

        return base;

    }

    private char[] codec_table = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    /**
     * 优化BASE64Encoder的encode方法，解决编码过长时自动换行及编码效率低的问题
     * 
     * @param bytes the bytes
     * @return encode String
     */
    @Override
    public String encode(byte[] bytes) {
        // return super.encode(bytes);

        int totalBits = bytes.length * 8;
        int nn = totalBits % 6;
        int curPos = 0;// process bits
        StringBuilder builder = new StringBuilder();
        while (curPos < totalBits) {
            int bytePos = curPos / 8;
            switch (curPos % 8) {
                case 0:
                    builder.append(codec_table[(bytes[bytePos] & 0xfc) >> 2]);
                    break;
                case 2:

                    builder.append(codec_table[(bytes[bytePos] & 0x3f)]);
                    break;
                case 4:
                    if (bytePos == bytes.length - 1) {
                        builder.append(codec_table[((bytes[bytePos] & 0x0f) << 2) & 0x3f]);
                    } else {
                        int pos = (((bytes[bytePos] & 0x0f) << 2) | ((bytes[bytePos + 1] & 0xc0) >> 6)) & 0x3f;
                        builder.append(codec_table[pos]);
                    }
                    break;
                case 6:
                    if (bytePos == bytes.length - 1) {
                        builder.append(codec_table[((bytes[bytePos] & 0x03) << 4) & 0x3f]);
                    } else {
                        int pos = (((bytes[bytePos] & 0x03) << 4) | ((bytes[bytePos + 1] & 0xf0) >> 4)) & 0x3f;
                        builder.append(codec_table[pos]);
                    }
                    break;
                default:
                    // never hanppen
                    break;
            }
            curPos += 6;
        }
        if (nn == 2) {
            builder.append("==");

        } else if (nn == 4) {
            builder.append('=');

        }

        return builder.toString();
    }

}
