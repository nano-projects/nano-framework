/**
 * Copyright 2015- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.commons.crypt;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午8:59:44
 */
public class CryptUtil {
	
	private static final String DEFAULT_PASSWORD = "nano-framework";
	
	public static String encrypt(String content) {
		return encrypt(content, DEFAULT_PASSWORD);
	}
	
	public static String encrypt(String content, String password) {
		if(null == password || 0 == password.length())
			password = DEFAULT_PASSWORD;
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(password.getBytes(Charset.forName("UTF-8")));
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = (content).getBytes("UTF-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);//加密
			Base64 encoder = new Base64();
			return new String(encoder.encode(parseByte2HexStr(result).getBytes())).replace("=", "");
			
		} catch (Exception e) {
			throw new EncryptException(e.getMessage(), e);
		} 

	}
	
	public static String decrypt(String date) {
		return decrypt(date, DEFAULT_PASSWORD);
	}

	public static String decrypt(String date, String password) {
		if(StringUtils.isEmpty(password))
			password = DEFAULT_PASSWORD;
		
		switch(date.length() % 4) { 
	        case 3: 
	        	date += "==="; 
	        	break; // 注：其实只需要补充一个或者两个等号，不存在补充三个等号的情况  
	        case 2: 
	        	date += "=="; 
	        	break; 
	        case 1: 
	        	date += "="; 
	        	break; 
	        default: 
	        	break;
	    }
		
		try {
			Base64 decoder = new Base64();
			byte[] content = parseHexStr2Byte(new String(decoder.decode(date.getBytes())));
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(password.getBytes(Charset.forName("UTF-8")));
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");//创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);//初始化
			byte[] result = cipher.doFinal(content);//解密
			return new String(result);
			
		} catch (Exception e) {
			throw new DecryptException(e.getMessage(), e);
		} 
	}
	
	private static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0XFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	} 
	
	private static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		
		byte[] result = new byte[hexStr.length()/2];
		for (int i = 0;i< hexStr.length()/2; i++) {
			int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
			int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	} 
	
}
