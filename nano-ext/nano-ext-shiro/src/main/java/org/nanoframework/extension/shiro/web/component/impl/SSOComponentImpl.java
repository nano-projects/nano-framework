package org.nanoframework.extension.shiro.web.component.impl;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.nanoframework.extension.shiro.web.component.SSOComponent;
import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;

public class SSOComponentImpl extends AbstractSSOComponent implements SSOComponent {
	protected static final String ERROR_MODEL_NAME = "error";
	
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
}
