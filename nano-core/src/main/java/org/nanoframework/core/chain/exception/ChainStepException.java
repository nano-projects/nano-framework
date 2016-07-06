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
package org.nanoframework.core.chain.exception;

/**
 * @author yanghe
 * @since 1.1
 */
public class ChainStepException extends ChainException {
    private static final long serialVersionUID = -5737419285297498105L;

    public ChainStepException() {
        super();
    }

    public ChainStepException(String message) {
        super(message);
    }

    public ChainStepException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChainStepException(Throwable cause) {
        super(cause);
    }

    protected ChainStepException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
