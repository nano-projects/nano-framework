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
package org.nanoframework.extension.shiro.util;

import java.util.Collection;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;

/**
 *
 * @author yanghe
 * @since 1.2
 */
public class ShiroSecurityHelper {
	private final Logger LOG = LoggerFactory.getLogger(ShiroSecurityHelper.class);
	private SessionDAO sessionDAO;
	
	public SessionDAO getSessionDAO() {
		if(sessionDAO == null)
			sessionDAO = ((DefaultSessionManager) ((DefaultSecurityManager) SecurityUtils.getSecurityManager()).getSessionManager()).getSessionDAO();
		
		return sessionDAO;
	}
	
	public String getCurrentUsername() {
		Subject subject = getSubject();
		PrincipalCollection collection = subject.getPrincipals();
		if (null != collection && !collection.isEmpty()) {
			return (String) collection.iterator().next();
		}
		
		return null;
	}

	public Session getSession() {
		return SecurityUtils.getSubject().getSession();
	}

	public String getSessionId() {
		Session session = getSession();
		if (null == session) {
			return null;
		}
		return getSession().getId().toString();
	}
	
	public Session getSessionByUsername(String username){
		Collection<Session> sessions = getSessionDAO().getActiveSessions();
		for(Session session : sessions){
			if(null != session && StringUtils.equals(String.valueOf(session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY)), username)){
				return session;
			}
		}
		
		return null;
	}
	
	public boolean kickOutUser(String username){
		Session session = getSessionByUsername(username);
		if(null != session && !StringUtils.equals(String.valueOf(session.getId()), getSessionId())){
			getSessionDAO().delete(session);
			LOG.info("Success kick out user {}", new Object[] { username });
			return true;
		}
		
		return false;
	}

	public boolean hasAuthenticated() {
		return getSubject().isAuthenticated();
	}

	private Subject getSubject() {
		return SecurityUtils.getSubject();
	}
}
