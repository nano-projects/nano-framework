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

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.concurrent.scheduler.cluster.AbstractConsulTests;
import org.nanoframework.concurrent.scheduler.cluster.TestScheduler;
import org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener;

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
    @Named("consul.kv:test")
    private KeyValueClient keyValueClient;

    @Inject
    private SecureRandom random;

    @Test
    public void configureTest() throws Exception {
        injects();
        final String clusterId = "test";
        final String schedulerClassName = TestScheduler.class.getName();

        final Configure configure = injector.getInstance(Configure.class);
        configure.setClusterId(clusterId);
        configure.setCls(TestScheduler.class);

        final Node curNode = createNode0(clusterId, schedulerClassName);
        configure.setCurrentNode(curNode);
        configure.addNode(curNode.getId(), curNode);

        final KVCache cache = KVCache.newCache(keyValueClient, schedulerClassName);
        cache.addListener(injector.getInstance(SchedulerListener.class).init(configure));
        cache.start();

        createNode1(clusterId, schedulerClassName);
        Thread.sleep(1000);

        final String[] nodeIds = configure.getNodeIds().toArray(new String[0]);
        final String nodeId = nodeIds[random.nextInt(nodeIds.length)];
        setLeader(clusterId, schedulerClassName, nodeId);
        Thread.sleep(1000);

        if (StringUtils.equals(configure.getCurrentNode().getId(), nodeId)) {
            Assert.assertTrue(configure.getLeader());
        } else {
            Assert.assertFalse(configure.getLeader());
        }

        cache.stop();

        LOGGER.debug("{}", configure);
        keyValueClient.deleteKeys(schedulerClassName);
    }

    private Node createNode0(final String clusterId, final String schedulerClassName) {
        final Node node = injector.getInstance(Node.class);
        node.setId("node0");
        node.setHost("localhost");
        node.setStatus(NodeStatus.UP);
        node.setUpTime(System.currentTimeMillis());
        node.setLiveTime(node.getUpTime());
        syncNode(node, clusterId, schedulerClassName);
        return node;
    }

    private Node createNode1(final String clusterId, final String schedulerClassName) {
        final Node node = injector.getInstance(Node.class);
        node.setId("node1");
        node.setHost("localhost");
        node.setStatus(NodeStatus.UP);
        node.setUpTime(System.currentTimeMillis());
        node.setLiveTime(node.getUpTime());
        syncNode(node, clusterId, schedulerClassName);
        return node;
    }

    private void syncNode(final Node node, final String clusterId, final String schedulerClassName) {
        final String nodePath = schedulerClassName + '/' + clusterId + "/Node/" + node.getId();
        keyValueClient.putValue(nodePath + "/");
        keyValueClient.putValue(nodePath + "/host", node.getHost());
        keyValueClient.putValue(nodePath + "/status", String.valueOf(node.getStatus().code()));
        keyValueClient.putValue(nodePath + "/upTime", String.valueOf(node.getUpTime()));
        keyValueClient.putValue(nodePath + "/liveTime", String.valueOf(node.getLiveTime()));
    }

    private void setLeader(final String clusterId, final String schedulerClassName, final String nodeId) {
        final String nodePath = schedulerClassName + '/' + clusterId + "/Leader/" + nodeId;
        keyValueClient.putValue(nodePath + "/");
    }
}
