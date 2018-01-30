/*
 * Copyright © 2015-2017 the original author or authors.
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
package org.nanoframework.extension.elasticjob.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.plugins.PluginLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author wangtong
 * @since 1.4.11
 */
public abstract class PluginLoaderInit {

    @BeforeClass
    public static void init() throws Throwable {
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

    @AfterClass
    public static void clear() throws Throwable {
        Components.destroy();
        PropertiesLoader.PROPERTIES.clear();
        System.getProperties().keySet().iterator().forEachRemaining(key -> System.setProperty((String) key, ""));
    }
}
