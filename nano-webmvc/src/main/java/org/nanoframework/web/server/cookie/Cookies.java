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
package org.nanoframework.web.server.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;

/**
 * Cookie通用类.
 * 
 * @author yanghe
 * @since 1.3.7
 */
public class Cookies {

    /**
     * 
     * @param request HttpServletRequest
     * @param name cookie name
     * @return cookie value
     */
	public static final String get(HttpServletRequest request, String name) {
		if(StringUtils.isEmpty(name)) {
			throw new NullPointerException("Cookie name cannot be null");
		}
		
		Cookie cookie = getCookie(request, name);
		if(cookie != null) {
			return cookie.getValue();
		}
		
		return null;
	}
	
    /**
     * 
     * @param request HttpServletRequest
     * @param name cookie name
     * @return Cookie
     */
	public static final Cookie getCookie(HttpServletRequest request, String name) {
		if(StringUtils.isEmpty(name)) {
			throw new NullPointerException("Cookie name cannot be null");
		}
		
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
	
	public static final String get(String name) {
        if(StringUtils.isEmpty(name)) {
            throw new NullPointerException("Cookie name cannot be null");
        }
        
        Cookie cookie = getCookie(name);
        if(cookie != null) {
            return cookie.getValue();
        }
        
        return null;
    }
    
    public static final Cookie getCookie(String name) {
        if(StringUtils.isEmpty(name)) {
            throw new NullPointerException("Cookie name cannot be null");
        }
        
        final HttpServletRequest request = HttpContext.get(HttpServletRequest.class);
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
