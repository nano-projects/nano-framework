/**
 * Copyright 2015- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.orm.jdbc.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:09:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface JdbcTransactional {

    /**
     * Flag to indicate that myBatis has to force the transaction {@code commit().}
     *
     * @return false by default, user defined otherwise.
     */
    boolean force() default false;

    /**
     * The exception re-thrown when an error occurs during the transaction.
     *
     * @return the exception re-thrown when an error occurs during the
     *         transaction.
     */
    Class<? extends Throwable> rethrowExceptionsAs() default Exception.class;

    /**
     * A custom error message when throwing the custom exception.
     *
     * It supports java.util.Formatter place holders, intercepted method
     * arguments will be used as message format arguments.
     *
     * @return a custom error message when throwing the custom exception.
     * @see java.util.Formatter#format(String, Object...)
     */
    String exceptionMessage() default "";

    /**
     * If true, the transaction will never committed but rather rolled back, useful for testing purposes.
     *
     * This parameter is false by default.
     *
     * @return if true, the transaction will never committed but rather rolled back, useful for testing purposes.
     */
    boolean rollbackOnly() default false;
    
    /** 数据源名称 */
    String[] envId();

}
