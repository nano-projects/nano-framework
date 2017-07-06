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
package org.nanoframework.concurrent.scheduler.cluster.storage.listener;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.concurrent.scheduler.cluster.BaseClusterScheduler;
import org.nanoframework.concurrent.scheduler.cluster.config.Configure;
import org.nanoframework.concurrent.scheduler.cluster.config.Node;
import org.nanoframework.concurrent.scheduler.cluster.config.Status;
import org.nanoframework.concurrent.scheduler.cluster.config.Worker;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.orbitz.consul.cache.ConsulCache.Listener;
import com.orbitz.consul.model.kv.Value;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class SchedulerListener implements Listener<String, Value> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerListener.class);
    private static final String SEPARATOR = "/";
    private static final int CLUSTER_ID_INDEX = 1;
    private static final int TYPE_NAME_INDEX = 2;
    private static final int TYPE_ID_INDEX = 3;
    private static final int ATTRIBUTE_INDEX = 4;
    private static final String NODE = "Node";
    private static final String WORKER = "Worker";

    private final ConcurrentMap<String, Value> lastResponse = Maps.newConcurrentMap();
    private final ConcurrentMap<String, Value> news = Maps.newConcurrentMap();
    private final ConcurrentMap<String, Value> modified = Maps.newConcurrentMap();
    private final ConcurrentMap<String, Value> removed = Maps.newConcurrentMap();

    private final Class<? extends BaseClusterScheduler> cls;
    private final Configure config;

    public SchedulerListener(Class<? extends BaseClusterScheduler> cls) {
        this.cls = cls;
        this.config = new Configure(cls);
    }

    @Override
    public void notify(final Map<String, Value> values) {
        try {
            setup(values);
            update();
        } finally {
            news.clear();
            modified.clear();
            removed.clear();
        }
    }

    private void setup(final Map<String, Value> values) {
        if (!CollectionUtils.isEmpty(lastResponse)) {
            values.forEach((key, value) -> {
                if (lastResponse.containsKey(key)) {
                    final Value oldValue = lastResponse.get(key);
                    if (!StringUtils.equals(oldValue.getValueAsString().or(EMPTY), value.getValueAsString().or(EMPTY))) {
                        modified.put(key, value);
                    }
                } else {
                    news.put(key, value);
                }
            });

            lastResponse.forEach((key, value) -> {
                if (!values.containsKey(key)) {
                    removed.put(key, value);
                }
            });

            lastResponse.clear();
            lastResponse.putAll(values);
        } else {
            news.putAll(values);
            lastResponse.clear();
            lastResponse.putAll(values);
        }
    }

    private void update() {
        news.forEach((key, value) -> updateWithNewOrModified(key, value));
        modified.forEach((key, value) -> updateWithNewOrModified(key, value));
        removed.forEach((key, value) -> updateWithRemoved(key, value));
    }

    protected void updateWithNewOrModified(final String key, final Value value) {
        final String[] tokens = key.split(SEPARATOR);
        final int tokenLength = tokens.length;
        final int offset;
        if (StringUtils.isBlank(tokens[tokenLength - 1])) {
            offset = 1;
        } else {
            offset = 2;
        }

        if (ArrayUtils.isNotEmpty(tokens)) {
            switch (tokenLength - offset) {
                case CLUSTER_ID_INDEX:
                    config.setClusterId(tokens[CLUSTER_ID_INDEX]);
                    break;
                case TYPE_ID_INDEX:
                    setType(tokens);
                    break;
                case ATTRIBUTE_INDEX:
                    setType(tokens);
                    setAttribute(tokens, value);
                    break;
                default:
                    LOGGER.warn("Unknown configure.");
                    break;
            }
        }
    }

    private void setType(final String[] tokens) {
        final String typeName = tokens[TYPE_NAME_INDEX];
        final String typeId = tokens[TYPE_ID_INDEX];
        switch (typeName) {
            case NODE:
                if (!config.hasNode(typeId)) {
                    config.addNode(typeId, new Node(typeId));
                }
                break;
            case WORKER:
                if (!config.hasWorker(typeId)) {
                    config.addWorker(typeId, new Worker(typeId));
                }
                break;
            default:
                LOGGER.warn("Unknown type name configure.");
                break;
        }
    }

    private void setAttribute(final String[] tokens, final Value value) {
        final String typeName = tokens[TYPE_NAME_INDEX];
        final String typeId = tokens[TYPE_ID_INDEX];
        final String attributeName = tokens[ATTRIBUTE_INDEX];
        final Optional<String> val = value.getValueAsString();
        switch (typeName) {
            case NODE:
                setNodeAttribute(typeId, attributeName, val);
                break;
            case WORKER:
                setWorkerAttribute(typeId, attributeName, val);
                break;
            default:
                LOGGER.warn("Unknown type name configure.");
                break;
        }
    }
    
    private void setNodeAttribute(final String nodeId, final String attributeName, final Optional<String> value) {
        final Node node = config.getNode(nodeId);
        switch (attributeName) {
            case Node.STATUS:
                node.setStatus(Status.of(Integer.parseInt(value.or(String.valueOf(Status.UNKNOWN.code())))));
                break;
            case Node.WORKER_IDS:
                final String[] workerIds = JSON.parseObject(value.or("[]"), String[].class);
                if (ArrayUtils.isEmpty(workerIds)) {
                    node.removeWorkers();
                } else {
                    for (final String workerId : workerIds) {
                        node.removeWorker(workerId);
                    }
                }
            default:
                node.setAttributeValue(attributeName, value.orNull());
                break;
        }
    }
    
    private void setWorkerAttribute(final String workerId, final String attributeName, final Optional<String> value) {
        final Worker worker = config.getWorker(workerId);
        final String v = value.orNull();
        switch (attributeName) {
            case Worker.NODE_ID:
                if (StringUtils.isBlank(v)) {
                    worker.setNode(null);
                } else {
                    worker.setNode(config.getNode(v));
                }
                break;
            default:
                worker.setAttributeValue(attributeName, v);
                break;
        }
    }

    protected void updateWithRemoved(final String key, final Value value) {
        final String[] tokens = key.split(SEPARATOR);
        final int tokenLength = tokens.length;
        final int offset;
        if (StringUtils.isBlank(tokens[tokenLength - 1])) {
            offset = 1;
        } else {
            offset = 2;
        }

        if (ArrayUtils.isNotEmpty(tokens)) {
            switch (tokenLength - offset) {
                case CLUSTER_ID_INDEX:
                    removeAll();
                    break;
                case TYPE_NAME_INDEX:
                    removeTypes(tokens);
                    break;
                case TYPE_ID_INDEX:
                    removeTypeWithId(tokens);
                    break;
                case ATTRIBUTE_INDEX:
                    removeAttribute(tokens);
                    break;
                default:
                    LOGGER.warn("Unknown configure.");
                    break;
            }
        }
    }

    private void removeAll() {
        config.setClusterId(null);
        config.removeNodes();
        config.removeWorkers();
    }

    private void removeTypes(final String[] tokens) {
        final String typeName = tokens[TYPE_NAME_INDEX];
        switch (typeName) {
            case NODE:
                config.removeNodes();
                break;
            case WORKER:
                config.removeWorkers();
                break;
            default:
                LOGGER.warn("Unknown type name configure.");
                break;
        }
    }

    private void removeTypeWithId(final String[] tokens) {
        final String typeName = tokens[TYPE_NAME_INDEX];
        final String typeId = tokens[TYPE_ID_INDEX];
        switch (typeName) {
            case NODE:
                config.removeNode(typeId);
                break;
            case WORKER:
                config.removeWorker(typeId);
                break;
            default:
                LOGGER.warn("Unknown type name configure.");
                break;
        }
    }

    private void removeAttribute(final String[] tokens) {
        final String typeName = tokens[TYPE_NAME_INDEX];
        final String typeId = tokens[TYPE_ID_INDEX];
        final String attributeName = tokens[ATTRIBUTE_INDEX];
        switch (typeName) {
            case NODE:
                final Node node = config.getNode(typeId);
                switch (attributeName) {
                    case Node.STATUS:
                        node.setStatus(null);
                        break;
                    default:
                        node.setAttributeValue(attributeName, null);
                        break;
                }
                break;
            case WORKER:
                final Worker worker = config.getWorker(typeId);
                worker.setAttributeValue(attributeName, null);
                break;
            default:
                LOGGER.warn("Unknown type name configure.");
                break;
        }
    }

    public Class<? extends BaseClusterScheduler> getCls() {
        return cls;
    }

    public Configure getConfig() {
        return config;
    }
}
