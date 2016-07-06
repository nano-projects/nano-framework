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
package org.nanoframework.extension.shiro.client.session;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.session.Session;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.commons.util.SerializableUtils;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.httpclient.HttpResponse;
import org.nanoframework.web.server.http.status.HttpStatusCode;
import org.nanoframework.web.server.http.status.ResultMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class ShiroClientHttpSession extends ShiroHttpSession {
    public static final String ATTRIBUTE = "attribute";
    public static final String MAX_INACTIVE_INTERVAL = "max.inactive.internal";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroClientHttpSession.class);
    private static final TypeReference<Map<String, Object>> RESULTMAP_TYPE = new TypeReference<Map<String, Object>>() { };
    
    private final HttpClient httpClient;
    private final int retry;
    private final String url;
    private final RequestMethod requestMethod;

    /**
     * @param session the session
     * @param currentRequest the currentRequest
     * @param servletContext the servletContext
     * @param httpClient the httpClient
     * @param retry the retry
     * @param url the url
     */
    public ShiroClientHttpSession(final Session session, final HttpServletRequest currentRequest, final ServletContext servletContext,
            final HttpClient httpClient, final int retry, final String url) {
        this(session, currentRequest, servletContext, httpClient, retry, url, RequestMethod.POST);
    }

    /**
     * @param session the session
     * @param currentRequest the currentRequest
     * @param servletContext the servletContext
     * @param httpClient the httpClient
     * @param retry the retry
     * @param url the url
     * @param requestMethod the requestMethod
     */
    public ShiroClientHttpSession(final Session session, final HttpServletRequest currentRequest, final ServletContext servletContext,
            final HttpClient httpClient, final int retry, final String url, final RequestMethod requestMethod) {
        super(session, currentRequest, servletContext);
        this.httpClient = httpClient;
        this.retry = retry;
        this.url = url;
        this.requestMethod = requestMethod;
    }

    @Override
    public void setAttribute(String s, Object o) {
        super.setAttribute(s, o);
        final String url = this.url + '/' + ATTRIBUTE;
        final String attribute = SerializableUtils.encode(MapBuilder.<Object, Object> create().put(s, o).build());
        if(!syncSession(url, MapBuilder.<String, String> create().put(ATTRIBUTE, attribute).build())) {
            LOGGER.warn("UnSync Session Attribute: {}", s);
        }
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        super.setMaxInactiveInterval(i);
        final String url = this.url + '/' + MAX_INACTIVE_INTERVAL;
        if(!syncSession(url, MapBuilder.<String, String> create().put(MAX_INACTIVE_INTERVAL, String.valueOf(i)).build())) {
            LOGGER.warn("UnSync Session MaxInactiveInterval: {}", i);
        }
    }

    protected boolean syncSession(final String url, final Map<String, String> params) {
        for(int count = 0; count < retry; count++) {
            try {
                return syncSession0(url, params);
            } catch (final IOException e) {
                LOGGER.error("Sync session error {}, retry {}...", e.getMessage(), count);
            }
        }
        
        return false;
    }

    protected boolean syncSession0(final String url, final Map<String, String> params) throws IOException {
        HttpResponse response = httpClient.execute(requestMethod, url, params);
        if(response.statusCode == HttpStatusCode.SC_OK) {
            ResultMap resultMap = ResultMap.create(JSON.parseObject(response.entity, RESULTMAP_TYPE));
            if(resultMap.getStatus() == HttpStatusCode.SC_OK) {
                return true;
            }
        }
        
        return false;
    }
}
