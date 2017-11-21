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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.MD5Utils;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.core.spi.SPIException;
import org.nanoframework.core.spi.SPILoader;
import org.nanoframework.core.spi.SPIMapper;
import org.nanoframework.extension.httpclient.HttpClient;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author yanghe
 * @since 1.4.10
 */
public class HttpConfigure extends BaseEntity {
    /** */
    public static final String TIME_TO_LIVE = "context.httpclient.time.to.live";
    /** */
    public static final String TIME_UNIT = "context.httpclient.timeunit";
    /** */
    public static final String MAX_TOTAL = "context.httpclient.max.total";
    /** */
    public static final String MAX_PER_ROUTE = "context.httpclient.default.max.per.route";
    /** */
    public static final String CHARSET = "context.httpclient.charset";

    /**
     * 默认HttpClient实现.
     */
    public static final String DEFAULT_SPI_NAME = "default";
    /**
     * 超时时间.
     */
    public static final String DEFAULT_TIME_TO_LIVE = "5000";
    /**
     * 超时时间单位.
     */
    public static final String DEFAULT_TIME_UNIT = "MILLISECONDS";
    /**
     * 最大连接数.
     */
    public static final String DEFAULT_MAX_TOTAL = "1024";
    /**
     * 最大并发连接数.
     */
    public static final String DEFAULT_MAX_PER_ROUTE = "512";
    /**
     * 字符集.
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    private static final ConcurrentMap<String, HttpClient> CLIENTS = Maps.newConcurrentMap();

    private final String spi;
    private final long timeToLive;
    private final TimeUnit tunit;
    private final int maxTotal;
    private final int maxPerRoute;
    private final Charset charset;

    /**
     * Default Constructor.
     */
    public HttpConfigure() {
        this(Long.parseLong(System.getProperty(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE)));
    }

    /**
     * @param timeToLive 超时时间
     */
    public HttpConfigure(final long timeToLive) {
        this(timeToLive, Charset.forName(System.getProperty(CHARSET, DEFAULT_CHARSET)));
    }

    /**
     * @param timeToLive 超时时间
     * @param charset    字符集
     */
    public HttpConfigure(final long timeToLive, final Charset charset) {
        this(DEFAULT_SPI_NAME, timeToLive, TimeUnit.valueOf(System.getProperty(TIME_UNIT, DEFAULT_TIME_UNIT)),
                Integer.parseInt(System.getProperty(MAX_TOTAL, DEFAULT_MAX_TOTAL)),
                Integer.parseInt(System.getProperty(MAX_PER_ROUTE, DEFAULT_MAX_PER_ROUTE)), charset);
    }

    /**
     * @param spi         SPI名称
     * @param timeToLive  超时时间
     * @param tunit       超时时间单位
     * @param maxTotal    最大连接数
     * @param maxPerRoute 最大并发连接数
     * @param charset     字符集
     */
    public HttpConfigure(final String spi, final long timeToLive, final TimeUnit tunit, final int maxTotal,
                         final int maxPerRoute, final Charset charset) {
        this.spi = spi;
        this.timeToLive = timeToLive;
        this.tunit = tunit;
        this.maxTotal = maxTotal;
        this.maxPerRoute = maxPerRoute;
        this.charset = charset;
    }

    /**
     * @return HttpClient
     */
    public HttpClient get() {
        final String key = toString();
        if (CLIENTS.containsKey(key)) {
            return CLIENTS.get(key);
        } else {
            final List<SPIMapper> spis = SPILoader.spis().get(HttpClient.class);
            if (!CollectionUtils.isEmpty(spis)) {
                for (final SPIMapper spi : spis) {
                    if (StringUtils.equals(spi.getName(), this.spi)) {
                        final Class<?> instance = spi.getInstance();
                        if (HttpClient.class.isAssignableFrom(instance)) {
                            final HttpClient client = (HttpClient) ReflectUtils.newInstance(spi.getInstance(), this);
                            CLIENTS.put(key, client);
                            return client;
                        } else {
                            throw new SPIException(MessageFormat.format("无效的SPI定义: HttpClient is not assignable from {0}", spi.getInstanceClsName()));
                        }
                    }
                }
            }

            throw new SPIException("无效的SPI定义");
        }
    }

    /**
     * @return 超时时间
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     * @return 超时时间单位
     */
    public TimeUnit getTunit() {
        return tunit;
    }

    /**
     * @return 最大连接数
     */
    public int getMaxTotal() {
        return maxTotal;
    }

    /**
     * @return 最大并发连接数
     */
    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    /**
     * @return 字符集
     */
    public Charset getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        return MD5Utils.md5(JSON.toJSONString(this));
    }

}
