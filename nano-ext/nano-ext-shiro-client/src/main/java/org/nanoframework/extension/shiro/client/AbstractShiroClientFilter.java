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
package org.nanoframework.extension.shiro.client;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.session.Session;
import org.nanoframework.commons.util.ContentType;
import org.nanoframework.commons.util.ObjectUtils;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.commons.util.SerializableUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.httpclient.HttpResponse;
import org.nanoframework.extension.shiro.client.authentication.AuthenticationServletRequest;
import org.nanoframework.extension.shiro.client.authentication.UnknownSessionException;
import org.nanoframework.extension.shiro.client.configuration.ConfigurationKeys;
import org.nanoframework.extension.shiro.client.matchers.ContainsPatternUrlPatternMatcherStrategy;
import org.nanoframework.extension.shiro.client.matchers.ExactUrlPatternMatcherStrategy;
import org.nanoframework.extension.shiro.client.matchers.RegexUrlPatternMatcherStrategy;
import org.nanoframework.extension.shiro.client.matchers.UrlPatternMatcherStrategy;
import org.nanoframework.extension.shiro.client.util.ServiceUtils;
import org.nanoframework.web.server.cookie.Cookies;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.HttpStatusCode;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.RedirectView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

/**
 * 
 * @author yanghe
 * @since 1.3.7
 */
public abstract class AbstractShiroClientFilter extends AbstractConfigurationFilter {
    protected static final String AJAX_REQUEST = "AJAX_REQUEST";
    
    private static final Map<String, Class<? extends UrlPatternMatcherStrategy>> PATTERN_MATCHER_TYPES = Maps.newHashMap();
    static {
        PATTERN_MATCHER_TYPES.put("CONTAINS", ContainsPatternUrlPatternMatcherStrategy.class);
        PATTERN_MATCHER_TYPES.put("REGEX", RegexUrlPatternMatcherStrategy.class);
        PATTERN_MATCHER_TYPES.put("EXACT", ExactUrlPatternMatcherStrategy.class);
    }
    
    protected String shiroSessionURL;
    protected String shiroSessionBindURL;
    protected String sessionIdName;
    protected int serviceInvokeRetry;
    
    private UrlPatternMatcherStrategy ignoreUrlPatternMatcherStrategyClass;
    private Protocol protocol = Protocol.SHIRO;
    private String serverName;
    private String service;
    private boolean encodeServiceUrl = true;
    
    public final void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        if (!isIgnoreInitConfiguration()) {
            setServerName(getString(ConfigurationKeys.SERVER_NAME));
            setService(getString(ConfigurationKeys.SERVICE));
            setEncodeServiceUrl(getBoolean(ConfigurationKeys.ENCODE_SERVICE_URL));
            
            setShiroSessionURL(getString(ConfigurationKeys.SHIRO_SESSION_URL));
            setShiroSessionBindURL(getString(ConfigurationKeys.SHIRO_SESSION_BIND_URL));
            setSessionIdName(getString(ConfigurationKeys.SESSION_ID_NAME));
            setServiceInvokeRetry(getInt(ConfigurationKeys.SERVICE_INVOKE_RETRY));
            
            initInternal(filterConfig);
        }

        init();
    }

    /** Controls the ordering of filter initialization and checking by defining a method that runs before the init.
     * @param filterConfig the original filter configuration.
     * @throws ServletException if there is a problem.
     *
     */
    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        final String ignorePattern = getString(ConfigurationKeys.IGNORE_PATTERN);
        final String ignoreUrlPatternType = getString(ConfigurationKeys.IGNORE_URL_PATTERN_TYPE);

        if (ignorePattern != null) {
            final Class<? extends UrlPatternMatcherStrategy> ignoreUrlMatcherClass = PATTERN_MATCHER_TYPES.get(ignoreUrlPatternType);
            if (ignoreUrlMatcherClass != null) {
                this.ignoreUrlPatternMatcherStrategyClass = ReflectUtils.newInstance(ignoreUrlMatcherClass.getName());
            } else {
                try {
                    logger.debug("Assuming {} is a qualified class name...", ignoreUrlPatternType);
                    this.ignoreUrlPatternMatcherStrategyClass = ReflectUtils.newInstance(ignoreUrlPatternType);
                } catch (final IllegalArgumentException e) {
                    logger.error("Could not instantiate class [{}]", ignoreUrlPatternType, e);
                }
            }
            if (this.ignoreUrlPatternMatcherStrategyClass != null) {
                this.ignoreUrlPatternMatcherStrategyClass.setPattern(ignorePattern);
            }
        }
    }

    /**
     * Initialization method.  Called by Filter's init method or by Spring.  Similar in concept to the InitializingBean interface's
     * afterPropertiesSet();
     */
    public void init() {

    }

    // empty implementation as most filters won't need this.
    public void destroy() {
        // nothing to do
    }

    protected final String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
        return ServiceUtils.constructServiceUrl(request, response, this.service, this.serverName, protocol.getServiceParameterName(), protocol.getArtifactParameterName(), this.encodeServiceUrl);
    }
    
    /**
     * Template method to allow you to change how you retrieve the ticket.
     *
     * @param request the HTTP ServletRequest.  CANNOT be NULL.
     * @return the ticket if its found, null otherwise.
     */
    protected String retrieveTicketFromRequest(final HttpServletRequest request) {
        return ServiceUtils.safeGetParameter(request, this.protocol.getArtifactParameterName());
    }

    protected boolean isRequestUrlExcluded(final HttpServletRequest request) {
        if (this.ignoreUrlPatternMatcherStrategyClass == null) {
            return false;
        }

        final StringBuffer urlBuffer = request.getRequestURL();
        if (request.getQueryString() != null) {
            urlBuffer.append('?').append(request.getQueryString());
        }
        final String requestUri = urlBuffer.toString();
        return this.ignoreUrlPatternMatcherStrategyClass.matches(requestUri);
    }
    
    protected String localSessionId(final HttpServletRequest request) {
        final String sessionId = Cookies.get(request, sessionIdName);
        if(StringUtils.isNotBlank(sessionId)) {
            return sessionId;
        }
        
        final HttpSession session = request.getSession();
        if(session != null) {
            return session.getId();
        }
        
        throw new NullPointerException("Not found session id.");
    }

    protected HttpClient httpClient() {
        return Globals.get(Injector.class).getInstance(HttpClient.class);
    }
    
    protected Session decodeSession(HttpResponse response) {
        if (response.statusCode == HttpStatusCode.SC_OK) {
            try {
                final Session session = SerializableUtils.decode(response.entity);
                if(session == null) {
                    throw new UnknownSessionException("Remote Session not exsits");
                }
                
                return session;
            } catch (final Throwable e) {
                if(e instanceof AuthenticationException) {
                    throw e;
                }
                
                throw new AuthenticationException(e.getMessage(), e);
            }
        }
        
        throw new AuthenticationException("Response Error. status: " + response.statusCode);
    }
    
    protected HttpServletRequest requestWrapper0(final HttpServletRequest request, HttpResponse response) {
        final Session session = decodeSession(response);
        bindAttributes(request);
        return new AuthenticationServletRequest(request, request.getServletContext(), session, httpClient(), serviceInvokeRetry, sessionURL(request));
    }
    
    protected void bindAttributes(final HttpServletRequest request) {
        request.setAttribute(ConfigurationKeys.SHIRO_SESSION_URL.getName(), shiroSessionURL);
        request.setAttribute(ConfigurationKeys.SHIRO_SESSION_BIND_URL.getName(), shiroSessionBindURL);
        request.setAttribute(ConfigurationKeys.SESSION_ID_NAME.getName(), sessionIdName);
        request.setAttribute(ConfigurationKeys.SERVICE_INVOKE_RETRY.getName(), serviceInvokeRetry);
    }
    
    protected String sessionURL(final HttpServletRequest request, final String... tokens) {
        final StringBuilder urlBuilder = new StringBuilder(shiroSessionURL + (shiroSessionURL.endsWith("/") ? "" : '/') + localSessionId(request));
        if(!ArrayUtils.isEmpty(tokens)) {
            for(final String token : tokens) {
                urlBuilder.append(token);
            }
        }
        
        return urlBuilder.toString();
    }
    
    protected void write(final HttpServletRequest request, final HttpServletResponse response, final Object result) throws IOException {
        write(request, response, result, ContentType.APPLICATION_JSON);
    }
    
    protected void write(final HttpServletRequest request, final HttpServletResponse response, final Object result, final String contentType) throws IOException {
        response.setContentType(contentType);
        final Writer out = response.getWriter();
        final String callback;
        if(!ObjectUtils.isEmpty(callback = request.getParameter("callback"))) {
            out.write(callback + '(' + JSON.toJSONString(result, SerializerFeature.WriteDateUseDateFormat) + ')');
        } else { 
            out.write(JSON.toJSONString(result, SerializerFeature.WriteDateUseDateFormat));
        }
    }
    
    protected void responseFailure(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        final String ajaxRequest = request.getParameter(AJAX_REQUEST);
        if(StringUtils.isNotBlank(ajaxRequest) && Boolean.parseBoolean(ajaxRequest)) {
            final String service = this.serverName;
            final String shiroServer = ServiceUtils.constructRedirectUrl(this.shiroSessionBindURL, getProtocol().getServiceParameterName(), service,
                    "sessionId", localSessionId(request));
            
            final Map<String, Object> map = HttpStatus.UNAUTHORIZED.to().beanToMap();
            map.put("__SERVICE", shiroServer);
            write(request, response, map);
        } else {
            final String service = constructServiceUrl(request, response);
            final String shiroServer = ServiceUtils.constructRedirectUrl(this.shiroSessionBindURL, getProtocol().getServiceParameterName(), service,
                    "sessionId", localSessionId(request));
            
            final View view = new RedirectView(shiroServer);
            view.redirect(null, (HttpServletRequest) request, (HttpServletResponse) response);
        }
    }

    /**
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @param encodeServiceUrl the encodeServiceUrl to set
     */
    public void setEncodeServiceUrl(boolean encodeServiceUrl) {
        this.encodeServiceUrl = encodeServiceUrl;
    }
    
    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
    
    /**
     * @return the protocol
     */
    public Protocol getProtocol() {
        return protocol;
    }
    
    public void setShiroSessionURL(final String shiroSessionURL) {
        this.shiroSessionURL = shiroSessionURL;
    }

    public void setShiroSessionBindURL(final String shiroSessionBindURL) {
        this.shiroSessionBindURL = shiroSessionBindURL;
    }

    public void setSessionIdName(final String sessionIdName) {
        this.sessionIdName = sessionIdName;
    }
    
    public void setServiceInvokeRetry(final int serviceInvokeRetry) {
        this.serviceInvokeRetry = serviceInvokeRetry;
    }
}
