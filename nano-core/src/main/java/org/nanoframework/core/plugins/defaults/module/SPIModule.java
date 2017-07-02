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

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.spi.Lazy;
import org.nanoframework.core.spi.SPILoader;
import org.nanoframework.core.spi.SPIMapper;
import org.nanoframework.core.spi.SPIProvider;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
public class SPIModule implements Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPIModule.class);

    @SuppressWarnings("unchecked")
    @Override
    public void configure(final Binder binder) {
        final Map<Class<?>, List<SPIMapper>> spiMappers = SPILoader.spis();
        if (!CollectionUtils.isEmpty(spiMappers)) {
            final Injector injector = Globals.get(Injector.class);
            spiMappers.forEach((spiCls, spis) -> {
                if (!spiCls.isAnnotationPresent(Lazy.class)) {
                    spis.forEach(spi -> {
                        final String spiClsName = spi.getSpiClsName();
                        final String name = spi.getName();
                        final String instanceClsName = spi.getInstanceClsName();
                        if (!spi.getLazy()) {
                            final Object instance = injector.getInstance(spi.getInstance());
                            binder.bind(spi.getSpi()).annotatedWith(Names.named(name)).toInstance(instance);
                            LOGGER.debug("绑定即时SPI, 接口定义: {}, 绑定名称: {}, 实现类: {}", spiClsName, name, instanceClsName);
                        } else {
                            binder.bind(spi.getSpi()).annotatedWith(Names.named(spi.getName())).toProvider(new SPIProvider(spi));
                            LOGGER.debug("绑定延时SPI, 接口定义: {}, 绑定名称: {}, 实现类: {}", spiClsName, name, instanceClsName);
                        }
                    });
                } else {
                    spis.forEach(spi -> {
                        final String spiClsName = spi.getSpiClsName();
                        final String name = spi.getName();
                        final String instanceClsName = spi.getInstanceClsName();
                        binder.bind(spi.getSpi()).annotatedWith(Names.named(spi.getName())).toProvider(new SPIProvider(spi));
                        LOGGER.debug("绑定延时SPI, 接口定义: {}, 绑定名称: {}, 实现类: {}", spiClsName, name, instanceClsName);
                    });
                }
            });
        }
    }

}
