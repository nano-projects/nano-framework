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
package org.nanoframework.orm.rocketmq.config;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.rocketmq.exception.RocketMQProducerException;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class RocketMQConfig extends BaseEntity {
    /** RocketMQ配置统一前缀. */
    public static final String ROCKETMQ_PREFIX = "rocketmq.";
    /** RocketMQ配置根属性. */
    public static final String ROCKETMQ_ROOT = ROCKETMQ_PREFIX + "root";
    public static final String ID = ".id";
    public static final String BASE = ".base";
    public static final String SPLIT = ",";

    private static final long serialVersionUID = 3350196447468686384L;

    private String id;
    private DefaultMQProducer producer;

    private RocketMQConfig() {

    }

    public static Map<String, RocketMQConfig> create(final Properties config) {
        final String root = StringUtils.trim(config.getProperty(ROCKETMQ_ROOT));
        if (StringUtils.isNotBlank(root)) {
            final String[] cfgIds = root.split(SPLIT);
            if (ArrayUtils.isNotEmpty(cfgIds)) {
                final Map<String, RocketMQConfig> cfgs = Maps.newHashMap();
                for (final String cfgId : cfgIds) {
                    final RocketMQConfig cfg = create(cfgId, config);
                    if (cfgs.containsKey(cfg.id)) {
                        throw new RocketMQProducerException(MessageFormat.format("重复的RocketMQ数据源定义: {0}", cfg.id));
                    }

                    cfgs.put(cfg.id, cfg);
                }

                return cfgs;
            }
        }

        return Collections.emptyMap();
    }

    private static RocketMQConfig create(final String cfgId, final Properties config) {
        final RocketMQConfig cfg = new RocketMQConfig();
        cfg.id = get(config, ROCKETMQ_PREFIX + cfgId + ID);
        cfg.producer = producer(config, ROCKETMQ_PREFIX + cfgId + BASE);
        return cfg;
    }

    private static String get(final Properties config, final String key) {
        final String value = StringUtils.trim(config.getProperty(key));
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return value;
    }

    private static DefaultMQProducer producer(final Properties config, final String key) {
        return JSON.parseObject(config.getProperty(key), DefaultMQProducer.class);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the producer
     */
    public DefaultMQProducer getProducer() {
        return producer;
    }
}
