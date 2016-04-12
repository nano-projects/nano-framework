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

import org.nanoframework.commons.entity.BaseEntity;

/**
 *
 * @author yanghe
 * @since 1.3.3
 */
public class HttpResponse extends BaseEntity {
    private static final long serialVersionUID = -3709502418094416380L;

    public static final HttpResponse EMPTY = create(0, "", "");

    public final int statusCode;
    public final String reasonPhrase;
    public final String entity;

    public HttpResponse(int statusCode, String reasonPhrase, String entity) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.entity = entity;
    }

    public static final HttpResponse create(int statusCode, String reasonPhrase, String entity) {
        return new HttpResponse(statusCode, reasonPhrase, entity);
    }
}
