/*
 * Copyright 2015-2017 the original author or authors.
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
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.CHARSET;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.DEFAULT_CHARSET;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.DEFAULT_MAX_PER_ROUTE;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.DEFAULT_MAX_TOTAL;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.DEFAULT_TIME_TO_LIVE;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.DEFAULT_TIME_UNIT;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.MAX_PER_ROUTE;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.MAX_TOTAL;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.TIME_TO_LIVE;
import static org.nanoframework.extension.httpclient.inject.HttpConfigure.TIME_UNIT;

import com.google.common.collect.Lists;
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
import org.nanoframework.extension.httpclient.exception.HttpClientException;
import org.nanoframework.extension.httpclient.inject.HttpConfigure;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yanghe
 * @since 1.3.7
 */
public abstract class AbstractHttpClient implements HttpClient {
    private final HttpConfigure conf;
    private CloseableHttpClient client;

    /**
     * Default Constructor.
     */
    public AbstractHttpClient() {
        this(Long.parseLong(System.getProperty(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE)));
    }

    /**
     * @param timeToLive 超时时间
     */
    public AbstractHttpClient(final long timeToLive) {
        this(timeToLive, Charset.forName(System.getProperty(CHARSET, DEFAULT_CHARSET)));
    }

    /**
     * @param timeToLive 超时时间
     * @param charset    字符集
     */
    public AbstractHttpClient(final long timeToLive, final Charset charset) {
        this(HttpConfigure.DEFAULT_SPI_NAME, timeToLive, TimeUnit.valueOf(System.getProperty(TIME_UNIT, DEFAULT_TIME_UNIT)),
                Integer.parseInt(System.getProperty(MAX_TOTAL, DEFAULT_MAX_TOTAL)),
                Integer.parseInt(System.getProperty(MAX_PER_ROUTE, DEFAULT_MAX_PER_ROUTE)), charset);
    }

    /**
     * @param spi         SPI名称
     * @param timeToLive  超时时间
     * @param tunit       超时时间单位
     * @param maxTotal    最大连接数
     * @param maxPerRoute 最大并发连接数
     * @param charset     字符集
     */
    public AbstractHttpClient(final String spi, final long timeToLive, final TimeUnit tunit, final int maxTotal, final int maxPerRoute, final Charset charset) {
        this(new HttpConfigure(spi, timeToLive, tunit, maxTotal, maxPerRoute, charset));
    }

    /**
     * @param conf HttpClient配置
     */
    public AbstractHttpClient(final HttpConfigure conf) {
        this.conf = conf;
        initHttpClientPool();
    }

    /**
     * 初始化HttpClient连接池.
     */
    protected void initHttpClientPool() {
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(this.conf.getTimeToLive(), this.conf.getTunit());
        manager.setMaxTotal(this.conf.getMaxTotal());
        manager.setDefaultMaxPerRoute(this.conf.getMaxPerRoute());
        client = HttpClients.custom().setConnectionManager(manager).build();
    }

    /**
     * @return HttpConfigure
     */
    protected HttpConfigure conf() {
        return (HttpConfigure) conf.clone();
    }

    /**
     * 根据请求信息创建HttpRequestBase.
     *
     * @param cls    类型Class
     * @param url    URL
     * @param params 参数列表
     * @return HttpRequestBase
     */
    protected HttpRequestBase createBase(final Class<? extends HttpRequestBase> cls, final String url, final Map<String, String> params) {
        final URIBuilder builder = new URIBuilder();
        builder.setPath(url);

        final List<NameValuePair> pairs = covertParams2Nvps(params);
        builder.setParameters(pairs);

        try {
            final URI uri = builder.build();
            return ReflectUtils.newInstance(cls, uri);
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    /**
     * 根据请求信息创建HttpRequestBase.
     *
     * @param cls     类型Class
     * @param url     URL
     * @param headers Http请求头信息列表
     * @param params  参数列表
     * @return HttpRequestBase
     */
    protected HttpRequestBase createBase(final Class<? extends HttpRequestBase> cls, final String url, final Map<String, String> headers,
                                         final Map<String, String> params) {
        final URIBuilder builder = new URIBuilder();
        builder.setPath(url);

        final List<NameValuePair> pairs = covertParams2Nvps(params);
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

    /**
     * 根据请求信息创建HttpEntityEnclosingRequestBase.
     *
     * @param cls    类型Class
     * @param url    URL
     * @param params 参数列表
     * @return HttpEntityEnclosingRequestBase
     */
    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
                                                              final Map<String, String> params) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            final List<NameValuePair> pairs = covertParams2Nvps(params);
            entityBase.setEntity(new UrlEncodedFormEntity(pairs, this.conf.getCharset()));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    /**
     * 根据请求信息创建HttpEntityEnclosingRequestBase.
     *
     * @param cls  类型Class
     * @param url  URL
     * @param json JSON请求报文
     * @return HttpEntityEnclosingRequestBase
     */
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

    /**
     * 根据请求信息创建HttpEntityEnclosingRequestBase.
     *
     * @param cls         类型Class
     * @param url         URL
     * @param stream      流式报文
     * @param contentType 报文类型
     * @return HttpEntityEnclosingRequestBase
     */
    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
                                                              final String stream, final ContentType contentType) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            entityBase.setEntity(new StringEntity(stream, contentType));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    /**
     * 根据请求信息创建HttpEntityEnclosingRequestBase.
     *
     * @param cls     类型Class
     * @param url     URL
     * @param headers Http请求头信息
     * @param json    JSON请求报文
     * @return HttpEntityEnclosingRequestBase
     */
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

    /**
     * 根据请求信息创建HttpEntityEnclosingRequestBase.
     *
     * @param cls         类型Class
     * @param url         URL
     * @param headers     Http请求头信息
     * @param stream      流式报文
     * @param contentType 报文类型
     * @return HttpEntityEnclosingRequestBase
     */
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

    /**
     * 根据请求信息创建HttpEntityEnclosingRequestBase.
     *
     * @param cls     类型Class
     * @param url     URL
     * @param headers Http请求头信息
     * @param params  请求参数列表
     * @return HttpEntityEnclosingRequestBase
     */
    protected HttpEntityEnclosingRequestBase createEntityBase(final Class<? extends HttpEntityEnclosingRequestBase> cls, final String url,
                                                              final Map<String, String> headers, final Map<String, String> params) {
        try {
            final HttpEntityEnclosingRequestBase entityBase = ReflectUtils.newInstance(cls, url);
            if (!CollectionUtils.isEmpty(headers)) {
                headers.forEach((key, value) -> entityBase.addHeader(key, value));
            }

            final List<NameValuePair> pairs = covertParams2Nvps(params);
            entityBase.setEntity(new UrlEncodedFormEntity(pairs, this.conf.getCharset()));
            return entityBase;
        } catch (final Throwable e) {
            throw new HttpClientInvokeException(e.getMessage(), e);
        }
    }

    /**
     * 构建HttpClient请求参数列表.
     *
     * @param params 参数列表
     * @return NameValuePair集合
     */
    protected List<NameValuePair> covertParams2Nvps(final Map<String, String> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Collections.emptyList();
        }

        final List<NameValuePair> pairs = Lists.newArrayList();
        params.forEach((key, value) -> pairs.add(new BasicNameValuePair(key, value)));
        return pairs;
    }

    @Override
    public HttpResponse execute(final HttpRequestBase request) {
        final long send = System.currentTimeMillis();
        try (CloseableHttpResponse response = client.execute(request)) {
            final StatusLine status = response.getStatusLine();
            final HttpEntity entity = response.getEntity();
            final String entityStr;
            if (entity != null) {
                entityStr = EntityUtils.toString(entity, this.conf.getCharset());
            } else {
                entityStr = null;
            }

            return HttpResponse.create(status.getStatusCode(), status.getReasonPhrase(),
                    entityStr, send, System.currentTimeMillis());
        } catch (final Throwable e) {
            throw new HttpClientException(e.getMessage(), e, send, System.currentTimeMillis());
        }
    }
}
