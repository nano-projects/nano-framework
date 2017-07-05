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

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.CoordinateClient;
import com.orbitz.consul.EventClient;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.OperatorClient;
import com.orbitz.consul.PreparedQueryClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.StatusClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.cache.ConsulCache.Listener;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.QueryOptions;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class ConsulTests extends AbstractConsulTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulTests.class);

    @Inject
    @Named("consul:test")
    private Consul consul;

    @Inject
    @Named("consul.agent:test")
    private AgentClient agentClient;

    @Inject
    @Named("consul.health:test")
    private HealthClient healthClient;

    @Inject
    @Named("consul.kv:test")
    private KeyValueClient keyValueClient;

    @Inject
    @Named("consul.catalog:test")
    private CatalogClient catalogClient;

    @Inject
    @Named("consul.status:test")
    private StatusClient statusClient;

    @Inject
    @Named("consul.session:test")
    private SessionClient sessionClient;

    @Inject
    @Named("consul.event:test")
    private EventClient eventClient;

    @Inject
    @Named("consul.prepared-query:test")
    private PreparedQueryClient pqClient;

    @Inject
    @Named("consul.coordinate:test")
    private CoordinateClient coordinateClient;

    @Inject
    @Named("consul.operator:test")
    private OperatorClient operatorClient;

    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String NEW_VALUE = "newValue";
    private static final String DELETED = "deleted";

    @Test
    public void connectTest() {
        injects();
        Assert.assertNotNull(consul);
        Assert.assertNotNull(agentClient);
        Assert.assertNotNull(healthClient);
        Assert.assertNotNull(keyValueClient);
        Assert.assertNotNull(catalogClient);
        Assert.assertNotNull(statusClient);
        Assert.assertNotNull(sessionClient);
        Assert.assertNotNull(eventClient);
        Assert.assertNotNull(pqClient);
        Assert.assertNotNull(coordinateClient);
        Assert.assertNotNull(operatorClient);
    }

    @Test
    public void registerAndCheckServiceTest() throws NotRegisteredException {
        injects();
        final String serviceName = "MyService";
        final String serviceId = "1";
        agentClient.register(8080, 3L, serviceName, serviceId);
        agentClient.pass(serviceId);
        agentClient.deregister(serviceId);
    }

    @Test
    public void findAvailableServicesTest() {
        injects();
        final List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances("DataService").getResponse();
        Assert.assertTrue(CollectionUtils.isEmpty(nodes));
    }

    @Test
    public void keyValueReadAndWriteTest() {
        injects();
        keyValueClient.putValue(KEY, VALUE);
        final String v = keyValueClient.getValueAsString(KEY).get();
        Assert.assertEquals(v, VALUE);

        keyValueClient.deleteKey(KEY);
        final String delValue = keyValueClient.getValueAsString(KEY).or(DELETED);
        Assert.assertEquals(delValue, DELETED);
    }

    @Test
    public void blockingCallTest() throws InterruptedException {
        injects();
        keyValueClient.putValue(KEY, VALUE);
        final StringBuilder builder = new StringBuilder();
        final ConsulResponseCallback<Optional<Value>> callback = new ConsulResponseCallback<Optional<Value>>() {
            final AtomicReference<BigInteger> index = new AtomicReference<BigInteger>(null);

            @Override
            public void onComplete(final ConsulResponse<Optional<Value>> consulResponse) {
                if (consulResponse.getResponse().isPresent()) {
                    final Value v = consulResponse.getResponse().get();
                    builder.setLength(0);
                    builder.append(v.getValueAsString().get());
                }

                index.set(consulResponse.getIndex());
                watch();
            }

            void watch() {
                keyValueClient.getValue(KEY, QueryOptions.blockMinutes(5, index.get()).build(), this);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOGGER.error("Error encountered", throwable);
                watch();
            }
        };

        keyValueClient.getValue(KEY, QueryOptions.blockMinutes(5, new BigInteger("0")).build(), callback);

        Thread.sleep(1000);

        keyValueClient.putValue(KEY, NEW_VALUE);

        Thread.sleep(500);

        Assert.assertEquals(builder.toString(), NEW_VALUE);

        keyValueClient.deleteKey(KEY);
        final String delValue = keyValueClient.getValueAsString(KEY).or(DELETED);
        Assert.assertEquals(delValue, DELETED);
    }

    @Test
    public void subscribeKVTest() throws Exception {
        injects();

        final StringBuilder builder = new StringBuilder();
        final KVCache cache = KVCache.newCache(keyValueClient, KEY);
        final Listener<String, Value> listener = values -> {
            values.values().forEach(v -> {
                builder.setLength(0);
                builder.append(v.getValueAsString().get());
            });
        };

        cache.addListener(listener);

        cache.start();

        Thread.sleep(1000);

        keyValueClient.putValue(KEY, VALUE);

        Thread.sleep(500);

        Assert.assertEquals(VALUE, builder.toString());

        keyValueClient.deleteKey(KEY);
        final String delValue = keyValueClient.getValueAsString(KEY).or(DELETED);
        Assert.assertEquals(delValue, DELETED);

        cache.removeListener(listener);
        Assert.assertTrue(CollectionUtils.isEmpty(cache.getListeners()));
        cache.stop();
    }
}
