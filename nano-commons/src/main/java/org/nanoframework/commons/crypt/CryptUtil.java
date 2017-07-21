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
package org.nanoframework.commons.crypt;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author yanghe
 * @since 1.0
 */
public final class CryptUtil {
    private static final String DEFAULT_PASSWORD = "nano-framework";
    private static final String CRYPT_MODE = "AES";
    private static final String SHA_MODE = "SHA1PRNG";
    private static final int CRYPT_KEY_SIZE = 128;
    private static final int HEX = 16;
    private static final String UTF8 = "UTF-8";

    private CryptUtil() {

    }

    /**
     * 使用默认密钥对明文进行AES加密，并返回密文内容.
     * @param content 明文
     * @return 密文
     */
    public static String encrypt(final String content) {
        return encrypt(content, DEFAULT_PASSWORD);
    }

    /**
     * 使用密钥对明文进行AES加密，并返回密文内容.
     * @param content 明文
     * @param passwd 密钥
     * @return 密文
     */
    public static String encrypt(final String content, final String passwd) {
        final String password;
        if (passwd == null || passwd.trim().length() == 0) {
            password = DEFAULT_PASSWORD;
        } else {
            password = passwd;
        }

        try {
            final KeyGenerator kgen = KeyGenerator.getInstance(CRYPT_MODE);
            final SecureRandom random = SecureRandom.getInstance(SHA_MODE);
            random.setSeed(password.getBytes(UTF8));
            kgen.init(CRYPT_KEY_SIZE, random);

            final SecretKey secretKey = kgen.generateKey();
            final byte[] enCodeFormat = secretKey.getEncoded();
            final SecretKeySpec key = new SecretKeySpec(enCodeFormat, CRYPT_MODE);

            final Cipher cipher = Cipher.getInstance(CRYPT_MODE);// 创建密码器
            final byte[] byteContent = (content).getBytes(UTF8);
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            final byte[] result = cipher.doFinal(byteContent);//加密
            return encrypt0(result);
        } catch (final Throwable e) {
            throw new EncryptException(e.getMessage(), e);
        }
    }

    private static String encrypt0(final byte[] result) {
        String encodeStr = new String(Base64.getEncoder().encode(parseByte2HexStr(result).getBytes()));
        final int idx;
        if ((idx = encodeStr.indexOf('=')) > -1) {
            final String tmp = encodeStr.substring(0, idx);
            final int len = encodeStr.substring(idx).length();
            encodeStr = tmp + len;
        } else {
            encodeStr += '0';
        }

        return encodeStr;
    }

    /**
     * 使用默认密钥对密文进行解密，并返回明文.
     * 
     * @param data 密文
     * @return 明文
     */
    public static String decrypt(final String data) {
        return decrypt(data, DEFAULT_PASSWORD);
    }

    /**
     * 使用密钥对密文进行解密，并返回明文.
     * 
     * @param data 密文
     * @param passwd 密钥
     * @return 明文
     */
    public static String decrypt(final String data, final String passwd) {
        final String password;
        if (passwd == null || passwd.trim().length() == 0) {
            password = DEFAULT_PASSWORD;
        } else {
            password = passwd;
        }

        final StringBuilder cryptBuilder = new StringBuilder(data.substring(0, data.length() - 1));
        final int len = Integer.parseInt(data.substring(data.length() - 1));
        if (len > 0) {
            for (int idx = 0; idx < len; idx++) {
                cryptBuilder.append('=');
            }
        }

        final String cryptData = cryptBuilder.toString();
        try {
            final byte[] content = parseHexStr2Byte(new String(Base64.getDecoder().decode(cryptData.getBytes())));
            final KeyGenerator kgen = KeyGenerator.getInstance(CRYPT_MODE);
            final SecureRandom random = SecureRandom.getInstance(SHA_MODE);
            random.setSeed(password.getBytes(UTF8));
            kgen.init(CRYPT_KEY_SIZE, random);

            final SecretKey secretKey = kgen.generateKey();
            final byte[] enCodeFormat = secretKey.getEncoded();
            final SecretKeySpec key = new SecretKeySpec(enCodeFormat, CRYPT_MODE);

            final Cipher cipher = Cipher.getInstance(CRYPT_MODE);//创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);//初始化
            final byte[] result = cipher.doFinal(content);//解密
            return new String(result);
        } catch (final Exception e) {
            throw new DecryptException(e.getMessage(), e);
        }
    }

    private static String parseByte2HexStr(final byte[] buf) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0XFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            builder.append(hex.toUpperCase());
        }

        return builder.toString();
    }

    private static byte[] parseHexStr2Byte(final String hex) {
        final byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; i++) {
            final int high = Integer.parseInt(hex.substring(i * 2, i * 2 + 1), HEX);
            final int low = Integer.parseInt(hex.substring(i * 2 + 1, i * 2 + 2), HEX);
            result[i] = (byte) (high * HEX + low);
        }

        return result;
    }

}
