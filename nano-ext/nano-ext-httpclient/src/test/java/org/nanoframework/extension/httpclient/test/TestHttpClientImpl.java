/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.extension.httpclient.test;

import org.nanoframework.extension.httpclient.HttpClientImpl;
import org.nanoframework.extension.httpclient.inject.HttpConfigure;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author yanghe
 * @since 1.4.10
 */
public class TestHttpClientImpl extends HttpClientImpl {
    /**
     * @param conf HttpClient配置
     */
    public TestHttpClientImpl(final HttpConfigure conf) {
        super(conf);
    }
}
