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
package org.nanoframework.concurrent.scheduler;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoaderException;

/**
 * @author yanghe
 * @since 1.3
 */
public class SchedulerPlugin implements Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerPlugin.class);

    @Override
    public boolean load() throws Throwable {
        try {
            final long time = System.currentTimeMillis();
            LOGGER.info("开始加载任务调度");
            SchedulerFactory.load();
            SchedulerFactory.getInstance().startAll();
            LOGGER.info("加载任务调度结束, 耗时: {}ms", System.currentTimeMillis() - time);
        } catch (final Throwable e) {
            throw new PluginLoaderException(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {

    }

}
