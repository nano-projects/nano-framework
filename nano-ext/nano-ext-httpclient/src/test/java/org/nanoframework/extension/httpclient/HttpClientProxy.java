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

import com.google.inject.Inject;
import org.nanoframework.extension.httpclient.inject.HttpConfig;

/**
 *
 * @author yanghe
 * @since 1.4.10
 */
public class HttpClientProxy {
    private HttpClient client;
    private HttpClient testClient;

    @Inject
    @HttpConfig(charset = "GBK")
    public HttpClient getClient() {
        return client;
    }

    public void setClient(final HttpClient client) {
        this.client = client;
    }

    @Inject
    @HttpConfig(spi = "test")
    public HttpClient getTestClient() {
        return testClient;
    }

    public void setTestClient(final HttpClient testClient) {
        this.testClient = testClient;
    }
}
