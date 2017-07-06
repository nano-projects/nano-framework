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

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.concurrent.scheduler.cluster.BaseClusterScheduler;

/**
 * 工作线程配置.
 * @author yanghe
 * @since 1.4.9
 */
public class Worker extends BaseEntity {
    /** node field. */
    public static final String NODE_ID = "nodeId";
    /** status field. */
    public static final String STATUS = "status";
    private static final long serialVersionUID = 6329815738228489691L;

    private String id;
    private Long upTime;
    private Long runTime;
    private WorkerStatus status;
    private Node node;
    private Map<String, Object> cfg;
    private BaseClusterScheduler scheduler;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Long getUpTime() {
        return upTime;
    }

    public void setUpTime(final Long upTime) {
        this.upTime = upTime;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(final Long runTime) {
        this.runTime = runTime;
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public void setStatus(final WorkerStatus status) {
        this.status = status;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(final Node node) {
        this.node = node;
    }

    public Map<String, Object> getCfg() {
        return Collections.unmodifiableMap(cfg);
    }

    public void setCfg(final Map<String, Object> cfg) {
        this.cfg = cfg;
    }

    public BaseClusterScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(final BaseClusterScheduler scheduler) {
        this.scheduler = scheduler;
    }

}
