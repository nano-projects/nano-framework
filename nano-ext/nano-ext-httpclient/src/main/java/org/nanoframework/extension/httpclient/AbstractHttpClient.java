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
package org.nanoframework.extension.httpclient;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ReflectUtils;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
abstract class AbstractHttpClient {
    public static final String TIME_TO_LIVE = "context.httpclient.time.to.live";
    public static final String TIME_UNIT = "context.httpclient.timeunit";
    public static final String MAX_TOTAL = "context.httpclient.max.total";
    public static final String MAX_PER_ROUTE = "context.httpclient.default.max.per.route";
    public static final String CHARSET = "context.httpclient.charset";

    /** 超时时间. */
    protected static final String DEFAULT_TIME_TO_LIVE = "30000";
    /** 超时时间单位. */
    protected static final String DEFAULT_TIME_UNIT = "MILLISECONDS";
    /** 最大连接数 */
    protected static final String DEFAULT_MAX_TOTAL = "1024";
    /** 最大并发连接数. */
    protected static final String DEFAULT_MAX_PER_ROUTE = "512";
    /** 字符集. */
    protected static final String DEFAULT_CHARSET = "UTF-8";

    protected static CloseableHttpClient HTTP_CLIENT;

    protected long timeToLive;
    protected TimeUnit tunit;
    protected int maxTotal;
    protected int maxPerRoute;
    protected Charset charset;

    public AbstractHttpClient() {
        this(false);
    }

    public AbstractHttpClient(boolean force) {
        if (HTTP_CLIENT == null || force) {
            this.timeToLive = Long.parseLong(System.getProperty(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE));
            this.tunit = TimeUnit.valueOf(System.getProperty(TIME_UNIT, DEFAULT_TIME_UNIT));
            this.maxTotal = Integer.parseInt(System.getProperty(MAX_TOTAL, DEFAULT_MAX_TOTAL));
            this.maxPerRoute = Integer.parseInt(System.getProperty(MAX_PER_ROUTE, DEFAULT_MAX_PER_ROUTE));
            this.charset = Charset.forName(System.getProperty(CHARSET, DEFAULT_CHARSET));
            initHttpClientPool(timeToLive, tunit, maxTotal, maxPerRoute);
        }
    }

    public AbstractHttpClient(final boolean force, final long timeToLive, final TimeUnit tunit, final int maxTotal, final int maxPerRoute,
            final Charset charset) {
        if (HTTP_CLIENT == null || force) {
            this.timeToLive = timeToLive;
            this.tunit = tunit;
            this.maxTotal = maxTotal;
            this.maxPerRoute = maxPerRoute;
            this.charset = charset;
            initHttpClientPool(timeToLive, tunit, maxTotal, maxPerRoute);
        }
    }

    protected void initHttpClientPool(long timeToLive, TimeUnit tunit, int maxTotal, int maxPerRoute) {
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(timeToLive, tunit);
        manager.setMaxTotal(maxTotal);
        manager.setDefaultMaxPerRoute(maxPerRoute);
        HTTP_CLIENT = HttpClients.custom().setConnectionManager(manager).build();
    }

    protected HttpRequestBase createBase(final Class<? extends HttpRequestBase> cls, final String url, final Map<String, String> params) {
        final URIBuilder builder = new URIBuilder();
        builder.setPath(url);

        final List<NameValuePair> pairs = covertParams2NVPS(params);
        builder.setParameters(pairs);

        try {
            final URI uri = builder.build();
            return ReflectUtils.newInstance(cls, uri);
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected HttpRequestBase createBase(final Class<? extends HttpRequestBase> cls, final String url, final Map<String, String> headers,
            Map<String, String> params) {
        final URIBuilder builder = new URIBuilder();
        builder.setPath(url);

        final List<NameValuePair> pairs = covertParams2NVPS(params);
        builder.setParameters(pairs);

        try {
            final URI uri = builder.build();
            final HttpRequestBase base = ReflectUtils.newInstance(cls, uri);
            if (!CollectionUtils.isEmpty(headers)) {
                headers.forEach((key, value) -> base.addHeader(key, value));
            }

            return base;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
            final Map<String, String> params) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            final List<NameValuePair> pairs = covertParams2NVPS(params);
            entityBase.setEntity(new UrlEncodedFormEntity(pairs, charset));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
            final String json) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            entityBase.setEntity(new StringEntity(json, APPLICATION_JSON));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
            final String stream, ContentType contentType) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            entityBase.setEntity(new StringEntity(stream, contentType));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
            final Map<String, String> headers, final String json) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            if (!CollectionUtils.isEmpty(headers)) {
                headers.forEach((key, value) -> entityBase.addHeader(key, value));
            }

            entityBase.setEntity(new StringEntity(json, APPLICATION_JSON));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
            final Map<String, String> headers, final String stream, final ContentType contentType) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            if (!CollectionUtils.isEmpty(headers)) {
                headers.forEach((key, value) -> entityBase.addHeader(key, value));
            }

            entityBase.setEntity(new StringEntity(stream, contentType));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
            final Map<String, String> headers, final Map<String, String> params) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            if (!CollectionUtils.isEmpty(headers)) {
                headers.forEach((key, value) -> entityBase.addHeader(key, value));
            }

            final List<NameValuePair> pairs = covertParams2NVPS(params);
            entityBase.setEntity(new UrlEncodedFormEntity(pairs, charset));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected List<NameValuePair> covertParams2NVPS(Map<String, String> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Collections.emptyList();
        }

        List<NameValuePair> pairs = new ArrayList<>();
        params.forEach((key, value) -> pairs.add(new BasicNameValuePair(key, value)));
        return pairs;
    }

    /**
     * 处理Http请求
     * 
     * @param request
     * @return
     */
    protected HttpResponse getResult(HttpRequestBase request) throws IOException {
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                StatusLine status = response.getStatusLine();
                return HttpResponse.create(status.getStatusCode(), status.getReasonPhrase(), EntityUtils.toString(entity));
            }
        }

        return HttpResponse.EMPTY;
    }
}
