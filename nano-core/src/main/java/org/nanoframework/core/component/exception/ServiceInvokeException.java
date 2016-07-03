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
package org.nanoframework.core.component.exception;

import org.nanoframework.commons.exception.ExtensionRuntimeException;

/**
 * 组件服务调用异常
 * 
 * @author yanghe
 * @date 2015年6月11日 下午2:59:14 
 *
 */
public class ServiceInvokeException extends ExtensionRuntimeException {
    private static final long serialVersionUID = -729420009370728985L;

    public ServiceInvokeException() {

    }

    public ServiceInvokeException(String message) {
        super(message);

    }

    public ServiceInvokeException(String message, Throwable cause) {
        super(message, cause);

    }

    @Override
    public String getMessage() {
        return "服务调用异常: " + super.getMessage();
    }

}
