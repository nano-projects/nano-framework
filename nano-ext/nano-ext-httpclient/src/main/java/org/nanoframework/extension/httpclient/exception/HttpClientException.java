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
package org.nanoframework.extension.httpclient.exception;

/**
 * @author yanghe
 * @since 1.4.10
 */
public class HttpClientException extends RuntimeException {
    private static final long serialVersionUID = -5055249760651955234L;

    private final long send;
    private final long receive;
    private final long response;

    /**
     * @param send    请求时间
     * @param receive 接收时间
     * @param message 异常信息
     */
    public HttpClientException(final String message, final long send, final long receive) {
        super(message);
        this.send = send;
        this.receive = receive;
        this.response = receive - send;
    }

    /**
     * @param send    请求时间
     * @param receive 接收时间
     * @param message 异常信息
     * @param cause   异常对象
     */
    public HttpClientException(final String message, final Throwable cause, final long send, final long receive) {
        super(message, cause);
        this.send = send;
        this.receive = receive;
        this.response = receive - send;
    }

    public long getSend() {
        return send;
    }

    public long getReceive() {
        return receive;
    }

    public long getResponse() {
        return response;
    }
}
