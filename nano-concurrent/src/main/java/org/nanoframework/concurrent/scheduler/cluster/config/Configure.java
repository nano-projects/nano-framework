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
import org.nanoframework.concurrent.scheduler.cluster.BaseClusterScheduler;

import com.google.common.collect.Maps;

/**
 * 任务配置.
 * @author yanghe
 * @since 1.4.9
 */
public class Configure extends BaseEntity {
    private static final long serialVersionUID = -6582459356567595335L;

    private Boolean leader = Boolean.FALSE;
    private Class<? extends BaseClusterScheduler> cls;
    private String clusterId;
    private Node currentNode;
    private final Map<String, Node> nodes = Maps.newHashMap();
    private final Map<String, Worker> workers = Maps.newHashMap();

    public Boolean getLeader() {
        return leader;
    }

    public void setLeader(final Boolean leader) {
        this.leader = leader;
    }

    public Class<? extends BaseClusterScheduler> getCls() {
        return cls;
    }

    public void setCls(final Class<? extends BaseClusterScheduler> cls) {
        this.cls = cls;
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

    public Map<String, Node> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public Map<String, Worker> getWorkers() {
        return Collections.unmodifiableMap(workers);
    }
}
