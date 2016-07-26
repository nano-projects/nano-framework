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
package org.nanoframework.orm.jedis.sharded;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.RedisClientPool;
import org.nanoframework.orm.jedis.cluster.HashTest;
import org.nanoframework.orm.jedis.exception.RedisClientException;

import com.alibaba.fastjson.JSON;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class ShardedInfoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashTest.class);

    protected RedisClient redisClient;

    @Before
    public void before() throws LoaderException, IOException {
        if (redisClient == null) {
            Properties prop = PropertiesLoader.load("/redis-test.properties");
            RedisClientPool.POOL.initRedisConfig(prop).createJedis();
            RedisClientPool.POOL.bindGlobal();
            redisClient = GlobalRedisClient.get("sharded");
        }
    }
    
    @Test
    public void infoTest() {
        try {
            LOGGER.debug(JSON.toJSONString(redisClient.info()));
        } catch (final Throwable e) {
            if (!(e instanceof RedisClientException)) {
                throw e;
            }
            
            LOGGER.error("Redis Server not up");
        }
    }
    
    @Test
    public void infoSectionTest() {
        try {
            LOGGER.debug(JSON.toJSONString(redisClient.info("memory")));
        } catch (final Throwable e) {
            if (!(e instanceof RedisClientException)) {
                throw e;
            }
            
            LOGGER.error("Redis Server not up");
        }
    }
}
