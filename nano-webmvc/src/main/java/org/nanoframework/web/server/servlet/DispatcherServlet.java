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
package org.nanoframework.web.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.DefaultPluginLoader;

/**
 * 
 * @author yanghe
 * @since 1.0
 */
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 2341783013239890497L;
    private Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            final String pluginLoader = this.getInitParameter(ApplicationContext.PLUGIN_LOADER);
            if (StringUtils.isBlank(pluginLoader)) {
                LOGGER.debug("Use default plugin loader: " + DefaultPluginLoader.class.getName());
                DefaultPluginLoader.newInstance().init(this);
            } else {
                final Class<?> cls = Class.forName(pluginLoader);
                if (PluginLoader.class.isAssignableFrom(cls)) {
                    LOGGER.debug("Use plugin loader: " + pluginLoader);
                    ((PluginLoader) cls.newInstance()).init(this);
                } else {
                    throw new IllegalArgumentException("The plugin loader must inherit from the PluginLoader class");
                }
            }
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }

}
