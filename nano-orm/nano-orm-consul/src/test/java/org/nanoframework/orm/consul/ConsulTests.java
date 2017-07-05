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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.util.CollectionUtils;

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
import com.orbitz.consul.model.health.ServiceHealth;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class ConsulTests extends AbstractConsulTests {

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
        final String key = "key";
        final String value = "value";
        keyValueClient.putValue(key, value);
        final String v = keyValueClient.getValueAsString(key).get();
        Assert.assertEquals(v, value);

        keyValueClient.deleteKey(key);
        final String deleted = "deleted";
        final String delValue = keyValueClient.getValueAsString(key).or(deleted);
        Assert.assertEquals(delValue, deleted);
    }
}
