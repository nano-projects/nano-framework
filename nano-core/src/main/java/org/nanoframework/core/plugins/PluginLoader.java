/*
 * Copyright 2015-2016 the original author or authors.
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
package org.nanoframework.core.plugins;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.globals.Globals;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author yanghe
 * @since 1.1
 */
public abstract class PluginLoader {
    private Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    private Configure<String> properties = new Configure<>();
    private Configure<Plugin> plugins = new Configure<>();
    private Configure<Module> modules = new Configure<>();
    private Configure<Module> childrenModules = new Configure<>();

    protected ServletConfig config;
    protected ServletContext context;

    public void init(final HttpServlet servlet) {
        this.init(servlet.getServletConfig(), servlet.getServletContext());
    }

    public void init(final ServletConfig config) {
        this.init(config, null);
    }

    public void init(final ServletConfig config, final ServletContext context) {
        this.config = config;
        this.context = context;

        try {
            initProperties();
            initModules();
            initChildrenModules();
            initPlugins();
            initComponent();

        } catch (final Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }
    }

    private void initProperties() {
        final long time = System.currentTimeMillis();
        try {
            configProperties(properties);
            for (final String path : properties.get()) {
                PropertiesLoader.load(path, true);
            }
        } catch (final Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }

        logger.info("Loading the properties file complete, times: {}ms", System.currentTimeMillis() - time);
    }

    private void initModules() throws Throwable {
        final long time = System.currentTimeMillis();
        configModules(this.modules);
        final List<Module> loadedModules = Lists.newArrayList();
        final List<Module> modules = this.modules.get();
        for (final Module module : modules) {
            logger.info("Loading Module: {}", module.getClass().getName());
            if (module instanceof org.nanoframework.core.plugins.Module) {
                final org.nanoframework.core.plugins.Module mdu = (org.nanoframework.core.plugins.Module) module;
                mdu.config(config);
                loadedModules.addAll(mdu.load());
            } else {
                loadedModules.add(module);
            }
        }

        logger.info("Starting inject");
        Globals.set(Injector.class, Guice.createInjector(loadedModules));
        logger.info("Inject Modules complete, times: {}ms", System.currentTimeMillis() - time);
    }

    private void initChildrenModules() throws Throwable {
        final long time = System.currentTimeMillis();
        configChildrenModules(this.childrenModules);
        final List<Module> loadedModules = Lists.newArrayList();
        final List<Module> modules = this.childrenModules.get();
        for (final Module module : modules) {
            logger.info("Loading Module: {}", module.getClass().getName());
            if (module instanceof org.nanoframework.core.plugins.Module) {
                final org.nanoframework.core.plugins.Module mdu = (org.nanoframework.core.plugins.Module) module;
                mdu.config(config);
                loadedModules.addAll(mdu.load());
            } else {
                loadedModules.add(module);
            }
        }

        logger.info("Starting inject");
        final Injector parent = Globals.get(Injector.class);
        Globals.set(Injector.class, parent.createChildInjector(loadedModules));
        logger.info("Inject Children Modules complete, times: {}ms", System.currentTimeMillis() - time);
    }

    private void initPlugins() throws Throwable {
        final long time = System.currentTimeMillis();
        configPlugin(plugins);
        final List<Plugin> plugins = this.plugins.get();
        for (final Plugin plugin : plugins) {
            plugin.config(config);
            if (plugin.load()) {
                logger.info("Loading Plugin: {}", plugin.getClass().getName());
            }
        }

        logger.info("Loading Plugins complete, times: {}ms", System.currentTimeMillis() - time);
    }

    private void initComponent() throws Throwable {
        final long time = System.currentTimeMillis();
        logger.info("Starting inject component");
        Components.load();
        logger.info("Inject Component complete, times: {}ms", System.currentTimeMillis() - time);
    }

    protected abstract void configProperties(Configure<String> properties);

    protected abstract void configModules(Configure<Module> modules);

    protected abstract void configChildrenModules(Configure<Module> modules);

    protected abstract void configPlugin(Configure<Plugin> plugins);

}
