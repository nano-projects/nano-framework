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
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClientPool;
import org.nanoframework.orm.jedis.cluster.SetTest;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class ShardedSetTest extends SetTest {
    @BeforeClass
    public static void before() throws LoaderException, IOException {
        if (redisClient == null) {
            try {
                Properties prop = PropertiesLoader.load("/redis-test.properties");
                RedisClientPool.POOL.initRedisConfig(prop).createJedis();
                RedisClientPool.POOL.bindGlobal();
                redisClient = GlobalRedisClient.get("sharded");
            } catch (final Throwable e) {
                // ignore
            }
        }
    }
    
    @Test
    public void setTest() {
        try {
            final TypeReference<Integer> type = new TypeReference<Integer>(){ };
            Assert.assertEquals(redisClient.sadd("setTest", 1, 2, 3), 3);
            Assert.assertEquals(redisClient.sreplace("setTest", Lists.newArrayList(1, 2, 3), Lists.newArrayList(4, 5, 6)), 3);
            Assert.assertEquals(redisClient.scard("setTest"), 3);
            
            Assert.assertEquals(redisClient.sadd("setTest-1", 3, 5, 7), 3);
            final Set<Integer> diff = redisClient.sdiff(Lists.newArrayList("setTest", "setTest-1"), type);
            Assert.assertEquals(diff.size(), 2);
            Assert.assertEquals(diff.contains(4), true);
            Assert.assertEquals(diff.contains(6), true);
            
            Assert.assertEquals(redisClient.sdiffstore("setTest-2", "setTest", "setTest-1"), 2);
            final Set<Integer> diffstore = redisClient.smembers("setTest-2", type);
            Assert.assertEquals(diffstore.size(), 2);
            Assert.assertEquals(diffstore.contains(4), true);
            Assert.assertEquals(diffstore.contains(6), true);
            Assert.assertEquals(redisClient.del("setTest-2"), 1);
            
            final Set<Integer> inter = redisClient.sinter(Lists.newArrayList("setTest", "setTest-1"), type);
            Assert.assertEquals(inter.size(), 1);
            Assert.assertEquals(inter.contains(5), true);
            
            Assert.assertEquals(redisClient.sinterstore("setTest-3", "setTest", "setTest-1"), 1);
            final Set<Integer> interstore = redisClient.smembers("setTest-3", type);
            Assert.assertEquals(interstore.size(), 1);
            Assert.assertEquals(interstore.contains(5), true);
            Assert.assertEquals(redisClient.del("setTest-3"), 1);
            
            Assert.assertEquals(redisClient.sismember("setTest", 5), true);
            final Set<Integer> members = redisClient.smembers("setTest", type);
            Assert.assertEquals(members.size(), 3);
            
            Assert.assertEquals(redisClient.smove("setTest", "setTest-4", 6), true);
            
            Integer member = redisClient.spop("setTest", type);
            Assert.assertNotNull(member);
            
            Integer member2 = redisClient.srandmember("setTest", type);
            Assert.assertNotNull(member2);
            
            List<Integer> member3 = redisClient.srandmember("setTest", 2, type);
            Assert.assertEquals(member3.size(), 1);
            
            Assert.assertEquals(redisClient.sadd("setTest", 1, 2, 3), 3);
            Assert.assertEquals(redisClient.srem("setTest", 2, 3), 2);
            
            Assert.assertEquals(redisClient.del("setTest", "setTest-1", "setTest-4"), 3);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }
            
            LOGGER.error(e.getMessage());
        }
    }
}
