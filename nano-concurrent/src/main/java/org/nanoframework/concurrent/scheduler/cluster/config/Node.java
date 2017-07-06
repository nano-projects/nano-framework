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

import org.apache.commons.codec.binary.StringUtils;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;

/**
 * 任务对应的节点配置.
 * @author yanghe
 * @since 1.4.9
 */
public class Node extends BaseEntity {
    /** workers keys. */
    public static final String WORKER_IDS = "workerIds";
    /** status field. */
    public static final String STATUS = "status";
    /** workers fastjson type. */
    public static final TypeReference<Set<String>> WORKER_IDS_TYPE = new TypeReference<Set<String>>() {
    };
    private static final long serialVersionUID = -4484992348486825071L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

    private String id;
    private String host;
    private NodeStatus status;
    private Long upTime;
    private Long liveTime;
    private Map<String, Worker> workers = Maps.newHashMap();

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        if (StringUtils.equals(this.id, id)) {
            return;
        }

        this.id = id;
        LOGGER.debug("同步任务调度节点: {}", id);
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        if (StringUtils.equals(this.host, host)) {
            return;
        }

        this.host = host;
        LOGGER.debug("同步任务调度节点: {}, host: {}", id, host);
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(final NodeStatus status) {
        if (this.status == status) {
            return;
        }

        this.status = status;
        LOGGER.debug("同步任务调度节点: {}, status: {}", id, status);
    }

    public Long getUpTime() {
        return upTime;
    }

    public void setUpTime(final Long upTime) {
        if (StringUtils.equals(String.valueOf(this.upTime), String.valueOf(upTime))) {
            return;
        }

        this.upTime = upTime;
        LOGGER.debug("同步任务调度节点: {}, upTime: {}", id, upTime);
    }

    public Long getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(final Long liveTime) {
        if (StringUtils.equals(String.valueOf(this.liveTime), String.valueOf(liveTime))) {
            return;
        }

        this.liveTime = liveTime;
        LOGGER.debug("同步任务调度节点: {}, liveTime: {}", id, liveTime);
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
}
