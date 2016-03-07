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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.util.URLContext;

/**
 * 字符串转换处理类
 * 
 * @author yanghe
 * @date 2015年6月5日 下午10:59:25 
 *
 */
public class StringFormat {

	/**
	 * 转换URL <br>
	 * 将context及参数名转位小写，以使其不区分大小写
	 * @param url 请求URL
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final URLContext formatURL(String url) {
		if (StringUtils.isEmpty(url))
			throw new NullPointerException("URL不能为空");

		String[] _url = url.split("[?]");

		String context = _url[0];
		URLContext urlContext = URLContext.create().setContext(context.toLowerCase());

		// 拆分参数列表
		if (_url.length > 1) {
			Map<String, Object> keyValue = new HashMap<>();
			String[] params = _url[1].split("&");
			if (params.length > 0) {
				boolean hasArray = false;
				for (String param : params) {
					String[] kv = param.split("=");
					if(kv[0].endsWith("[]")) {
						hasArray = true;
						List<String> values = (List<String>) keyValue.get(kv[0]);
						if(values == null)
							values = new ArrayList<>();
						
						values.add(kv[1]);
						keyValue.put(kv[0], values);
					} else 
						keyValue.put(kv[0].toLowerCase(), kv[1]);
				}
				
				if(hasArray) {
					keyValue.entrySet().stream().filter(entry -> entry.getKey().endsWith("[]")).forEach(entry -> {
						keyValue.put(entry.getKey(), ((List<String>) entry.getValue()).toArray(new String[0]));
					});
				}
			}

			urlContext.setParameter(keyValue);
		} else {
			urlContext.setParameter(Collections.emptyMap());
		}

		return urlContext;

	}
	
	public static final byte[] toBytes(String strings, Charset charset) {
		return strings.getBytes(charset);
	}
	
}
