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

    private static final Logger LOG = LoggerFactory.getLogger(MD5Utils.class);

    private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static MessageDigest messagedigest = null;

    private static ConcurrentMap<String, String> fileMd5Map = new ConcurrentHashMap<String, String>();

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("MD5FileUtil messagedigest初始化失败", e);
        }
    }

    private MD5Utils() {
    }

    public static String getFileMD5String(File file) {

        FileInputStream in = null;
        FileChannel ch = null;
        String md5 = null;

        try {
            if (fileMd5Map.get(file.getAbsolutePath()) != null) {
                return fileMd5Map.get(file.getAbsolutePath());
            }

            in = new FileInputStream(file);
            ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest.update(byteBuffer);
            md5 = bufferToHex(messagedigest.digest());

            fileMd5Map.put(file.getAbsolutePath(), md5);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

        } finally {
            try {
                if (ch != null)
                    ch.close();

                if (in != null)
                    in.close();

            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }

        }

        return md5;

    }

    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    public static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    public static boolean checkPassword(String password, String md5PwdStr) {
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }

    /** 
     * 转换byte为Hex字符串 
     * @param value 要转换的byte数据 
     * @param minlength 生成hex的最小长度（长度不足时会在前面加0） 
     * @return hex String
     */
    public static String byteToHex(byte value, int minlength) {
        String s = Integer.toHexString(value & 0xff);
        if (s.length() < minlength) {
            for (int i = 0; i < (minlength - s.length()); i++)
                s = '0' + s;
        }
        return s;
    }

    /** 
     * MD5加密字符串 
     * @param value the value
     * @return md5 byte array
     */
    public static byte[] MD5(byte[] value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(value);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}