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
package org.nanoframework.extension.shiro.web.service.impl;

import java.io.Serializable;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.nanoframework.extension.shiro.web.service.SSOService;

import com.google.inject.Singleton;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
@Singleton
public class SSOServiceImpl implements SSOService {

    @Override
    public Session readSession(Serializable sessionId) throws UnknownSessionException {
        final SessionDAO sessionDAO = getSessionDAO();
        if(sessionDAO != null) {
            sessionDAO.readSession(sessionId);
        }
        
        return null;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        final SessionDAO sessionDAO = getSessionDAO();
        if(sessionDAO != null) {
            sessionDAO.update(session);
        }
    }

    @Override
    public void delete(Session session) {
        final SessionDAO sessionDAO = getSessionDAO();
        if(sessionDAO != null) {
            sessionDAO.delete(session);
        }
    }

    protected SessionDAO getSessionDAO() {
        final SecurityManager securityManager = SecurityUtils.getSecurityManager();
        if(securityManager instanceof SessionsSecurityManager) {
            final SessionManager sessionManager = ((SessionsSecurityManager) securityManager).getSessionManager();
            if(sessionManager instanceof DefaultSessionManager) {
                return ((DefaultSessionManager) sessionManager).getSessionDAO();
            }
        }
        
        return null;
    }
    
}
