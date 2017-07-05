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
package org.nanoframework.orm.jedis;

import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.plugins.PluginLoaderException;
import org.nanoframework.core.spi.Order;

import com.google.common.collect.Lists;
import com.google.inject.Binder;

/**
 * @author yanghe
 * @since 1.1
 */
@Order(1000)
public class JedisModule implements Module {
    public static final String DEFAULT_REDIS_PARAMETER_NAME = "redis";
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisModule.class);
    private static final String DEFAULT_REDIS_PATH = "/redis.properties";
    private final List<Module> modules = Lists.newArrayList();
    private final List<Properties> properties = Lists.newArrayList();

    @Override
    public List<Module> load() throws Throwable {
        try {
            final long time = System.currentTimeMillis();
            final RedisClientPool pool = RedisClientPool.POOL;
            pool.initRedisConfig(properties);
            pool.createJedis();
            pool.bindGlobal();
            LOGGER.info("加载Redis配置, 耗时: " + (System.currentTimeMillis() - time) + "ms");
        } catch (final Throwable e) {
            if (!(e instanceof ClassNotFoundException)) {
                throw new PluginLoaderException(e.getMessage(), e);
            }
        }

        return modules;
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {
        final String redis = config.getInitParameter(DEFAULT_REDIS_PARAMETER_NAME);
        if (StringUtils.isNotBlank(redis)) {
            final String[] paths = redis.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        final String contextRedis = System.getProperty(ApplicationContext.CONTEXT_REDIS);
        if (StringUtils.isNotBlank(contextRedis)) {
            final String[] paths = contextRedis.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        if (CollectionUtils.isEmpty(properties)) {
            try {
                properties.add(PropertiesLoader.load(DEFAULT_REDIS_PATH));
            } catch (final Throwable e) {
                // ignore
            }
        }
    }

    @Override
    public void configure(final Binder binder) {

    }

}
