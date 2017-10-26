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

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.jedis.RedisClientInitialize;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class SetTests extends RedisClientInitialize {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SetTests.class);

    @Test
    public void setTest() {
        try {
            final TypeReference<Integer> type = new TypeReference<Integer>() {
            };
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
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void sscanTest() {
        final String key = "sscan.test";
        final String prefix = "sscan.test-";
        try {
            final List<String> values = Lists.newArrayList();
            for (int idx = 0; idx < 1000; idx++) {
                values.add(prefix + idx);
            }

            redisClient.sadd(key, values.toArray(new String[values.size()]));
            final AtomicLong cursor = new AtomicLong(-1);
            final ScanParams params = new ScanParams().count(10);
            while (cursor.get() == -1 || cursor.get() > 0) {
                if (cursor.get() == -1) {
                    cursor.set(0);
                }

                final ScanResult<String> res = redisClient.sscan(key, cursor.get(), params);
                cursor.set(Long.valueOf(res.getStringCursor()));
            }
        } finally {
            redisClient.del(key);
        }
    }
}
