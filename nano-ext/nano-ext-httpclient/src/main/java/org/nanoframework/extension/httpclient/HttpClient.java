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
     * @deprecated rename to get
     */
    @Deprecated
    HttpResponse httpGetRequest(String url) throws IOException;

    /**
     * Http 'GET' request.
     * 
     * @param url request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws URISyntaxException if URI Syntax errors occur
     * @throws IOException if I/O errors occur
     * @deprecated rename to get
     */
    @Deprecated
    HttpResponse httpGetRequest(String url, Map<String, String> params) throws URISyntaxException, IOException;

    /**
     * Http 'GET' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param params request parameter map
     * @return HttpResponse
     * @throws URISyntaxException if URI Syntax errors occur
     * @throws IOException if I/O errors occur
     * @deprecated rename to get
     */
    @Deprecated
    HttpResponse httpGetRequest(String url, Map<String, String> headers, Map<String, String> params) throws URISyntaxException, IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to post
     */
    @Deprecated
    HttpResponse httpPostRequest(String url) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to post
     */
    @Deprecated
    HttpResponse httpPostRequest(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param json request json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to post
     */
    @Deprecated
    HttpResponse httpPostRequest(String url, String json) throws IOException;
    
    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param stream stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to post
     */
    @Deprecated
    HttpResponse httpPostRequest(String url, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param json json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to post
     */
    @Deprecated
    HttpResponse httpPostRequest(String url, Map<String, String> headers, String json) throws IOException;
    
    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param stream stream string
     * @param contentType httpclient ContentType
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to post
     */
    @Deprecated
    HttpResponse httpPostRequest(String url, Map<String, String> headers, String stream, ContentType contentType) throws IOException;

    /**
     * Http 'POST' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to post
     */
    @Deprecated
    HttpResponse httpPostRequest(String url, Map<String, String> headers, Map<String, String> params) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to put
     */
    @Deprecated
    HttpResponse httpPutRequest(String url) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to put
     */
    @Deprecated
    HttpResponse httpPutRequest(String url, Map<String, String> params) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param json request json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to put
     */
    @Deprecated
    HttpResponse httpPutRequest(String url, String json) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @param json json string
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to put
     */
    @Deprecated
    HttpResponse httpPutRequest(String url, Map<String, String> headers, String json) throws IOException;

    /**
     * Http 'PUT' request.
     * 
     * @param url request url
     * @param headers request hreaders map
     * @param params request parameter map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to put
     */
    @Deprecated
    HttpResponse httpPutRequest(String url, Map<String, String> headers, Map<String, String> params) throws IOException;
    
    /**
     * Http 'DELETE' request.
     * 
     * @param url request url
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to delete
     */
    @Deprecated
    HttpResponse httpDeleteRequest(String url) throws IOException;
    
    /**
     * Http 'DELETE' request.
     * 
     * @param url request url
     * @param headers request headers map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     * @deprecated rename to delete
     */
    @Deprecated
    HttpResponse httpDeleteRequest(String url, Map<String, String> headers) throws IOException;
    
    
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
     * @param headers request headers map
     * @return HttpResponse
     * @throws IOException if I/O errors occur
     */
    HttpResponse delete(String url, Map<String, String> headers) throws IOException;
}
