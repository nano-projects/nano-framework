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
package org.nanoframework.extension.shiro.authc;

import org.apache.shiro.authc.AuthenticationException;

public class DisabledAuthenticationException extends AuthenticationException {
    private static final long serialVersionUID = -4703042566287167334L;

    /**
     * Creates a new DisabledAuthenticationException.
     */
    public DisabledAuthenticationException() {
        super();
    }

    /**
     * Constructs a new DisabledAuthenticationException.
     *
     * @param message the reason for the exception
     */
    public DisabledAuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DisabledAuthenticationException.
     *
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public DisabledAuthenticationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new DisabledAuthenticationException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public DisabledAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
