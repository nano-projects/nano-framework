/*
 * Copyright 2015-2017 the original author or authors.
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
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.junit.BeforeClass;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.jedis.lock.RedisLocker;
import org.nanoframework.orm.jedis.lock.impl.RedisLockerImpl;

/**
 *
 * @author yanghe
 * @since
 */
public class RedisLockerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockerTests.class);
    private static RedisClient CLIENT;

    @BeforeClass
    public static void before() throws LoaderException, IOException {
        if (CLIENT == null) {
            try {
                Properties prop = PropertiesLoader.load("/redis-test.properties");
                RedisClientPool.POOL.initRedisConfig(prop).createJedis();
                RedisClientPool.POOL.bindGlobal();
                CLIENT = GlobalRedisClient.get("sharded");
            } catch (final Throwable e) {
                // ignore
            }
        }
    }

    @Test
    public void lockTest() {
        final RedisLocker locker = new RedisLockerImpl(CLIENT);
        final String key = "RedisLocker-Test";
        try {
            final CountDownLatch latch = new CountDownLatch(3);
            final Thread t1 = new Thread(() -> {
                try {
                    LOGGER.debug("Thread-1 create lock");
                    locker.lock(key);
                    LOGGER.debug("Thread-1 locked");
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    locker.unlock(key);
                    LOGGER.debug("Thread-1 release lock");
                    latch.countDown();
                }
            });

            final Thread t2 = new Thread(() -> {
                try {
                    LOGGER.debug("Thread-2 create lock");
                    locker.lock(key);
                    LOGGER.debug("Thread-2 locked");
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    locker.unlock(key);
                    LOGGER.debug("Thread-2 release lock");
                    latch.countDown();
                }
            });

            final Thread t3 = new Thread(() -> {
                try {
                    LOGGER.debug("Thread-3 create lock");
                    if (locker.tryLock(key)) {
                        try {
                            LOGGER.debug("Thread-3 locked");
                            Thread.sleep(1000);
                        } finally {
                            locker.unlock(key);
                            LOGGER.debug("Thread-3 release lock");
                        }
                    } else {
                        LOGGER.debug("Thread-3 unlocked");
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });

            t1.start();
            t2.start();
            t3.start();
            latch.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
