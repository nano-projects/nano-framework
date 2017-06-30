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
package org.nanoframework.core.plugins.defaults.module;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.spi.Lazy;
import org.nanoframework.core.spi.SPILoader;
import org.nanoframework.core.spi.SPIMapper;
import org.nanoframework.core.spi.SPIProvider;

import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
public class SPIModule extends Module {

    @Override
    public List<Module> load() throws Throwable {
        modules.add(this);
        return modules;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        final Map<Class<?>, List<SPIMapper>> spiMappers = SPILoader.spiMappers();
        if (!CollectionUtils.isEmpty(spiMappers)) {
            final Injector injector = Globals.get(Injector.class);
            spiMappers.forEach((spiCls, spis) -> {
                if (!spiCls.isAnnotationPresent(Lazy.class)) {
                    spis.forEach(spi -> {
                        if (!spi.getLazy()) {
                            final Object instance = injector.getInstance(spi.getInstance());
                            binder().bind(spi.getSpi()).annotatedWith(Names.named(spi.getName())).toInstance(instance);
                        } else {
                            binder().bind(spi.getSpi()).annotatedWith(Names.named(spi.getName())).toProvider(new SPIProvider(spi));
                        }
                    });
                } else {
                    spis.forEach(spi -> binder().bind(spi.getSpi()).annotatedWith(Names.named(spi.getName())).toProvider(new SPIProvider(spi)));
                }
            });
        }
    }

}
