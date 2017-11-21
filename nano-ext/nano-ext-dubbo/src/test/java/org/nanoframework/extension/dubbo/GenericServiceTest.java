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
package org.nanoframework.extension.dubbo;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.plugins.PluginLoader;

import com.google.inject.Guice;
import org.nanoframework.core.plugins.defaults.module.FieldInjectModule;

/**
 *
 * @author yanghe
 * @since 1.4.10
 */
public class GenericServiceTest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(GenericServiceTest.class);

    @BeforeClass
    public static void init() {
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
            public String getInitParameter(String name) {
                return null;
            }
        });
    }

    @AfterClass
    public static void clear() throws Throwable {
        Components.destroy();
    }

    @Test
    public void genericTest() {
        final GenericServiceProxy generic = Guice.createInjector(new FieldInjectModule(), new DubboReferenceModule()).getInstance(GenericServiceProxy.class);
        LOGGER.info("{}", generic.get(Integer.class));
        LOGGER.info("{}", generic.get(String.class));
        LOGGER.info("{}", generic.get(Map.class));
    }
}
