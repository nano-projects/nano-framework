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
package org.nanoframework.orm.jedis.commands;

import redis.clients.jedis.JedisPubSub;

/**
 * 发布订阅.
 * @author yanghe
 * @since 1.4.10
 */
public interface PubSubRedisClient {
    /**
     * 发布消息.
     * @param channel 频道
     * @param message 消息
     * @return 订阅者数量
     */
    Long publish(String channel, String message);

    /**
     * 按频道订阅消息.
     * @param jedisPubSub 订阅实现
     * @param channels 频道列表
     */
    void subscribe(JedisPubSub jedisPubSub, String... channels);

    /**
     * 按匹配值订阅消息.
     * @param jedisPubSub 订阅实现
     * @param patterns 匹配值列表
     */
    void psubscribe(JedisPubSub jedisPubSub, String... patterns);

}
