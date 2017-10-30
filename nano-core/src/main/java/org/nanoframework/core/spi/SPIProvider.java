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
package org.nanoframework.core.spi;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.globals.Globals;

import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
public class SPIProvider implements Provider<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPIProvider.class);

    private final SPIMapper spi;
    private boolean echo;

    public SPIProvider(final SPIMapper spi) {
        this.spi = spi;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get() {
        try {
            return Globals.get(Injector.class).getInstance(spi.getInstance());
        } finally {
            if (!echo) {
                LOGGER.debug("创建SPI实现, 接口定义: {}, 绑定名称: {}, 实现类: {}", spi.getSpiClsName(), spi.getName(), spi.getInstanceClsName());
                echo = true;
            }
        }
    }

}
