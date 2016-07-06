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
 *
 * @author yanghe
 * @since 1.3.7
 */
public class UnsupportedAccessException extends RuntimeException {
    private static final long serialVersionUID = 5353228077114158278L;

    /**
     * Default Constructor. 
     */
    public UnsupportedAccessException() {
        
    }
    
    /**
     * 
     * @param message the message
     */
    public UnsupportedAccessException(String message) {
        super(message);
    }

    /**
     * 
     * @param message the message
     * @param cause the cause
     */
    public UnsupportedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
