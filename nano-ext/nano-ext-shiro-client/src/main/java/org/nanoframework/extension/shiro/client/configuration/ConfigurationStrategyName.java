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
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.commons.util.StringUtils;

/**
 * 
 * @author yanghe
 * @since 1.3.7
 */
public enum ConfigurationStrategyName {
    DEFAULT(LegacyConfigurationStrategyImpl.class), JNDI(JndiConfigurationStrategyImpl.class), WEB_XML(WebXmlConfigurationStrategyImpl.class),
    PROPERTY_FILE(PropertiesConfigurationStrategyImpl.class), SYSTEM_PROPERTIES(SystemPropertiesConfigurationStrategyImpl.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationStrategyName.class);

    private final Class<? extends ConfigurationStrategy> configurationStrategyClass;

    private ConfigurationStrategyName(final Class<? extends ConfigurationStrategy> configurationStrategyClass) {
        this.configurationStrategyClass = configurationStrategyClass;
    }

    /**
     * Static helper method that will resolve a simple string to either an enum value or a ConfigurationStrategy class.
     *
     * @param value the value to attempt to resolve.
     * @return the underlying class that this maps to (either via simple name or fully qualified class name).
     */
    public static Class<? extends ConfigurationStrategy> resolveToConfigurationStrategy(final String value) {
        if (StringUtils.isBlank(value)) {
            return DEFAULT.configurationStrategyClass;
        }

        for (final ConfigurationStrategyName csn : values()) {
            if (csn.name().equalsIgnoreCase(value)) {
                return csn.configurationStrategyClass;
            }
        }

        try {
            final Class<?> clazz = Class.forName(value);
            if (ConfigurationStrategy.class.isAssignableFrom(clazz)) {
                return ReflectUtils.convert(clazz);
            }
        } catch (final ClassNotFoundException e) {
            LOGGER.error("Unable to locate strategy {} by name or class name.  Using default strategy instead.", value, e);
        }

        return DEFAULT.configurationStrategyClass;
    }
}
