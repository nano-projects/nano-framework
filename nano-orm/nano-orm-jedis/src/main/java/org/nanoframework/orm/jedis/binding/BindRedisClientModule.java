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
package org.nanoframework.orm.jedis.binding;

import static com.google.inject.name.Names.named;

import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.spi.Order;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.lock.RedisLocker;
import org.nanoframework.orm.jedis.lock.impl.RedisLockerImpl;

import com.google.common.collect.Lists;
import com.google.inject.Binder;

/**
 *
 * @author yanghe
 * @since 1.3.16
 */
@Order(1100)
public class BindRedisClientModule implements Module {
    private static final String REDIS_NAMED_PRIFIX = "redis:";
    private static final String REDIS_LOCK_NAMED_PREFIX = "redis.lock:";

    @Override
    public void configure(final Binder binder) {
        GlobalRedisClient.keys().forEach(redisName -> {
            final RedisClient client = GlobalRedisClient.get(redisName);
            binder.bind(RedisClient.class).annotatedWith(named(REDIS_NAMED_PRIFIX + redisName)).toInstance(client);
            binder.bind(RedisLocker.class).annotatedWith(named(REDIS_LOCK_NAMED_PREFIX + redisName)).toInstance(new RedisLockerImpl(client));
        });
    }

    @Override
    public List<Module> load() throws Throwable {
        return Lists.newArrayList(this);
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {

    }

}
