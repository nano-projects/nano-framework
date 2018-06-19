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

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.spi.Lazy;
import org.nanoframework.extension.httpclient.exception.HttpClientException;

import com.google.inject.ImplementedBy;

/**
 * @author yanghe
 * @since 1.3.3
 */
@Lazy
@ImplementedBy(HttpClientImpl.class)
public interface HttpClient {
    /**
     * Http 'GET' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse get(String url) throws HttpClientException;

    /**
     * Http 'GET' request.
     *
     * @param url    request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws URISyntaxException  if URI Syntax errors occur
     * @throws HttpClientException Http请求异常
     */
    HttpResponse get(String url, Map<String, String> params) throws URISyntaxException, HttpClientException;

    /**
     * Http 'GET' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param params  request parameter map
     * @return HttpResponse
     * @throws URISyntaxException  if URI Syntax errors occur
     * @throws HttpClientException Http请求异常
     */
    HttpResponse get(String url, Map<String, String> headers, Map<String, String> params) throws URISyntaxException, HttpClientException;

    /**
     * Http 'POST' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse post(String url) throws HttpClientException;

    /**
     * Http 'POST' request.
     *
     * @param url    request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse post(String url, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'POST' request.
     *
     * @param url  request url
     * @param json request json string
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse post(String url, String json) throws HttpClientException;

    /**
     * Http 'POST' request.
     *
     * @param url         request url
     * @param stream      stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse post(String url, String stream, ContentType contentType) throws HttpClientException;

    /**
     * Http 'POST' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param json    json string
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse post(String url, Map<String, String> headers, String json) throws HttpClientException;

    /**
     * Http 'POST' request.
     *
     * @param url         request url
     * @param headers     request hreaders map
     * @param stream      stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse post(String url, Map<String, String> headers, String stream, ContentType contentType) throws HttpClientException;

    /**
     * Http 'POST' request.
     *
     * @param url     request url
     * @param headers request hreaders map
     * @param params  request parameter map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse post(String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'PUT' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse put(String url) throws HttpClientException;

    /**
     * Http 'PUT' request.
     *
     * @param url    request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse put(String url, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'PUT' request.
     *
     * @param url  request url
     * @param json request json string
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse put(String url, String json) throws HttpClientException;

    /**
     * Http 'PUT' request.
     *
     * @param url         the url
     * @param stream      the stream
     * @param contentType the contentType
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse put(String url, String stream, ContentType contentType) throws HttpClientException;

    /**
     * Http 'PUT' request.
     *
     * @param url         the url
     * @param headers     请求头
     * @param stream      the stream
     * @param contentType the contentType
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse put(String url, Map<String, String> headers, String stream, ContentType contentType) throws HttpClientException;

    /**
     * Http 'PUT' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param json    json string
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse put(String url, Map<String, String> headers, String json) throws HttpClientException;

    /**
     * Http 'PUT' request.
     *
     * @param url     request url
     * @param headers request hreaders map
     * @param params  request parameter map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse put(String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'DELETE' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse delete(String url) throws HttpClientException;

    /**
     * Http 'DELETE' request.
     *
     * @param url    request url
     * @param params request params map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse delete(String url, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'DELETE' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param params  the params
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse delete(String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'PATCH' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException IOException Http请求异常
     */
    HttpResponse patch(String url) throws HttpClientException;

    /**
     * Http 'PATCH' request.
     *
     * @param url    request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse patch(String url, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'PATCH' request.
     *
     * @param url  request url
     * @param json request json string
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse patch(String url, String json) throws HttpClientException;

    /**
     * Http 'PATCH' request.
     *
     * @param url         request url
     * @param stream      stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse patch(String url, String stream, ContentType contentType) throws HttpClientException;

    /**
     * Http 'PATCH' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param json    json string
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse patch(String url, Map<String, String> headers, String json) throws HttpClientException;

    /**
     * Http 'PATCH' request.
     *
     * @param url         request url
     * @param headers     request hreaders map
     * @param stream      stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse patch(String url, Map<String, String> headers, String stream, ContentType contentType) throws HttpClientException;

    /**
     * Http 'PATCH' request.
     *
     * @param url     request url
     * @param headers request hreaders map
     * @param params  request parameter map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse patch(String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'HEAD' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse head(String url) throws HttpClientException;

    /**
     * Http 'HEAD' request.
     *
     * @param url    request url
     * @param params request params map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse head(String url, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'HEAD' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param params  the params
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse head(String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'OPTIONS' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse options(String url) throws HttpClientException;

    /**
     * Http 'OPTIONS' request.
     *
     * @param url    request url
     * @param params request params map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse options(String url, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'OPTIONS' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param params  the params
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse options(String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'TRACE' request.
     *
     * @param url request url
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse trace(String url) throws HttpClientException;

    /**
     * Http 'TRACE' request.
     *
     * @param url    request url
     * @param params request params map
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse trace(String url, Map<String, String> params) throws HttpClientException;

    /**
     * Http 'TRACE' request.
     *
     * @param url     request url
     * @param headers request headers map
     * @param params  the params
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse trace(String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * @param requestMethod 请求类型
     * @param url           URL
     * @return HttpResonse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse process(RequestMethod requestMethod, String url) throws HttpClientException;

    /**
     * @param requestMethod 请求类型
     * @param url           URL
     * @param params        参数列表
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse process(RequestMethod requestMethod, String url, Map<String, String> params) throws HttpClientException;

    /**
     * @param requestMethod 请求类型
     * @param url           URL
     * @param json          JSON格式流式报文
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse process(RequestMethod requestMethod, String url, String json) throws HttpClientException;

    /**
     * @param requestMethod 请求类型
     * @param url           URL
     * @param headers       请求头列表
     * @param params        参数列表
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse process(RequestMethod requestMethod, String url, Map<String, String> headers, Map<String, String> params) throws HttpClientException;

    /**
     * @param requestMethod 请求类型
     * @param url           URL
     * @param headers       请求头列表
     * @param json          JSON格式流式报文
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse process(RequestMethod requestMethod, String url, Map<String, String> headers, String json) throws HttpClientException;

    /**
     * @param requestMethod 请求类型
     * @param url           URL
     * @param stream        流式报文
     * @param contentType   报文类型
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse process(RequestMethod requestMethod, String url, String stream, ContentType contentType) throws HttpClientException;

    /**
     * @param requestMethod 请求类型
     * @param url           URL
     * @param headers       请求头列表
     * @param stream        流式报文
     * @param contentType   报文类型
     * @return HttpResponse
     * @throws HttpClientException Http请求异常
     */
    HttpResponse process(RequestMethod requestMethod, String url, Map<String, String> headers, String stream, ContentType contentType)
            throws HttpClientException;

    /**
     * 处理Http请求.
     *
     * @param request 请求
     * @return 响应
     * @throws HttpClientException Http调用异常
     */
    HttpResponse process(HttpRequestBase request) throws HttpClientException;
}
