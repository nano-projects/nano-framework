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
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.concurrent.scheduler.cluster.BaseClusterScheduler;
import org.nanoframework.concurrent.scheduler.cluster.consts.ConsulSources;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.orbitz.consul.KeyValueClient;

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
    /** Scheduler ability */
    public static final String SCHEDULER_ABILITY = "schedulerAbility";
    /** workers fastjson type. */
    public static final TypeReference<Set<String>> SET_STRING_TYPE = new TypeReference<Set<String>>() {
    };
    private static final long serialVersionUID = -4484992348486825071L;

    @Inject
    @Named(ConsulSources.KV_SCHEDULER_CLUSTER)
    private KeyValueClient kvClient;

    private String id;
    private String host;
    private NodeStatus status;
    private Long uptime;
    private Long livetime;
    private final Map<String, Worker> workers = Maps.newHashMap();
    private final Set<Class<? extends BaseClusterScheduler>> clses = Sets.newHashSet();
    private final Set<String> notFoundClses = Sets.newHashSet();

    @Inject
    private Configure config;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        if (StringUtils.equals(this.id, id)) {
            return;
        }

        this.id = id;
        kvClient.putValue(config.getClusterId() + "/Node/" + id + '/');
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        if (StringUtils.equals(this.host, host)) {
            return;
        }

        this.host = host;
        kvClient.putValue(config.getClusterId() + "/Node/" + id + "/host", host);
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(final NodeStatus status) {
        if (this.status == status) {
            return;
        }

        syncStatus(status);
    }
    
    public void syncStatus(final NodeStatus status) {
        this.status = status;
        kvClient.putValue(config.getClusterId() + "/Node/" + id + "/status", String.valueOf(status.code()));
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(final Long uptime) {
        if (StringUtils.equals(String.valueOf(this.uptime), String.valueOf(uptime))) {
            return;
        }

        this.uptime = uptime;
        kvClient.putValue(config.getClusterId() + "/Node/" + id + "/uptime", String.valueOf(uptime));
    }

    public Long getLivetime() {
        return livetime;
    }

    public void setLivetime(final Long livetime) {
        if (StringUtils.equals(String.valueOf(this.livetime), String.valueOf(livetime))) {
            return;
        }

        this.livetime = livetime;
        kvClient.putValue(config.getClusterId() + "/Node/" + id + "/livetime", String.valueOf(livetime));
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

    public void setSchedulerAbility(final Set<Class<? extends BaseClusterScheduler>> clses) {
        if (!CollectionUtils.isEmpty(clses)) {
            this.clses.addAll(clses);
            kvClient.putValue(config.getClusterId() + "/Node/" + id + "/schedulerAbility", JSON.toJSONString(clses));
        }
    }

    @SuppressWarnings("unchecked")
    public void addSchedulerAbility(final String clsName) {
        try {
            if (notFoundClses.contains(clsName)) {
                return;
            }

            final Class<? extends BaseClusterScheduler> cls = (Class<? extends BaseClusterScheduler>) Class.forName(clsName);
            clses.add(cls);
        } catch (final ClassNotFoundException e) {
            notFoundClses.add(clsName);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean hasSchedulerAbility(final String clsName) {
        try {
            if (notFoundClses.contains(clsName)) {
                return false;
            }

            final Class<? extends BaseClusterScheduler> cls = (Class<? extends BaseClusterScheduler>) Class.forName(clsName);
            return clses.contains(cls);
        } catch (final ClassNotFoundException e) {
            notFoundClses.add(clsName);
            return false;
        }
    }

    public Set<String> getSchedulerAbility() {
        final Set<String> clsNames = Sets.newHashSet();
        this.clses.forEach(cls -> clsNames.add(cls.getName()));
        return Collections.unmodifiableSet(clsNames);
    }
}
