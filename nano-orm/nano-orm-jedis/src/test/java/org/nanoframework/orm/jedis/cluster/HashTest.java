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
package org.nanoframework.orm.jedis.cluster;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.RedisClientPool;

import com.alibaba.fastjson.TypeReference;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class HashTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashTest.class);
    
    protected RedisClient redisClient;

    @Before
    public void before() throws LoaderException, IOException {
        if (redisClient == null) {
            Properties prop = PropertiesLoader.load("/redis-test.properties");
            RedisClientPool.POOL.initRedisConfig(prop).createJedis();
            RedisClientPool.POOL.bindGlobal();
            redisClient = GlobalRedisClient.get("cluster");
        }
    }
    
    @Test
    public void hdelTest() {
        try {
            Assert.assertEquals(redisClient.hset("hdelTest", "1", 1), true);
            Assert.assertEquals(redisClient.hexists("hdelTest", "1"), true);
            Assert.assertEquals(redisClient.hget("hdelTest", "1"), "1");
            Assert.assertEquals(redisClient.hdel("hdelTest", "1"), 1);
            Assert.assertEquals(redisClient.hexists("hdelTest", "1"), false);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }
            
            LOGGER.error(e.getMessage());
        }
    }
    
    @Test
    public void hmTest() {
        try {
            Assert.assertEquals(redisClient.hmset("hmTest", MapBuilder.<String, Object>create()
                    .put("1", 1).put("2", 2).put("3", 3).build()), true);
            
            final Map<String, Integer> values = redisClient.hmget("hmTest", new String[]{"1", "2"}, new TypeReference<Integer>(){ });
            Assert.assertEquals(values.size(), 2);
            Assert.assertEquals(values.get("1"), (Integer) 1);
            Assert.assertEquals(values.get("2"), (Integer) 2);
            
            final Map<String, Integer> all = redisClient.hgetAll("hmTest", new TypeReference<Integer>(){ });
            Assert.assertEquals(all.size(), 3);
            Assert.assertEquals(all.get("3"), (Integer) 3);
            
            final Set<String> keys = redisClient.hkeys("hmTest");
            Assert.assertEquals(keys.size(), 3);
            Assert.assertEquals(redisClient.hlen("hmTest"), 3);
            
            Assert.assertEquals(redisClient.hdel("hmTest", "1", "2", "3"), 3);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }
            
            LOGGER.error(e.getMessage());
        }
    }
    
    @Test
    public void hsetnxTest() {
        try {
            Assert.assertEquals(redisClient.hsetByNX("hsetnxTest", "1", 1), true);
            final Map<String, Boolean> response = redisClient.hsetByNX("hsetnxTest", MapBuilder.<String, Object>create()
                    .put("1", 1)
                    .put("2", 2)
                    .put("3", 3)
                    .build());
            
            Assert.assertEquals(response.size(), 3);
            Assert.assertEquals(response.get("1"), false);
            Assert.assertEquals(response.get("2"), true);
            Assert.assertEquals(response.get("3"), true);
            
            final List<Integer> values = redisClient.hvals("hsetnxTest", new TypeReference<Integer>(){ });
            Assert.assertEquals(values.size(), 3);
            
            Assert.assertEquals(redisClient.hdel("hsetnxTest", "1", "2", "3"), 3);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }
            
            LOGGER.error(e.getMessage());
        }
    }
    
}
