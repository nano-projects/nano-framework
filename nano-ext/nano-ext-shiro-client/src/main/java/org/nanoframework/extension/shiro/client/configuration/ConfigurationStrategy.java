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

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public interface ConfigurationStrategy {

    /**
     * Retrieves the value for the provided configurationKey, falling back to the configurationKey's {@link ConfigurationKey#getDefaultValue()} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @return the configured value, or the default value.
     */
    boolean getBoolean(ConfigurationKey<Boolean> configurationKey);

    /**
     * Retrieves the value for the provided configurationKey, falling back to the configurationKey's {@link ConfigurationKey#getDefaultValue()} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @return the configured value, or the default value.
     */
    String getString(ConfigurationKey<String> configurationKey);

    /**
     * Retrieves the value for the provided configurationKey, falling back to the configurationKey's {@link ConfigurationKey#getDefaultValue()} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @return the configured value, or the default value.
     */
    long getLong(ConfigurationKey<Long> configurationKey);

    /**
     * Retrieves the value for the provided configurationKey, falling back to the configurationKey's {@link ConfigurationKey#getDefaultValue()} if nothing can be found.
     *
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @return the configured value, or the default value.
     */
    int getInt(ConfigurationKey<Integer> configurationKey);

    /**
     * Retrieves the value for the provided configurationKey, falling back to the configurationKey's {@link ConfigurationKey#getDefaultValue()} if nothing can be found.
     * @param <T> the configurationKey type
     * @param configurationKey the configuration key.  MUST NOT BE NULL.
     * @return the configured value, or the default value.
     */
    <T> Class<? extends T> getClass(ConfigurationKey<Class<? extends T>> configurationKey);

    /**
     * Initializes the strategy.  This must be called before calling any of the "get" methods.
     *
     * @param filterConfig the filter configuration object.
     * @param filterClazz  the filter
     */
    void init(FilterConfig filterConfig, Class<? extends Filter> filterClazz);
}
