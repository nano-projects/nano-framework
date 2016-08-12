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

import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.google.inject.AbstractModule;

/**
 *
 * @author yanghe
 * @since 1.3.16
 */
public class BindRedisClientModule extends AbstractModule {
    private static final String REDIS_NAMED_PRIFIX = "redis:";

    @Override
    protected void configure() {
        GlobalRedisClient.keys().forEach(redisName -> bind(RedisClient.class).annotatedWith(named(REDIS_NAMED_PRIFIX + redisName))
                .toInstance(GlobalRedisClient.get(redisName)));
    }

}
