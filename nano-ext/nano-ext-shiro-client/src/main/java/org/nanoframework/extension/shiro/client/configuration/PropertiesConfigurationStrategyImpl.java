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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.ObjectUtils;
import org.nanoframework.commons.util.StringUtils;

/**
 * 
 * @author yanghe
 * @since 1.3.7
 */
public final class PropertiesConfigurationStrategyImpl extends BaseConfigurationStrategy {

    /**
     * Property name we'll use in the {@link javax.servlet.FilterConfig} and {@link javax.servlet.ServletConfig} to try and find where
     * you stored the configuration file.
     */
    private static final String CONFIGURATION_FILE_LOCATION = "configFileLocation";

    /**
     * Default location of the configuration file.  Mostly for testing/demo.  You will most likely want to configure an alternative location.
     */
    private static final String DEFAULT_CONFIGURATION_FILE_LOCATION = "/nano-shiro-client.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfigurationStrategyImpl.class);

    private String simpleFilterName;

    private Properties properties = new Properties();

    @Override
    protected String get(final ConfigurationKey<?> configurationKey) {
        final String property = configurationKey.getName();
        final String filterSpecificProperty = this.simpleFilterName + '.' + property;

        final String filterSpecificValue = this.properties.getProperty(filterSpecificProperty);

        if (StringUtils.isNotEmpty(filterSpecificValue)) {
            return filterSpecificValue;
        }

        return this.properties.getProperty(property);
    }

    public void init(final FilterConfig filterConfig, final Class<? extends Filter> filterClazz) {
        this.simpleFilterName = filterClazz.getSimpleName();
        final String fileLocationFromFilterConfig = filterConfig.getInitParameter(CONFIGURATION_FILE_LOCATION);
        final boolean filterConfigFileLoad = loadPropertiesFromFile(fileLocationFromFilterConfig);

        if (!filterConfigFileLoad) {
            final String fileLocationFromServletConfig = filterConfig.getServletContext().getInitParameter(CONFIGURATION_FILE_LOCATION);
            final boolean servletContextFileLoad = loadPropertiesFromFile(fileLocationFromServletConfig);

            if (!servletContextFileLoad) {
                final boolean defaultConfigFileLoaded = loadPropertiesFromFile(DEFAULT_CONFIGURATION_FILE_LOCATION);
                Assert.isTrue(defaultConfigFileLoaded, "unable to load properties to configure CAS client");
            }
        }
    }

    private boolean loadPropertiesFromFile(final String file) {
        if (StringUtils.isEmpty(file)) {
            return false;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            this.properties.load(fis);
            return true;
        } catch (final IOException e) {
            LOGGER.warn("Unable to load properties for file {}", file, e);
            return false;
        } finally {
            ObjectUtils.closeQuietly(fis);
        }
    }
}
