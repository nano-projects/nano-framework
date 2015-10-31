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

/**
 * 使用commons-lang3中的DateFormatUtils替代此实现
 * 
 * @author yanghe
 * @date 2015年8月19日 上午8:58:41
 * 
 * @see org.apache.commons.lang3.time.DateFormatUtils
 */
@Deprecated
public class DateFormat {

	private static final ConcurrentMap<Pattern, ThreadLocal<SimpleDateFormat>> FORMAT_MAP = new ConcurrentHashMap<>();
	
	public static final SimpleDateFormat getAndSetDateFormat(Pattern pattern) {
		SimpleDateFormat format;
		ThreadLocal<SimpleDateFormat> formatLocal;
		if((formatLocal = FORMAT_MAP.get(pattern)) == null) {
			formatLocal = new ThreadLocal<>();
			formatLocal.set(format = new SimpleDateFormat(pattern.get()));
			FORMAT_MAP.put(pattern, formatLocal);
		}
		
		if((format = formatLocal.get()) == null) {
			format = new SimpleDateFormat(pattern.get());
			formatLocal.set(format);
		}
		
		return format;
	}
	
	public static final String format(Date date, Pattern pattern) {
		return getAndSetDateFormat(pattern).format(date);
	}
	
	public static final String format(Object date, Pattern pattern) {
		return getAndSetDateFormat(pattern).format(date); 
	}

	@SuppressWarnings("unchecked")
	public static final <T extends Date> T parse(String date, Pattern pattern) throws ParseException {
		return (T) getAndSetDateFormat(pattern).parse(date);
	}
	
}
