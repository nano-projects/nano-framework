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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import com.alibaba.fastjson.TypeReference;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:14:39
 */
public class RedisClientTest {

	private Logger LOG = LoggerFactory.getLogger(RedisClientTest.class);
	
	private RedisClient redisClient;
	
	@Before
	public void before() throws LoaderException, IOException {
		if(redisClient == null) {
			Properties prop = PropertiesLoader.load("/redis-test.properties");
			RedisClientPool.POOL.initRedisConfig(prop).createJedis();
			redisClient = GlobalRedisClient.get("test");
		}	
	}
	
	@Test
	public void test0() {
		if(redisClient == null)
			return ;
		
		String key = "test0";
		redisClient.set(key, "hello world!");
		LOG.debug("get test0: " + redisClient.get(key));
		redisClient.del(key);
		boolean exists;
		LOG.debug("exists test0: " + (exists = redisClient.exists(key)));
		
		if(exists)
			throw new RedisClientException("redis key 没有被删除");
		
	}
	
	@Test
	public void test1() throws InterruptedException {
		if(redisClient == null)
			return ;
		
		String key = "test1";
		redisClient.set(key, "hello, test1");
		LOG.debug("exists test1: " + redisClient.exists(key));
		redisClient.expire(key, 1);
		LOG.debug("expire test1: 1s");
		Thread.sleep(2000L);
		boolean exists;
		LOG.debug("exists test1: " + (exists = redisClient.exists(key)));
		
		if(exists)
			throw new RedisClientException("redis key 没有过期");
		
	}
	
	@Test
	public void test2() throws InterruptedException {
		if(redisClient == null)
			return ;
		
		String key = "test2";
		if(redisClient.exists(key))
			redisClient.del(key);
		
		redisClient.set(key, "hello, test2");
		LOG.debug("exists test2: " + redisClient.exists(key));
		long now = System.currentTimeMillis() + 1000;
		redisClient.expireat(key, now);
		LOG.debug("expireAt test2: " + (now / 1000));
		long time = redisClient.ttl(key);
		LOG.debug("TTL Time has: " + time);
		if(time > 0) 
			Thread.sleep((time + 1) * 1000);
		
		boolean exists;
		LOG.debug("exists test2: " + (exists = redisClient.exists(key)));
		
		if(exists) {
			LOG.error(key + ": redis key 没有过期");
			redisClient.del(key);
			if(redisClient.exists(key))
				throw new RedisClientException("redis key 没有删除");
				
		}
	}
	
	@Test
	public void test3() {
		if(redisClient == null)
			return ;
		
		String key = "test3";
		redisClient.set(key, "hello, test3");
		LOG.debug("exists test3: " + redisClient.exists(key));
		LOG.debug("keys: " + redisClient.keys("test*"));
		redisClient.del(key);
		
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
	}
	
	public void test4() {
		if(redisClient == null)
			return ;
		
		String key = "test4";
		redisClient.set(key, "hello, test4");
		LOG.debug("test4 value: " + redisClient.get(key));
		
		redisClient.append(key, "append string");
		LOG.debug("test4 value: " + redisClient.get(key));
		
		redisClient.append(key, "append string2", ";");
		LOG.debug("test4 value: " + redisClient.get(key));
		
		Map<String, String> appendMap = new HashMap<>();
		appendMap.put("mapKey", "append Map");
		redisClient.append(key, appendMap);
		LOG.debug("test4 value: " + redisClient.get(key));
		
		appendMap = new HashMap<>();
		appendMap.put("mapKey", "append Map2");
		redisClient.append(key, appendMap, ";");
		LOG.debug("test4 value: " + redisClient.get(key));
		redisClient.del(key);
		
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
	}
	
	@Test
	public void test5() throws InterruptedException {
		if(redisClient == null)
			return ;
		
		String key = "test5";
		Map<String, String> value = new HashMap<>();
		value.put("value", "hello, test5");
		redisClient.set(key, value);
		LOG.debug("test5 value: " + redisClient.get(key, new TypeReference<Map<String, String>>() { }));
		redisClient.del(key);
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
		Map<String, Object> test5 = new HashMap<>();
		test5.put(key, "hello, test5");
		redisClient.set(test5);
		LOG.debug("test5 value: " + redisClient.get(key));
		redisClient.del(key);
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
		test5 = new HashMap<>();
		test5.put(key + "-1", "hello, test5-1");
		test5.put(key + "-2", "hello, test5-2");
		test5.put(key + "-3", "hello, test5-3");
		redisClient.set(test5);
		LOG.debug("test5 value: " + redisClient.get(key + "-1", key + "-2", key + "-3"));
		redisClient.getset(key + "-2", "hello, test5-2 to update");
		LOG.debug("test5-2 value: " + redisClient.get(key + "-2"));
		redisClient.del(key + "-1", key + "-2", key + "-3");
		if(redisClient.exists(key + "-1") || redisClient.exists(key + "-2") || redisClient.exists(key + "-3"))
			throw new RedisClientException("redis key 没有删除");
		
		redisClient.set(key, "hello, test5 and test setByNX");
		try {
			long changed = redisClient.setByNX(key, "update value");
			if(changed > 0)
				LOG.debug("set by nx: OK");
			else 
				LOG.debug("set by nx: EXISTS");
			
		} catch(Exception e) {
			LOG.error("set by nx: " + e.getMessage());
		}
		
		try {
			long changed = redisClient.setByNX(key, test5);
			if(changed > 0)
				LOG.debug("set by nx: OK");
			else 
				LOG.debug("set by nx: EXISTS");
			
		} catch(Exception e) {
			LOG.error("set by nx on Object: " + e.getMessage());
		}
		
		redisClient.del(key);
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
		redisClient.set(key + "-2", "test5-2");
		try {
			Map<String, Long> exists = redisClient.setByNX(test5);
			if(exists.size() > 0)
				LOG.debug("set by nx: FAIL, value: " + exists);
			else 
				LOG.debug("set by nx: OK");
			
		} catch(Exception e) {
			LOG.error("set by nx on Object: " + e.getMessage());
		}
		redisClient.del(key + "-1", key + "-2", key + "-3");
		if(redisClient.exists(key + "-1") || redisClient.exists(key + "-2") || redisClient.exists(key + "-3"))
			throw new RedisClientException("redis key 没有删除");
		
		redisClient.setByEX(key, value, 3);
		LOG.debug("test5 value: " + redisClient.get(key));
		Thread.sleep(5000L);
		LOG.debug("test5: value: " + redisClient.get(key));
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有过期");
		
	}
	
	@Test
	public void test6() {
		if(redisClient == null)
			return ;
		
		String key = "test6";
		redisClient.set(key, "strLen");
		LOG.debug("test6 value length: " + redisClient.strLen(key));
		redisClient.del(key);
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
	}
	
	@Test
	public void test7() {
		if(redisClient == null)
			return ;
		
		String key = "test7";
		redisClient.hset(key, "field-1", "value-1");
		LOG.debug("test7 value: " + redisClient.hget(key, "field-1"));
		LOG.debug("test7 value exists: " + redisClient.hexists(key, "field-1"));
		redisClient.hset(key, "field-2", "value-2");
		LOG.debug("test7 values: " + redisClient.hmget(key, "field-1", "field-2"));
		redisClient.del(key);
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
		Map<String, String> value = new HashMap<>();
		value.put("key1", "value1");
		value.put("key2", "value2");
		redisClient.hset(key, "field-1", value);
		redisClient.hget(key, "field-1", new TypeReference<Map<String, String>>() { });
		
		redisClient.hset(key, "field-2", value);
		redisClient.hset(key, "field-3", value);
		Map<String, Map<String, String>> map = redisClient.hmget(key, new String[]{ "field-1", "field-3" }, new TypeReference<Map<String, String>>() { });
		LOG.debug("test7 hash values: " + map);
		LOG.debug("test7 hgetAll values: " + redisClient.hgetAll(key));
		
		map = redisClient.hgetAll(key, new TypeReference<Map<String, String>>() { });
		LOG.debug("test7 hgetAll values: " + map);
		
		LOG.debug("test7 hkeys: " + redisClient.hkeys(key));
		LOG.debug("test7 hlen: " + redisClient.hlen(key));
		
		try {
			redisClient.hsetByNX(key, "field-1", "value");
		} catch(Exception e) {
			LOG.error("hset nx: " + e.getMessage());
		}
		
		LOG.debug("test7 hvals: " + redisClient.hvals(key));
		redisClient.del(key);
		
		if(redisClient.exists(key))
			throw new RedisClientException("redis key 没有删除");
		
	}
	
	@Test
	public void test8() {
		if(redisClient == null)
			return ;
		
		String key = "test8";
		Map<String, Object> map = new HashMap<>();
		map.put("test8-1", "value-test8-1");
		map.put("test8-2", "value-test8-2");
		map.put("test8-3", "value-test8-3");
		map.put("test8-4", "value-test8-4");
		map.put("test8-5", "value-test8-5");
		redisClient.hmset(key, map);
		
		System.out.println(redisClient.hmget(key, "test8-1", "test8-3", "test8-10", "test8-5", "test1-1", "test8-2"));
		
		redisClient.del(key);
		
	}
	
}
