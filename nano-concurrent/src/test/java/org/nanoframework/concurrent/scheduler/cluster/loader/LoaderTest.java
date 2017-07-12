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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.concurrent.scheduler.SchedulerFactory;
import org.nanoframework.concurrent.scheduler.cluster.config.Configure;
import org.nanoframework.concurrent.scheduler.cluster.config.NodeStatus;
import org.nanoframework.concurrent.scheduler.cluster.consts.ConsulSources;
import org.nanoframework.concurrent.scheduler.cluster.lock.ElectionLocker;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.PluginLoader;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.orbitz.consul.KeyValueClient;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class LoaderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoaderTest.class);

    @Inject
    private static Configure configure;

    @Inject
    @Named(ConsulSources.KV_SCHEDULER_CLUSTER)
    private static KeyValueClient kvClient;

    @Inject
    private static ElectionLocker locker;

    @Inject
    private static ClusterSchedulerLoader loader;

    @BeforeClass
    public static void setup() {
        try {
            new PluginLoader().init(new ServletConfig() {
                final Map<String, String> maps = MapBuilder.<String, String> builder()
                        .put(ApplicationContext.CONTEXT, "/cluster-scheduler-context.properties").build();

                @Override
                public String getServletName() {
                    return null;
                }

                @Override
                public ServletContext getServletContext() {
                    return null;
                }

                @Override
                public String getInitParameter(final String name) {
                    return maps.get(name);
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return null;
                }

            });

            Globals.set(Injector.class, Globals.get(Injector.class).createChildInjector(binder -> binder.requestStaticInjection(LoaderTest.class)));
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @AfterClass
    public static void shutdown() throws IOException {
        SchedulerFactory.getInstance().destory();
        Components.destroy();
        PropertiesLoader.PROPERTIES.clear();
        loader.close();
        System.getProperties().keySet().iterator().forEachRemaining(key -> System.setProperty((String) key, ""));
    }

    @Test
    public void registedTest() throws Throwable {
        try {
            Thread.sleep(30_000);
            LOGGER.debug("{}", configure);
            Assert.assertEquals(configure.getCurrentNode().getStatus(), NodeStatus.LEADER);

            kvClient.deleteKeys(configure.getClusterId());
            locker.unlock();
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
