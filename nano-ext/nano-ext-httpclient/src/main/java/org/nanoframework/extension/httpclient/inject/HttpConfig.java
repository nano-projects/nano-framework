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
package org.nanoframework.extension.httpclient.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author yanghe
 * @since 1.4.10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface HttpConfig {
    /**
     * 默认HttpClient实现.
     */
    String DEFAULT_SPI_NAME = "default";
    /**
     * 超时时间.
     */
    long DEFAULT_TIME_TO_LIVE = 5000;
    /**
     * 最大连接数.
     */
    int DEFAULT_MAX_TOTAL = 1024;
    /**
     * 最大并发连接数.
     */
    int DEFAULT_MAX_PER_ROUTE = 512;
    /**
     * 字符集.
     */
    String DEFAULT_CHARSET = "UTF-8";

    /**
     * @return 默认HttpClient实现
     */
    String spi() default DEFAULT_SPI_NAME;

    /**
     * @return 超时时间
     */
    long timeToLive() default DEFAULT_TIME_TO_LIVE;

    /**
     * @return 超时时间单位
     */
    TimeUnit tunit() default TimeUnit.MILLISECONDS;

    /**
     * @return 最大连接数
     */
    int maxTotal() default DEFAULT_MAX_TOTAL;

    /**
     * @return 最大并发连接数
     */
    int maxPerRoute() default DEFAULT_MAX_PER_ROUTE;

    /**
     * @return 字符集
     */
    String charset() default DEFAULT_CHARSET;
}
