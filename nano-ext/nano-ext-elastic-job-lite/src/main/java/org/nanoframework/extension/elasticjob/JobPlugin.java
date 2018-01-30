/*
 * Copyright © 2015-2017 the original author or authors.
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
package org.nanoframework.extension.elasticjob;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoaderException;

import javax.servlet.ServletConfig;
import java.util.List;
import java.util.Properties;

/**
 * @author wangtong
 * @since 1.4.11
 */
public class JobPlugin implements Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobPlugin.class);
    private static final String DEFAULT_JOB_PARAMETER_NAME = "elastic-job";
    private static final String DEFAULT_JOB_PATH = "/elastic-job.properties";
    private final List<Properties> properties = Lists.newArrayList();

    @Override
    public boolean load() throws Throwable {
        try {
            final long time = System.currentTimeMillis();
            LOGGER.info("开始加载Elastic Job");
            JobFactory jobFactory = JobFactory.getInstance();
            jobFactory.initJobConfig(properties);
            jobFactory.load();
            jobFactory.registerAll();
            LOGGER.info("加载Elastic Job结束, 耗时: {}ms", System.currentTimeMillis() - time);
        } catch (final Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {
        final String job = config.getInitParameter(DEFAULT_JOB_PARAMETER_NAME);
        if (StringUtils.isNotBlank(job)) {
            final String[] paths = job.split(",");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        final String contextJob = System.getProperty(ApplicationContext.CONTEXT_JOB);
        if (StringUtils.isNotBlank(contextJob)) {
            final String[] paths = contextJob.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        try {
            properties.add(PropertiesLoader.load(DEFAULT_JOB_PATH));
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
