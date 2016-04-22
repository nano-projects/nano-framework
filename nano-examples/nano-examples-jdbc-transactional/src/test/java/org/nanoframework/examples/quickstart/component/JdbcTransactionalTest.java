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

import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.httpclient.HttpClient;

import com.google.inject.Guice;

/**
 *
 * @author yanghe
 * @since 1.3.5
 */
public class JdbcTransactionalTest {
    private Logger logger = LoggerFactory.getLogger(JdbcTransactionalTest.class);
    private HttpClient httpClient;
    
    @Before
    public void before() {
        httpClient = Guice.createInjector().getInstance(HttpClient.class);
    }
    
    private void getAllTest() throws IOException {
        logger.debug("Get ALL: {}", httpClient.httpGetRequest("http://localhost:8080/quickstart/rest/elements").entity);
    }
    
    @Test
    public void transactionalTest() throws IOException {
        logger.debug("");
        logger.debug("Batch Test: ");
        logger.debug(httpClient.httpPostRequest("http://localhost:8080/quickstart/rest/elements/batch", 
                "els[]={\"text\":\"new hello batch 0\"}&els[]={\"text\":\"new hello batch 1\"}", ContentType.APPLICATION_FORM_URLENCODED).entity);
        
        getAllTest();
    }
    
    @Test
    public void transactionalFailTest() throws IOException {
        logger.debug("");
        logger.debug("Fail Batch Test: ");
        logger.debug(httpClient.httpPostRequest("http://localhost:8080/quickstart/rest/elements/batch", 
                "els[]={\"text\":\"new hello batch 2\"}&els[]={}", ContentType.APPLICATION_FORM_URLENCODED).entity);
        
        getAllTest();
    }
}
