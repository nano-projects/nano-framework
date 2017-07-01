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
package org.nanoframework.concurrent.scheduler.tests;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Before;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.concurrent.scheduler.SchedulerFactory;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.plugins.PluginLoader;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public abstract class PluginLoaderInit {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PluginLoaderInit.class);
    
    @Before
    public void init() throws Throwable {
        final Map<String, String> map = MapBuilder.<String, String>builder().put("context", "/test-context.properties").build();
        new PluginLoader().init(new ServletConfig() {
            
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
            public String getInitParameter(final String name) {
                return map.get(name);
            }
        });
    }
    
    @After
    public void clear() throws Throwable {
        SchedulerFactory.getInstance().destory();
        Components.destroy();
        PropertiesLoader.PROPERTIES.clear();
    }
}
