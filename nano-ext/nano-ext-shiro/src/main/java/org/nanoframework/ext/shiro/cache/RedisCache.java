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
package org.nanoframework.ext.shiro.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.Session;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.ext.shiro.util.SerializableUtils;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.google.common.collect.Maps;

/**
 * @author yanghe
 * @date 2015年12月10日 下午1:20:59
 */
public class RedisCache implements Cache<Serializable, Session> {
	private Logger LOG = LoggerFactory.getLogger(RedisCache.class);
	private final String redisSourceNames;
	private Map<String, RedisClient> sessions;
	private final String cacheName;
	
	public RedisCache(String redisSourceNames, String cacheName) {
		this.redisSourceNames = redisSourceNames;
		this.cacheName = cacheName;
		
		if(StringUtils.isBlank(redisSourceNames))
			throw new IllegalArgumentException("redisSourceNames must be not blank.");
		
		if(StringUtils.isBlank(cacheName))
			throw new IllegalArgumentException("cacheName must be not blank.");
	}
	
	@Override
	public Session get(Serializable key) throws CacheException {
		initRedisClient();
		for(Entry<String, RedisClient> item : sessions.entrySet()) {
			try {
				RedisClient client = item.getValue();
				return SerializableUtils.deserialize(client.hget(cacheName, (String) key));
			} catch(Exception e) {
				LOG.error("读取Cache异常[get()]["+item.getKey()+"]: " + e.getMessage());
			}
		}
		
		return null;
	}

	@Override
	public Session put(Serializable key, Session value) throws CacheException {
		initRedisClient();
		RedisClient client = sessions.values().iterator().next();
		client.hset(cacheName, (String) key, SerializableUtils.serialize((Session) value));
		return null;
	}

	@Override
	public Session remove(Serializable key) throws CacheException {
		initRedisClient();
		RedisClient client = sessions.values().iterator().next();
		client.hdel(cacheName, (String) key);
		return null;
	}

	@Override
	public void clear() throws CacheException {
		initRedisClient();
		RedisClient client = sessions.values().iterator().next();
		client.del(cacheName);
	}

	@Override
	public int size() {
		initRedisClient();
		for(Entry<String, RedisClient> item : sessions.entrySet()) {
			try {
				RedisClient client = item.getValue();
				return Long.valueOf(client.hlen(cacheName)).intValue();
			} catch(Exception e) {
				LOG.error("读取Cache异常[size()]["+item.getKey()+"]: " + e.getMessage());
			}
		}
		
		return 0;
	}

	@Override
	public Set<Serializable> keys() {
		initRedisClient();
		for(Entry<String, RedisClient> item : sessions.entrySet()) {
			try {
				RedisClient client = item.getValue();
				Set<String> keys = client.hkeys(cacheName);
				if(!CollectionUtils.isEmpty(keys)) {
					Set<Serializable> seriKeys = new LinkedHashSet<>();
					keys.forEach(key -> seriKeys.add(key));
					return seriKeys;
				}
			} catch(Exception e) {
				LOG.error("读取Cache异常[keys()]["+item.getKey()+"]: " + e.getMessage());
			}
		}
		
		return null;
	}

	/** 每次进行校验时都会调用此方法，为了防止在集群环境下每个节点都进行此扫描，特意加上活锁进行访问限制 */
	@Override
	public Collection<Session> values() {
		initRedisClient();
		for(Entry<String, RedisClient> item : sessions.entrySet()) {
			try {
				RedisClient client = item.getValue();
				Collection<String> values = client.hvals(cacheName);
				if(!CollectionUtils.isEmpty(values)) {
					Collection<Session> session = new ArrayList<>();
					values.forEach(value -> session.add(SerializableUtils.deserialize(value)));
					return session;
				}
			} catch(Exception e) {
				LOG.error("读取Cache异常[values()]["+item.getKey()+"]: " + e.getMessage());
			}
		}
		
		return null;
	}
	
	public void setSessions(String redisSourceNames) {
		if(StringUtils.isNotBlank(redisSourceNames)) {
			String[] names = redisSourceNames.split(",");
			Map<String, RedisClient> sessionMap = Maps.newLinkedHashMap();
			for(String name : names) {
				if(StringUtils.isBlank(name))
					continue ;
				
				RedisClient client;
				if((client = GlobalRedisClient.get(name)) != null)
					sessionMap.put(name, client);
			}
			
			this.sessions = sessionMap;
		}
	}
	
	private void initRedisClient() {
		if(CollectionUtils.isEmpty(sessions))
			setSessions(redisSourceNames);
	}
}
