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
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.RedisClientPool;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class SetTest {
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
    public void setTest() {
        final TypeReference<Integer> type = new TypeReference<Integer>(){ };
        Assert.assertEquals(redisClient.sadd("setTest", 1, 2, 3), 3);
        Assert.assertEquals(redisClient.sreplace("setTest", Lists.newArrayList(1, 2, 3), Lists.newArrayList(4, 5, 6)), 3);
        Assert.assertEquals(redisClient.scard("setTest"), 3);
        
        Assert.assertEquals(redisClient.sismember("setTest", 5), true);
        final Set<Integer> members = redisClient.smembers("setTest", type);
        Assert.assertEquals(members.size(), 3);
        
        Integer member = redisClient.spop("setTest", type);
        Assert.assertNotNull(member);
        
        Integer member2 = redisClient.srandmember("setTest", type);
        Assert.assertNotNull(member2);
        
        List<Integer> member3 = redisClient.srandmember("setTest", 2, type);
        Assert.assertEquals(member3.size(), 2);
        
        Assert.assertEquals(redisClient.sadd("setTest", 1, 2, 3), 3);
        Assert.assertEquals(redisClient.srem("setTest", 2, 3), 2);
        
        Assert.assertEquals(redisClient.del("setTest"), 1);
    }
}
