/**
 * Copyright 2015- the original author or authors.
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
package org.nanoframework.orm.jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

/**
 * RedisClient连接池管理类
 * 
 * @author yanghe
 * @date 2015年7月26日 下午10:12:12 
 *
 */
public class RedisClientPool {
	
	private Logger LOG = LoggerFactory.getLogger(RedisClientPool.class);

	// REDIS连接池，可以对应的是操作类型
	private Map<String, ShardedJedisPool> jedisPool = new HashMap<String, ShardedJedisPool>();

	public static RedisClientPool POOL = new RedisClientPool();
	
	private Map<String, RedisConfig> redisConfigs = new LinkedHashMap<>();
	
	public static final String MAIN_REDIS = "classpath:redis.properties";
	
	public RedisClientPool initRedisConfig(List<Properties> redis) throws LoaderException, IOException {
		if(redis == null || redis.isEmpty())
			return this;
		
		return initRedisConfig(redis.toArray(new Properties[redis.size()]));
	}

	public RedisClientPool initRedisConfig(Properties... redis) throws LoaderException, IOException {
		List<Properties> redises = new ArrayList<>();
		if(redis == null || redis.length == 0) 
			redises.add(PropertiesLoader.load(MAIN_REDIS));
		else 
			redises.addAll(Arrays.asList(redis));

		/** 修正了因有多个idx导致的config加载的BUG */
		for(Properties _redis : redises) {
			String root = _redis.getProperty(RedisConfig.ROOT);
			if(StringUtils.isNotEmpty(root)) {
				Map<String, RedisConfig> configs = new LinkedHashMap<>();
				String[] idxs = root.split(",");
				for(String idx : idxs) {
					RedisConfig config = RedisConfig.newInstance();
					for(String name : config._getAttributeNames()) {
						if(RedisConfig.REDIS_TYPE.equals(name))
							Assert.hasLength(_redis.getProperty(RedisConfig.REDIS + idx + "." + name));
						
						config._setAttributeValue(name, _redis.getProperty(RedisConfig.REDIS + idx + "." + name));
					}
					
					configs.put(config.getRedisType(), config);
				}
				
				redisConfigs.putAll(configs);
			}
		}
		
		return this;
		
	}
	
	// 初始化连接池
	public Map<String, ShardedJedisPool> createJedis() {
		redisConfigs.values().forEach(config -> jedisPool.put(config.getRedisType(), createJedisPool(config)));
		
		/** 增加全局RedisClient的绑定 */
		bindGlobal();
		return jedisPool;
		
	}
	
	public void bindGlobal() {
		jedisPool.forEach((type, pool) -> GlobalRedisClient.set(type, new RedisClientImpl(type)));
		LOG.info("RedisClient Pools: " + GlobalRedisClient.keys());
	}

	// 根据连接池名，取得连接
	public ShardedJedis getJedis(String poolName) {
		Assert.hasLength(poolName);
		
		ShardedJedis shardedJedis = null;
		ShardedJedisPool pool = null;
		try {
			pool = jedisPool.get(poolName);
			if (pool != null) 
				shardedJedis = pool.getResource();
			
			Assert.notNull(shardedJedis);
			
			return shardedJedis;
			
		} catch (Throwable e) {
			close(shardedJedis);
			throw new RedisClientException(e.getMessage());
			
		}
	}

	// 创建连接池
	private ShardedJedisPool createJedisPool(RedisConfig config) {
		Assert.notNull(config);
		
		try {
			String[] hostAndports = config.getHostNames().split(";");
			List<String> redisHosts = new ArrayList<String>();
			List<Integer> redisPorts = new ArrayList<Integer>();

			for (int i = 0; i < hostAndports.length; i++) {
				String[] hostPort = hostAndports[i].split(":");
				redisHosts.add(hostPort[0]);
				redisPorts.add(Integer.valueOf(hostPort[1]));
			}

			List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
			for (int i = 0; i < redisHosts.size(); i++) {
				String host = (String) redisHosts.get(i);
				Integer port1 = (Integer) redisPorts.get(i);
				if (config.getTimeOut() > 0) {
					JedisShardInfo si = new JedisShardInfo(host, port1.intValue(), config.getTimeOut());
					shards.add(si);
				} else {
					JedisShardInfo si = new JedisShardInfo(host, port1.intValue());
					shards.add(si);
				}
			}

			return new ShardedJedisPool(getJedisPoolConfig(config), shards, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
		} catch (Exception e) {
			throw new RedisClientException(e.getMessage());
			
		}
		
	}

	// 设置redis连接池的属性
	private JedisPoolConfig getJedisPoolConfig(RedisConfig config) {
		Assert.notNull(config);
		
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(config.getMaxTotal());
		poolConfig.setMaxIdle(config.getMaxIdle());
		poolConfig.setTestOnBorrow(config.getTestOnBorrow());
		return poolConfig;
	}
	
	public Map<String, RedisConfig> getRedisConfigs() {
		return redisConfigs;
		
	}
	
	public RedisConfig getRedisConfig(String redisType) {
		Assert.hasLength(redisType);
		
		RedisConfig config;
		if((config = redisConfigs.get(redisType)) == null)
			throw new RedisClientException("无效的RedisType");
		
		return config;
		
	}

	public void close(ShardedJedis shardedJedis) {
		if(shardedJedis == null)
			return ;
		
		shardedJedis.close();
		
	}
}