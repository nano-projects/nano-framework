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
package org.nanoframework.concurrent.scheduler.cluster.config;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.concurrent.scheduler.cluster.AbstractConsulTests;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache.Listener;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class ConfigureListenerTest extends AbstractConsulTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureListenerTest.class);
    
    @Inject
    @Named("consul.kv:test")
    private KeyValueClient keyValueClient;

    @Test
    public void subscribeSchedulerTest() throws Exception {
        injects();
        final KVCache cache = KVCache.newCache(keyValueClient, "org.nanoframework.scheduler.TestScheduler");
        final Listener<String, Value> listener = new Listener<String, Value>() {
            final ConcurrentMap<String, Value> lastResponse = Maps.newConcurrentMap();
            final ConcurrentMap<String, Value> news = Maps.newConcurrentMap();
            final ConcurrentMap<String, Value> modified = Maps.newConcurrentMap();
            final ConcurrentMap<String, Value> removed = Maps.newConcurrentMap();

            @Override
            public void notify(final Map<String, Value> newValues) {
                news.clear();
                modified.clear();
                removed.clear();
                if (!CollectionUtils.isEmpty(lastResponse)) {
                    newValues.forEach((key, value) -> {
                        if (lastResponse.containsKey(key)) {
                            final Value oldValue = lastResponse.get(key);
                            if (!StringUtils.equals(oldValue.getValueAsString().or(""), value.getValueAsString().or(""))) {
                                modified.put(key, value);
                            }
                        } else {
                            news.put(key, value);
                        }
                    });

                    lastResponse.forEach((key, value) -> {
                        if (!newValues.containsKey(key)) {
                            removed.put(key, value);
                        }
                    });

                    lastResponse.clear();
                    lastResponse.putAll(newValues);
                } else {
                    news.putAll(newValues);
                    lastResponse.clear();
                    lastResponse.putAll(newValues);
                }

                LOGGER.debug("news: ");
                news.forEach((key, value) -> {
                    LOGGER.debug("{}: {}", key, value.getValueAsString().or(""));
                });

                LOGGER.debug("modified: ");
                modified.forEach((key, value) -> {
                    LOGGER.debug("{}: {}", key, value.getValueAsString().or(""));
                });

                LOGGER.debug("removed: ");
                removed.forEach((key, value) -> {
                    LOGGER.debug("{}: {}", key, value.getValueAsString().or(""));
                });
            }

        };

        cache.addListener(listener);

        cache.start();

        Thread.sleep(1000);
        cache.stop();
    }
}
