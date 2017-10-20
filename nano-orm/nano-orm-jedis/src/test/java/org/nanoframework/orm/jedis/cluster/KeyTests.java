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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.exception.UnsupportedAccessException;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.orm.jedis.RedisClientInitialize;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class KeyTests extends RedisClientInitialize {
    protected static final Logger LOGGER = LoggerFactory.getLogger(KeyTests.class);

    @Test
    public void setAndDelKeyTest() {
        try {
            Assert.assertEquals(redisClient.set("setKeyTest", "1"), true);
            Assert.assertEquals(redisClient.del("setKeyTest"), 1);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void setAndDelKeysTest() {
        try {
            final Map<String, Object> map = MapBuilder.<String, Object> builder().put("setAndDelKeysTest-0", "0").put("setAndDelKeysTest-1", "1")
                    .put("setAndDelKeysTest-2", "2").build();

            final Map<String, Boolean> response = redisClient.set(map);
            for (String key : response.keySet()) {
                Assert.assertEquals(response.get(key), true);
            }

            Assert.assertEquals(redisClient.del(map.keySet().toArray(new String[map.size()])), 3);
            final Map<String, Boolean> response2 = redisClient.set(map);
            for (String key : response2.keySet()) {
                Assert.assertEquals(response2.get(key), true);
            }

            Assert.assertEquals(redisClient.del(Lists.newArrayList(map.keySet().iterator())), 3);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void existsTest() {
        try {
            Assert.assertEquals(redisClient.set("existsTest", "1"), true);
            Assert.assertEquals(redisClient.exists("existsTest"), true);
            Assert.assertEquals(redisClient.del("existsTest"), 1);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void expireTest() {
        try {
            Assert.assertEquals(redisClient.set("expireTest", "1"), true);
            Assert.assertEquals(redisClient.ttl("expireTest"), -1);
            Assert.assertEquals(redisClient.expire("expireTest", 2), 1);
            Assert.assertEquals(redisClient.ttl("expireTest") > 0, true);
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                // ignore
            }
            Assert.assertEquals(redisClient.exists("existsTest"), false);
            Assert.assertEquals(redisClient.ttl("expireTest"), -2);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void expireDefaultTest() {
        try {
            Assert.assertEquals(redisClient.set("expireDefaultTest", "1"), true);
            Assert.assertEquals(redisClient.expire("expireDefaultTest"), 1);
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                // ignore
            }
            Assert.assertEquals(redisClient.exists("expireDefaultTest"), false);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void expireatTest() {
        try {
            Assert.assertEquals(redisClient.set("expireatTest", "1"), true);
            final long time = System.currentTimeMillis() + 2000;
            Assert.assertEquals(redisClient.expireat("expireatTest", time), 1);
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                // ignore
            }
            Assert.assertEquals(redisClient.exists("expireatTest"), false);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void keysTest() {
        try {
            Throwable error = null;
            try {
                redisClient.keys("*");
            } catch (final Throwable e) {
                error = e;
            }

            Assert.assertEquals(error.getClass(), UnsupportedAccessException.class);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void appendTest() {
        try {
            Assert.assertEquals(redisClient.append("appendTest", "123456"), 6);
            Assert.assertEquals(redisClient.append("appendTest", "123456"), 13);
            Assert.assertEquals(redisClient.append("appendTest", "123456", "::"), 21);
            Assert.assertEquals(redisClient.append("appendTest", Lists.newArrayList("123", "456")), 35);
            Assert.assertEquals(redisClient.append("appendTest", Lists.newArrayList("789", "000"), "::"), 50);
            Assert.assertEquals(redisClient.del("appendTest"), 1);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void getTest() {
        try {
            Assert.assertEquals(redisClient.set("getTest", Lists.newArrayList("el-0", "el-1", "el-2")), true);
            Assert.assertEquals(redisClient.get("getTest"), "[\"el-0\",\"el-1\",\"el-2\"]");

            final List<String> values = redisClient.get("getTest", new TypeReference<List<String>>() {
            });
            Assert.assertEquals(values.size(), 3);
            Assert.assertEquals(values.get(0), "el-0");
            Assert.assertEquals(values.get(1), "el-1");
            Assert.assertEquals(values.get(2), "el-2");

            Assert.assertEquals(redisClient.del("getTest"), 1);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void getBatchTest() {
        try {
            Assert.assertEquals(redisClient.set("getBatchTest-0", Lists.newArrayList("el-0", "el-1", "el-2")), true);
            Assert.assertEquals(redisClient.set("getBatchTest-1", Lists.newArrayList("el-3", "el-4", "el-5")), true);
            Assert.assertEquals(redisClient.set("getBatchTest-2", Lists.newArrayList("el-6", "el-7", "el-8")), true);

            final Map<String, String> keysvalues = redisClient.get("getBatchTest-0", "getBatchTest-1", "getBatchTest-2");
            Assert.assertEquals(keysvalues.size(), 3);
            Assert.assertEquals(keysvalues.get("getBatchTest-0"), "[\"el-0\",\"el-1\",\"el-2\"]");
            Assert.assertEquals(keysvalues.get("getBatchTest-1"), "[\"el-3\",\"el-4\",\"el-5\"]");
            Assert.assertEquals(keysvalues.get("getBatchTest-2"), "[\"el-6\",\"el-7\",\"el-8\"]");

            final Map<String, List<String>> objKeysValues = redisClient.get(new String[] { "getBatchTest-0", "getBatchTest-1", "getBatchTest-2" },
                    new TypeReference<List<String>>() {
                    });
            Assert.assertEquals(keysvalues.size(), 3);
            final List<String> values0 = objKeysValues.get("getBatchTest-0");
            Assert.assertEquals(values0.size(), 3);
            Assert.assertEquals(values0.get(0), "el-0");
            Assert.assertEquals(values0.get(1), "el-1");
            Assert.assertEquals(values0.get(2), "el-2");
            final List<String> values1 = objKeysValues.get("getBatchTest-1");
            Assert.assertEquals(values1.size(), 3);
            Assert.assertEquals(values1.get(0), "el-3");
            Assert.assertEquals(values1.get(1), "el-4");
            Assert.assertEquals(values1.get(2), "el-5");
            final List<String> values2 = objKeysValues.get("getBatchTest-2");
            Assert.assertEquals(values2.size(), 3);
            Assert.assertEquals(values2.get(0), "el-6");
            Assert.assertEquals(values2.get(1), "el-7");
            Assert.assertEquals(values2.get(2), "el-8");

            final Map<String, List<String>> listKeysValues = redisClient.get(Lists.newArrayList("getBatchTest-0", "getBatchTest-1", "getBatchTest-3"),
                    new TypeReference<List<String>>() {
                    });
            Assert.assertEquals(listKeysValues.size(), 3);

            Assert.assertEquals(redisClient.del(Lists.newArrayList("getBatchTest-0", "getBatchTest-1", "getBatchTest-2")), 3);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void getSetTest() {
        try {
            Assert.assertEquals(redisClient.set("getSetTest", "getset"), true);
            Assert.assertEquals(redisClient.getset("getSetTest", "new-getset"), "getset");
            Assert.assertEquals(redisClient.get("getSetTest"), "new-getset");
            Assert.assertEquals(redisClient.del("getSetTest"), 1);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void setnxTest() {
        try {
            Assert.assertEquals(redisClient.setByNX("setnxTest", Lists.newArrayList("setByNX-0")), true);
            Assert.assertEquals(redisClient.setByNX("setnxTest", Lists.newArrayList("setByNX-1")), false);
            Assert.assertEquals(redisClient.del("setnxTest"), 1);

            final Map<String, Boolean> values = redisClient
                    .setByNX(MapBuilder.<String, Object> builder().put("setnxTest-0", "setByNX-0").put("setnxTest-1", "setByNX-1").build());
            Assert.assertEquals(values.size(), 2);
            Assert.assertEquals(values.get("setnxTest-0"), true);
            Assert.assertEquals(values.get("setnxTest-1"), true);

            final Map<String, Boolean> values1 = redisClient
                    .setByNX(MapBuilder.<String, Object> builder().put("setnxTest-1", "setByNX-1").put("setnxTest-2", "setByNX-2").build());
            Assert.assertEquals(values1.size(), 2);
            Assert.assertEquals(values1.get("setnxTest-1"), false);
            Assert.assertEquals(values1.get("setnxTest-2"), true);
            Assert.assertEquals(redisClient.del("setnxTest-0", "setnxTest-1", "setnxTest-2"), 3);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void setexTest() {
        try {
            Assert.assertEquals(redisClient.setByEX("setexTest", Lists.newArrayList("1")), true);
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                // ignore
            }

            Assert.assertEquals(redisClient.exists("setexTest"), false);

            Assert.assertEquals(redisClient.setByEX("setexTest", Lists.newArrayList("1"), 2), true);
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                // ignore
            }

            Assert.assertEquals(redisClient.exists("setexTest"), false);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void strLenTest() {
        try {
            Assert.assertEquals(redisClient.set("setexTest", Lists.newArrayList("1")), true);
            Assert.assertEquals(redisClient.strLen("setexTest"), 5);
            Assert.assertEquals(redisClient.del("setexTest"), 1);
        } catch (final Throwable e) {
            if (e instanceof AssertionError) {
                throw e;
            }

            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void scanTest() {
        final String prefix = "scan.test-";
        final Map<String, Object> map = Maps.newHashMap();
        for (int idx = 0; idx < 100; idx++) {
            map.put(prefix + idx, idx);
        }

        redisClient.set(map);

        final ScanParams params = new ScanParams().match(prefix + '*');
        long cursor = -1;
        while (cursor == -1 || cursor > 0) {
            final ScanResult<String> res = redisClient.scan(cursor == -1 ? 0 : cursor, params);
            final String nextCursor = res.getStringCursor();
            cursor = Long.parseLong(nextCursor);
            final List<String> keys = res.getResult();
            LOGGER.debug("{}", keys);
            if (cursor > 0) {
                Assert.assertTrue(keys.size() > 0);
            }
        }

        redisClient.del(map.keySet().toArray(new String[map.size()]));
    }

}
