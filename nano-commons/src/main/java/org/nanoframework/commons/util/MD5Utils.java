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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * 
 * @author yanghe
 * @since 1.0
 */
public class MD5Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MD5Utils.class);

    private static char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static MessageDigest MESSAGE_DIGEST;

    private static ConcurrentMap<String, String> FILE_MD5_MAP = new ConcurrentHashMap<String, String>();

    static {
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("MD5FileUtil messagedigest初始化失败", e);
        }
    }

    private MD5Utils() {
    }

    public static String md5(final File file) {
        FileInputStream in = null;
        FileChannel ch = null;
        String md5 = null;
        try {
            if (FILE_MD5_MAP.get(file.getAbsolutePath()) != null) {
                return FILE_MD5_MAP.get(file.getAbsolutePath());
            }

            in = new FileInputStream(file);
            ch = in.getChannel();
            final MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MESSAGE_DIGEST.update(byteBuffer);
            md5 = bufferToHex(MESSAGE_DIGEST.digest());

            FILE_MD5_MAP.put(file.getAbsolutePath(), md5);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (ch != null) {
                    ch.close();
                }

                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        return md5;

    }

    public static String md5(final String s) {
        return md5(s.getBytes());
    }

    public static String md5(final byte[] bytes) {
        MESSAGE_DIGEST.update(bytes);
        return bufferToHex(MESSAGE_DIGEST.digest());
    }

    private static String bufferToHex(final byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(final byte bytes[], final int begin, final int end) {
        final StringBuilder builder = new StringBuilder(2 * end);
        final int length = begin + end;
        for (int index = begin; index < length; index++) {
            appendHexPair(bytes[index], builder);
        }
        
        return builder.toString();
    }

    private static void appendHexPair(final byte bt, final StringBuilder builder) {
        builder.append(HEX_DIGITS[(bt & 0xf0) >> 4]);
        builder.append(HEX_DIGITS[bt & 0xf]);
    }

    public static boolean check(final String plaintext, final String ciphertext) {
        final String cipher = md5(plaintext);
        return cipher.equals(ciphertext);
    }

    /** 
     * 转换byte为Hex字符串.
     * @param value 要转换的byte数据 
     * @param minLength 生成hex的最小长度（长度不足时会在前面加0） 
     * @return hex String
     */
    public static String byteToHex(final byte value, final int minLength) {
        String hex = Integer.toHexString(value & 0xff);
        if (hex.length() < minLength) {
            for (int i = 0; i < (minLength - hex.length()); i++)
                hex = '0' + hex;
        }
        return hex;
    }

}