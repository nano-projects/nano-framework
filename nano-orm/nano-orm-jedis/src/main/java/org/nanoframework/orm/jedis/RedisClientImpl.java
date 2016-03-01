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

import static org.nanoframework.orm.jedis.RedisClientPool.POOL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.Tuple;

/**
 * RedisClient的实现类，主要实现对Jedis的操作实现封装
 * 
 * @author yanghe
 * @date 2015年7月26日 上午11:01:19 
 *
 */
public class RedisClientImpl implements RedisClient {
	
	private RedisConfig config;
	
	public RedisClientImpl(String type) {
		config = POOL.getRedisConfig(type);
	}
	
	public RedisClientImpl(RedisConfig config) {
		RedisClientPool.POOL.appendJedis(config);
		this.config = config;
	}
	
	/**
	 * FastJson Object to JsonString
	 * 
	 * @param value
	 * @return
	 * 
	 * @see com.alibaba.fastjson.JSON#toJSONString(Object, SerializerFeature...)
	 */
	private String toJSONString(Object value) {
		if(value == null)
			return null;
		
		if(value instanceof String)
			return (String) value;
		
		return JSON.toJSONString(value, SerializerFeature.WriteDateUseDateFormat);
	}
	
	private String[] toJSONString(Object... values) {
		if(values.length == 0)
			return new String[0];
		
		List<String> newValues = new ArrayList<>();
		for(Object value : values) {
			String jsonValue;
			if((jsonValue = toJSONString(value)) != null) 
				newValues.add(jsonValue);
		}
		
		return newValues.toArray(new String[newValues.size()]);
	}
	
	/**
	 * FastJson parse String to Object by TypeReference
	 * 
	 * @param value
	 * @param type
	 * @return
	 * 
	 * @see com.alibaba.fastjson.TypeReference
	 * @see com.alibaba.fastjson.JSON#parseObject(String, TypeReference, com.alibaba.fastjson.parser.Feature...)
	 */
	@SuppressWarnings("unchecked")
	private <T> T parseObject(String value, TypeReference<T> type) {
		if(StringUtils.isEmpty(value))
			return null;
		
		if(type.getType() == String.class)
			return (T) value;
		
		return JSON.parseObject(value, type);
	}
	
	private boolean isOK(String value) {
		return OK.equals(value);
	}
	
	private boolean isSuccess(long value) {
		return value == SUCCESS ? true : false;
	}

	@Override
	public long del(String... keys) {
		if(keys.length == 0)
			return 0;
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			List<Response<Long>> responses = new ArrayList<>();
			for(String key : keys) {
				responses.add(pipeline.del(key));
			}
			
			pipeline.sync();
			
			AtomicLong dels = new AtomicLong(0);
			if(!CollectionUtils.isEmpty(responses)) {
				responses.forEach(res -> dels.addAndGet(res.get()));
			}
			
			return dels.get();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}
	
	@Override
	public long del(List<String> keys) {
		Assert.notNull(keys);
		if(keys.isEmpty())
			return 0;
		
		return del(keys.toArray(new String[keys.size()]));
	}

	@Override
	public boolean exists(String key) {
		Assert.notNull(key);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.exists(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long expire(String key, int seconds) {
		Assert.notNull(key);
		Assert.notNull(seconds);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.expire(key, seconds);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public long expire(String key) {
		Assert.notNull(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.expire(key, config.getExpireTime());
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long expireat(String key, long timestamp) {
		Assert.notNull(key);
		Assert.notNull(timestamp);
		
		/** expireat不是很稳定，对传入的时间处理结果不合理，现在采用timestamp-当前时间戳的方式调用expire来设置时间，效果与expireat一致
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.expireAt(key, timestamp);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		*/
		
		long time = (timestamp - System.currentTimeMillis()) / 1000;
		return expire(key, ((Long) time).intValue());
	}
	
	@Override
	public long ttl(String key) {
		Assert.notNull(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.ttl(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public Set<String> keys(String pattern) {
		Assert.notNull(pattern);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Set<String> keys = new LinkedHashSet<>();
			Collection<Jedis> allShards = jedis.getAllShards();
			for(Jedis _jedis : allShards) {
				keys.addAll(_jedis.keys(pattern));
			}
			
			return keys;
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long append(String key, String value) {
		return append(key, value, DEFAULT_SEPARATOR);
		
	}

	@Override
	public long append(String key, String value, String separator) {
		Assert.notNull(key);
		Assert.notNull(value);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			if(StringUtils.isNotEmpty(separator) && jedis.exists(key))
				value = separator + value;
			
			return jedis.append(key, value);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long append(String key, Object value, String separator) {
		return append(key, toJSONString(value), separator);
	}
	
	public long append(String key, Object value) {
		return append(key, toJSONString(value), DEFAULT_SEPARATOR);
	}

	@Override
	public String get(String key) {
		Assert.notNull(key);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.get(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key, TypeReference<T> type) {
		Assert.notNull(key);
		Assert.notNull(type);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			String value = jedis.get(key);
			
			if(StringUtils.isBlank(value)) {
				if(String.class.getName().equals(type.getType().getTypeName()))
					return (T) value;
				
				return null;
			}
			
			return parseObject(value, type);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public Map<String, String> get(String... keys) {
		Assert.notNull(keys);
		Assert.notEmpty(keys);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipelined = jedis.pipelined();
			Map<String, Response<String>> values = new HashMap<>();
			for(String key : keys)
				values.put(key, pipelined.get(key));
			
			pipelined.sync();
			
			Map<String, String> valueMap = new HashMap<>();
			values.forEach((key, response) -> valueMap.put(key, response.get()));
			
			return valueMap;
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> Map<String, T> get(String[] keys, TypeReference<T> type) {
		Assert.notNull(keys);
		Assert.notEmpty(keys);
		Assert.notNull(type);
		
		Map<String, String> values = get(keys);
		if(values.isEmpty())
			return Collections.emptyMap();
		
		Map<String, T> newValues = new HashMap<>();
		for(Entry<String, String> entry : values.entrySet()) {
			newValues.put(entry.getValue(), parseObject(entry.getValue(), type));
		}
		
		return newValues;
	}

	@Override
	public <T> Map<String, T> get(List<String> keys, TypeReference<T> type) {
		Assert.notNull(keys);
		Assert.notEmpty(keys);
		Assert.notNull(type);
		
		return get(keys.toArray(new String[keys.size()]), type);
	}

	@Override
	public String getset(String key, String value) {
		Assert.notNull(key);
		Assert.notNull(value);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.getSet(key, value);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public boolean set(String key, String value) {
		Assert.notNull(key);
		Assert.notNull(value);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return isOK(jedis.set(key, value));
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public boolean set(String key, Object value) {
		Assert.notNull(key);
		Assert.notNull(value);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return isOK(jedis.set(key, toJSONString(value)));
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}

	@Override
	public Map<String, Boolean> set(Map<String, Object> map) {
		Assert.notNull(map);
		Assert.notEmpty(map);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipelined = jedis.pipelined();
			Map<String, Response<String>> responses = new HashMap<>();
			map.forEach((key, value) -> responses.put(key, pipelined.set(key, toJSONString(value))));
			pipelined.sync();
			
			Map<String, Boolean> values = new HashMap<>();
			responses.forEach((key, response) -> values.put(key, isOK(response.get())));
			return values;
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long setByNX(String key, String value) {
		Assert.notNull(key);
		Assert.notNull(value);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.setnx(key, value);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long setByNX(String key, Object value) {
		return setByNX(key, toJSONString(value));
	}

	@Override
	public Map<String, Long> setByNX(Map<String, Object> map) {
		Assert.notNull(map);
		Assert.notEmpty(map);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipelined = jedis.pipelined();
			Map<String, Response<Long>> responses = new HashMap<>();
			for(Entry<String, Object> entry : map.entrySet()) {
				responses.put(entry.getKey(), pipelined.setnx(entry.getKey(), toJSONString(entry.getValue())));
			}
			
			pipelined.sync();
			
			Map<String, Long> values = new HashMap<>();
			responses.forEach((key, response) -> values.put(key, response.get()));
			return values;
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public boolean setByEX(String key, String value) {
		return setByEX(key, value, config.getExpireTime());
		
	}
	
	@Override
	public boolean setByEX(String key, Object value) {
		return setByEX(key, toJSONString(value), config.getExpireTime());
		
	}

	@Override
	public boolean setByEX(String key, String value, int seconds) {
		Assert.notNull(key);
		Assert.notNull(value);

		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return isOK(jedis.setex(key, seconds, value));
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public boolean setByEX(String key, Object value, int seconds) {
		Assert.notNull(key);
		Assert.notNull(value);

		return setByEX(key, toJSONString(value), seconds);
	}

	@Override
	public long strLen(String key) {
		Assert.notNull(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.strlen(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long hdel(String key, String... fields) {
		Assert.notNull(key);
		Assert.notNull(fields);
		Assert.notEmpty(fields);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.hdel(key, fields);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public boolean hexists(String key, String field) {
		Assert.notNull(key);
		Assert.notNull(field);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.hexists(key, field);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public String hget(String key, String field) {
		Assert.notNull(key);
		Assert.notNull(field);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.hget(key, field);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public Map<String, String> hmget(String key, String... fields) {
		Assert.notNull(key);
		Assert.notNull(fields);
		Assert.notEmpty(fields);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			List<String> values = jedis.hmget(key, fields);
			Map<String, String> valuesMap = new LinkedHashMap<>();
			for(int idx = 0; idx < values.size(); idx ++) {
				String value;
				if((value = values.get(idx)) != null)
					valuesMap.put(fields[idx], value);
			}
			
			return valuesMap;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> T hget(String key, String field, TypeReference<T> type) {
		Assert.notNull(key);
		Assert.notNull(field);
		Assert.notNull(type);
		
		return parseObject(hget(key, field), type);
	}

	@Override
	public <T> Map<String, T> hmget(String key, String[] fields, TypeReference<T> type) {
		Assert.notNull(type);
		
		Map<String, String> map = hmget(key, fields);
		if(map == null || map.isEmpty())
			return null;
		
		Map<String, T> values = new HashMap<>();
		for(Entry<String, String> entry : map.entrySet()) {
			values.put(entry.getKey(), parseObject(entry.getValue(), type));
		}
		
		return values;
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		Assert.notNull(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.hgetAll(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> Map<String, T> hgetAll(String key, TypeReference<T> type) {
		Assert.notNull(type);
		
		Map<String, String> map = hgetAll(key);
		if(map == null || map.isEmpty())
			return null;
		
		Map<String, T> values = new HashMap<>();
		for(Entry<String, String> entry : map.entrySet()) {
			values.put(entry.getKey(), parseObject(entry.getValue(), type));
		}
		
		return values;
	}

	@Override
	public Set<String> hkeys(String key) {
		Assert.notNull(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.hkeys(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> Set<T> hkeys(String key, TypeReference<T> type) {
		Set<String> keys = hkeys(key);
		if(!CollectionUtils.isEmpty(keys)) {
			Set<T> sets = Sets.newLinkedHashSet();
			keys.forEach(item -> sets.add(parseObject(item, type)));
			return sets;
		}
		
		return Collections.emptySet();
	}

	@Override
	public long hlen(String key) {
		Assert.notNull(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.hlen(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public boolean hset(String key, String field, String value) {
		Assert.notNull(key);
		Assert.notNull(field);
		Assert.notNull(value);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return isSuccess(jedis.hset(key, field, value));
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public boolean hset(String key, String field, Object value) {
		return hset(key, field, toJSONString(value));
		
	}

	@Override
	public boolean hmset(String key, Map<String, Object> map) {
		Assert.notNull(key);
		Assert.notNull(map);
		Assert.notEmpty(map);
		
		ShardedJedis jedis = null;
		try{
			Map<String, String> newMap = new HashMap<>();
			map.forEach((field, value) -> newMap.put(field, toJSONString(value)));
			
			jedis = POOL.getJedis(config.getRedisType());
			return isOK(jedis.hmset(key, newMap));
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}

	@Override
	public boolean hsetByNX(String key, String field, String value) {
		Assert.notNull(key);
		Assert.notNull(field);
		Assert.notNull(value);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return isSuccess(jedis.hsetnx(key, field, value));
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}

	@Override
	public boolean hsetByNX(String key, String field, Object value) {
		return hsetByNX(key, field, toJSONString(value));
	}

	@Override
	public Map<String, Boolean> hsetByNX(String key, Map<String, Object> map) {
		Assert.notNull(key);
		Assert.notNull(map);
		Assert.notEmpty(map);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			Map<String, Response<Long>> responses = new HashMap<>();
			for(Entry<String, Object> entry : map.entrySet()) {
				responses.put(entry.getKey(), pipeline.hsetnx(key, entry.getKey(), toJSONString(entry.getValue())));
			}
			
			pipeline.sync();
			
			Map<String, Boolean> values = new HashMap<>();
			responses.forEach((field, response) -> values.put(field, isSuccess(response.get())));
			
			return values;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public List<String> hvals(String key) {
		Assert.notNull(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.hvals(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> List<T> hvals(String key, TypeReference<T> type) {
		List<String> vals = hvals(key);
		if(!CollectionUtils.isEmpty(vals)) {
			List<T> lists = Lists.newArrayList();
			vals.forEach(item -> lists.add(parseObject(item, type)));
			return lists;
		}
		
		return Collections.emptyList();
	}

	@Override
	public String bpop(String key, Mark pop) {
		return bpop(key, 0, pop);
	}
	
	@Override
	public <T> T bpop(String key, Mark pop, TypeReference<T> type) {
		return bpop(key, 0, pop, type);
		
	}

	@Override
	public String bpop(String key, int timeout, Mark type) {
		Assert.hasLength(key);
		Assert.notNull(timeout);
		Assert.notNull(type);
		
		ShardedJedis jedis = null;
		try {
			jedis = POOL.getJedis(config.getRedisType());
			List<String> values = null;
			switch(type) {
				case LPOP:
					values = jedis.blpop(timeout, key);
					break;
					
				case RPOP:
					values = jedis.brpop(timeout, key);
					break;
					
				default:
					throw new RedisClientException("Unknown Pop type");
			}
			
			if(values != null && !values.isEmpty()) {
				return values.get(1);
			}
			
			return null;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}
	
	@Override
	public <T> T bpop(String key, int timeout, Mark pop, TypeReference<T> type) {
		String value = bpop(key, timeout, pop);
		if(StringUtils.isNotEmpty(value)) {
			return parseObject(value, type);
		} 
		
		return null;
	}

	@Override
	public Map<String, String> bpop(String[] keys, Mark pop) {
		Assert.notNull(keys);
		Assert.notEmpty(keys);
		Assert.notNull(pop);
		
		ShardedJedis jedis = null;
		try {
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			
			Map<String, String> map = new HashMap<>();
			List<Response<List<String>>> responses = new ArrayList<>();
			switch(pop) {
				case LPOP:
					for(String key : keys) {
						responses.add(pipeline.blpop(key));
					}
					break;
					
				case RPOP:
					for(String key : keys) {
						responses.add(pipeline.brpop(key));
					}
					
					break;
					
				default:
					throw new RedisClientException("Unknown Pop type");
			}
			
			pipeline.sync();
			
			responses.forEach(response -> {
				List<String> values = response.get();
				if(values != null && !values.isEmpty())
					map.put(values.get(0), values.get(1));
				
			});
			
			return map;
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}

	@Override
	public <T> Map<String, T> bpop(String[] keys, Mark pop, TypeReference<T> type) {
		Map<String, String> valuesMap = bpop(keys, pop);
		if(valuesMap != null && !valuesMap.isEmpty()) {
			Map<String, T> newMap = new HashMap<>();
			for(Entry<String, String> entry : valuesMap.entrySet()) {
				newMap.put(entry.getKey(), parseObject(entry.getValue(), type));
			}
			
			return newMap;
		}
		
		return Collections.emptyMap();
	}
	
	@Override
	public Map<String, String> bpop(String[] keys, int timeout, Mark pop) {
		Assert.notNull(keys);
		Assert.notEmpty(keys);
		Assert.notNull(timeout);
		Assert.notNull(pop);
		
		ShardedJedis jedis = null;
		try {
			jedis = POOL.getJedis(config.getRedisType());
			Map<String, String> values = new HashMap<>();
			switch(pop) {
				case LPOP:
					for(String key : keys) {
						List<String> value = jedis.blpop(timeout, key);
						if(value != null && !value.isEmpty())
							values.put(value.get(0), value.get(1));
						
					}
					
					return values;
					
				case RPOP:
					for(String key : keys) {
						List<String> value = jedis.brpop(timeout, key);
						if(value != null && !value.isEmpty())
							values.put(value.get(0), value.get(1));
						
					}
					
					return values;
					
				default:
					throw new RedisClientException("Unknown Pop type");
			}
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}
	
	@Override
	public <T> Map<String, T> bpop(String[] keys, int timeout, Mark pop, TypeReference<T> type) {
		Map<String, String> values = bpop(keys, timeout, pop);
		if(values != null && !values.isEmpty()) {
			Map<String, T> newMap = new HashMap<>();
			for(Entry<String, String> entry : values.entrySet()) {
				newMap.put(entry.getKey(), parseObject(entry.getValue(), type));
			}
			
			return newMap;
		}
		
		return Collections.emptyMap();
	}

	@Override
	public String brpoplpush(String source, String destination) {
		return brpoplpush(source, destination, 0);
	}

	@Override
	public <T> T brpoplpush(String source, String destination, TypeReference<T> type) {
		return brpoplpush(source, destination, 0, type);
		
	}

	@Override
	public String brpoplpush(String source, String destination, int timeout) {
		Assert.hasLength(source);
		Assert.hasLength(destination);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			if(jedis.getAllShards().size() == 1)
				return jedis.getAllShards().iterator().next().brpoplpush(source, destination, timeout);
			
			else 
				throw new RedisClientException("不支持Sharding的模式进行brpoplpush操作，如果只配置一个节点则支持此操作.");
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> T brpoplpush(String source, String destination, int timeout, TypeReference<T> type) {
		String value = brpoplpush(source, destination, timeout);
		if(StringUtils.isNotEmpty(value)) 
			return parseObject(value, type);
		
		return null;
		
	}

	@Override
	public String lindex(String key, int index) {
		Assert.hasLength(key);
		Assert.notNull(index);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.lindex(key, index);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> T lindex(String key, int index, TypeReference<T> type) {
		String value = lindex(key, index);
		if(StringUtils.isNotEmpty(value))
			return parseObject(value, type);
		
		return null;
	}

	@Override
	public long linsert(String key, String pivot, String value, Mark position) {
		Assert.hasLength(key);
		Assert.notNull(value);
		Assert.notNull(pivot);
		Assert.notNull(position);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			switch(position) {
				case BEFORE:
					return jedis.linsert(key, LIST_POSITION.BEFORE, pivot, value);
					
				case AFTER:
					return jedis.linsert(key, LIST_POSITION.AFTER, pivot, value);
					
				default:
					throw new RedisClientException("Unknown pivot type");
			}
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long linsert(String key, String pivot, Object value, Mark position) {
		return linsert(key, pivot, toJSONString(value), position);
	}

	@Override
	public long llen(String key) {
		Assert.hasLength(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.llen(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public String pop(String key, Mark pop) {
		Assert.hasLength(key);
		Assert.notNull(pop);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			switch(pop) {
				case LPOP:
					return jedis.lpop(key);
					
				case RPOP:
					return jedis.rpop(key);
				
				default:
					throw new RedisClientException("Unknown pop type");
			}
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public List<String> pop(String key, int count) {
		Assert.hasLength(key);
		
		Mark pop;
		if(count >= 0)
			pop = Mark.LPOP;
		else {
			pop = Mark.RPOP;
			count = count * -1;
		}
		
		ShardedJedis jedis = null;
		try{
			long len = llen(key);
			if(count > len)
				count = ((Long) len).intValue();
			
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			List<Response<String>> responses = new ArrayList<>();
			switch(pop) {
				case LPOP:
					for(int idx = 0; idx <= count; idx ++)
						responses.add(pipeline.lpop(key));
					
					break;
				case RPOP:
					for(int idx = 0; idx <= count; idx ++)
						responses.add(pipeline.rpop(key));
				
					break;
				default:
					throw new RedisClientException("Unknown pop type");
			}
			
			pipeline.sync();
			
			List<String> values = new ArrayList<>();
			responses.forEach(response -> values.add(response.get()));
			
			return values;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> T pop(String key, Mark pop, TypeReference<T> type) {
		String value = pop(key, pop);
		if(StringUtils.isNotEmpty(value))
			return parseObject(key, type);
		
		return null;
	}
	
	@Override
	public <T> List<T> pop(String key, int count, TypeReference<T> type) {
		Assert.notNull(type);
		List<String> values = pop(key, count);
		if(!values.isEmpty()) {
			List<T> newValues = new ArrayList<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptyList();
	}

	@Override
	public boolean push(String key, String[] values, Mark push) {
		if(values == null || values.length == 0)
			return false;
		
		Assert.hasLength(key);
		Assert.notNull(push);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			switch(push) {
				case LPUSH:
					jedis.lpush(key, values);
					break;
				case RPUSH:
					jedis.rpush(key, values);
					break;
				default:
					throw new RedisClientException("未知的写入(PUSH)类型");
			}
			
			return true;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public boolean push(String key, String value, Mark push) {
		if(StringUtils.isEmpty(value))
			return false;
		
		return push(key, new String[] { value }, push);
	}

	@Override
	public boolean push(String key, Object[] values, Mark push) {
		if(values == null || values.length == 0)
			return false;
		
		List<String> newValues = new ArrayList<>();
		for(Object value : values) {
			if(value == null)
				continue ;
			
			newValues.add(toJSONString(value));
		}
		
		return push(key, newValues.toArray(new String[newValues.size()]), push);
	}

	@Override
	public boolean push(String key, Object value, Mark push) {
		if(value == null)
			return false;
		
		return push(key, new String[] { toJSONString(value)}, push);
		
	}
	
	@Override
	public boolean push(String key, List<Object> values, Mark push) {
		if(values == null || values.size() == 0)
			return false;
		
		List<String> newValues = new ArrayList<>();
		for(Object value : values) {
			if(value == null)
				continue ;
			
			newValues.add(toJSONString(value));
		}
		
		return push(key, newValues.toArray(new String[newValues.size()]), push);
	}
	
	@Override
	public boolean push(String key, String scanKey, String value, Mark push, Mark policy) {
		Assert.hasLength(key);
		Assert.hasLength(scanKey);
		Assert.hasLength(value);
		Assert.notNull(push);
		Assert.notNull(policy);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			
			switch(push) {
				case LPUSH:
					switch(policy) {
						case KEY: 
							pipeline.lpush(key, scanKey);
							break;
						case VALUE: 
							pipeline.lpush(key, value);
							break;
						default: 
							throw new RedisClientException("未知的策略(policy)类型");
					}
					
					break;
				case RPUSH:
					switch(policy) {
						case KEY: 
							pipeline.rpush(key, scanKey);
							break;
						case VALUE: 
							pipeline.rpush(key, value);
							break;
						default: 
							throw new RedisClientException("未知的策略(policy)类型");
					}
					
					break;
				default:
					throw new RedisClientException("未知的写入(PUSH)类型");
			}
			
			Response<String> okResponse = pipeline.set(scanKey, value);
			
			pipeline.sync();
			
			return isOK(okResponse.get());
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public boolean push(String key, String scanKey, Object value, Mark push, Mark policy) {
		return push(key, scanKey, toJSONString(value), push, policy);
	}
	
	@Override
	public Map<String, Boolean> push(String key, Map<String, Object> scanMap, Mark push, Mark policy) {
		Assert.hasLength(key);
		Assert.notEmpty(scanMap);
		Assert.notNull(push);
		Assert.notNull(policy);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			
			Map<String, Response<String>> okResponses = new HashMap<>();
			for(Entry<String, Object> entry : scanMap.entrySet()) {
				switch(push) {
					case LPUSH:
						switch(policy) {
							case KEY: 
								pipeline.lpush(key, entry.getKey());
								break;
							case VALUE: 
								pipeline.lpush(key, toJSONString(entry.getValue()));
								break;
							default: 
								throw new RedisClientException("未知的策略(policy)类型");
						}
						
						break;
					case RPUSH:
						switch(policy) {
						case KEY: 
							pipeline.rpush(key, entry.getKey());
							break;
						case VALUE: 
							pipeline.rpush(key, toJSONString(entry.getValue()));
							break;
							default: 
								throw new RedisClientException("未知的策略(policy)类型");
						}
						
						break;
					default:
						throw new RedisClientException("未知的写入(PUSH)类型");
				}
				
				okResponses.put(entry.getKey(), pipeline.set(entry.getKey(), toJSONString(entry.getValue())));
			
			}
			
			pipeline.sync();
			
			Map<String, Boolean> values = new HashMap<>();
			okResponses.forEach((scanKey, okResponse) -> values.put(scanKey, isOK(okResponse.get())));
			return values;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}

	@Override
	public void pushx(String key, String[] values, Mark push) {
		Assert.hasLength(key);
		Assert.notNull(values);
		Assert.notEmpty(values);
		Assert.notNull(push);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			switch(push) {
				case LPUSH:
					pipeline.lpushx(key, values);
					
					break;
					
				case RPUSH:
					pipeline.rpushx(key, values);
					break;
					
				default:
					throw new RedisClientException("未知的写入(PUSH)类型");
			}
			
			pipeline.sync();
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public void pushx(String key, String value, Mark push) {
		Assert.notNull(value);
		pushx(key, new String[] { value }, push);
	}

	@Override
	public void pushx(String key, Object[] values, Mark push) {
		Assert.notNull(values);
		Assert.notEmpty(values);
		List<String> newValues = new ArrayList<>();
		for(Object value : values) {
			if(value == null)
				continue ;
		
			newValues.add(toJSONString(value));
		}
		
		pushx(key, newValues.toArray(new String[newValues.size()]), push);
		
	}

	@Override
	public void pushx(String key, Object value, Mark push) {
		Assert.notNull(value);
		pushx(key, toJSONString(value), push);
		
	}

	@Override
	public List<String> lrange(String key, int start, int end) {
		Assert.hasLength(key);
		Assert.notNull(start);
		Assert.notNull(end);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.lrange(key, start, end);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public List<String> lrange(String key, int count) {
		return lrange(key, 0, count);
	}
	
	@Override
	public List<String> lrange(String key) {
		return lrange(key, 0, -1);
	}

	@Override
	public <T> List<T> lrange(String key, int start, int end, TypeReference<T> type) {
		Assert.notNull(type);
		
		List<String> values = lrange(key, start, end);
		if(values != null && !values.isEmpty()) {
			List<T> newValues = new ArrayList<>();
			for(String value : values) {
				if(StringUtils.isEmpty(value))
					continue ;
				
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public <T> List<T> lrange(String key, int count, TypeReference<T> type) {
		return lrange(key, 0, count, type);
	}
	
	@Override
	public <T> List<T> lrange(String key, TypeReference<T> type) {
		return lrange(key, 0, -1, type);
	}
	
	@Override
	public List<String> lrangeltrim(String key, int count) {
		Assert.hasLength(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			Response<List<String>> values = pipeline.lrange(key, 0, count);
			pipeline.ltrim(key, count + 1, -1);
			pipeline.sync();
			return values.get();
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> List<T> lrangeltrim(String key, int count, TypeReference<T> type) {
		Assert.notNull(type);
		
		List<String> values = lrangeltrim(key, count);
		if(!values.isEmpty()) {
			List<T> newValues = new ArrayList<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public long lrem(String key, int count, String value) {
		Assert.hasLength(key);
		Assert.notNull(count);
		Assert.notNull(value);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.lrem(key, count, value);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
		
	}

	@Override
	public long lrem(String key, String value) {
		return lrem(key, 0, value);
	}

	@Override
	public long lrem(String key, int count, Object value) {
		return lrem(key, count, toJSONString(value));
	}

	@Override
	public long lrem(String key, Object value) {
		return lrem(key, 0, value);
	}

	@Override
	public String lset(String key, int index, String value) {
		Assert.hasLength(key);
		Assert.notNull(index);
		Assert.notNull(value);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.lset(key, index, value);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> T lset(String key, int index, T value, TypeReference<T> type) {
		return parseObject(lset(key, index, toJSONString(value)), type);
	}

	@Override
	public String ltrim(String key, int start, int stop) {
		Assert.hasLength(key);
		Assert.notNull(start);
		Assert.notNull(stop);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.ltrim(key, start, stop);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long sadd(String key, String... members) {
		Assert.hasLength(key);
		if(members.length == 0)
			return 0;
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.sadd(key, members);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public long sadd(String key, Object... members) {
		if(members.length == 0)
			return 0;
		
		List<String> newMembers = new ArrayList<>();
		for(Object member : members) {
			if(member != null)
				newMembers.add(toJSONString(member));
		}
		
		return sadd(key, newMembers.toArray(new String[newMembers.size()]));
	}
	
	@Override
	public long sreplace(String key, String[] oldMembers, String[] newMembers) {
		Assert.hasLength(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			if(oldMembers != null && oldMembers.length > 0) 
				pipeline.srem(key, oldMembers);
			
			Response<Long> response = null;
			if(newMembers != null && newMembers.length > 0)
				response = pipeline.sadd(key, newMembers);
			
			pipeline.sync();
			
			if(response != null)
				return response.get();
			
			return 0;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public long sreplace(String key, Object[] oldMembers, Object[] newMembers) {
		return sreplace(key, toJSONString(oldMembers), toJSONString(newMembers));
	}
	
	@Override
	public long sreplace(String key, Collection<Object> oldMembers, Collection<Object> newMembers) {
		Object[] olds = oldMembers == null || oldMembers.size() == 0 ? new Object[0] : oldMembers.toArray(new Object[oldMembers.size()]);
		Object[] news = newMembers == null || newMembers.size() == 0 ? new Object[0] : newMembers.toArray(new Object[newMembers.size()]);
		return sreplace(key, olds, news);
	}

	@Override
	public long scard(String key) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.scard(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public Map<String, Long> scard(String... keys) {
		Assert.notEmpty(keys);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			ShardedJedisPipeline pipeline = jedis.pipelined();
			Map<String, Response<Long>> responses = new HashMap<>();
			for(String key : keys) {
				responses.put(key, pipeline.scard(key));
			}
			
			pipeline.sync();
			Map<String, Long> values = new HashMap<>();
			responses.forEach((key, response) -> values.put(key, response.get()));
			
			return values;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public Map<String, Long> scard(Collection<String> keys) {
		Assert.notNull(keys);
		Assert.notEmpty(keys);
		return scard(keys.toArray(new String[keys.size()]));
	}

	@Override
	public Set<String> sdiff(String... keys) {
		Assert.notEmpty(keys);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Collection<Jedis>  allShards;
			if((allShards = jedis.getAllShards()).size() == 1) {
				return allShards.iterator().next().sdiff(keys);
			} else if(allShards.size() > 1) {
				Set<String> unionSet = new LinkedHashSet<>();
				Set<String> diffSet = new LinkedHashSet<>();
				allShards.forEach(shard -> { 
					Set<String> diff = shard.sdiff(keys);
					if(!unionSet.isEmpty()) {
						diff.stream().filter(item -> !unionSet.contains(item)).forEach(item -> diffSet.add(item));
					} else {
						diffSet.addAll(diff);
					}
					
					unionSet.addAll(diff);
				});
				
				return diffSet;
			} 
				
			return Collections.emptySet();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}

	@Override
	public <T> Set<T> sdiff(String[] keys, TypeReference<T> type) {
		Assert.notNull(type);
		Set<String> values = sdiff(keys);
		if(!values.isEmpty()) {
			Set<T> newValues = new LinkedHashSet<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}

	@Override
	public <T> Set<T> sdiff(Collection<String> keys, TypeReference<T> type) {
		Assert.notNull(keys);
		Assert.notEmpty(keys);
		return sdiff(keys.toArray(new String[keys.size()]), type);
	}

	@Override
	public long sdiffstore(String destination, String... keys) {
		Assert.notEmpty(keys);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Collection<Jedis>  allShards;
			if((allShards = jedis.getAllShards()).size() == 1) {
				return allShards.iterator().next().sdiffstore(destination, keys);
				
			} else if(allShards.size() > 1) {
				Set<String> diffSet = sdiff(keys);
				if(!diffSet.isEmpty()) {
					ShardedJedisPipeline pipeline = jedis.pipelined();
					pipeline.del(destination);
					Response<Long> response = pipeline.sadd(destination, diffSet.toArray(new String[diffSet.size()]));
					pipeline.sync();
					return response.get();
					
				}
			} 
			
			return 0;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public Set<String> sinter(String... keys) {
		Assert.notEmpty(keys);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Collection<Jedis>  allShards;
			if((allShards = jedis.getAllShards()).size() == 1) {
				return allShards.iterator().next().sinter(keys);
			} else if(allShards.size() > 1) {
				Set<String> diffSet = new LinkedHashSet<>();
				Set<String> interSet = new LinkedHashSet<>();
				for(String key : keys) {
					Set<String> now;
					diffSet.addAll(now = jedis.smembers(key));
					now.stream().filter(item -> diffSet.contains(item)).forEach(item -> interSet.add(item));
				}
				
				return interSet;
			} 

			return Collections.emptySet();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> Set<T> sinter(String[] keys, TypeReference<T> type) {
		Assert.notNull(type);
		
		Set<String> interSet = sinter(keys);
		if(!interSet.isEmpty()) {
			Set<T> newInterSet = new LinkedHashSet<>();
			for(String value : interSet) {
				newInterSet.add(parseObject(value, type));
			}
			
			return newInterSet;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Set<T> sinter(Collection<String> keys, TypeReference<T> type) {
		Assert.notNull(keys);
		return sinter(keys.toArray(new String[keys.size()]), type);
	}
	
	@Override
	public long sinterstore(String destination, String... keys) {
		Assert.hasLength(destination);
		if(keys.length == 0)
			return 0;
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Collection<Jedis>  allShards;
			if((allShards = jedis.getAllShards()).size() == 1) {
				return allShards.iterator().next().sinterstore(destination, keys);
				
			} else if(allShards.size() > 1) {
				Set<String> interSet = sinter(keys);
				if(!interSet.isEmpty()) {
					ShardedJedisPipeline pipeline = jedis.pipelined();
					pipeline.del(destination);
					Response<Long> response = pipeline.sadd(destination, interSet.toArray(new String[interSet.size()]));
					pipeline.sync();
					return response.get();
					
				}
			} 
			
			return 0;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public boolean sismember(String key, String member) {
		Assert.hasLength(key);
		Assert.notNull(member);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.sismember(key, member);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public boolean sismember(String key, Object member) {
		Assert.notNull(member);
		return sismember(key, toJSONString(member));
	}
	
	@Override
	public Set<String> smembers(String key) {
		Assert.hasLength(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.smembers(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> Set<T> smembers(String key, TypeReference<T> type) {
		Assert.notNull(type);
		Set<String> members = smembers(key);
		if(!members.isEmpty()) {
			Set<T> newMembers = new LinkedHashSet<>();
			for(String member : members) {
				newMembers.add(parseObject(member, type));
			}
			
			return newMembers;
		}
			
		return Collections.emptySet();
	}
	
	@Override
	public boolean smove(String source, String destination, String member) {
		Assert.hasLength(source);
		Assert.hasLength(destination);
		Assert.notNull(member);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Collection<Jedis>  allShards;
			if((allShards = jedis.getAllShards()).size() == 1) {
				return isSuccess(allShards.iterator().next().smove(source, destination, member));
			} else if(allShards.size() > 1) {
				AtomicLong val = new AtomicLong();
				allShards.parallelStream().forEach(shard -> {
					Pipeline pipeline = shard.pipelined();
					pipeline.sismember(source, member);
					Response<Long> response = pipeline.smove(source, destination, member);
					pipeline.sync();
					val.addAndGet(response.get());
				});
				
				if(val.get() > 0)
					return true;
			}
			
			return false;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public Map<String, Boolean> smove(String source, String destination, String... members) {
		Assert.notEmpty(members);
		Map<String, Boolean> values = new HashMap<>();
		for(String member : members) {
			values.put(member, smove(source, destination, member));
		}
		
		return values;
	}
	
	@Override
	public boolean smove(String source, String destination, Object member) {
		Assert.notNull(member);
		return smove(source, destination, toJSONString(member));
		
	}
	
	@Override
	public Map<Object, Boolean> smove(String source, String destination, Object... members) {
		Assert.notEmpty(members);
		Map<Object, Boolean> values = new HashMap<>();
		for(Object member : members) {
			values.put(member, smove(source, destination, member));
		}
		
		return values;
	}
	
	@Override
	public String spop(String key) {
		Assert.hasLength(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.spop(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> T spop(String key, TypeReference<T> type) {
		Assert.notNull(type);
		return parseObject(spop(key), type);
	}
	
	@Override
	public Set<String> spop(String key, int count) {
		Assert.hasLength(key);
		Assert.notNull(count);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.spop(key, count);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> Set<T> spop(String key, int count, TypeReference<T> type) {
		Assert.notNull(type);
		Set<String> values = spop(key, count);
		if(!values.isEmpty()) {
			Set<T> newValues = new LinkedHashSet<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public String srandmember(String key) {
		Assert.hasLength(key);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.srandmember(key);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> T srandmember(String key, TypeReference<T> type) {
		Assert.notNull(type);
		return parseObject(srandmember(key), type);
	}
	
	@Override
	public List<String> srandmember(String key, int count) {
		Assert.hasLength(key);
		Assert.notNull(count);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.srandmember(key, count);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> List<T> srandmember(String key, int count, TypeReference<T> type) {
		Assert.notNull(type);
		List<String> values = srandmember(key, count);
		if(!values.isEmpty()) {
			List<T> newValues = new ArrayList<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public long srem(String key, String... members) {
		Assert.hasLength(key);
		
		if(members.length == 0)
			return 0;
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.srem(key, members);
			
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public long srem(String key, Object... members) {
		if(members.length == 0)
			return 0;
		
		Set<String> newMembers = new LinkedHashSet<>();
		for(Object member : members) {
			newMembers.add(toJSONString(member));
		}
		
		return srem(key, newMembers.toArray(new String[newMembers.size()]));
	}
	
	@Override
	public Set<String> sunion(String... keys) {
		Assert.notEmpty(keys);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Collection<Jedis>  allShards;
			if((allShards = jedis.getAllShards()).size() == 1) {
				return allShards.iterator().next().sdiff(keys);
			} else if(allShards.size() > 1) {
				Set<String> unionSet = new LinkedHashSet<>();
				allShards.forEach(shard -> unionSet.addAll(shard.sunion(keys)));
				return unionSet;
			} 
				
			return Collections.emptySet();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public <T> Set<T> sunion(String[] keys, TypeReference<T> type) {
		Assert.notNull(type);
		Set<String> values = sunion(keys);
		if(!values.isEmpty()) {
			Set<T> newValues = new LinkedHashSet<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public long sunionstore(String destination, String... keys) {
		Assert.hasLength(destination);
		if(keys.length == 0)
			return 0;
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Collection<Jedis>  allShards;
			if((allShards = jedis.getAllShards()).size() == 1) {
				return allShards.iterator().next().sunionstore(destination, keys);
				
			} else if(allShards.size() > 1) {
				Set<String> unionSet = sunion(keys);
				if(!unionSet.isEmpty()) {
					ShardedJedisPipeline pipeline = jedis.pipelined();
					pipeline.del(destination);
					Response<Long> response = pipeline.sadd(destination, unionSet.toArray(new String[unionSet.size()]));
					pipeline.sync();
					return response.get();
					
				}
			} 
			
			return 0;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
			
		} finally {
			POOL.close(jedis);
			
		}
	}
	
	@Override
	public long zadd(String key, double score, String member) {
		Assert.hasLength(key);
		Assert.notNull(member);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zadd(key, score, member);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> long zadd(String key, double score, T member) {
		Assert.notNull(member);
		return zadd(key, score, toJSONString(member));
	}
	
	@Override
	public <T> long zadd(String key, Map<T, Double> values) {
		Assert.hasLength(key);
		Assert.notEmpty(values);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			long changed = 0;
			for(Entry<T, Double> value : values.entrySet()) {
				changed += zadd(key, value.getValue(), value.getKey());
			}
			
			return changed;
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zcard(String key) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zcard(key);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zcount(String key, double min, double max) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zcount(key, min, max);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zcount(String key) {
		return zcount(key, INF0, INF1);
	}
	
	@Override
	public long zcount(String key, String min, String max) {
		Assert.hasLength(key);
		Assert.hasLength(min);
		Assert.hasLength(max);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zcount(key, min, max);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zlexcount(String key, String min, String max) {
		Assert.hasLength(key);
		Assert.hasLength(min);
		Assert.hasLength(max);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zlexcount(key, min, max);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public double zincrby(String key, double increment, String member) {
		Assert.hasLength(key);
		Assert.hasLength(member);
		
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zincrby(key, increment, member);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> double zincrby(String key, double increment, T member) {
		Assert.notNull(member);
		return zincrby(key, increment, toJSONString(member));
	}
	
	@Override
	public Set<String> zrange(String key, long start, long end) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrange(key, start, end);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public Set<String> zrange(String key, long end) {
		return zrange(key, 0, end);
	}
	
	@Override
	public Set<String> zrange(String key) {
		return zrange(key, 0, -1);
	}
	
	@Override
	public <T> Set<T> zrange(String key, long start, long end, TypeReference<T> type) {
		Assert.notNull(type);
		Set<String> values = zrange(key, start, end);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>(values.size());
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Set<T> zrange(String key, long end, TypeReference<T> type) {
		return zrange(key, 0, end, type);
	}
	
	@Override
	public <T> Set<T> zrange(String key, TypeReference<T> type) {
		return zrange(key, 0, -1, type);
	}
	
	@Override
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrangeByLex(key, min, max, offset, count);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Set<T> zrangeByLex(String key, String min, String max, int offset, int count, TypeReference<T> type) {
		Set<String> values = zrangeByLex(key, min, max, offset, count);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>(values.size());
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Map<T, Double> zrangeWithScores(String key, long start, long end, TypeReference<T> type) {
		Assert.hasLength(key);
		Assert.notNull(type);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Set<Tuple> values = jedis.zrangeWithScores(key, start, end);
			if(!CollectionUtils.isEmpty(values)) {
				Map<T, Double> newValues = new HashMap<>();
				for(Tuple value : values) {
					newValues.put(parseObject(value.getElement(), type), value.getScore());
				}
				
				return newValues;
			}
			
			return Collections.emptyMap();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Map<T, Double> zrangeWithScores(String key, long end, TypeReference<T> type) {
		return zrangeWithScores(key, 0, end, type);
	}
	
	@Override
	public <T> Map<T, Double> zrangeWithScores(String key, TypeReference<T> type) {
		return zrangeWithScores(key, 0, -1, type);
	}
	
	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		return zrangeByScore(key, String.valueOf(min), String.valueOf(max));
	}
	
	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return zrangeByScore(key, String.valueOf(min), String.valueOf(max), offset, count);
	}
	
	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrangeByScore(key, min, max);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrangeByScore(key, min, max, offset, count);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Set<T> zrangeByScore(String key, double min, double max, TypeReference<T> type) {
		return zrangeByScore(key, String.valueOf(min), String.valueOf(max), type);
	}
	
	@Override
	public <T> Set<T> zrangeByScore(String key, double min, double max, int offset, int count, TypeReference<T> type) {
		return zrangeByScore(key, String.valueOf(min), String.valueOf(max), offset, count, type);
	}
	
	@Override
	public <T> Set<T> zrangeByScore(String key, String min, String max, TypeReference<T> type) {
		Set<String> values = zrangeByScore(key, min, max);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>(values.size());
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Set<T> zrangeByScore(String key, String min, String max, int offset, int count, TypeReference<T> type) {
		Set<String> values = zrangeByScore(key, min, max, offset, count);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>(values.size());
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, double min, double max, TypeReference<T> type) {
		return zrangeByScoreWithScores(key, String.valueOf(min), String.valueOf(max), type);
	}
	
	@Override
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, String min, String max, TypeReference<T> type) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Set<Tuple> values = jedis.zrangeByScoreWithScores(key, min, max);
			if(!CollectionUtils.isEmpty(values)) {
				Map<T, Double> newValues = new HashMap<>();
				for(Tuple value : values) {
					newValues.put(parseObject(value.getElement(), type), value.getScore());
				}
				
				return newValues;
			}
			
			return Collections.emptyMap();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, double min, double max, int offset, int count, TypeReference<T> type) {
		return zrangeByScoreWithScores(key, String.valueOf(min), String.valueOf(max), offset, count, type);
	}
	
	@Override
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, String min, String max, int offset, int count, TypeReference<T> type) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Set<Tuple> values = jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			if(!CollectionUtils.isEmpty(values)) {
				Map<T, Double> newValues = new HashMap<>();
				for(Tuple value : values) {
					newValues.put(parseObject(value.getElement(), type), value.getScore());
				}
				
				return newValues;
			}
			
			return Collections.emptyMap();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zrank(String key, String member) {
		Assert.hasLength(key);
		Assert.hasLength(member);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrank(key, member);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> long zrank(String key, T member) {
		return zrank(key, toJSONString(member));
	}
	
	@Override
	public long zrem(String key, String... members) {
		Assert.hasLength(key);
		Assert.notEmpty(members);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrem(key, members);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> long zrem(String key, @SuppressWarnings("unchecked") T... members) {
		return zrem(key, toJSONString(members));
	}
	
	@Override
	public long zremrangeByLex(String key, String min, String max) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zremrangeByLex(key, min, max);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zremrangeByRank(String key, long start, long end) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zremrangeByRank(key, start, end);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zremrangeByScore(String key, double min, double max) {
		return zremrangeByScore(key, String.valueOf(min), String.valueOf(max));
	}
	
	@Override
	public long zremrangeByScore(String key, String min, String max) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zremrangeByScore(key, min, max);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrevrange(key, start, end);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public Set<String> zrevrange(String key, long end) {
		return zrevrange(key, 0, end);
	}
	
	@Override
	public Set<String> zrevrange(String key) {
		return zrevrange(key, 0, -1);
	}
	
	@Override
	public <T> Set<T> zrevrange(String key, long start, long end, TypeReference<T> type) {
		Set<String> values = zrevrange(key, start, end);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Set<T> zrevrange(String key, long end, TypeReference<T> type) {
		return zrevrange(key, 0, end, type);
	}
	
	@Override
	public <T> Set<T> zrevrange(String key, TypeReference<T> type) {
		return zrevrange(key, 0, -1, type);
	}
	
	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrevrangeByLex(key, max, min);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Set<T> zrevrangeByLex(String key, String max, String min, TypeReference<T> type) {
		Set<String> values = zrevrangeByLex(key, max, min);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrevrangeByLex(key, max, min, offset, count);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Set<T> zrevrangeByLex(String key, String max, String min, int offset, int count, TypeReference<T> type) {
		Set<String> values = zrevrangeByLex(key, max, min, offset, count);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>();
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Map<T, Double> zrevrangeWithScores(String key, long start, long end, TypeReference<T> type) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Set<Tuple> values = jedis.zrevrangeWithScores(key, start, end);
			if(!CollectionUtils.isEmpty(values)) {
				Map<T, Double> newValues = new HashMap<>();
				for(Tuple value : values) {
					newValues.put(parseObject(value.getElement(), type), value.getScore());
				}
				
				return newValues;
			}
			
			return Collections.emptyMap();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Map<T, Double> zrevrangeWithScores(String key, long end, TypeReference<T> type) {
		return zrevrangeWithScores(key, 0, end, type);
	}
	
	@Override
	public <T> Map<T, Double> zrevrangeWithScores(String key, TypeReference<T> type) {
		return zrevrangeWithScores(key, 0, -1, type);
	}
	
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrevrangeByScore(key, max, min);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrevrangeByScore(key, max, min, offset, count);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Set<T> zrevrangeByScore(String key, double max, double min, TypeReference<T> type) {
		Set<String> values = zrevrangeByScore(key, max, min);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>(values.size());
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Set<T> zrevrangeByScore(String key, double max, double min, int offset, int count, TypeReference<T> type) {
		Set<String> values = zrevrangeByScore(key, max, min, offset, count);
		if(!CollectionUtils.isEmpty(values)) {
			Set<T> newValues = new LinkedHashSet<>(values.size());
			for(String value : values) {
				newValues.add(parseObject(value, type));
			}
			
			return newValues;
		}
		
		return Collections.emptySet();
	}
	
	@Override
	public <T> Map<T, Double> zrevrangeByScoreWithScores(String key, double max, double min, TypeReference<T> type) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Set<Tuple> values = jedis.zrevrangeByScoreWithScores(key, max, min);
			if(!CollectionUtils.isEmpty(values)) {
				Map<T, Double> newValues = new HashMap<>();
				for(Tuple value : values) {
					newValues.put(parseObject(value.getElement(), type), value.getScore());
				}
				
				return newValues;
			}
			
			return Collections.emptyMap();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> Map<T, Double> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count, TypeReference<T> type) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			Set<Tuple> values = jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
			if(!CollectionUtils.isEmpty(values)) {
				Map<T, Double> newValues = new HashMap<>();
				for(Tuple value : values) {
					newValues.put(parseObject(value.getElement(), type), value.getScore());
				}
				
				return newValues;
			}
			
			return Collections.emptyMap();
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public long zrevrank(String key, String member) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zrevrank(key, member);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> long zrevrank(String key, T member) {
		return zrevrank(key, toJSONString(member));
	}
	
	@Override
	public double zscore(String key, String member) {
		Assert.hasLength(key);
		ShardedJedis jedis = null;
		try{
			jedis = POOL.getJedis(config.getRedisType());
			return jedis.zscore(key, member);
		} catch(Exception e) {
			throw new RedisClientException(e.getMessage());
		} finally {
			POOL.close(jedis);
		}
	}
	
	@Override
	public <T> double zscore(String key, T member) {
		return zscore(key, toJSONString(member));
	}
	
}
