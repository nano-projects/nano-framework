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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.CollectionUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;

/**
 * 任务配置.
 * @author yanghe
 * @since 1.4.9
 */
@Singleton
public class Configure extends BaseEntity {
    private static final long serialVersionUID = -6582459356567595335L;

    private String clusterId;
    private Node currentNode;
    private String leader;
    private final Map<String, Node> nodes = Maps.newHashMap();
    private final Map<String, Worker> workers = Maps.newHashMap();
    private final Set<String> filterSchedulers = Sets.newHashSet();
    private final Set<String> schedulers = Sets.newHashSet();

    public Configure() {

    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(final String clusterId) {
        this.clusterId = clusterId;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(final Node currentNode) {
        this.currentNode = currentNode;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(final String leader) {
        this.leader = leader;
    }

    public void clearLeader() {
        this.leader = null;
        nodes.forEach((nodeId, node) -> {
            if (node.getStatus() == NodeStatus.LEADER) {
                node.setStatus(NodeStatus.UNKNOWN);
            }
        });
    }

    public Node getNode(final String nodeId) {
        return nodes.get(nodeId);
    }

    public Node addNode(final String nodeId, final Node node) {
        return nodes.putIfAbsent(nodeId, node);
    }

    public Set<String> getNodeIds() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    public boolean hasNode(final String nodeId) {
        return nodes.containsKey(nodeId);
    }

    public void removeNodes() {
        nodes.clear();
    }

    public void removeNode(final String nodeId) {
        nodes.remove(nodeId);
    }

    public Map<String, Node> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public Worker getWorker(final String workerId) {
        return workers.get(workerId);
    }

    public Worker addWorker(final String workerId, final Worker worker) {
        return workers.putIfAbsent(workerId, worker);
    }

    public Set<String> getWorkerIds() {
        return Collections.unmodifiableSet(workers.keySet());
    }

    public boolean hasWorker(final String workerId) {
        return workers.containsKey(workerId);
    }

    public void removeWorkers() {
        workers.clear();
    }

    public void removeWorker(final String workerId) {
        workers.remove(workerId);
    }

    public Map<String, Worker> getWorkers() {
        return Collections.unmodifiableMap(workers);
    }

    public boolean hasFilterScheduler(final String className) {
        return filterSchedulers.contains(className);
    }

    public void addFilterScheduler(final String className) {
        filterSchedulers.add(className);
    }

    public void addScheduler(final Set<String> schedulers) {
        if (!CollectionUtils.isEmpty(schedulers)) {
            this.schedulers.addAll(schedulers);
        }
    }

    public boolean hasScheduler(final String scheduler) {
        return schedulers.contains(scheduler);
    }

    public Set<String> getSchedulers() {
        return Collections.unmodifiableSet(schedulers);
    }
}
