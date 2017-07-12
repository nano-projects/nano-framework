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
package org.nanoframework.concurrent.scheduler.cluster.storage;

import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.NODE;
import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.SEPARATOR_CHAR;
import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.WORKER;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.concurrent.scheduler.BaseScheduler;
import org.nanoframework.concurrent.scheduler.SchedulerConfig;
import org.nanoframework.concurrent.scheduler.SchedulerFactory;
import org.nanoframework.concurrent.scheduler.cluster.config.Configure;
import org.nanoframework.concurrent.scheduler.cluster.config.Election;
import org.nanoframework.concurrent.scheduler.cluster.config.Node;
import org.nanoframework.concurrent.scheduler.cluster.config.NodeStatus;
import org.nanoframework.concurrent.scheduler.cluster.config.Worker;
import org.nanoframework.concurrent.scheduler.cluster.consts.ConsulSources;
import org.nanoframework.concurrent.scheduler.cluster.lock.ElectionLocker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.orbitz.consul.KeyValueClient;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
@Singleton
public class NodeStatusSyncScheduler extends BaseScheduler {
    private static final long INTERVAL = 15_000;
    private static final long SYNC_TIMEOUT = 180_000;
    private static final long START_WAIT_TIME = 5_000;

    private final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    @Inject
    private Configure configure;

    @Inject
    private Election election;

    @Inject
    private ElectionLocker locker;

    @Inject
    @Named(ConsulSources.KV_SCHEDULER_CLUSTER)
    private KeyValueClient kvClient;

    private Node node;
    private String clusterId;
    private String nodeId;

    public NodeStatusSyncScheduler() {
        final SchedulerConfig conf = new SchedulerConfig();
        final String group = NodeStatusSyncScheduler.class.getSimpleName();
        conf.setGroup(group);
        conf.setId(group + '-' + getIndex(group));
        conf.setName(SchedulerFactory.DEFAULT_SCHEDULER_NAME_PREFIX + conf.getId());
        conf.setBeforeAfterOnly(Boolean.TRUE);
        conf.setInterval(INTERVAL);
        conf.setService(service);
        conf.setDaemon(Boolean.TRUE);
        setConfig(conf);
    }

    public void start() {
        service.execute(this);
    }

    public void close() {
        setClose(true);
    }

    @Override
    public void before() {
        node = configure.getCurrentNode();
        clusterId = configure.getClusterId();
        nodeId = node.getId();
        thisWait(START_WAIT_TIME);
    }

    @Override
    public void execute() {
        node.setLivetime(System.currentTimeMillis());
        final NodeStatus status = node.getStatus();
        if (status != NodeStatus.LEADER) {
            final String leader = configure.getLeader();
            if (StringUtils.isNotBlank(leader)) {
                final Node leaderNode = configure.getNode(leader);
                final long livetime = leaderNode.getLivetime();
                if (System.currentTimeMillis() - livetime > SYNC_TIMEOUT) {
                    election();
                }
            } else {
                election();
            }
        }
    }

    private void election() {
        if (locker.lock()) {
            election.push();
        }
    }

    @Override
    public void after() {

    }

    @Override
    public void destroy() {
        final Map<String, Worker> workers = node.getWorkers();
        if (!CollectionUtils.isEmpty(workers)) {
            workers.forEach((workerId, worker) -> {
                final String cls = worker.getCls();
                kvClient.deleteKeys(clusterId + SEPARATOR_CHAR + WORKER + SEPARATOR_CHAR + cls + SEPARATOR_CHAR + workerId);
            });
        }

        kvClient.deleteKeys(clusterId + SEPARATOR_CHAR + NODE + SEPARATOR_CHAR + nodeId);
    }

}
