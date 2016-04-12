/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.core.httpclient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;

/**
 *
 * @author yanghe
 * @since 1.3.3
 * @deprecated move to nano-ext-httpclient module
 */
@Deprecated
public class HttpClientFactory {
	private PoolingHttpClientConnectionManager pool;
	private final String UTF8 = "UTF-8";
	private static HttpClientFactory FACTORY;

	private HttpClientFactory(PoolingHttpClientConnectionManager pool) {
		this.pool = pool;
	}

	public static final void create() {
		Assert.isNull(FACTORY, "HttpClientFactory has been instantiated.");
		FACTORY = new HttpClientFactory(new PoolingHttpClientConnectionManager());
	}

	public static final void create(PoolingHttpClientConnectionManager pool) {
		Assert.isNull(FACTORY, "HttpClientFactory has been instantiated.");
		FACTORY = new HttpClientFactory(pool);
	}

	public static final void create(Object pool) {
		Assert.isNull(FACTORY, "HttpClientFactory has been instantiated.");
		FACTORY = new HttpClientFactory((PoolingHttpClientConnectionManager) pool);
	}

	public static final HttpClientFactory get() {
		Assert.notNull(FACTORY, "HttpClientFactory must be instantiation.");
		return FACTORY;
	}

	/**
	 * 通过连接池获取HttpClient
	 * 
	 * @return
	 */
	private CloseableHttpClient getHttpClient() {
		return HttpClients.custom().setConnectionManager(pool).build();
	}

	/**
	 * @param url
	 * @return
	 */
	public HttpResponse httpGetRequest(String url) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		return getResult(httpGet);
	}

	public HttpResponse httpGetRequest(String url, Map<String, String> params) throws URISyntaxException, IOException {
		URIBuilder builder = new URIBuilder();
		builder.setPath(url);

		List<NameValuePair> pairs = covertParams2NVPS(params);
		builder.setParameters(pairs);

		HttpGet httpGet = new HttpGet(builder.build());
		return getResult(httpGet);
	}

	public HttpResponse httpGetRequest(String url, Map<String, String> headers, Map<String, String> params)
			throws URISyntaxException, IOException {
		URIBuilder builder = new URIBuilder();
		builder.setPath(url);

		List<NameValuePair> pairs = covertParams2NVPS(params);
		builder.setParameters(pairs);

		HttpGet httpGet = new HttpGet(builder.build());
		if (!CollectionUtils.isEmpty(headers))
			headers.forEach((key, value) -> httpGet.addHeader(key, value));

		return getResult(httpGet);
	}

	public HttpResponse httpPostRequest(String url) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		return getResult(httpPost);
	}

	public HttpResponse httpPostRequest(String url, Map<String, String> params) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> pairs = covertParams2NVPS(params);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF8));
		return getResult(httpPost);
	}

	public HttpResponse httpPostRequest(String url, String json) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		return getResult(httpPost);
	}
	
	public HttpResponse httpPostRequest(String url, String stream, ContentType contentType) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(stream, contentType));
		return getResult(httpPost);
	}

	public HttpResponse httpPostRequest(String url, Map<String, String> headers, String json) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		if (!CollectionUtils.isEmpty(headers))
			headers.forEach((key, value) -> httpPost.addHeader(key, value));

		httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		return getResult(httpPost);
	}
	
	public HttpResponse httpPostRequest(String url, Map<String, String> headers, String stream, ContentType contentType) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		if (!CollectionUtils.isEmpty(headers))
			headers.forEach((key, value) -> httpPost.addHeader(key, value));

		httpPost.setEntity(new StringEntity(stream, contentType));
		return getResult(httpPost);
	}

	public HttpResponse httpPostRequest(String url, Map<String, String> headers, Map<String, String> params)
			throws IOException {
		HttpPost httpPost = new HttpPost(url);
		if (!CollectionUtils.isEmpty(headers))
			headers.forEach((key, value) -> httpPost.addHeader(key, value));

		List<NameValuePair> pairs = covertParams2NVPS(params);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF8));
		return getResult(httpPost);
	}

	public HttpResponse httpPutRequest(String url) throws IOException {
		HttpPut httpPut = new HttpPut(url);
		return getResult(httpPut);
	}

	public HttpResponse httpPutRequest(String url, Map<String, String> params) throws IOException {
		HttpPut httpPut = new HttpPut(url);
		List<NameValuePair> pairs = covertParams2NVPS(params);
		httpPut.setEntity(new UrlEncodedFormEntity(pairs, UTF8));
		return getResult(httpPut);
	}

	public HttpResponse httpPutRequest(String url, String json) throws IOException {
		HttpPut httpPut = new HttpPut(url);
		httpPut.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		return getResult(httpPut);
	}

	public HttpResponse httpPutRequest(String url, Map<String, String> headers, String json) throws IOException {
		HttpPut httpPut = new HttpPut(url);
		if (!CollectionUtils.isEmpty(headers))
			headers.forEach((key, value) -> httpPut.addHeader(key, value));

		httpPut.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		return getResult(httpPut);
	}

	public HttpResponse httpPutRequest(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
		HttpPut httpPut = new HttpPut(url);
		if (!CollectionUtils.isEmpty(headers))
			headers.forEach((key, value) -> httpPut.addHeader(key, value));

		List<NameValuePair> pairs = covertParams2NVPS(params);
		httpPut.setEntity(new UrlEncodedFormEntity(pairs, UTF8));
		return getResult(httpPut);
	}
	
	public HttpResponse httpDeleteRequest(String url) throws IOException {
		HttpDelete httpDelete = new HttpDelete(url);
		return getResult(httpDelete);
	}
	
	public HttpResponse httpDeleteRequest(String url, Map<String, String> headers) throws IOException {
		HttpDelete httpDelete = new HttpDelete(url);
		if (!CollectionUtils.isEmpty(headers)) {
			headers.forEach((key, value) -> httpDelete.addHeader(key, value));
		}
		
		return getResult(httpDelete);
	}

	private List<NameValuePair> covertParams2NVPS(Map<String, String> params) {
		if (CollectionUtils.isEmpty(params))
			return Collections.emptyList();

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
	private HttpResponse getResult(HttpRequestBase request) throws IOException {
		CloseableHttpClient httpClient = getHttpClient();
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