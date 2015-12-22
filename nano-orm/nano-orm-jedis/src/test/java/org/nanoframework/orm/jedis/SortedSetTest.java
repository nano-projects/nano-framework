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
package org.nanoframework.orm.jedis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;

import com.alibaba.fastjson.TypeReference;

import junit.framework.Assert;

/**
 * @author yanghe
 * @date 2015年11月1日 上午12:02:08
 */
public class SortedSetTest {
	private RedisClient redisClient;
	
	@Before
	public void before() throws LoaderException, IOException {
		if(redisClient == null) {
			Properties prop = PropertiesLoader.load("/redis-test.properties");
			RedisClientPool.POOL.initRedisConfig(prop).createJedis();
			redisClient = GlobalRedisClient.get("test");
		}	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void sortedSetTest() {
		if(redisClient == null)
			return ;
		
		String key = "sortedSetTest";
		redisClient.del(key);
		redisClient.zadd(key, 1, "value1");
		Map<String, String> map;
		redisClient.zadd(key, 2, map = new HashMap<String, String>() {
			private static final long serialVersionUID = 3101027130935931066L; { 
			put("key2", "value2"); 
		}});
		
		Assert.assertEquals("zcard(key) not eq 2", redisClient.zcard(key), 2);
		Assert.assertEquals("zcount(key, 1, 2) not eq 2", redisClient.zcount(key, 1, 2), 2); // 1 <= key <= 2
		Assert.assertEquals("zcount(key) not eq 2", redisClient.zcount(key), 2);
		Assert.assertEquals("zcount(key, '(1', '2') not eq 1", redisClient.zcount(key, "(1", "2"), 1); // 1 < key <= 2
		Assert.assertEquals("zcount(key, '1', +inf) not eq 2", redisClient.zcount(key, "1", RedisClient.INF1), 2); // 1 <= key <= +MAX
		Assert.assertEquals("zcount(key, '(1', +inf) not eq 1", redisClient.zcount(key, "(1", RedisClient.INF1), 1); // 1 < key <= +MAX
		Assert.assertEquals("zcount(key, '-inf', '0') not eq 0", redisClient.zcount(key, RedisClient.INF0, "0"), 0); // -MAX <= key <= 0
		
		redisClient.zadd(key, 1, "value3");
		Assert.assertEquals("zlexcount(key, '[value1', '[value2') not eq 1", redisClient.zlexcount(key, "[value1", "[value2"), 1);
		Assert.assertEquals("zlexcount(key, '[value1', '[value3') not eq 2", redisClient.zlexcount(key, "[value1", "[value3"), 2);
		
		redisClient.zincrby(key, 3, "value3"); // 1 + 3
		redisClient.zincrby(key, 20, map); // 2 + 20
		
		Assert.assertEquals("zrange(key) not eq 3", redisClient.zrange(key).size(), 3);
		Assert.assertEquals("zrangeByLex(key, '[value', '[valuez', 1, 10) not eq 1", redisClient.zrangeByLex(key, "[value", "[valuez", 1, 10).size(), 1);
		
		Assert.assertEquals("zrangeWithScores(key, type) not eq 3", redisClient.zrangeWithScores(key, new TypeReference<String>() { }).size(), 3);
		Assert.assertEquals("zrangeByScore(key, 4, 22) not eq 2", redisClient.zrangeByScore(key, 4, 22).size(), 2);
		Assert.assertEquals("zrangeByScore(key, 4, 22, 1, 10) not eq 1", redisClient.zrangeByScore(key, 4, 22, 1, 10).size(), 1);
		
		Assert.assertEquals("zrangeWithScores(key, 4, 22, type) not eq 2", redisClient.zrangeByScoreWithScores(key, 4, 22, new TypeReference<String>() { }).size(), 2);
		Assert.assertEquals("zrangeWithScores(key, 4, 22, 1, 10, type) not eq 1", redisClient.zrangeByScoreWithScores(key, 4, 22, 1, 10, new TypeReference<String>() { }).size(), 1);
		
		Assert.assertEquals("zrank(key, 'value3') eq 1", redisClient.zrank(key, "value3"), 1);
		Assert.assertEquals("zrank(key, map) eq 2", redisClient.zrank(key, map), 2);
		
		Assert.assertEquals("zrem(key, 'value3') eq 1", redisClient.zrem(key, "value3"), 1);
		Assert.assertEquals("zrem(key, map) eq 1", redisClient.zrem(key, map), 1);
		Assert.assertEquals("zrangeWithScores(key, 4, 22, type) not eq 0", redisClient.zrangeByScoreWithScores(key, 4, 22, new TypeReference<String>() { }).size(), 0);
		
		redisClient.zadd(key, 10, "value10");
		redisClient.zadd(key, 11, "value11");
		redisClient.zadd(key, 12, "value12");
		redisClient.zadd(key, 13, "value13");
		
		Assert.assertEquals("zrangeWithScores(key, 10, 13, type) not eq 4", redisClient.zrangeByScoreWithScores(key, 10, 13, new TypeReference<String>() { }).size(), 4);
		Assert.assertEquals("zremrangeByLex(key, '[value10', '[value12') not eq 3", redisClient.zremrangeByLex(key, "[value10", "[value12"), 3);
		
		redisClient.zadd(key, 10, "value10");
		redisClient.zadd(key, 11, "value11");
		redisClient.zadd(key, 12, "value12");
		
		Assert.assertEquals("zremrangeByScore(key, 10, 11) not eq 2", redisClient.zremrangeByScore(key, 10, 11), 2);
		
		redisClient.zadd(key, 10, "value10");
		redisClient.zadd(key, 11, "value11");
		
		Assert.assertEquals("zremrangeByRank(key, 1, 3) not eq 2", redisClient.zremrangeByRank(key, 1, 3), 3);
		
		Assert.assertEquals("zrevrange(key) not eq value13", redisClient.zrevrange(key).iterator().next(), "value13");
		Assert.assertEquals("zrevrangeByLex(key, '[value13', '(value1') not eq 1", redisClient.zrevrangeByLex(key, "[value13", "(value1").size(), 1);
		Assert.assertEquals("zrevrangeByLex(key, '[value13', '(value1') not eq value13", redisClient.zrevrangeByLex(key, "[value13", "(value1").iterator().next(), "value13");
		
		Set<String> values = redisClient.zrevrangeByLex(key, "[value13", "[value1");
		Iterator<String> iter = values.iterator();
		Assert.assertEquals("zrevrangeByLex(key, '[value13', '[value1') not eq 2", values.size(), 2);
		String value = iter.next();
		Assert.assertEquals("zrevrangeByLex(key, '[value13', '[value1') not eq value13", value, "value13");
		value = iter.next();
		Assert.assertEquals("zrevrangeByLex(key, '[value13', '[value1') not eq value1", value, "value1");
		
	}
}
