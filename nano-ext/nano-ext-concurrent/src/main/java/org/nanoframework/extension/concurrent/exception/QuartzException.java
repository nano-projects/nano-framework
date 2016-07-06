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
package org.nanoframework.extension.concurrent.exception;

/**
 * 调度任务异常.
 * @author yanghe
 * @since 1.0
 */
@Deprecated
public class QuartzException extends RuntimeException {
    private static final long serialVersionUID = -2775206802007728986L;

    public QuartzException() {

    }

    public QuartzException(String message) {
        super(message);

    }

    public QuartzException(String message, Throwable cause) {
        super(message, cause);

    }

    @Override
    public String getMessage() {
        return "调度任务异常: " + super.getMessage();
    }

}
