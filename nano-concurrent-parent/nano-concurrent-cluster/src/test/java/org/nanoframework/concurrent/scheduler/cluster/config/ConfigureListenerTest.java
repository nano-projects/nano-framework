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

import java.security.SecureRandom;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.concurrent.scheduler.cluster.AbstractConsulTests;
import org.nanoframework.concurrent.scheduler.cluster.BaseClusterScheduler;
import org.nanoframework.concurrent.scheduler.cluster.Test2Scheduler;
import org.nanoframework.concurrent.scheduler.cluster.TestScheduler;
import org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class ConfigureListenerTest extends AbstractConsulTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureListenerTest.class);

    @Inject
    @Named("consul.kv:scheduler-cluster")
    private KeyValueClient keyValueClient;

    @Inject
    private SecureRandom random;

    @Test
    public void configureTest() throws Exception {
        injects();
        final String clusterId = "test";
        final Configure configure = injector.getInstance(Configure.class);
        configure.setClusterId(clusterId);

        final Node curNode = createNode0(clusterId, configure);
        configure.setCurrentNode(curNode);
        configure.addNode(curNode.getId(), curNode);

        final KVCache cache = KVCache.newCache(keyValueClient, clusterId);
        final SchedulerListener listener = injector.getInstance(SchedulerListener.class);
        cache.addListener(listener);
        cache.start();

        createNode1(clusterId, configure);
        Thread.sleep(1000);

        final String[] nodeIds = configure.getNodeIds().toArray(new String[0]);
        final String nodeId = nodeIds[random.nextInt(nodeIds.length)];
        setLeader(clusterId, nodeId);
        Thread.sleep(1000);

        if (StringUtils.equals(curNode.getId(), nodeId)) {
            Assert.assertTrue(curNode.getStatus() == NodeStatus.LEADER);
        } else {
            Assert.assertTrue(curNode.getStatus() == NodeStatus.FOLLOWING);
        }

        createWorker0(clusterId, configure);

        cache.removeListener(listener);
        cache.stop();

        LOGGER.debug("{}", configure);
        keyValueClient.deleteKeys(clusterId);
    }

    private Node createNode0(final String clusterId, final Configure configure) {
        final Node node = injector.getInstance(Node.class);
        node.setId("node0");
        node.setHost("localhost");
        node.setStatus(NodeStatus.LOOKING);
        node.setUptime(System.currentTimeMillis());
        node.setLivetime(node.getUptime());

        final Set<Class<? extends BaseClusterScheduler>> clses = Sets.newHashSet();
        clses.add(TestScheduler.class);
        node.setSchedulerAbility(clses);
        return node;
    }

    private Node createNode1(final String clusterId, final Configure configure) {
        final Node node = injector.getInstance(Node.class);
        node.setId("node1");
        node.setHost("localhost");
        node.setStatus(NodeStatus.LOOKING);
        node.setUptime(System.currentTimeMillis());
        node.setLivetime(node.getUptime());

        final Set<Class<? extends BaseClusterScheduler>> clses = Sets.newHashSet();
        clses.add(TestScheduler.class);
        clses.add(Test2Scheduler.class);
        node.setSchedulerAbility(clses);
        return node;
    }

    private void setLeader(final String clusterId, final String nodeId) {
        final String leaderPath = clusterId + "/Leader";
        keyValueClient.putValue(leaderPath, nodeId);
    }

    private void createWorker0(final String clusterId, final Configure configure) {

    }
}
