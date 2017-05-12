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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;

import com.google.inject.ImplementedBy;

/**
 *
 * @author yanghe
 * @since 1.3.3
 */
@ImplementedBy(HttpClientImpl.class)
public interface HttpClient {
    /**
     * Http 'GET' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse get(String url) throws IOException;

    /**
     * Http 'GET' request.
     * 
     * @param url request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws URISyntaxException if URI Syntax errors occur
     * @throws IOException if I/O errors occur
     */
    HttpResponse get(String url, Map<String, String> params) throws URISyntaxException, IOException;

    /**
     * Http 'GET' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param params request parameter map
     * @return HttpResponse
     * @throws URISyntaxException if URI Syntax errors occur
     * @throws IOException if I/O errors occur
     */
    HttpResponse get(String url, Map<String, String> headers, Map<String, String> params) throws URISyntaxException, IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse post(String url) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse post(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param json request json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse post(String url, String json) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param stream stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse post(String url, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param json json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse post(String url, Map<String, String> headers, String json) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param stream stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse post(String url, Map<String, String> headers, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse post(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse put(String url) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse put(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param json request json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse put(String url, String json) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url the url
     * @param stream the stream
     * @param contentType the contentType
     * @return HttpResponse
     * @throws IOException
     */
    HttpResponse put(String url, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url the url
     * @param stream the stream
     * @param contentType the contentType
     * @return HttpResponse
     * @throws IOException
     */
    HttpResponse put(String url, Map<String, String> headers, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param json json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse put(String url, Map<String, String> headers, String json) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse put(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    /**
     * Http 'DELETE' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse delete(String url) throws IOException;

    /**
     * Http 'DELETE' request.
     * 
     * @param url request url
     * @param params request params map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse delete(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'DELETE' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param params the params
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse delete(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    /**
     * Http 'PATCH' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException IOException if I/O errors occur
     */
    HttpResponse patch(String url) throws IOException;

    /**
     * Http 'PATCH' request.
     * 
     * @param url request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse patch(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'PATCH' request.
     * 
     * @param url request url
     * @param json request json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse patch(String url, String json) throws IOException;

    /**
     * Http 'PATCH' request.
     * 
     * @param url request url
     * @param stream stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse patch(String url, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'PATCH' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param json json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse patch(String url, Map<String, String> headers, String json) throws IOException;

    /**
     * Http 'PATCH' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param stream stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse patch(String url, Map<String, String> headers, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'PATCH' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse patch(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    /**
     * Http 'HEAD' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse head(String url) throws IOException;

    /**
     * Http 'HEAD' request.
     * 
     * @param url request url
     * @param params request params map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse head(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'HEAD' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param params the params
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse head(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    /**
     * Http 'OPTIONS' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse options(String url) throws IOException;

    /**
     * Http 'OPTIONS' request.
     * 
     * @param url request url
     * @param params request params map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse options(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'OPTIONS' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param params the params
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse options(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    /**
     * Http 'TRACE' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse trace(String url) throws IOException;

    /**
     * Http 'TRACE' request.
     * 
     * @param url request url
     * @param params request params map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse trace(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'TRACE' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param params the params
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse trace(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    HttpResponse execute(RequestMethod requestMethod, String url) throws IOException;

    HttpResponse execute(RequestMethod requestMethod, String url, Map<String, String> params) throws IOException;

    HttpResponse execute(RequestMethod requestMethod, String url, String json) throws IOException;

    HttpResponse execute(RequestMethod requestMethod, String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    HttpResponse execute(RequestMethod requestMethod, String url, Map<String, String> headers, String json) throws IOException;

    HttpResponse execute(RequestMethod requestMethod, String url, String stream, ContentType contentType) throws IOException;

    HttpResponse execute(RequestMethod requestMethod, String url, Map<String, String> headers, String stream, ContentType contentType)
            throws IOException;
}
