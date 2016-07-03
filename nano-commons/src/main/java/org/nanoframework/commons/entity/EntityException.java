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
package org.nanoframework.commons.entity;

import org.nanoframework.commons.exception.ExtensionRuntimeException;

/**
 * 实体类操作异常类.
 * @author yanghe
 * @date 2015年6月9日 上午8:57:21 
 */
public class EntityException extends ExtensionRuntimeException {
    private static final long serialVersionUID = 3642079270948106738L;

    public EntityException() {

    }

    public EntityException(String message) {
        super(message);

    }

    public EntityException(String message, Throwable cause) {
        super(message, cause);

    }

    @Override
    public String getMessage() {
        return "实体类操作异常: " + super.getMessage();
    }

}
