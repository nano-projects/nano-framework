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

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.commons.util.StringUtils;

/**
 * 
 * @author yanghe
 * @since 1.3.7
 */
public abstract class BaseConfigurationStrategy implements ConfigurationStrategy {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public final boolean getBoolean(final ConfigurationKey<Boolean> configurationKey) {
        return getValue(configurationKey, new Parser<Boolean>() {
            public Boolean parse(final String value) {
                return StringUtils.toBoolean(value);
            }
        });
    }

    public final long getLong(final ConfigurationKey<Long> configurationKey) {
        return getValue(configurationKey, new Parser<Long>() {
            public Long parse(final String value) {
                return StringUtils.toLong(value, configurationKey.getDefaultValue());
            }
        });
    }

    public final int getInt(final ConfigurationKey<Integer> configurationKey) {
        return getValue(configurationKey, new Parser<Integer>() {
            public Integer parse(final String value) {
                return StringUtils.toInt(value, configurationKey.getDefaultValue());
            }
        });
    }

    public final String getString(final ConfigurationKey<String> configurationKey) {
        return getValue(configurationKey, new Parser<String>() {
            public String parse(final String value) {
                return value;
            }
        });
    }

    public <T> Class<? extends T> getClass(final ConfigurationKey<Class<? extends T>> configurationKey) {
        return getValue(configurationKey, new Parser<Class<? extends T>>() {
            public Class<? extends T> parse(final String value) {
                try {
                    return ReflectUtils.loadClass(value);
                } catch (final IllegalArgumentException e) {
                    return configurationKey.getDefaultValue();
                }
            }
        });
    }

    private <T> T getValue(final ConfigurationKey<T> configurationKey, final Parser<T> parser) {
        final String value = getWithCheck(configurationKey);

        if (StringUtils.isBlank(value)) {
            logger.debug("No value found for property {}, returning default {}", configurationKey.getName(), configurationKey.getDefaultValue());
            return configurationKey.getDefaultValue();
        } else {
            logger.debug("Loaded property {} with value {}", configurationKey.getName(), configurationKey.getDefaultValue());
        }

        return parser.parse(value);
    }

    private String getWithCheck(final ConfigurationKey<?> configurationKey) {
        Assert.notNull(configurationKey, "configurationKey cannot be null");
        return get(configurationKey);
    }

    /**
     * Retrieve the String value for this key.  Returns null if there is no value.
     *
     * @param configurationKey the key to retrieve.  MUST NOT BE NULL.
     * @return the String if its found, null otherwise.
     */
    protected abstract String get(ConfigurationKey<?> configurationKey);

    private interface Parser<T> {

        T parse(String value);
    }
}
