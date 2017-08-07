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
package org.nanoframework.orm.jedis.lock;

import org.nanoframework.orm.jedis.exception.TimeoutException;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public interface RedisLocker {

    /**
     * 设置分布式锁.
     * @param key 分布式锁Key
     * @throws TimeoutException 超时异常
     */
    void lock(String key) throws TimeoutException;

    /**
     * @param key 分布式锁Key
     * @param timeout 超时时间，单位: 秒
     * @throws TimeoutException 超时异常
     */
    void lock(String key, int timeout) throws TimeoutException;

    /**
     * 设置分布式锁，当设置失败时立即返回.
     * @param key 分布式锁Key
     */
    boolean tryLock(String key);
    
    /**
     * 设置分布式锁，当设置失败时立即返回.
     * @param key 分布式锁Key
     * @param timeout 超时时间，单位: 秒
     */
    boolean tryLock(String key, int timeout);

    /**
     * 取消分布式锁.
     * @param key 分布式锁Key
     */
    void unlock(String key);
}
