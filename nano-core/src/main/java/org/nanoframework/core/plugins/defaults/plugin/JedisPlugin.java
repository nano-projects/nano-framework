/**
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.core.plugins.defaults.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoaderException;

/**
 * @author yanghe
 * @date 2015年10月30日 下午11:23:52
 */
public class JedisPlugin implements Plugin {
	private Logger LOG = LoggerFactory.getLogger(JedisPlugin.class);
	public static final String DEFAULT_REDIS_PARAMETER_NAME = "redis";
	private List<Properties> properties;
	
	@Override
	public void load() throws Throwable {
		try {
			Class<?> redisClientPool = Class.forName("org.nanoframework.orm.jedis.RedisClientPool");
			long time = System.currentTimeMillis();
			Object pool = redisClientPool.getField("POOL").get(redisClientPool);
			pool.getClass().getMethod("initRedisConfig", List.class).invoke(pool, properties);
			pool.getClass().getMethod("createJedis").invoke(pool);
			LOG.info("加载Redis配置, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		} catch(Throwable e) {
			if(!(e instanceof ClassNotFoundException))
				throw new PluginLoaderException(e.getMessage(), e);
		}
	}

	@Override
	public void config(ServletConfig config) throws Throwable {
		String redis = config.getInitParameter(DEFAULT_REDIS_PARAMETER_NAME);
		if(StringUtils.isNotBlank(redis)) {
			properties = new ArrayList<>();
			String[] paths = redis.split(";");
			for(String path : paths) {
				properties.add(PropertiesLoader.load(path));
			}
		}
	}
}
