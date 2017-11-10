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
import static org.nanoframework.extension.httpclient.Http.CHARSET;
import static org.nanoframework.extension.httpclient.Http.DEFAULT_CHARSET;
import static org.nanoframework.extension.httpclient.Http.DEFAULT_MAX_PER_ROUTE;
import static org.nanoframework.extension.httpclient.Http.DEFAULT_MAX_TOTAL;
import static org.nanoframework.extension.httpclient.Http.DEFAULT_TIME_TO_LIVE;
import static org.nanoframework.extension.httpclient.Http.DEFAULT_TIME_UNIT;
import static org.nanoframework.extension.httpclient.Http.MAX_PER_ROUTE;
import static org.nanoframework.extension.httpclient.Http.MAX_TOTAL;
import static org.nanoframework.extension.httpclient.Http.TIME_TO_LIVE;
import static org.nanoframework.extension.httpclient.Http.TIME_UNIT;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
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

import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public abstract class AbstractHttpClient implements HttpClient {
    private final Http conf;
    private CloseableHttpClient client;

    public AbstractHttpClient() {
        this(Long.parseLong(System.getProperty(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE)));
    }

    public AbstractHttpClient(final long timeToLive) {
        this(timeToLive, Charset.forName(System.getProperty(CHARSET, DEFAULT_CHARSET)));
    }

    public AbstractHttpClient(final long timeToLive, final Charset charset) {
        this(timeToLive, TimeUnit.valueOf(System.getProperty(TIME_UNIT, DEFAULT_TIME_UNIT)),
                Integer.parseInt(System.getProperty(MAX_TOTAL, DEFAULT_MAX_TOTAL)),
                Integer.parseInt(System.getProperty(MAX_PER_ROUTE, DEFAULT_MAX_PER_ROUTE)), charset);
    }

    public AbstractHttpClient(final long timeToLive, final TimeUnit tunit, final int maxTotal, final int maxPerRoute, final Charset charset) {
        this(new Http(timeToLive, tunit, maxTotal, maxPerRoute, charset));
    }

    public AbstractHttpClient(final Http conf) {
        this.conf = conf;
        initHttpClientPool();
    }

    protected void initHttpClientPool() {
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(this.conf.timeToLive, this.conf.tunit);
        manager.setMaxTotal(this.conf.maxTotal);
        manager.setDefaultMaxPerRoute(this.conf.maxPerRoute);
        client = HttpClients.custom().setConnectionManager(manager).build();
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
            entityBase.setEntity(new UrlEncodedFormEntity(pairs, this.conf.charset));
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
            entityBase.setEntity(new UrlEncodedFormEntity(pairs, this.conf.charset));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    protected List<NameValuePair> covertParams2NVPS(final Map<String, String> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Collections.emptyList();
        }

        final List<NameValuePair> pairs = Lists.newArrayList();
        params.forEach((key, value) -> pairs.add(new BasicNameValuePair(key, value)));
        return pairs;
    }

    @Override
    public HttpResponse execute(final HttpRequestBase request) throws IOException {
        try (CloseableHttpResponse response = client.execute(request)) {
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                StatusLine status = response.getStatusLine();
                return HttpResponse.create(status.getStatusCode(), status.getReasonPhrase(), EntityUtils.toString(entity, this.conf.charset));
            }
        }

        return HttpResponse.EMPTY;
    }
}
