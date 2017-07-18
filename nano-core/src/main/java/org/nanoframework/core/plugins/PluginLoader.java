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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.defaults.module.SPIModule;
import org.nanoframework.core.spi.Level;
import org.nanoframework.core.spi.SPILoader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 1.1
 */
public class PluginLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);

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
            initRootInjector();
            initModules();
            initPlugins();
            initComponent();

        } catch (final Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }
    }

    private void initProperties() {
        final long time = System.currentTimeMillis();
        try {
            final String context = config.getInitParameter(ApplicationContext.CONTEXT);
            if (StringUtils.isNotBlank(context)) {
                PropertiesLoader.load(context, true);
            } else {
                PropertiesLoader.load(ApplicationContext.MAIN_CONTEXT, true);
            }
        } catch (final Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }

        LOGGER.info("Loading the properties file complete, times: {}ms", System.currentTimeMillis() - time);
    }

    private void initRootInjector() {
        final Injector injector = Guice.createInjector();
        Globals.set(Injector.class, injector);
        Globals.set(Injector.class, injector.createChildInjector(new SPIModule()));
    }

    private void initModules() throws Throwable {
        final Set<String> moduleNames = SPILoader.spiNames(Module.class);
        if (!CollectionUtils.isEmpty(moduleNames)) {
            final Injector injector = Globals.get(Injector.class);
            final Map<Integer, List<Module>> modules = Maps.newHashMap();
            for (final String moduleName : moduleNames) {
                final Module module = injector.getInstance(Key.get(Module.class, Names.named(moduleName)));
                final Level level = module.getClass().getAnnotation(Level.class);
                if (level != null) {
                    addModules(modules, level.value(), module);
                } else {
                    addModules(modules, 0, module);
                }
            }

            loadModules(modules);
        }
    }

    private void addModules(final Map<Integer, List<Module>> modules, final Integer level, final Module module) {
        if (modules.containsKey(level)) {
            modules.get(level).add(module);
        } else {
            modules.put(level, Lists.newArrayList(module));
        }
    }

    private void loadModules(final Map<Integer, List<Module>> loadingModules) throws Throwable {
        final List<Integer> levels = Lists.newArrayList();
        loadingModules.keySet().forEach(level -> levels.add(level));
        Collections.sort(levels);
        for (final Integer level : levels) {
            final List<Module> modules = loadingModules.get(level);
            if (!CollectionUtils.isEmpty(modules)) {
                final List<Module> mdus = Lists.newArrayList();
                for (final Module module : modules) {
                    module.config(config);
                    mdus.addAll(module.load());
                }

                if (!CollectionUtils.isEmpty(mdus)) {
                    if (level.intValue() == 0) {
                        mdus.add(0, new SPIModule());
                        Globals.set(Injector.class, Guice.createInjector(mdus));
                    } else {
                        Globals.set(Injector.class, Globals.get(Injector.class).createChildInjector(mdus));
                    }
                }
            }
        }
    }

    private void initPlugins() throws Throwable {
        final Set<String> pluginNames = SPILoader.spiNames(Plugin.class);
        if (!CollectionUtils.isEmpty(pluginNames)) {
            final Injector injector = Globals.get(Injector.class);
            for (final String pluginName : pluginNames) {
                final Plugin plugin = injector.getInstance(Key.get(Plugin.class, Names.named(pluginName)));
                plugin.config(config);
                if (plugin.load()) {
                    LOGGER.info("Loading Plugin: {}", plugin.getClass().getName());
                }
            }
        }
    }

    private void initComponent() throws Throwable {
        final long time = System.currentTimeMillis();
        LOGGER.info("Starting inject component");
        Components.load();
        LOGGER.info("Inject Component complete, times: {}ms", System.currentTimeMillis() - time);
    }
}
