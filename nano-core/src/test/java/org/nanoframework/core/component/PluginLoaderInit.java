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
package org.nanoframework.core.component;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.DefaultPluginLoader;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public abstract class PluginLoaderInit {
    protected static PluginLoader PLUGIN_LOADER;
    
    @Before
    public void init() {
        if (PLUGIN_LOADER == null) {
            PLUGIN_LOADER = new DefaultPluginLoader();
            PLUGIN_LOADER.init(new ServletConfig() {
                
                @Override
                public String getServletName() {
                    return null;
                }
                
                @Override
                public ServletContext getServletContext() {
                    return null;
                }
                
                @Override
                public Enumeration<String> getInitParameterNames() {
                    return null;
                }
                
                @Override
                public String getInitParameter(String name) {
                    return null;
                }
            });
        }
    }
}
