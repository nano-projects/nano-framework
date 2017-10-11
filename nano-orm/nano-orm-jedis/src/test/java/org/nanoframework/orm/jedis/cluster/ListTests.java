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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.orm.jedis.RedisClient.Mark;
import org.nanoframework.orm.jedis.RedisClientInitialize;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class ListTests extends RedisClientInitialize {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListTests.class);
    
    @Test
    public void listTest() {
        try {
            final TypeReference<Integer> type = new TypeReference<Integer>(){ };
            Assert.assertEquals(redisClient.push("pushTest", 1, Mark.RPUSH), true); // 1
            Assert.assertEquals(redisClient.push("pushTest", 2, Mark.LPUSH), true); // 2, 1
            final List<Integer> values = redisClient.lrange("pushTest", 0, type);
            Assert.assertEquals(values.size(), 1);
            Assert.assertEquals(values.get(0), (Integer) 2);
            final List<Integer> values2 = redisClient.lrange("pushTest", 1, 1, type);
            Assert.assertEquals(values2.size(), 1);
            Assert.assertEquals(values2.get(0), (Integer) 1);
            Assert.assertEquals(redisClient.push("pushTest", 3, Mark.RPUSH), true); // 2, 1, 3
            final List<Integer> values3 = redisClient.lrange("pushTest", 2, 2, type);
            Assert.assertEquals(values3.size(), 1);
            Assert.assertEquals(values3.get(0), (Integer) 3);
            
            Assert.assertEquals(redisClient.push("pushTest", Lists.newArrayList(4, 5, 6), Mark.LPUSH), true); // 6, 5, 4, 6, 2, 1, 3
            Assert.assertEquals(redisClient.push("pushTest", Lists.newArrayList(7, 8, 9), Mark.RPUSH), true); // 6, 5, 4, 2, 1, 3, 7, 8, 9
            Assert.assertEquals(redisClient.bpop("pushTest", Mark.LPOP, type), (Integer) 6); // 5, 4, 2, 1, 3, 7, 8, 9
            Assert.assertEquals(redisClient.bpop("pushTest", Mark.RPOP, type), (Integer) 9); // 5, 4, 2, 1, 3, 7, 8
            
            Assert.assertEquals(redisClient.brpoplpush("pushTest", "pushTest", type), (Integer) 8); // 8, 5, 6, 2, 1, 3, 7
            final List<Integer> values4 = redisClient.lrange("pushTest", 0, type);
            Assert.assertEquals(values4.size(), 1);
            Assert.assertEquals(values4.get(0), (Integer) 8);
    
            Assert.assertEquals(redisClient.lindex("pushTest", 3, type), (Integer) 2);
            Assert.assertEquals(redisClient.linsert("pushTest", "5", 10, Mark.AFTER), 8); // 8, 5, 10, 6, 2, 1, 3, 7
            Assert.assertEquals(redisClient.linsert("pushTest", "1", 11, Mark.BEFORE), 9); // 8, 5, 10, 6, 2, 11, 1, 3, 7
            Assert.assertEquals(redisClient.llen("pushTest"), 9);
            
            List<Integer> values5 = redisClient.pop("pushTest", 1, type); // 10, 6, 2, 11, 1, 3, 7
            Assert.assertEquals(values5.size(), 2);
            Assert.assertEquals(values5.get(0), (Integer) 8);
            Assert.assertEquals(values5.get(1), (Integer) 5);
            
            List<Integer> values6 = redisClient.pop("pushTest", -1, type); // 10, 6, 2, 11, 1
            Assert.assertEquals(values6.size(), 2);
            Assert.assertEquals(values6.get(0), (Integer) 7);
            Assert.assertEquals(values6.get(1), (Integer) 3);
            
            final Integer lpop = redisClient.pop("pushTest", Mark.LPOP, type); // 6, 2, 11, 1
            Assert.assertEquals(lpop, (Integer) 10);
            
            final Integer rpop = redisClient.pop("pushTest", Mark.RPOP, type); // 6, 2, 11
            Assert.assertEquals(rpop, (Integer) 1);
            
            Assert.assertEquals(redisClient.push("pushTest", "pushTest_scanKey", 12, Mark.RPUSH, Mark.VALUE), true); // 6, 2, 11, 12
            final Integer scanVal = redisClient.get("pushTest_scanKey", type);
            Assert.assertEquals(scanVal, (Integer) 12);
            final List<Integer> scanVals = redisClient.lrange("pushTest", 3, 3, type);
            Assert.assertEquals(scanVals.size(), 1);
            Assert.assertEquals(scanVals.get(0), (Integer) 12);
            Assert.assertEquals(redisClient.del("pushTest_scanKey"), 1);
            
            Assert.assertEquals(redisClient.push("pushTest", "pushTest_scanKey_KEY", 13, Mark.RPUSH, Mark.KEY), true); // 6, 2, 11, 12, pushTest_scanKey_KEY
            final Integer scanVal2 = redisClient.get("pushTest_scanKey_KEY", type);
            Assert.assertEquals(scanVal2, (Integer) 13);
            final List<String> scanVals2 = redisClient.lrange("pushTest", 4, 4);
            Assert.assertEquals(scanVals2.size(), 1);
            Assert.assertEquals(scanVals2.get(0), "pushTest_scanKey_KEY");
            Assert.assertEquals(redisClient.del("pushTest_scanKey_KEY"), 1);
            
            final Map<String, Boolean> scanVal3 = redisClient.push("pushTest", MapBuilder.<String, Object>create(ReflectUtils.convert(LinkedHashMap.class))
                    .put("pushTest_map_0", 14)
                    .put("pushTest_map_1", 15)
                    .build(), Mark.LPUSH, Mark.KEY); // pushTest_map_1, pushTest_map_0, 6, 2, 11, 12, pushTest_scanKey_KEY
            Assert.assertEquals(scanVal3.size(), 2);
            Assert.assertEquals(scanVal3.get("pushTest_map_0"), true);
            Assert.assertEquals(scanVal3.get("pushTest_map_1"), true);
            
            Assert.assertEquals(redisClient.pushx("pushTest", new Integer[]{16, 17}, Mark.RPUSH), 9); // pushTest_map_1, pushTest_map_0, 6, 2, 11, 12, pushTest_scanKey_KEY, 16, 17
            final List<String> ranges = redisClient.lrangeltrim("pushTest", 1); // 6, 2, 11, 12, pushTest_scanKey_KEY, 16, 17
            Assert.assertEquals(ranges.size(), 2);
            Assert.assertEquals(ranges.get(0), "pushTest_map_1");
            Assert.assertEquals(ranges.get(1), "pushTest_map_0");
            
            Assert.assertEquals(redisClient.lrem("pushTest", 2), 1); // 6, 11, 12, pushTest_scanKey_KEY, 16, 17
            Assert.assertEquals(redisClient.lrem("pushTest", 2), 0);
            
            Assert.assertEquals(redisClient.lset("pushTest", 3, 19), true); // 6, 11, 12, 19, 16, 17
            
            Assert.assertEquals(redisClient.ltrim("pushTest", 2, 3), true); // 12, 19
            
            Assert.assertEquals(redisClient.del("pushTest"), 1);
            
            final Integer val = redisClient.bpop("pushTest", 1, Mark.LPOP, type);
            Assert.assertEquals(val, null);
            final Integer val1 = redisClient.brpoplpush("pushTest", "pushTest", 1, type);
            Assert.assertEquals(val1, null);
            
            Assert.assertEquals(redisClient.pushx("pushTest", 18, Mark.RPUSH), 0);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }
            
            LOGGER.error(e.getMessage());
        }
    }
}
