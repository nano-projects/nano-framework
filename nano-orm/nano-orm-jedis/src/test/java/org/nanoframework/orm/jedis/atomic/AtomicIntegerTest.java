/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.orm.jedis.atomic;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.RedisClientPool;

/**
 * @author yanghe
 * @date 2016年3月24日 上午10:13:56
 */
public class AtomicIntegerTest {

	private Logger LOG = LoggerFactory.getLogger(AtomicIntegerTest.class);
	private RedisClient redisClient;
	
	@Before
	public void before() throws LoaderException, IOException {
		if(redisClient == null) {
			Properties prop = PropertiesLoader.load("/redis-test.properties");
			RedisClientPool.POOL.initRedisConfig(prop).createJedis();
			redisClient = GlobalRedisClient.get("test");
		}	
	}
	
	@Ignore
	@Test
	public void loopSingleTest() {
		long time = System.currentTimeMillis();
		LOG.debug("now: {}", time);
		
		AtomicInteger integer = new AtomicInteger(redisClient);
		for(int i = 0; i < 100_000; i ++) {
			int value = integer.getAndIncrement();
			if(i % 100 == 0)
				LOG.debug("now value: {}", value);
		}
		
		LOG.debug("use time: {}ms", System.currentTimeMillis() - time);
	}
	
	@Ignore
	@Test
	public void loopConcurrentTest() {
		final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		java.util.concurrent.atomic.AtomicInteger isOver = new java.util.concurrent.atomic.AtomicInteger();
		final int parallel = 20;
		for(int i = 0; i < parallel; i ++) {
			pool.execute(() -> {
				AtomicInteger integer = new AtomicInteger(redisClient);
				for(int idx = 0; idx < 100; idx ++) {
					int value = integer.incrementAndGet();
					LOG.debug("now value: {}", value);
				}
				
				isOver.incrementAndGet();
			});
		}
		
		while(isOver.get() != parallel) {
			try { Thread.sleep(1000L); } catch(Exception e) { }
		}
	}
}
