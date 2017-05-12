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
package org.nanoframework.extension.shiro.client.authentication;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.shiro.session.Session;
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.shiro.client.session.ShiroClientHttpSession;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class AuthenticationServletRequest extends HttpServletRequestWrapper {
    protected final ServletContext servletContext;
    protected final Session session;
    protected final HttpClient httpClient;
    protected final int retry;
    protected final String sessionURL;
    
    public AuthenticationServletRequest(final HttpServletRequest wrapped, final ServletContext servletContext, final Session session, final HttpClient httpClient, final int retry, final String sessionURL) {
        super(wrapped);
        this.servletContext = servletContext;
        this.session = session;
        this.httpClient = httpClient;
        this.retry = retry;
        this.sessionURL = sessionURL;
    }
    
    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public HttpSession getSession(boolean create) {
        if(this.session != null) {
            return new ShiroClientHttpSession(session, this, this.getServletContext(), httpClient, retry, sessionURL);
        }
        
        throw new UnknownSessionException("Not found remote session");
    }
}
