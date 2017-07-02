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
package org.nanoframework.orm;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.plugins.PluginLoaderException;
import org.nanoframework.core.spi.Order;
import org.nanoframework.core.spi.SPILoader;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author yanghe
 * @since 1.1
 */
@Order(2000)
public class DataSourceModule implements Module {

    @Override
    public List<Module> load() throws Throwable {
        final Injector injector = Globals.get(Injector.class);
        final Set<String> dataSourceLoaderNames = SPILoader.spiNames(DataSourceLoader.class);
        if (!CollectionUtils.isEmpty(dataSourceLoaderNames)) {
            final List<Module> modules = Lists.newArrayList();
            dataSourceLoaderNames.forEach(dataSourceLoaderName -> {
                final DataSourceLoader loader = injector.getInstance(Key.get(DataSourceLoader.class, Names.named(dataSourceLoaderName)));
                try {
                    PropertiesLoader.PROPERTIES.putAll(loader.getProperties());
                    modules.addAll(loader.getModules());
                } catch (final Throwable e) {
                    if (!(e instanceof ClassNotFoundException)) {
                        throw new PluginLoaderException(e.getMessage(), e);
                    }
                }
            });

            return modules;
        }

        return Collections.emptyList();
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {

    }

    @Override
    public void configure(final Binder binder) {

    }

}
