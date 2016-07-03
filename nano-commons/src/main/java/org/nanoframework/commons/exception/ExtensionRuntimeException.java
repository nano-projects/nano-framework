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
package org.nanoframework.commons.exception;

/**
 * 扩展运行时异常.
 * @author yanghe
 * @date 2015年6月6日 下午10:34:21 
 */
public class ExtensionRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 3857733068566760284L;

    public ExtensionRuntimeException() {

    }

    public ExtensionRuntimeException(String message) {
        super(message);

    }

    public ExtensionRuntimeException(String message, Throwable cause) {
        super(message, cause);

    }

    /** 现在不在现实异常类型 by yanghe on 2015-07-01 19:32
    @Override
    public String getMessage() {
    	return getCause() == null ? super.getMessage() : getCause().getClass().getName() + ": " + super.getMessage();
    	
    }
    */
}
