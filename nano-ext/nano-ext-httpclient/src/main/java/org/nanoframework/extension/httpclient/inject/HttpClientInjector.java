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

package org.nanoframework.extension.httpclient.inject;

import com.google.inject.MembersInjector;
import org.nanoframework.extension.httpclient.HttpClient;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * @author yanghe
 * @since 1.4.10
 */
public class HttpClientInjector<T> implements MembersInjector<T> {
    private final Field field;

    /**
     * @param field 依赖注入属性Field
     */
    public HttpClientInjector(final Field field) {
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(final T instance) {
        try {
            final HttpConfig http = field.getAnnotation(HttpConfig.class);
            final HttpConfigure conf = new HttpConfigure(http.spi(), http.timeToLive(), http.tunit(),
                    http.maxTotal(), http.maxPerRoute(), Charset.forName(http.charset()));
            final HttpClient client = conf.get();
            field.set(instance, client);
        } catch (final IllegalAccessException e) {
            throw (Error) new IllegalAccessError(e.getMessage()).initCause(e);
        }
    }
}