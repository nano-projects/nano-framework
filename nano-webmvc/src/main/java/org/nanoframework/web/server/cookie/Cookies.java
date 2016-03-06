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
package org.nanoframework.web.server.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * Cookie通用类
 * 
 * @author yanghe
 * @date 2015年7月25日 下午8:42:58 
 *
 */
public class Cookies {

	public static final String get(HttpServletRequest request, String name) {
		if(StringUtils.isEmpty(name))
			throw new NullPointerException("Cookie name cannot be null");
		
		Cookie cookie = getCookie(request, name);
		if(cookie != null) {
			return cookie.getValue();
		}
		
		return null;
	}
	
	public static final Cookie getCookie(HttpServletRequest request, String name) {
		if(StringUtils.isEmpty(name))
			throw new NullPointerException("Cookie name cannot be null");
		
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		
		return null;
	}
}
