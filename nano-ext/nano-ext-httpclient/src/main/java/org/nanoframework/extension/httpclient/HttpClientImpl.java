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
import static org.nanoframework.commons.util.Charsets.UTF_8;

import java.io.IOException;
import java.net.URISyntaxException;
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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

import com.google.inject.Singleton;

/**
 *
 * @author yanghe
 * @since 1.3.3
 */
@Singleton
public class HttpClientImpl implements HttpClient {
    public static final String TIME_TO_LIVE = "context.httpclient.time.to.live";
    public static final String TIME_UNIT = "context.httpclient.timeunit";
    public static final String MAX_TOTAL = "context.httpclient.max.total";
    public static final String MAX_PER_ROUTE = "context.httpclient.default.max.per.route";
    
    protected final String DEFAULT_TIME_TO_LIVE = "-1";
    protected final String DEFAULT_TIME_UNIT = "MILLISECONDS";
    protected final String DEFAULT_MAX_TOTAL = "20";
    protected final String DEFAULT_MAX_PER_ROUTE = "2";
    
    private long timeToLive;
    private TimeUnit tunit;
    private int maxTotal;
    private int maxPerRoute;
    private static PoolingHttpClientConnectionManager pool;

    public HttpClientImpl() {
        this(false);
    }
    
    public HttpClientImpl(boolean force) {
        if(pool == null || force) {
            this.timeToLive = Long.parseLong(System.getProperty(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE));
            this.tunit = TimeUnit.valueOf(System.getProperty(TIME_UNIT, DEFAULT_TIME_UNIT));
            this.maxTotal = Integer.parseInt(System.getProperty(MAX_TOTAL, DEFAULT_MAX_TOTAL));
            this.maxPerRoute = Integer.parseInt(System.getProperty(MAX_PER_ROUTE, DEFAULT_MAX_PER_ROUTE));
            initHttpClientPool(timeToLive, tunit, maxTotal, maxPerRoute);
        }
    }
    
    public HttpClientImpl(boolean force, long timeToLive, TimeUnit tunit, int maxTotal, int maxPerRoute) {
        if(pool == null || force) {
            this.timeToLive = timeToLive;
            this.tunit = tunit;
            this.maxTotal = maxTotal;
            this.maxPerRoute = maxPerRoute;
            initHttpClientPool(timeToLive, tunit, maxTotal, maxPerRoute);
        }
    }
    
    protected void initHttpClientPool(long timeToLive, TimeUnit tunit, int maxTotal, int maxPerRoute) {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(timeToLive, tunit);
        manager.setMaxTotal(maxTotal);
        manager.setDefaultMaxPerRoute(maxPerRoute);
        pool = manager;
    }

    @Override
    public HttpResponse httpGetRequest(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    @Override
    public HttpResponse httpGetRequest(String url, Map<String, String> params) throws URISyntaxException, IOException {
        URIBuilder builder = new URIBuilder();
        builder.setPath(url);

        List<NameValuePair> pairs = covertParams2NVPS(params);
        builder.setParameters(pairs);

        HttpGet httpGet = new HttpGet(builder.build());
        return getResult(httpGet);
    }

    @Override
    public HttpResponse httpGetRequest(String url, Map<String, String> headers, Map<String, String> params) throws URISyntaxException, IOException {
        URIBuilder builder = new URIBuilder();
        builder.setPath(url);

        List<NameValuePair> pairs = covertParams2NVPS(params);
        builder.setParameters(pairs);

        HttpGet httpGet = new HttpGet(builder.build());
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpGet.addHeader(key, value));
        }

        return getResult(httpGet);
    }

    @Override
    public HttpResponse httpPostRequest(String url) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }

    @Override
    public HttpResponse httpPostRequest(String url, Map<String, String> params) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    @Override
    public HttpResponse httpPostRequest(String url, String json) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(json, APPLICATION_JSON));
        return getResult(httpPost);
    }
    
    @Override
    public HttpResponse httpPostRequest(String url, String stream, ContentType contentType) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(stream, contentType));
        return getResult(httpPost);
    }

    @Override
    public HttpResponse httpPostRequest(String url, Map<String, String> headers, String json) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpPost.addHeader(key, value));
        }

        httpPost.setEntity(new StringEntity(json, APPLICATION_JSON));
        return getResult(httpPost);
    }
    
    @Override
    public HttpResponse httpPostRequest(String url, Map<String, String> headers, String stream, ContentType contentType) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpPost.addHeader(key, value));
        }

        httpPost.setEntity(new StringEntity(stream, contentType));
        return getResult(httpPost);
    }

    @Override
    public HttpResponse httpPostRequest(String url, Map<String, String> headers, Map<String, String> params)
            throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpPost.addHeader(key, value));
        }

        List<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    @Override
    public HttpResponse httpPutRequest(String url) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        return getResult(httpPut);
    }

    @Override
    public HttpResponse httpPutRequest(String url, Map<String, String> params) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        List<NameValuePair> pairs = covertParams2NVPS(params);
        httpPut.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPut);
    }

    @Override
    public HttpResponse httpPutRequest(String url, String json) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new StringEntity(json, APPLICATION_JSON));
        return getResult(httpPut);
    }

    @Override
    public HttpResponse httpPutRequest(String url, Map<String, String> headers, String json) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpPut.addHeader(key, value));
        }

        httpPut.setEntity(new StringEntity(json, APPLICATION_JSON));
        return getResult(httpPut);
    }

    @Override
    public HttpResponse httpPutRequest(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpPut.addHeader(key, value));
        }

        List<NameValuePair> pairs = covertParams2NVPS(params);
        httpPut.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPut);
    }
    
    @Override
    public HttpResponse httpDeleteRequest(String url) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        return getResult(httpDelete);
    }
    
    @Override
    public HttpResponse httpDeleteRequest(String url, Map<String, String> headers) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpDelete.addHeader(key, value));
        }
        
        return getResult(httpDelete);
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
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(pool).build();
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                StatusLine status = response.getStatusLine();
                return HttpResponse.create(status.getStatusCode(), status.getReasonPhrase(), EntityUtils.toString(entity));
            }
        }

        return HttpResponse.EMPTY;
    }
}
