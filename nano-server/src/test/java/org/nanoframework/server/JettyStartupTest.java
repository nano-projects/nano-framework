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
package org.nanoframework.server;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.httpclient.HttpResponse;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.HttpStatusCode;
import org.nanoframework.web.server.http.status.ResultMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.inject.Guice;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:16:27
 */
public class JettyStartupTest {

    @Before
	public void init() {
	    JettyCustomServer.DEFAULT.bootstrap("start");
	}
    
    @Test
    public void invoke() throws IOException {
        final HttpClient httpClient = Guice.createInjector().getInstance(HttpClient.class);
        final HttpResponse response = httpClient.get("http://localhost:8080/jetty/v1/test");
        Assert.assertEquals(response.statusCode, HttpStatusCode.SC_OK);
        final ResultMap result = ResultMap.create(JSON.parseObject(response.entity, new TypeReference<Map<String, Object>>() { }));
        Assert.assertEquals(result.getInfo(), HttpStatus.OK.info);
    }
	
}
