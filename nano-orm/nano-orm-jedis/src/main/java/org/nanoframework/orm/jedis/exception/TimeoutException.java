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
package org.nanoframework.orm.jedis.exception;

/**
 * Jedis操作异常处理类.
 * 
 * @author yanghe
 * @since 1.0
 */
public class TimeoutException extends RuntimeException {
    private static final long serialVersionUID = -6151365904901655741L;

    public TimeoutException() {

    }

    public TimeoutException(final String message) {
        super(message);
    }

    public TimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TimeoutException(final Throwable cause) {
        super(cause);
    }

}
