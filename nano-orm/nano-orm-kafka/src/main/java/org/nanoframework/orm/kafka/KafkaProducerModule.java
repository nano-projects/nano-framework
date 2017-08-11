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
package org.nanoframework.orm.kafka;

import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.plugins.Module;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class KafkaProducerModule implements Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerModule.class);
    private static final String DEFAULT_KAFKA_PATH = "/kafka-producer.properties";

    @Override
    public void configure(final Binder binder) {
        final Properties properties;
        try {
            properties = PropertiesLoader.load(DEFAULT_KAFKA_PATH);
        } catch (final Throwable e) {
            return;
        }

        if (properties != null) {
            try {
                binder.bind(new TypeLiteral<Producer<Object, Object>>() {}).toInstance(new KafkaProducer<>(properties));
            } catch (final Throwable e) {
                LOGGER.error("创建KafkaProducer异常: {}", e.getMessage());
            }
        }
    }

    @Override
    public List<Module> load() throws Throwable {
        return Lists.newArrayList(this);
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {

    }

}
