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

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;

import com.google.inject.Guice;

/**
 *
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
}
