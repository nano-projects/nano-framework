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
package org.nanoframework.extension.shiro.web.session.mgt;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class DefaultWebSessionManager extends org.apache.shiro.web.session.mgt.DefaultWebSessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSessionManager.class);
    
    @Override
    protected void onStart(final Session session, final SessionContext context) {
        if (!WebUtils.isHttp(context)) {
            LOGGER.debug("SessionContext argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair. No session ID cookie will be set.");
            return;
        }
        
        final HttpServletRequest request = WebUtils.getHttpRequest(context);
        final HttpServletResponse response = WebUtils.getHttpResponse(context);

        if (isSessionIdCookieEnabled()) {
            final Serializable sessionId = session.getId();
            storeSessionId(sessionId, request, response);
        } else {
            LOGGER.debug("Session ID cookie is disabled.  No cookie has been set for new session with id {}", session.getId());
        }

        request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE);
        request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_IS_NEW, Boolean.TRUE);
    }
    
    private void storeSessionId(final Serializable currentId, final HttpServletRequest request, final HttpServletResponse response) {
        if (currentId == null) {
            String msg = "sessionId cannot be null when persisting for subsequent requests.";
            throw new IllegalArgumentException(msg);
        }
        
        final String idString = currentId.toString();
        final Cookie cookie = getSessionIdCookie();
        cookie.setValue(idString);
        cookie.saveTo(request, response);
        LOGGER.debug("Set session ID cookie for session with id {}", idString);
    }
    
}
