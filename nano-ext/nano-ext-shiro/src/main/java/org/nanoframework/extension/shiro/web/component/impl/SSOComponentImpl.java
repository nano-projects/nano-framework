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
import static org.nanoframework.extension.shiro.web.component.Status.INTERNAL_SERVER_ERROR;
import static org.nanoframework.extension.shiro.web.component.Status.INVALID_AUTH;
import static org.nanoframework.extension.shiro.web.component.Status.INVALID_USER_PASS;
import static org.nanoframework.extension.shiro.web.component.Status.OK;
import static org.nanoframework.extension.shiro.web.component.Status.PASSWORD_ERROR;
import static org.nanoframework.extension.shiro.web.component.Status.UNAUTH;
import static org.nanoframework.extension.shiro.web.component.Status.UNLOGIN;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.nanoframework.commons.util.SerializableUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.shiro.util.ShiroSecurityHelper;
import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * @author yanghe
 * @since 1.3
 */
@Singleton
public class SSOComponentImpl extends AbstractSSOComponent {
	protected static final String ERROR_MODEL_NAME = "error";
	
	@Inject
	protected ShiroSecurityHelper helper;
	
	@Override
	public String getSession(final String clientSessionId) {
	    for(int count = 0; count < ERROR_RETRY; count++) {
    	    try {
    	        return super.getSession(clientSessionId);
    	    } catch(final Throwable e) {
    	        LOGGER.error("getSession Error: {}, retry {}...", e.getMessage(), count + 1);
    	    }
	    }
	    
	    return EMPTY;
	}
	
	@Override
	public String registrySession(final String clientSessionId, final String serverEncryptSessionId) {
	    for(int count = 0; count < ERROR_RETRY; count++) {
            try {
                return super.registrySession(clientSessionId, serverEncryptSessionId);
            } catch(final Throwable e) {
                LOGGER.error("getSession Error: {}, retry {}...", e.getMessage(), count + 1);
            }
        }
        
        return EMPTY;
	}
	
	@Override
	public View bindSession(final String service, final String clientSessionId) {
	    for(int count = 0; count < ERROR_RETRY; count++) {
            try {
                return super.bindSession(service, clientSessionId);
            } catch(final Throwable e) {
                LOGGER.error("getSession Error: {}, retry {}...", e.getMessage(), count + 1);
            }
        }
        
	    return unAuthenticated(service);
	}
	
	@Override
	public ResultMap syncSessionAttribute(String clientSessionId, String serialAttribute) {
	    try {
    	    final String sessionSerail = super.getSession(clientSessionId);
    	    final Session session = SerializableUtils.decode(sessionSerail);
    	    final Map<Object, Object> map = SerializableUtils.decode(serialAttribute);
    	    map.forEach((key, value) -> session.setAttribute(key, value));
    	    accessSession(session);
    	    return HttpStatus.OK.to();
	    } catch(final Throwable e) {
	        LOGGER.error("Sync session error: {}", e.getMessage());
	        return ResultMap.create(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	    
	}
	
	@Override
	public ResultMap syncSessionMaxInactiveInternal(String clientSessionId, Integer maxInactiveInternal) {
	    try {
            final String sessionSerail = super.getSession(clientSessionId);
            final Session session = SerializableUtils.decode(sessionSerail);
            session.setTimeout(maxInactiveInternal * 1000);
            accessSession(session);
            return HttpStatus.OK.to();
        } catch(final Throwable e) {
            LOGGER.error("Sync session error: {}", e.getMessage());
            return ResultMap.create(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@Override
	public View loginFailure(final String shiroLoginFailure, final String service) {
	    final Model model = HttpContext.get(Model.class);
	    
	    if(AccountException.class.getName().equals(shiroLoginFailure)) {
	        model.addAttribute(ERROR_MODEL_NAME, "无效的用户名");
	    } else if(UnknownAccountException.class.getName().equals(shiroLoginFailure)) {
            model.addAttribute(ERROR_MODEL_NAME, "用户不存在");
        } else if(IncorrectCredentialsException.class.getName().equals(shiroLoginFailure)) {
            model.addAttribute(ERROR_MODEL_NAME, "密码错误");
        } else if(shiroLoginFailure != null) {
            model.addAttribute(ERROR_MODEL_NAME, "未知错误：" + shiroLoginFailure);
        }
	    
	    return unAuthenticated(service);
	}
	
	@Override
	public Map<String, Object> login(final UsernamePasswordToken token, final String service) {
	    if (StringUtils.isBlank(token.getUsername()) || ArrayUtils.isEmpty(token.getPassword())) {
            return INVALID_USER_PASS.beanToMap();
        }

        Subject subject = SecurityUtils.getSubject();
        try {
            if (subject.isAuthenticated()) {
                return createOKResult(service);
            }

            subject.login(token);
            if (subject.isAuthenticated()) {
                return createOKResult(service);
            } else {
                return INVALID_AUTH.beanToMap();
            }
            
        } catch (final AuthenticationException e) {
            return authenticationException(e);
            
        } catch (final Throwable e) {
            LOGGER.error("处理异常: {}", e.getMessage());
            return INTERNAL_SERVER_ERROR.beanToMap();
        }
	}
	
	protected Map<String, Object> authenticationException(final AuthenticationException e) {
	    LOGGER.error("权限认证失败: {}", e.getMessage());
        if (e.getMessage().indexOf("did not match the expected credentials") > -1) {
            return PASSWORD_ERROR.beanToMap();
        } else {
            Map<String, Object> authError = UNAUTH.beanToMap();
            authError.put(ResultMap.MESSAGE, e.getMessage());
            return authError;
        }

	}
	
	@Override
	public ResultMap logout() {
	    try {
            final Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated() || subject.isRemembered()) {
                subject.logout();
                return OK;
            } else {
                return UNLOGIN;
            }
        } catch (final Throwable e) {
            LOGGER.error("处理异常: {}", e.getMessage());
            return INTERNAL_SERVER_ERROR;
        }
	}
	
	@Override
    public Map<String, Object> isLogined(final String service) {
	    try {
	        final Subject subject = SecurityUtils.getSubject();
	        if(subject.isAuthenticated() || subject.isRemembered()) {
                return createOKResult(service);
	        } else {
	            return UNLOGIN.beanToMap();
	        }
	    } catch (final Throwable e) {
	        LOGGER.error("登陆校验异常: {}", e.getMessage());
	        return INTERNAL_SERVER_ERROR.beanToMap();
	    }
    }
	
	protected Map<String, Object> createOKResult() {
        Map<String, Object> ok = OK.beanToMap();
        String username = helper.getCurrentUsername();
        ok.put("username", username);
        return ok;
    }
	
	protected Map<String, Object> createOKResult(final String service) {
        Map<String, Object> ok = createOKResult();
        if(StringUtils.isNotBlank(service)) {
            try {
                ok.put("service", URLDecoder.decode(service, Charsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("service url decode error: {}", e.getMessage());
            }
        }
        
        return ok;
    }
	
}
