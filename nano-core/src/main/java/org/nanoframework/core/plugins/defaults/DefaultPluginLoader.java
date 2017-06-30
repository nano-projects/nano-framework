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
package org.nanoframework.core.plugins.defaults;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.plugins.Configure;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.module.AOPModule;
import org.nanoframework.core.plugins.defaults.module.APIModule;
import org.nanoframework.core.plugins.defaults.module.BindModule;
import org.nanoframework.core.plugins.defaults.module.DataSourceModule;
import org.nanoframework.core.plugins.defaults.module.JedisModule;
import org.nanoframework.core.plugins.defaults.module.SPIModule;
import org.nanoframework.core.plugins.defaults.module.SysAttrModule;
import org.nanoframework.core.plugins.defaults.plugin.Log4j2Plugin;
import org.nanoframework.core.plugins.defaults.plugin.SchedulerPlugin;
import org.nanoframework.core.plugins.defaults.plugin.WebSocketPlugin;

import com.google.inject.Module;

/**
 * 默认的插件加载器
 * @author yanghe
 * @since 1.1
 */
public class DefaultPluginLoader extends PluginLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPluginLoader.class);

    public static PluginLoader newInstance() {
        return new DefaultPluginLoader();
    }

    @Override
    protected void configProperties(final Configure<String> properties) {
        final String context = config.getInitParameter(ApplicationContext.CONTEXT);
        if (StringUtils.isNotBlank(context)) {
            properties.add(context);
        } else {
            properties.add(ApplicationContext.MAIN_CONTEXT);
        }
    }

    @Override
    protected void configModules(final Configure<Module> modules) {
        modules.add(new SysAttrModule());
        modules.add(new AOPModule());
        modules.add(new DataSourceModule());
        modules.add(new JedisModule());
        modules.add(new BindModule());
        modules.add(new APIModule());
        createNativeModule(modules, "org.nanoframework.extension.dubbo.DubboReferenceModule");
        createNativeModule(modules, "org.apache.shiro.guice.aop.ShiroAopModule");
    }

    @Override
    protected void configChildrenModules(final Configure<Module> modules) {
        createNativeModule(modules, "org.nanoframework.extension.dubbo.DubboServiceModule");
        modules.add(new SPIModule());
    }

    private void createNativeModule(final Configure<Module> modules, final String className, Object... args) {
        try {
            final Module module = (Module) ReflectUtils.newInstance(className, args);
            modules.add(module);
        } catch (final Throwable e) {
            LOGGER.warn("创建Module异常: {}", e.getMessage());
        }
    }

    @Override
    protected void configPlugin(final Configure<Plugin> plugins) {
        plugins.add(new Log4j2Plugin());
        plugins.add(new SchedulerPlugin());
        plugins.add(new WebSocketPlugin());
    }

}
