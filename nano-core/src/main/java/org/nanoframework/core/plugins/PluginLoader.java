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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.globals.Globals;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author yanghe
 * @date 2015年10月30日 下午10:29:16
 */
public abstract class PluginLoader {
    private Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    private Configure<String> properties = new Configure<>();
    private Configure<Plugin> plugins = new Configure<>();
    private Configure<Module> modules = new Configure<>();

    private ServletConfig config;

    public void init(final ServletConfig config) {
        Assert.notNull(config);
        this.config = config;

        try {
            initProperties();
            initModules();
            initPlugins();
            initComponent();

        } catch (final Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }
    }

    private void initProperties() {
        long time = System.currentTimeMillis();

        try {
            configProperties(properties);
            for (String path : properties.get()) {
                PropertiesLoader.load(path, true);
            }
        } catch (Exception e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }

        logger.info("加载属性文件完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
    }

    private void initModules() throws Throwable {
        long time = System.currentTimeMillis();
        configModules(this.modules);
        List<Module> loadedModules = new ArrayList<>();
        for (Module module : this.modules.get()) {
            if (logger.isInfoEnabled()) {
                logger.info("加载Module: " + module.getClass().getName());
            }
            
            module.config(config);
            loadedModules.addAll(module.load());
        }

        logger.info("开始进行依赖注入");
        Globals.set(Injector.class, Guice.createInjector(loadedModules));
        logger.info("依赖注入完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
    }

    private void initComponent() throws LoaderException, IOException {
        long time = System.currentTimeMillis();
        logger.info("开始加载组件服务");
        Components.load();
        logger.info("加载组件服务结束, 耗时: " + (System.currentTimeMillis() - time) + "ms");
    }

    private void initPlugins() throws Throwable {
        long time = System.currentTimeMillis();
        configPlugin(plugins);
        for (Plugin plugin : plugins.get()) {
            plugin.config(config);
            if (plugin.load()) {
                logger.info("加载插件: " + plugin.getClass().getName());
            }
        }

        logger.info("加载插件完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
    }

    protected abstract void configProperties(Configure<String> properties);

    protected abstract void configModules(Configure<Module> modules);

    protected abstract void configPlugin(Configure<Plugin> plugins);

}
