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
package org.nanoframework.extension.shiro.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.SerializableUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author yanghe
 * @since 1.2
 */
public class RedisCache<K, V> implements Cache<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCache.class);
    private Map<String, RedisClient> sessions;
    private final String redisSourceNames;
    private final String cacheName;

    public RedisCache(final String redisSourceNames, final String cacheName) {
        this.redisSourceNames = redisSourceNames;
        this.cacheName = cacheName;

        if (StringUtils.isBlank(redisSourceNames)) {
            throw new IllegalArgumentException("redisSourceNames must be not blank.");
        }

        if (StringUtils.isBlank(cacheName)) {
            throw new IllegalArgumentException("cacheName must be not blank.");
        }
    }

    @Override
    public V get(final K key) throws CacheException {
        initRedisClient();
        for (final Entry<String, RedisClient> item : sessions.entrySet()) {
            try {
                final RedisClient client = item.getValue();
                final String encodeKey = SerializableUtils.encodeString(key);
                if (StringUtils.isBlank(encodeKey)) {
                    return null;
                }

                return SerializableUtils.decode(client.hget(cacheName, encodeKey));
            } catch (final Exception e) {
                LOGGER.error("读取Cache异常[get()][{}]: {}", item.getKey(), e.getMessage());
            }
        }

        return null;
    }

    @Override
    public V put(final K key, final V value) throws CacheException {
        initRedisClient();
        final RedisClient client = sessions.values().iterator().next();
        client.hset(cacheName, SerializableUtils.encodeString(key), SerializableUtils.encodeString(value));
        return value;
    }

    @Override
    public V remove(final K key) throws CacheException {
        initRedisClient();
        final RedisClient client = sessions.values().iterator().next();
        final String val = client.hget(cacheName, SerializableUtils.encodeString(key));
        if (!StringUtils.isEmpty(val)) {
            client.hdel(cacheName, SerializableUtils.encodeString(key));
            return SerializableUtils.decode(val);
        }

        return null;
    }

    @Override
    public void clear() throws CacheException {
        initRedisClient();
        final RedisClient client = sessions.values().iterator().next();
        client.del(cacheName);
    }

    @Override
    public int size() {
        initRedisClient();
        for (final Entry<String, RedisClient> item : sessions.entrySet()) {
            try {
                final RedisClient client = item.getValue();
                final Long len = Long.valueOf(client.hlen(cacheName));
                return len.intValue();
            } catch (final Exception e) {
                LOGGER.error("读取Cache异常[size()][{}]: {}", item.getKey(), e.getMessage());
            }
        }

        return 0;
    }

    @Override
    public Set<K> keys() {
        initRedisClient();
        for (final Entry<String, RedisClient> item : sessions.entrySet()) {
            try {
                final RedisClient client = item.getValue();
                final Set<String> keys = client.hkeys(cacheName);
                if (!CollectionUtils.isEmpty(keys)) {
                    final Set<K> sets = Sets.newLinkedHashSet();
                    keys.forEach(key -> sets.add(SerializableUtils.decode(key)));
                    return sets;
                }
            } catch (final Exception e) {
                LOGGER.error("读取Cache异常[keys()][" + item.getKey() + "]: " + e.getMessage());
            }
        }

        return Collections.emptySet();
    }

    /** 每次进行校验时都会调用此方法，为了防止在集群环境下每个节点都进行此扫描，特意加上活锁进行访问限制 */
    @Override
    public Collection<V> values() {
        initRedisClient();
        for (Entry<String, RedisClient> item : sessions.entrySet()) {
            try {
                RedisClient client = item.getValue();
                List<String> vals = client.hvals(cacheName);
                if (!CollectionUtils.isEmpty(vals)) {
                    List<V> lists = Lists.newArrayList();
                    vals.forEach(val -> lists.add(SerializableUtils.decode(val)));
                    return lists;
                }
            } catch (final Exception e) {
                LOGGER.error("读取Cache异常[values()][" + item.getKey() + "]: " + e.getMessage());
            }
        }

        return Collections.emptyList();
    }

    public void setSessions(String redisSourceNames) {
        if (StringUtils.isNotBlank(redisSourceNames)) {
            String[] names = redisSourceNames.split(",");
            Map<String, RedisClient> sessionMap = Maps.newLinkedHashMap();
            for (final String name : names) {
                if (StringUtils.isBlank(name)) {
                    continue;
                }

                RedisClient client;
                if ((client = GlobalRedisClient.get(name)) != null) {
                    sessionMap.put(name, client);
                }
            }

            this.sessions = sessionMap;
        }
    }

    private void initRedisClient() {
        if (CollectionUtils.isEmpty(sessions)) {
            setSessions(redisSourceNames);
        }
    }
}
