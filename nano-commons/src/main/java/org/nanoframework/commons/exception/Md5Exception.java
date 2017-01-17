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
package org.nanoframework.commons.exception;

/**
 *
 * @author yanghe
 * @since 1.4.6
 */
public class Md5Exception extends RuntimeException {
    private static final long serialVersionUID = 1618405623971362367L;

    /**
     * Default Constructor. 
     */
    public Md5Exception() {
        
    }
    
    /**
     * 
     * @param message the message
     */
    public Md5Exception(final String message) {
        super(message);
    }

    /**
     * 
     * @param message the message
     * @param cause the cause
     */
    public Md5Exception(final String message, final Throwable cause) {
        super(message, cause);
    }
}
