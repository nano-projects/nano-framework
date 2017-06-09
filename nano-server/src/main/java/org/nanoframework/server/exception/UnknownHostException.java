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
package org.nanoframework.server.exception;

/**
 *
 * @author yanghe
 * @since 1.4.6
 */
public class UnknownHostException extends RuntimeException {
    private static final long serialVersionUID = -8396402922781694861L;

    /**
     * Constructs a new {@code UnknownHostException} with the
     * specified detail message.
     *
     * @param   host   the detail message.
     */
    public UnknownHostException(final String host) {
        super(host);
    }

    /**
     * Constructs a new {@code UnknownHostException} with no detail
     * message.
     */
    public UnknownHostException() {
    }
}
