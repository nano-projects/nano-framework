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
package org.nanoframework.orm.jedis;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.jedis.RedisClient.Mark;

import com.alibaba.fastjson.JSON;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
@FixMethodOrder(MethodSorters.DEFAULT)
public class ExtendRedisClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendRedisClientTest.class);

    protected RedisClientExt redisClient;

    @Before
    public void before() throws LoaderException, IOException {
        if (redisClient == null) {
            Properties prop = PropertiesLoader.load("/redis-test.properties");
            RedisClientPool.POOL.initRedisConfig(prop).createJedis();
            RedisClientPool.POOL.bindGlobal();
            redisClient = (RedisClientExt) GlobalRedisClient.get("sharded2");
        }
    }
    
    @Test
    public void spec1Test() {
        try {
            final double random = Math.random();
            redisClient.push("MORE_PUSH", random, Mark.RPUSH);
            LOGGER.debug("PUSH: {}", random);
        } catch (final Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
            
            LOGGER.error("Redis Server not up");
        }
    }
    
    @Test
    public void spec2Test() {
        try {
            LOGGER.debug("RANGE: {}", JSON.toJSONString(redisClient.lrange("MORE_PUSH")));
        } catch (final Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
            
            LOGGER.error("Redis Server not up");
        }
    }
    
    @Test
    public void spec3Test() {
        try {
            List<String> values = redisClient.lrange("MORE_PUSH");
            values.forEach(value -> LOGGER.debug("REM: {}", redisClient.lrem("MORE_PUSH", value)));
        } catch (final Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
            
            LOGGER.error("Redis Server not up");
        }
    }
    
}
