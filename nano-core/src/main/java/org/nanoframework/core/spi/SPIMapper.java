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

import org.nanoframework.commons.entity.BaseEntity;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SPIMapper extends BaseEntity {
    private static final long serialVersionUID = -2120348787803208033L;

    private Class spi;
    private String spiClsName;
    private String name;
    private Class instance;
    private String instanceClsName;
    private Boolean lazy;

    private SPIMapper(final Class spi, final String name, final Class instance) {
        this.spi = spi;
        this.spiClsName = spi.getName();
        this.name = name;
        this.instance = instance;
        this.instanceClsName = instance.getName();
        lazy();
    }

    private void lazy() {
        lazy = instance.isAnnotationPresent(Lazy.class);
    }

    public static SPIMapper create(final Class spi, final String name, final Class instance) {
        return new SPIMapper(spi, name, instance);
    }

    public Class getSpi() {
        return spi;
    }

    public String getSpiClsName() {
        return spiClsName;
    }

    public String getName() {
        return name;
    }

    public Class getInstance() {
        return instance;
    }

    public String getInstanceClsName() {
        return instanceClsName;
    }

    public Boolean getLazy() {
        return lazy;
    }

}
