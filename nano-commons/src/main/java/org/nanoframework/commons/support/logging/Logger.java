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
package org.nanoframework.commons.support.logging;

/**
 *
 * @author yanghe
 * @since 1.0
 */
public interface Logger {

    boolean isErrorEnabled();
    
    void error(String message, Throwable e);

    void error(String message);

    void error(String message, Object... args);

    void error(Throwable cause);

    boolean isWarnEnabled();

    void warn(String message);

    void warn(String message, Throwable e);

    void warn(String message, Object... args);

    void warn(Throwable cause);

    boolean isInfoEnabled();

    void info(String message);

    void info(String message, Object... args);

    void info(Throwable cause);

    void info(String message, Throwable cause);

    boolean isDebugEnabled();
    
    void debug(String message);

    void debug(String message, Throwable e);

    void debug(String message, Object... args);

    void debug(Throwable cause);
    
    boolean isTraceEnabled();
    
    void trace(String message);
    
    void trace(String message, Object... args);
    
    void trace(Throwable cause);
    
    void trace(String message, Throwable cause);

}
