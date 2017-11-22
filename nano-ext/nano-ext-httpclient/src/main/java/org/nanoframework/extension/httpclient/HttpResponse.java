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
 * @author yanghe
 * @since 1.3.3
 */
public class HttpResponse extends BaseEntity {
    public static final HttpResponse EMPTY = create(0, "", "", 0, 0);
    private static final long serialVersionUID = -3709502418094416380L;
    public final int statusCode;
    public final String reasonPhrase;
    public final String entity;
    public final long send;
    public final long receive;
    public final long response;

    public HttpResponse(final int statusCode, final String reasonPhrase, final String entity,
                        final long send, final long receive) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.entity = entity;
        this.send = send;
        this.receive = receive;
        this.response = receive - send;
    }

    public static final HttpResponse create(final int statusCode, final String reasonPhrase, final String entity,
                                            final long send, final long receive) {
        return new HttpResponse(statusCode, reasonPhrase, entity, send, receive);
    }
}
