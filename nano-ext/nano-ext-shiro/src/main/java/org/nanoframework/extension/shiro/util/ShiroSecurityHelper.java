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
