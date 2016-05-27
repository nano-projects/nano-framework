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
package org.nanoframework.extension.shiro.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.nanoframework.commons.crypt.DefaultCipherExecutor;
import org.nanoframework.web.server.cookie.CookieValueManager;
import org.nanoframework.web.server.cookie.Cookies;
import org.nanoframework.web.server.cookie.DefaultCookieValueManager;
import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class CryptCookie extends SimpleCookie {
    protected static final String DEFAULT_SC_ENCRYPTION_KEY = "1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM";
    protected static final String DEFAULT_SC_SIGNING_KEY = "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w";

    protected String scEncryptionKey = DEFAULT_SC_ENCRYPTION_KEY;
    protected String scSingingKey = DEFAULT_SC_SIGNING_KEY;

    protected final CookieValueManager cookieValueManager = new DefaultCookieValueManager(new DefaultCipherExecutor(scEncryptionKey, scSingingKey));

    /**
     * 
     */
    public CryptCookie() {
        
    }
    
    public CryptCookie(final String name) {
        super(name);
    }
    
    public CryptCookie(final Cookie cookie) {
        super(cookie);
    }
    
    @Override
    public String getValue() {
        return obtainValue(super.getValue());
    }

    @Override
    public void setValue(String value) {
        try {
            obtainValue(value);
            return ;
        } catch (final Throwable e) {
            // ignore
        }
        
        super.setValue(this.cookieValueManager.buildCookieValue(value, HttpContext.get(HttpServletRequest.class)));
    }
    
    protected String obtainValue(String value) {
        return this.cookieValueManager.obtainCookieValue(getName(), value, HttpContext.get(HttpServletRequest.class));
    }
    
    @Override
    protected String buildHeaderValue(String name, String value, String comment, String domain, String path, int maxAge, int version, boolean secure,
            boolean httpOnly) {
        return super.buildHeaderValue(name, super.getValue(), comment, domain, path, maxAge, version, secure, httpOnly);
    }

    @Override
    public String readValue(HttpServletRequest request, HttpServletResponse ignored) {
        String name = getName();
        String value = null;
        javax.servlet.http.Cookie cookie = Cookies.getCookie(request, name);
        if (cookie != null) {
            value = cookie.getValue();
        } else {
            return value;
        }
        
        return this.cookieValueManager.obtainCookieValue(getName(), value, request);
    }
    
    /**
     * @param scEncryptionKey the scEncryptionKey to set
     */
    public void setScEncryptionKey(String scEncryptionKey) {
        this.scEncryptionKey = scEncryptionKey;
    }

    /**
     * @param scSingingKey the scSingingKey to set
     */
    public void setScSingingKey(String scSingingKey) {
        this.scSingingKey = scSingingKey;
    }
}
