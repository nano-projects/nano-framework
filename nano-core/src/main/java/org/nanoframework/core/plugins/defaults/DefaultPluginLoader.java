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

import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.plugins.Configure;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.module.AOPModule;
import org.nanoframework.core.plugins.defaults.module.DataSourceModule;
import org.nanoframework.core.plugins.defaults.plugin.JedisPlugin;
import org.nanoframework.core.plugins.defaults.plugin.Log4j2Plugin;
import org.nanoframework.core.plugins.defaults.plugin.SchedulerPlugin;
import org.nanoframework.core.plugins.defaults.plugin.WebSocketPlugin;

/**
 * 默认的插件加载器
 * @author yanghe
 * @since 1.1
 */
public class DefaultPluginLoader extends PluginLoader {

    @Override
    protected void configProperties(Configure<String> properties) {
        final String context = config.getInitParameter(ApplicationContext.CONTEXT);
        if (StringUtils.isNotBlank(context)) {
            properties.add(context);
        } else {
            properties.add(ApplicationContext.MAIN_CONTEXT);
        }
    }

    @Override
    protected void configModules(Configure<Module> modules) {
        modules.add(new AOPModule());
        modules.add(new DataSourceModule());
    }

    @Override
    protected void configPlugin(Configure<Plugin> plugins) {
        plugins.add(new Log4j2Plugin());
        plugins.add(new JedisPlugin());
        plugins.add(new SchedulerPlugin());
        plugins.add(new WebSocketPlugin());

    }

}
