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
package org.nanoframework.extension.httpclient;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.nanoframework.commons.util.MD5Utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.4.10
 */
public class Http {
    public static final String TIME_TO_LIVE = "context.httpclient.time.to.live";
    public static final String TIME_UNIT = "context.httpclient.timeunit";
    public static final String MAX_TOTAL = "context.httpclient.max.total";
    public static final String MAX_PER_ROUTE = "context.httpclient.default.max.per.route";
    public static final String CHARSET = "context.httpclient.charset";

    /** 超时时间. */
    public static final String DEFAULT_TIME_TO_LIVE = "5000";
    /** 超时时间单位. */
    public static final String DEFAULT_TIME_UNIT = "MILLISECONDS";
    /** 最大连接数 */
    public static final String DEFAULT_MAX_TOTAL = "1024";
    /** 最大并发连接数. */
    public static final String DEFAULT_MAX_PER_ROUTE = "512";
    /** 字符集. */
    public static final String DEFAULT_CHARSET = "UTF-8";

    private static final ConcurrentMap<String, HttpClient> CLIENTS = Maps.newConcurrentMap();

    public final long timeToLive;
    public final TimeUnit tunit;
    public final int maxTotal;
    public final int maxPerRoute;
    public final Charset charset;

    public Http() {
        this(Long.parseLong(System.getProperty(TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE)));
    }

    public Http(final long timeToLive) {
        this(timeToLive, Charset.forName(System.getProperty(CHARSET, DEFAULT_CHARSET)));
    }

    public Http(final long timeToLive, final Charset charset) {
        this(timeToLive, TimeUnit.valueOf(System.getProperty(TIME_UNIT, DEFAULT_TIME_UNIT)),
                Integer.parseInt(System.getProperty(MAX_TOTAL, DEFAULT_MAX_TOTAL)),
                Integer.parseInt(System.getProperty(MAX_PER_ROUTE, DEFAULT_MAX_PER_ROUTE)), charset);
    }

    public Http(final long timeToLive, final TimeUnit tunit, final int maxTotal, final int maxPerRoute, final Charset charset) {
        this.timeToLive = timeToLive;
        this.tunit = tunit;
        this.maxTotal = maxTotal;
        this.maxPerRoute = maxPerRoute;
        this.charset = charset;
    }

    public HttpClient get() {
        final String key = toString();
        if (CLIENTS.containsKey(key)) {
            return CLIENTS.get(key);
        } else {
            final HttpClient client = new HttpClientImpl(this);
            CLIENTS.put(key, client);
            return client;
        }
    }

    @Override
    public String toString() {
        return MD5Utils.md5(JSON.toJSONString(this));
    }

}
