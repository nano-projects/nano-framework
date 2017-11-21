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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.util.Charsets;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.defaults.module.SPIModule;
import org.nanoframework.extension.httpclient.inject.HttpConfigure;
import org.nanoframework.extension.httpclient.inject.HttpModule;
import org.nanoframework.extension.httpclient.test.TestHttpClientImpl;

import java.io.IOException;

/**
 * @author yanghe
 * @since 1.3.10
 */
public class HttpClientTest {

    @Test
    public void httpGetTest() throws IOException {
        HttpClient httpClient = Guice.createInjector().getInstance(HttpClient.class);
        HttpResponse response = httpClient.execute(RequestMethod.GET, "https://www.baidu.com");
        Assert.assertEquals(response.statusCode, HttpStatus.SC_OK);
    }

    @Test
    public void generalHttpTest() throws IOException {
        final HttpClient c1 = new HttpConfigure().get();
        final HttpClient c2 = new HttpConfigure().get();
        Assert.assertTrue(c1 == c2);
    }

    @Test
    public void injectHttpClientTest() {
        final HttpClientProxy proxy = Guice.createInjector(new HttpModule()).getInstance(HttpClientProxy.class);
        final HttpClient client = proxy.getClient();
        Assert.assertNotNull(client);
        Assert.assertTrue(client instanceof HttpClientImpl);
        Assert.assertEquals(((HttpClientImpl) client).conf().getCharset(), Charsets.GBK);
    }

    @Test
    public void spiHttpClientTest() {
        Globals.set(Injector.class, Guice.createInjector(new SPIModule()));
        final HttpClientProxy proxy = Guice.createInjector(new HttpModule()).getInstance(HttpClientProxy.class);
        final HttpClient client = proxy.getClient();
        final HttpClient testClient = proxy.getTestClient();

        Assert.assertNotNull(client);
        Assert.assertNotNull(testClient);
        Assert.assertTrue(testClient instanceof TestHttpClientImpl);
    }
}
