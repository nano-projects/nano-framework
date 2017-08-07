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
package org.nanoframework.orm.jedis.lock.impl;

import java.util.Optional;

import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.RedisConfig;
import org.nanoframework.orm.jedis.exception.TimeoutException;
import org.nanoframework.orm.jedis.lock.RedisLocker;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class RedisLockerImpl implements RedisLocker {
    /** 默认分布式锁超时时间，默认: 10秒. */
    public static final int DEFAULT_TIMEOUT = 10;
    private final RedisClient client;
    private final RedisConfig conf;
    private final int timeout;

    public RedisLockerImpl(final RedisClient client) {
        this.client = client;
        conf = client.getConfig();
        this.timeout = Optional.ofNullable(conf.getLockTimeout()).orElse(DEFAULT_TIMEOUT);
    }

    @Override
    public void lock(final String key) throws TimeoutException {
        lock(key, timeout);
    }

    @Override
    public void lock(final String key, final int timeout) throws TimeoutException {
        final long time = System.currentTimeMillis();
        final long longtime = timeout * 1000;
        while (!tryLock(key, timeout)) {
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (System.currentTimeMillis() - time > longtime) {
                throw new TimeoutException("加锁超时, 无法对Key {} 进行加锁.");
            }
        }
    }

    @Override
    public boolean tryLock(final String key) {
        return tryLock(key, timeout);
    }

    @Override
    public boolean tryLock(final String key, final int timeout) {
        return client.setByNX(key, System.currentTimeMillis(), timeout);
    }

    @Override
    public void unlock(final String key) {
        this.client.del(key);
    }

}
