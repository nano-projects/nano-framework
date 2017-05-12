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
package org.nanoframework.extension.shiro.client.configuration;

import org.nanoframework.commons.util.Assert;

/**
 * 
 *
 * @author yanghe
 * @since 1.3.7
 */
public final class ConfigurationKey<E> {

    private final String name;

    private final E defaultValue;

    public ConfigurationKey(final String name) {
        this(name, null);
    }

    public ConfigurationKey(final String name, final E defaultValue) {
        Assert.hasText(name, "name must not be null.");
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public E getDefaultValue() {
        return this.defaultValue;
    }
}
