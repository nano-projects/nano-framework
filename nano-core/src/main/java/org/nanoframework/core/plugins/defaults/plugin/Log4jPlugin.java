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
package org.nanoframework.core.plugins.defaults.plugin;

import java.lang.reflect.Method;
import java.net.URL;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoaderException;

/**
 * @author yanghe
 * @since 1.1
 */
public class Log4jPlugin implements Plugin {
    public static final String DEFAULT_LOG4J_PARAMETER_NAME = "log4j";
    private String log4j;

    @Override
    public boolean load() throws Throwable {
        if (StringUtils.isNotBlank(log4j)) {
            URL url = this.getClass().getResource(log4j);
            if (url != null) {
                try {
                    Class<?> cls = Class.forName("org.apache.log4j.xml.DOMConfigurator");
                    Method method = cls.getMethod("configure", URL.class);
                    method.invoke(cls, url);
                } catch (Exception e) {
                    if (!(e instanceof ClassNotFoundException))
                        throw new PluginLoaderException(e.getMessage(), e);

                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {
        log4j = config.getInitParameter(DEFAULT_LOG4J_PARAMETER_NAME);
    }

}
