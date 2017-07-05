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
package org.nanoframework.orm.consul;

import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.orm.consul.config.ConsulConfig;
import org.nanoframework.orm.consul.exception.ConsulException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;
import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.CoordinateClient;
import com.orbitz.consul.EventClient;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.OperatorClient;
import com.orbitz.consul.PreparedQueryClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.StatusClient;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class ConsulModule implements Module {
    public static final String DEFAULT_CONSUL_PARAMETER_NAME = "consul";
    private static final String DEFAULT_CONSUL_PATH = "/consul.properties";
    private static final String CONSUL_PREFIX = "consul:";
    private static final String CONSUL_AGENT_CLIENT_PREFIX = "consul.agent:";
    private static final String CONSUL_HEALTH_CLIENT_PREFIX = "consul.health:";
    private static final String CONSUL_KEY_VALUE_CLIENT_PREFIX = "consul.kv:";
    private static final String CONSUL_CATALOG_CLIENT_PREFIX = "consul.catalog:";
    private static final String CONSUL_STATUS_CLIENT_PREFIX = "consul.status:";
    private static final String CONSUL_SESSION_CLIENT_PREFIX = "consul.session:";
    private static final String CONSUL_EVENT_CLIENT_PREFIX = "consul.event:";
    private static final String CONSUL_PREPARED_QUERY_CLIENT_PREFIX = "consul.prepared-query:";
    private static final String CONSUL_COORDINATE_CLIENT_PREFIX = "consul.coordinate:";
    private static final String CONSUL_OPERATOR_CLIENT = "consul.operator:";

    private final List<Properties> properties = Lists.newArrayList();
    private final Map<String, ConsulConfig> cfgs = Maps.newHashMap();

    @Override
    public void configure(final Binder binder) {
        loading();

        cfgs.forEach((id, cfg) -> {
            final Consul consul = build(cfg);
            binder.bind(Consul.class).annotatedWith(Names.named(CONSUL_PREFIX + id)).toInstance(consul);
            binder.bind(AgentClient.class).annotatedWith(Names.named(CONSUL_AGENT_CLIENT_PREFIX + id)).toInstance(consul.agentClient());
            binder.bind(HealthClient.class).annotatedWith(Names.named(CONSUL_HEALTH_CLIENT_PREFIX + id)).toInstance(consul.healthClient());
            binder.bind(KeyValueClient.class).annotatedWith(Names.named(CONSUL_KEY_VALUE_CLIENT_PREFIX + id)).toInstance(consul.keyValueClient());
            binder.bind(CatalogClient.class).annotatedWith(Names.named(CONSUL_CATALOG_CLIENT_PREFIX + id)).toInstance(consul.catalogClient());
            binder.bind(StatusClient.class).annotatedWith(Names.named(CONSUL_STATUS_CLIENT_PREFIX + id)).toInstance(consul.statusClient());
            binder.bind(SessionClient.class).annotatedWith(Names.named(CONSUL_SESSION_CLIENT_PREFIX + id)).toInstance(consul.sessionClient());
            binder.bind(EventClient.class).annotatedWith(Names.named(CONSUL_EVENT_CLIENT_PREFIX + id)).toInstance(consul.eventClient());
            binder.bind(PreparedQueryClient.class).annotatedWith(Names.named(CONSUL_PREPARED_QUERY_CLIENT_PREFIX + id))
                    .toInstance(consul.preparedQueryClient());
            binder.bind(CoordinateClient.class).annotatedWith(Names.named(CONSUL_COORDINATE_CLIENT_PREFIX + id))
                    .toInstance(consul.coordinateClient());
            binder.bind(OperatorClient.class).annotatedWith(Names.named(CONSUL_OPERATOR_CLIENT + id)).toInstance(consul.operatorClient());
        });

    }

    private void loading() {
        if (!CollectionUtils.isEmpty(properties)) {
            properties.forEach(config -> {
                final Map<String, ConsulConfig> cfgs = ConsulConfig.create(config);
                if (!CollectionUtils.isEmpty(cfgs)) {
                    cfgs.forEach((id, cfg) -> {
                        if (this.cfgs.containsKey(id)) {
                            throw new ConsulException(MessageFormat.format("重复的Consul数据源定义: {0}", id));
                        }

                        this.cfgs.put(id, cfg);
                    });
                }
            });
        }
    }

    private Consul build(final ConsulConfig cfg) {
        final Consul.Builder builder = Consul.builder();

        final HostAndPort hostAndPort = cfg.getHostAndPort();
        if (hostAndPort != null) {
            builder.withHostAndPort(cfg.getHostAndPort());
        }

        final String username = cfg.getUsernaem();
        final String password = cfg.getPassword();
        if (username != null && password != null) {
            builder.withBasicAuth(cfg.getUsernaem(), cfg.getPassword());
        }

        final URL url = cfg.getUrl();
        if (url != null) {
            builder.withUrl(cfg.getUrl());
        }

        final String token = cfg.getToken();
        if (token != null) {
            builder.withAclToken(cfg.getToken());
        }

        final Map<String, String> headers = cfg.getHeaders();
        if (!CollectionUtils.isEmpty(headers)) {
            builder.withHeaders(cfg.getHeaders());
        }

        final Long connectTimeout = cfg.getConnectTimeout();
        if (connectTimeout != null) {
            builder.withConnectTimeoutMillis(cfg.getConnectTimeout());
        }

        final Long readTimeout = cfg.getReadTimeout();
        if (readTimeout != null) {
            builder.withReadTimeoutMillis(cfg.getReadTimeout());
        }

        final Long writeTimeout = cfg.getWriteTimeout();
        if (writeTimeout != null) {
            builder.withWriteTimeoutMillis(cfg.getWriteTimeout());
        }

        return builder.build();
    }

    @Override
    public List<Module> load() throws Throwable {
        return Lists.newArrayList(this);
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {
        final String consul = config.getInitParameter(DEFAULT_CONSUL_PARAMETER_NAME);
        if (StringUtils.isNotBlank(consul)) {
            final String[] paths = consul.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        final String contextConsul = System.getProperty(ApplicationContext.CONTEXT_CONSUL);
        if (StringUtils.isNotBlank(contextConsul)) {
            final String[] paths = contextConsul.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        if (CollectionUtils.isEmpty(properties)) {
            try {
                properties.add(PropertiesLoader.load(DEFAULT_CONSUL_PATH));
            } catch (final Throwable e) {
                // ignore
            }
        }
    }

}
