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

import org.nanoframework.commons.util.StringUtils;

/**
 * 
 * @author yanghe
 * @since 1.3.7
 */
public final class WebXmlConfigurationStrategyImpl extends BaseConfigurationStrategy {

    private FilterConfig filterConfig;

    protected String get(final ConfigurationKey<?> configurationKey) {
        final String value = this.filterConfig.getInitParameter(configurationKey.getName());

        if (StringUtils.isNotBlank(value)) {
            logger.info("Property [{}] loaded from ServletContext.getInitParameter with value [{}]", configurationKey, value);
            return value;
        }

        return null;
    }

    public void init(final FilterConfig filterConfig, final Class<? extends Filter> clazz) {
        this.filterConfig = filterConfig;
    }
}
