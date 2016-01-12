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
package org.nanoframework.commons.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.nanoframework.commons.util.Assert;

/**
 * @author yanghe
 * @date 2015年8月19日 上午8:58:41
 */
public class DateFormat {
	private static final ConcurrentMap<String, ThreadLocal<SimpleDateFormat>> FORMAT_MAP = new ConcurrentHashMap<>();
	
	public static final SimpleDateFormat get(String pattern) {
		SimpleDateFormat format;
		ThreadLocal<SimpleDateFormat> formatLocal;
		if((formatLocal = FORMAT_MAP.get(pattern)) == null) {
			formatLocal = new ThreadLocal<>();
			formatLocal.set(format = new SimpleDateFormat(pattern));
			FORMAT_MAP.put(pattern, formatLocal);
		}
		
		if((format = formatLocal.get()) == null) {
			format = new SimpleDateFormat(pattern);
			formatLocal.set(format);
		}
		
		return format;
	}
	
	public static final SimpleDateFormat get(Pattern pattern) {
		Assert.notNull(pattern, "pattern must be not null.");
		return get(pattern.get());
	}
	
	public static final String format(Date date, Pattern pattern) {
		return get(pattern).format(date);
	}
	
	public static final String format(Date date, String pattern) {
		return get(pattern).format(date);
	}
	
	public static final String format(Object date, Pattern pattern) {
		return get(pattern).format(date); 
	}
	
	public static final String format(Object date, String pattern) {
		return get(pattern).format(date); 
	}

	@SuppressWarnings("unchecked")
	public static final <T extends Date> T parse(String date, Pattern pattern) throws ParseException {
		return (T) get(pattern).parse(date);
	}
	
	@SuppressWarnings("unchecked")
	public static final <T extends Date> T parse(String date, String pattern) throws ParseException {
		return (T) get(pattern).parse(date);
	}
}
