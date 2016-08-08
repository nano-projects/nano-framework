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
import java.util.Properties;

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

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class SortedSetTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SortedSetTest.class);
    
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
    public void sortedTest() {
        try {
            Assert.assertEquals(redisClient.zadd("sortedTest", MapBuilder.<Object, Double>create()
                    .put("1", 1D)
                    .put("2", 2D)
                    .put("3", 3D)
                    .build()), 3);
            
            Assert.assertEquals(redisClient.zcard("sortedTest"), 3);
            
            Assert.assertEquals(redisClient.zcount("sortedTest", 2, 3), 2);
            
            Assert.assertEquals(redisClient.del("sortedTest"), 1);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }
            
            LOGGER.error(e.getMessage());
        }
    }
}
