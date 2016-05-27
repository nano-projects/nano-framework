package org.nanoframework.extension.shiro.component.impl;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.nanoframework.extension.shiro.component.SSOComponent;
import org.nanoframework.web.server.mvc.View;

public class SSOComponentImpl extends AbstractSSOComponent implements SSOComponent {
	
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
	
}
