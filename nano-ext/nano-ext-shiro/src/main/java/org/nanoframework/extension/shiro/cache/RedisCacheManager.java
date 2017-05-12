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
package org.nanoframework.extension.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * @author yanghe
 * @since 1.2
 */
public class RedisCacheManager implements CacheManager {
	protected static final String DEFAULT_REDIS_SOURCE_NAME = "shiro";
	protected String redisSourceNames = DEFAULT_REDIS_SOURCE_NAME;
	
	@Override
	public Cache<Object, Object> getCache(String name) throws CacheException {
		return new RedisCache<>(redisSourceNames, name);
	}
	
	public void setRedisSourceNames(String redisSourceNames) {
		this.redisSourceNames = redisSourceNames;
	}

}
