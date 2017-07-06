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

import com.google.common.collect.Maps;

/**
 * 任务对应的节点配置.
 * @author yanghe
 * @since 1.4.9
 */
public class Node extends BaseEntity {
    /** status field. */
    public static final String STATUS = "status";
    /** workers keys. */
    public static final String WORKER_IDS = "workerIds";
    private static final long serialVersionUID = -4484992348486825071L;

    private final String id;
    private String host;
    private Status status;
    private Long upTime;
    private Long liveTime;
    private Map<String, Worker> workers = Maps.newHashMap();

    public Node(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Long getUpTime() {
        return upTime;
    }

    public void setUpTime(final Long upTime) {
        this.upTime = upTime;
    }

    public Long getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(final Long liveTime) {
        this.liveTime = liveTime;
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

}
