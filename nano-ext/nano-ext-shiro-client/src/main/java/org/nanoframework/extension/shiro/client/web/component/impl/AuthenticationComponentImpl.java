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
package org.nanoframework.extension.shiro.client.web.component.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.httpclient.HttpResponse;
import org.nanoframework.extension.shiro.client.configuration.ConfigurationKeys;
import org.nanoframework.extension.shiro.client.web.component.AuthenticationComponent;
import org.nanoframework.web.server.cookie.Cookies;
import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;

import com.google.inject.Inject;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class AuthenticationComponentImpl implements AuthenticationComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationComponentImpl.class);
    private static final String PRINCIPALS_SESSION_KEY = "org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY";
    @Inject
    private HttpClient httpClient;
    
    @Override
    public Map<String, Object> findUserInfo() {
        try {
            final HttpServletRequest request = HttpContext.get(HttpServletRequest.class);
            final String principal = (String) ((SimplePrincipalCollection) request.getSession().getAttribute(PRINCIPALS_SESSION_KEY)).getPrimaryPrincipal();
            final Map<String, Object> map = HttpStatus.OK.to().beanToMap();
            map.put("username", principal);
            return map;
        } catch (final Throwable e) {
            LOGGER.error("Find user info error: {}", e.getMessage());
            return ResultMap.create("Find user info error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR).beanToMap();
        }
    }

    @Override
    public ResultMap logout() {
        try {
            final String sessionURL = sessionURL();
            final HttpResponse response = httpClient.delete(sessionURL);
            return ResultMap.create(response.statusCode, response.reasonPhrase, response.entity);
        } catch (IOException e) {
            LOGGER.error("Logout error: {}", e.getMessage());
            return ResultMap.create("Logout error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }
    
    protected String sessionURL() {
        final HttpServletRequest request = HttpContext.get(HttpServletRequest.class);
        final String shiroSessionURL = (String) request.getAttribute(ConfigurationKeys.SHIRO_SESSION_URL.getName());
        return shiroSessionURL + (shiroSessionURL.endsWith("/") ? "" : '/') + localSessionId(request);
    }
    
    protected String localSessionId(final HttpServletRequest request) {
        final String sessionIdName = (String) request.getAttribute(ConfigurationKeys.SESSION_ID_NAME.getName());
        final String sessionId = Cookies.get(request, sessionIdName);
        if(StringUtils.isNotBlank(sessionId)) {
            return sessionId;
        }
        
        final HttpSession session = request.getSession();
        if(session != null) {
            return session.getId();
        }
        
        throw new NullPointerException("Not found session id.");
    }

}
