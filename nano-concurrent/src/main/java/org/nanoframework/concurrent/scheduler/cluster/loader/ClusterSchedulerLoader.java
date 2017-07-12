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
package org.nanoframework.concurrent.scheduler.cluster.loader;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.concurrent.scheduler.cluster.BaseClusterScheduler;
import org.nanoframework.concurrent.scheduler.cluster.ClusterScheduler;
import org.nanoframework.concurrent.scheduler.cluster.config.Configure;
import org.nanoframework.concurrent.scheduler.cluster.config.Node;
import org.nanoframework.concurrent.scheduler.cluster.config.NodeStatus;
import org.nanoframework.concurrent.scheduler.cluster.consts.ConsulSources;
import org.nanoframework.concurrent.scheduler.cluster.consts.Keys;
import org.nanoframework.concurrent.scheduler.cluster.exception.SchedulerRegistryException;
import org.nanoframework.concurrent.scheduler.cluster.storage.NodeStatusSyncScheduler;
import org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener;
import org.nanoframework.concurrent.scheduler.loader.SchedulerLoader;
import org.nanoframework.core.component.scan.ClassScanner;
import org.nanoframework.core.globals.Globals;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
@Singleton
public class ClusterSchedulerLoader implements SchedulerLoader, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterSchedulerLoader.class);
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    private String clusterId;
    private String nodeId;
    private Configure configure;
    private KeyValueClient kvClient;
    private SchedulerListener listener;
    private KVCache cache;
    private NodeStatusSyncScheduler nodeStatusSync;

    @Override
    public void load() {
        final boolean enabled = Boolean.parseBoolean(System.getProperty(Keys.CLUSTER_SCHEDULER_ENABLED, "false"));
        if (!enabled) {
            return;
        }

        init();
        if (!valid()) {
            return;
        }

        scan();
        registry(filterSchedulerClusterClasses());
        listener();
        sync();
        loaded.set(true);
    }

    protected void init() {
        final Injector injector = Globals.get(Injector.class);
        clusterId = injector.getInstance(Key.get(String.class, Names.named(Keys.CLUSTER_ID)));
        nodeId = injector.getInstance(Key.get(String.class, Names.named(Keys.NODE_ID)));
        configure = injector.getInstance(Configure.class);
        kvClient = injector.getInstance(Key.get(KeyValueClient.class, Names.named(ConsulSources.KV_SCHEDULER_CLUSTER)));
        listener = injector.getInstance(SchedulerListener.class);
        nodeStatusSync = injector.getInstance(NodeStatusSyncScheduler.class);
    }

    protected boolean valid() {
        if (loaded.get()) {
            LOGGER.warn("已经加载Cluster任务.");
            return false;
        }

        if (PropertiesLoader.PROPERTIES.size() == 0) {
            LOGGER.warn("无任务调度配置.");
            return false;
        }

        return true;
    }

    /**
     * 
     * 扫描Scheduler Cluster组件.
     */
    protected void scan() {
        PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.get(Keys.BASE_PACKAGE) != null).forEach(item -> {
            final String basePacakge = item.getProperty(Keys.BASE_PACKAGE);
            ClassScanner.scan(basePacakge);
        });
    }

    protected Set<Class<? extends BaseClusterScheduler>> filterSchedulerClusterClasses() {
        final Set<Class<?>> clses = ClassScanner.filter(ClusterScheduler.class);
        if (!CollectionUtils.isEmpty(clses)) {
            final Set<Class<? extends BaseClusterScheduler>> bcses = Sets.newHashSet();
            clses.stream().filter(cls -> BaseClusterScheduler.class.isAssignableFrom(cls)).forEach(cls -> bcses.add(ReflectUtils.convert(cls)));
            LOGGER.info("Scheduler Cluster size: {}", bcses.size());
            return bcses;
        }

        return Collections.emptySet();
    }

    protected void registry(final Set<Class<? extends BaseClusterScheduler>> clses) {
        final Injector injector = Globals.get(Injector.class);
        configure.setClusterId(clusterId);
        final Node node = injector.getInstance(Node.class);
        try {
            final String hostAddress = InetAddress.getLocalHost().getHostAddress();
            node.setId(MessageFormat.format(nodeId, hostAddress));
            node.setHost(hostAddress);
        } catch (final UnknownHostException e) {
            throw new SchedulerRegistryException(e.getMessage(), e);
        }

        node.setStatus(NodeStatus.LOOKING);
        node.setUptime(System.currentTimeMillis());
        node.setLivetime(node.getUptime());
        node.setSchedulerAbility(clses);
        configure.setCurrentNode(node);
        configure.addNode(node.getId(), node);
    }

    protected void listener() {
        cache = KVCache.newCache(kvClient, clusterId);
        cache.addListener(listener);
        try {
            cache.start();
        } catch (final Throwable e) {
            throw new ConsulException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        nodeStatusSync.close();
        cache.removeListener(listener);
        try {
            cache.stop();
        } catch (final Exception e) {
            LOGGER.error("停止KVCache异常, {}", e.getMessage());
        }
    }

    protected void sync() {
        nodeStatusSync.start();
    }
}
