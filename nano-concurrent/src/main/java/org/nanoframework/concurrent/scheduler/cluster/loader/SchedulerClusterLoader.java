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

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.concurrent.scheduler.cluster.SchedulerCluster;
import org.nanoframework.concurrent.scheduler.loader.SchedulerLoader;
import org.nanoframework.core.component.scan.ClassScanner;

import com.google.inject.Singleton;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
@Singleton
public class SchedulerClusterLoader implements SchedulerLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerClusterLoader.class);
    private static final String BASE_PACKAGE = "context.scheduler-cluster-scan.base-package";
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    @Override
    public void load() {
        if (!valid()) {
            return;
        }

        scan();
        filterSchedulerClusterClasses();
        loaded.set(true);
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
        PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.get(BASE_PACKAGE) != null).forEach(item -> {
            final String basePacakge = item.getProperty(BASE_PACKAGE);
            ClassScanner.scan(basePacakge);
        });
    }

    protected Set<Class<?>> filterSchedulerClusterClasses() {
        final Set<Class<?>> clses = ClassScanner.filter(SchedulerCluster.class);
        LOGGER.info("Scheduler Cluster size: {}", clses.size());
        return clses;
    }
}
