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
package org.nanoframework.orm.consul.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.consul.exception.ConsulException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class ConsulConfig extends BaseEntity {
    /** Consul配置统一前缀. */
    public static final String CONSUL_PREFIX = "consul.";
    /** Consul配置根属性. */
    public static final String CONSUL_ROOT = CONSUL_PREFIX + "root";
    public static final String ID = ".id";
    public static final String HOST_AND_PORT = ".host.port";
    public static final String URL = ".url";
    public static final String USERNAME = ".username";
    public static final String PASSWORD = ".password";
    public static final String TOKEN = ".token";
    public static final String HEADERS = ".headers";
    public static final String TIMEOUT_CONNECT = ".timeout.connect";
    public static final String TIMEOUT_READ = ".timeout.read";
    public static final String TIMEOUT_WRITE = ".timeout.write";
    public static final String SPLIT = ",";

    private static final long serialVersionUID = 3334807345142687159L;
    private static final TypeReference<Map<String, String>> HEADERS_TYPE = new TypeReference<Map<String, String>>() {
    };

    private String id;
    private HostAndPort hostAndPort;
    private URL url;
    private String usernaem;
    private String password;
    private String token;
    private Map<String, String> headers;
    private Long connectTimeout;
    private Long readTimeout;
    private Long writeTimeout;

    private ConsulConfig() {

    }

    public static Map<String, ConsulConfig> create(final Properties config) {
        final String root = StringUtils.trim(config.getProperty(CONSUL_ROOT));
        if (StringUtils.isNotBlank(root)) {
            final String[] cfgIds = root.split(SPLIT);
            if (ArrayUtils.isNotEmpty(cfgIds)) {
                final Map<String, ConsulConfig> cfgs = Maps.newHashMap();
                for (final String cfgId : cfgIds) {
                    final ConsulConfig cfg = create(cfgId, config);
                    if (cfgs.containsKey(cfg.id)) {
                        throw new ConsulException(MessageFormat.format("重复的Consul数据源定义: {0}", cfg.id));
                    }

                    cfgs.put(cfg.id, cfg);
                }

                return cfgs;
            }
        }

        return Collections.emptyMap();
    }

    private static ConsulConfig create(final String cfgId, final Properties config) {
        final ConsulConfig cfg = new ConsulConfig();
        cfg.id = get(config, CONSUL_PREFIX + cfgId + ID);
        if (cfg.id == null) {
            throw new ConsulException(MessageFormat.format("consul.{0}.id为必填项", cfgId));
        }

        cfg.hostAndPort = hostAndPort(config, CONSUL_PREFIX + cfgId + HOST_AND_PORT);
        cfg.url = url(config, CONSUL_PREFIX + cfgId + URL);
        cfg.usernaem = get(config, CONSUL_PREFIX + cfgId + USERNAME);
        cfg.password = get(config, CONSUL_PREFIX + cfgId + PASSWORD);
        cfg.token = get(config, CONSUL_PREFIX + cfgId + TOKEN);
        cfg.headers = headers(config, CONSUL_PREFIX + cfgId + HEADERS);
        cfg.connectTimeout = longer(config, CONSUL_PREFIX + cfgId + TIMEOUT_CONNECT);
        cfg.readTimeout = longer(config, CONSUL_PREFIX + cfgId + TIMEOUT_READ);
        cfg.writeTimeout = longer(config, CONSUL_PREFIX + cfgId + TIMEOUT_WRITE);
        return cfg;
    }

    private static String get(final Properties config, final String key) {
        final String value = StringUtils.trim(config.getProperty(key));
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return value;
    }

    private static HostAndPort hostAndPort(final Properties config, final String key) {
        final String hostAndPort = get(config, key);
        if (hostAndPort != null) {
            return HostAndPort.fromString(hostAndPort);
        }

        return null;
    }

    private static URL url(final Properties config, final String key) {
        final String url = get(config, key);
        if (url != null) {
            try {
                return new URL(url);
            } catch (final MalformedURLException e) {
                throw new ConsulException(e);
            }
        }

        return null;
    }

    private static Map<String, String> headers(final Properties config, final String key) {
        final String headers = get(config, key);
        if (StringUtils.isNotBlank(headers)) {
            return JSON.parseObject(headers, HEADERS_TYPE);
        }

        return null;
    }

    private static Long longer(final Properties config, final String key) {
        final String longer = get(config, key);
        if (StringUtils.isNotBlank(longer)) {
            return Long.valueOf(longer);
        }

        return null;
    }

    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public URL getUrl() {
        return url;
    }

    public String getUsernaem() {
        return usernaem;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public Map<String, String> getHeaders() {
        if (headers == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(headers);
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public Long getReadTimeout() {
        return readTimeout;
    }

    public Long getWriteTimeout() {
        return writeTimeout;
    }

}
