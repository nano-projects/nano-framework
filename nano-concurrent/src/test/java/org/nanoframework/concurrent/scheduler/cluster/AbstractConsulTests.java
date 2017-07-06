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
package org.nanoframework.concurrent.scheduler.cluster;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.BeforeClass;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.plugins.defaults.module.SPIModule;
import org.nanoframework.core.spi.SPILoader;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public abstract class AbstractConsulTests {
    private static final ServletConfig config = new ServletConfig() {

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
            return null;
        }
    };
    
    protected Injector injector;

    @BeforeClass
    public static void setup() throws Throwable {
        final Injector injector = Guice.createInjector().createChildInjector(new SPIModule());
        Globals.set(Injector.class, injector);
        final Set<String> moduleNames = SPILoader.spiNames(Module.class);
        if (!CollectionUtils.isEmpty(moduleNames)) {
            final List<Module> loadedModules = Lists.newArrayList();
            for (final String moduleName : moduleNames) {
                final Module module = injector.getInstance(Key.get(Module.class, Names.named(moduleName)));
                module.config(config);
                loadedModules.addAll(module.load());
            }

            Globals.set(Injector.class, injector.createChildInjector(loadedModules));
        }
    }

    protected void injects() {
        injector = Globals.get(Injector.class).createChildInjector(binder -> binder.requestInjection(this));
    }

}
