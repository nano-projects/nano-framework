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
package org.nanoframework.examples.quickstart.component;

import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.extension.httpclient.HttpClient;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.inject.Guice;

/**
 *
 * @author yanghe
 * @since 1.3.4
 */
public class RestTest {
    private final Logger logger = LoggerFactory.getLogger(RestTest.class);
    private HttpClient httpClient;
    
    @Before
    public void before() {
        httpClient = Guice.createInjector().getInstance(HttpClient.class);
    }
    
    private void getAllTest() throws IOException {
        logger.debug("Get ALL: {}", httpClient.get("http://localhost:8080/quickstart/rest/elements").entity);
    }
    
    private void getByIdTest(long id) throws IOException {
        logger.debug("GET by ID [{}]: {}", id, httpClient.get("http://localhost:8080/quickstart/rest/elements/" + id).entity);
    }
    
    private void postTest() throws IOException {
        Element el = new Element();
        el.setText("new hello");
        Map<String, String> params = Maps.newHashMap();
        params.put("el", JSON.toJSONString(el));
        logger.debug(httpClient.post("http://localhost:8080/quickstart/rest/elements", params).entity);
        
        getAllTest();
        getByIdTest(1);
    }
    
    private void putTest() throws IOException {
        Element el = new Element();
        el.setId(1L);
        el.setText("Update Hello");
        Map<String, String> params = Maps.newHashMap();
        params.put("el", JSON.toJSONString(el));
        logger.debug(httpClient.put("http://localhost:8080/quickstart/rest/elements", params).entity);
        
        getAllTest();
        getByIdTest(1L);
    }
    
    private void deleteTest(long id) throws IOException {
        logger.debug(httpClient.delete("http://localhost:8080/quickstart/rest/elements/" + id).entity);
        getAllTest();
        getByIdTest(id);

    }
    
    @Ignore
    @Test
    public void httpTest() throws IOException {
        logger.debug("POST Request");
        postTest();
        logger.debug("");
        logger.debug("PUT Request");
        putTest();
        logger.debug("");
        logger.debug("DELETE Request");
        deleteTest(1);
    }
}
