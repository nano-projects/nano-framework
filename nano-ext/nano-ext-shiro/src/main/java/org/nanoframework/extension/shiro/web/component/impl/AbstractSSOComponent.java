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
package org.nanoframework.extension.shiro.web.component.impl;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.SerializableUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.shiro.Protocol;
import org.nanoframework.extension.shiro.web.component.SSOComponent;
import org.nanoframework.extension.shiro.web.service.SSOService;
import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.AngularRedirectView;
import org.nanoframework.web.server.mvc.support.ForwardView;
import org.nanoframework.web.server.mvc.support.RedirectView;

import com.google.inject.Inject;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public abstract class AbstractSSOComponent implements SSOComponent {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SSOComponentImpl.class);
    
    protected static final String DEFAULT_SHIRO_SESSION_PREFIX = "SHIRO_SESSION_";
    protected static final String DEFAULT_SHIRO_CLIENT_EXPIRE_TIME = "3600";
    protected static final String DEFAULT_SHIRO_SESSION_LISTENER_EXPIRE_TIME = "7200";
    protected static final String DEFAULT_IS_BIND_SESSION_FORWARD = "true";
    protected static final String DEFAULT_BIND_SESSION_FORWARD_URL = "/pages/login.jsp";
    protected static final String DEFAULT_BIND_SESSION_REDIRECT_URL = EMPTY;
    protected static final String DEFAULT_IS_ANGULAR_REDIRECT_VIEW = "false";
    
    protected static final String SHIRO_SESSION_PREFIX_PROPERTY = "context.sso.shiro.session.prefix";
    protected static final String SHIRO_CLIENT_EXPIRE_TIME_PROPERTY = "context.sso.shiro.client.expire.time";
    protected static final String SHIRO_SESSION_LISTENER_EXPIRE_TIME_PROPERTY = "context.sso.shiro.session.listener.expire.time";
    protected static final String IS_BIND_SESSION_FORWARD_PROPERTY = "context.sso.is.bind.session.forward";
    protected static final String BIND_SESSION_FORWARD_URL_PROPERTY = "context.sso.bind.session.forward.url";
    protected static final String BIND_SESSION_REDIRECT_URL_PROPERTY = "context.sso.bind.session.redirect.url";
    protected static final String IS_ANGULAR_REDIRECT_VIEW_PROPERTY = "context.sso.is.angular.redirect.view";
    
    protected static final String SHIRO_SESSION_PREFIX = System.getProperty(SHIRO_SESSION_PREFIX_PROPERTY, DEFAULT_SHIRO_SESSION_PREFIX);
    protected static final int SHIRO_CLIENT_EXPIRE_TIME = Integer.parseInt(System.getProperty(SHIRO_CLIENT_EXPIRE_TIME_PROPERTY, DEFAULT_SHIRO_CLIENT_EXPIRE_TIME));
    protected static final int SHIRO_SESSION_LISTENER_EXPIRE_TIME = Integer.parseInt(System.getProperty(SHIRO_SESSION_LISTENER_EXPIRE_TIME_PROPERTY, DEFAULT_SHIRO_SESSION_LISTENER_EXPIRE_TIME));
    protected static final boolean IS_BIND_SESSION_FORWARD = Boolean.parseBoolean(System.getProperty(IS_BIND_SESSION_FORWARD_PROPERTY, DEFAULT_IS_BIND_SESSION_FORWARD));
    protected static final String BIND_SESSION_FORWARD_URL = System.getProperty(BIND_SESSION_FORWARD_URL_PROPERTY, DEFAULT_BIND_SESSION_FORWARD_URL);
    protected static final String BIND_SESSION_REDIRECT_URL = System.getProperty(BIND_SESSION_REDIRECT_URL_PROPERTY, DEFAULT_BIND_SESSION_REDIRECT_URL);
    protected static final boolean IS_ANGULAR_REDIRECT_VIEW = Boolean.parseBoolean(System.getProperty(IS_ANGULAR_REDIRECT_VIEW_PROPERTY, DEFAULT_IS_ANGULAR_REDIRECT_VIEW));
    
    protected static final String AUTHENTICATED_SESSION_KEY = "AUTHENTICATED_SESSION_KEY";
    protected static final String PRINCIPALS_SESSION_KEY = "PRINCIPALS_SESSION_KEY";
    
    @Inject
    protected SSOService ssoService;
    
    @Override
    public String getSession(final String clientSessionId) {
        final String serverSessionId = SHIRO.get(SHIRO_CLIENT_SESSION_PREFIX + clientSessionId);
        if(StringUtils.isNotBlank(serverSessionId)) {
            final String sessionSerail = SHIRO.get(SHIRO_SESSION_PREFIX + serverSessionId);
            if(StringUtils.isNotBlank(sessionSerail)) {
                try {
                    if(!validationSession(sessionSerail)) {
                        return EMPTY;
                    }
                    
                    expireSession(clientSessionId, serverSessionId);
                    return sessionSerail;
                } catch (final Throwable e) {
                    LOGGER.error("Session validation error: {}", e.getMessage());
                    return EMPTY;
                }
            }
        }
        
        return EMPTY;
    }
    
    protected boolean validationSession(final String sessionSerail) {
        final Session session = SerializableUtils.decode(sessionSerail);
        if(session instanceof ValidatingSession) {
            if(!((ValidatingSession) session).isValid()) {
                return false;
            }
        }
        
        for(Object attributeKey : session.getAttributeKeys()) {
            if(!validationSession0(session, attributeKey)) {
                return false;
            }
        }
        
        accessSession(session);
        return true;
    }
    
    protected boolean validationSession0(final Session session, final Object attributeKey) {
        if(((String) attributeKey).contains(AUTHENTICATED_SESSION_KEY)) {
            if(!validationAuthenticatedSession(session.getAttribute(attributeKey))) {
                return false;
            }
        }
        
        if(((String) attributeKey).contains(PRINCIPALS_SESSION_KEY)) {
            if(!validationPrincipalsSession(session.getAttribute(attributeKey))) {
                return false;
            }
        }
        
        return true;
    }
    
    protected boolean validationAuthenticatedSession(final Object value) {
        if(value != null && value instanceof Boolean) {
            return (boolean) value;
        }
        
        return false;
    }
    
    protected boolean validationPrincipalsSession(final Object value) {
        if(value != null && value instanceof SimplePrincipalCollection) {
            if(!((SimplePrincipalCollection) value).isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    protected void accessSession(final Session session) {
        ((SimpleSession) session).setLastAccessTime(new Date());
        ssoService.update(session);
    }
    
    @Override
    public String registrySession(String clientSessionId, String serverEncryptSessionId) {
        final String serverSessionId = CryptUtil.decrypt(serverEncryptSessionId);
        final String sessionSerail = SHIRO.get(SHIRO_SESSION_PREFIX + serverSessionId);
        if(StringUtils.isNotBlank(sessionSerail)) {
            if(!validationSession(sessionSerail)) {
                return EMPTY;
            }
            
            storageSession(clientSessionId, serverSessionId);
            return sessionSerail;
        }
        
        return EMPTY;
    }
    
    @Override
    public ResultMap removeSession(final String clientSessionId) {
        try {
            final String serverSessionId = SHIRO.get(SHIRO_CLIENT_SESSION_PREFIX + clientSessionId);
            if(StringUtils.isNotBlank(serverSessionId)) {
                final String sessionSerail = SHIRO.get(SHIRO_SESSION_PREFIX + serverSessionId);
                if(StringUtils.isNotBlank(sessionSerail)) {
                    final Session session = SerializableUtils.decode(sessionSerail);
                    ssoService.delete(session);
                    clearOldSession(clientSessionId);
                    return HttpStatus.OK.to();
                }
            }
        } catch(final Throwable e) {
            LOGGER.error("Remove Session error: {}", e.getMessage());
            return ResultMap.create(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return HttpStatus.BAD_REQUEST.to();
    }
    
    @Override
    public View bindSession(String service, String clientSessionId) {
        final Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated() || subject.isRemembered()) {
            final String serverSessionId = (String) subject.getSession().getId();
            storageSession(clientSessionId, serverSessionId);
            return new RedirectView(service);
        }
        
        return unAuthenticated(service);
    }
    
    protected void storageSession(final String clientSessionId, final String serverSessionId) {
        clearOldSession(clientSessionId);
        
        SHIRO.set(SHIRO_CLIENT_SESSION_PREFIX + clientSessionId, serverSessionId);
        SHIRO.sadd(SHIRO_SESSION_LISTENER_PREFIX + serverSessionId, clientSessionId);
        
        expireSession(clientSessionId, serverSessionId);
    }
    
    protected void expireSession(final String clientSessionId, final String serverSessionId) {
        SHIRO.expire(SHIRO_CLIENT_SESSION_PREFIX + clientSessionId, SHIRO_CLIENT_EXPIRE_TIME);
        SHIRO.expire(SHIRO_SESSION_LISTENER_PREFIX + serverSessionId, SHIRO_SESSION_LISTENER_EXPIRE_TIME);
    }
    
    protected void clearOldSession(final String clientSessionId) {
        final String serverSessionId = SHIRO.get(SHIRO_CLIENT_SESSION_PREFIX + clientSessionId);
        if(StringUtils.isNotBlank(serverSessionId)) {
            SHIRO.srem(SHIRO_SESSION_LISTENER_PREFIX + serverSessionId, clientSessionId);
            SHIRO.del(SHIRO_CLIENT_SESSION_PREFIX + clientSessionId);
        }
    }
    
    protected void addServiceAttribute(final String service) {
        if(StringUtils.isNotBlank(service)) {
            final Model model = HttpContext.get(Model.class);
            model.addAttribute(Protocol.SHIRO.getServiceParameterName(), service);
        }
    }
    
    protected View unAuthenticated(final String service) {
        addServiceAttribute(service);
        return unAuthenticated();
    }
    
    protected View unAuthenticated() {
        if(IS_BIND_SESSION_FORWARD) {
            return new ForwardView(BIND_SESSION_FORWARD_URL, true);
        } else {
            if(IS_ANGULAR_REDIRECT_VIEW) {
                return new AngularRedirectView(BIND_SESSION_REDIRECT_URL);
            } else {
                return new RedirectView(BIND_SESSION_REDIRECT_URL);
            }
        }
    }
}
