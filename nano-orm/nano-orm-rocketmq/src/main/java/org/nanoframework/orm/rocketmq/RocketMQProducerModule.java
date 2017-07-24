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
package org.nanoframework.orm.rocketmq;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.orm.rocketmq.config.RocketMQConfig;
import org.nanoframework.orm.rocketmq.exception.RocketMQProducerException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.name.Names;

/**
 *
 * @author yanghe
 * @since
 */
public class RocketMQProducerModule implements Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQProducerModule.class);
    private static final String DEFAULT_ROCKETMQ_PARAMETER_NAME = "rocketmq";
    private static final String DEFAULT_ROCKETMQ_PATH = "/rocketmq.properties";
    private static final String ROCKETMQ_PREFIX = "rkt:";
    private final List<Properties> properties = Lists.newArrayList();
    private final Map<String, RocketMQConfig> cfgs = Maps.newHashMap();

    @Override
    public void configure(final Binder binder) {
        loading();
        if (!CollectionUtils.isEmpty(cfgs)) {
            shutdown();
            bind(binder);
        }
    }

    private void loading() {
        if (!CollectionUtils.isEmpty(properties)) {
            properties.forEach(config -> {
                final Map<String, RocketMQConfig> cfgs = RocketMQConfig.create(config);
                if (!CollectionUtils.isEmpty(cfgs)) {
                    cfgs.forEach((id, cfg) -> {
                        if (this.cfgs.containsKey(id)) {
                            throw new RocketMQProducerException(MessageFormat.format("重复的RocketMQ数据源定义: {0}", id));
                        }

                        this.cfgs.put(id, cfg);
                    });
                }
            });
        }
    }

    private void shutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cfgs.values().forEach(cfg -> {
                final DefaultMQProducer producer = cfg.getProducer();
                if (producer != null) {
                    try {
                        producer.shutdown();
                    } catch (final Throwable e) {
                        LOGGER.error("Shutdown producer error: {}", e.getMessage());
                    }
                }
            });
        }));
    }

    private void bind(final Binder binder) {
        cfgs.forEach((id, cfg) -> {
            final DefaultMQProducer producer = cfg.getProducer();
            if (producer != null) {
                try {
                    producer.start();
                    binder.bind(MQProducer.class).annotatedWith(Names.named(ROCKETMQ_PREFIX + id)).toInstance(producer);
                } catch (final MQClientException e) {
                    producer.shutdown();
                    throw new org.nanoframework.orm.rocketmq.exception.MQClientException(e.getErrorMessage(), e);
                }
            }
        });
    }

    public List<Module> load() throws Throwable {
        return Lists.newArrayList(this);
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {
        final String rocketmq = config.getInitParameter(DEFAULT_ROCKETMQ_PARAMETER_NAME);
        if (StringUtils.isNotBlank(rocketmq)) {
            final String[] paths = rocketmq.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        final String contextRocketMQ = System.getProperty(ApplicationContext.CONTEXT_ROCKETMQ);
        if (StringUtils.isNotBlank(contextRocketMQ)) {
            final String[] paths = contextRocketMQ.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        if (CollectionUtils.isEmpty(properties)) {
            try {
                properties.add(PropertiesLoader.load(DEFAULT_ROCKETMQ_PATH));
            } catch (final Throwable e) {
                // ignore
            }
        }
    }

}
