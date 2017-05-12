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
package org.nanoframework.extension.shiro.client.component.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.nanoframework.extension.shiro.client.component.SessionComponent;
import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class SessionComponentImpl implements SessionComponent {

    @Override
    public ResultMap setAttribute(String key, String value) {
        try {
            final HttpServletRequest request = HttpContext.get(HttpServletRequest.class);
            final HttpSession session = request.getSession();
            session.setAttribute(key, value);
            return HttpStatus.OK.to();
        } catch(final Throwable e) {
            return HttpStatus.INTERNAL_SERVER_ERROR.to();
        }
        
    }
    
    @Override
    public Map<String, Object> getAttribute(String key) {
        try {
            final HttpServletRequest request = HttpContext.get(HttpServletRequest.class);
            final HttpSession session = request.getSession();
            final Map<String, Object> map = HttpStatus.OK.to().beanToMap();
            final Object value = session.getAttribute(key);
            map.put(key, value == null ? "" : value);
            return map;
        } catch(final Throwable e) {
            return HttpStatus.INTERNAL_SERVER_ERROR.to().beanToMap();
        }
    }
}
