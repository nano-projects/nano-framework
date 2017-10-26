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

import static org.nanoframework.orm.jedis.RedisClientPool.POOL;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.exception.UnsupportedAccessException;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.orm.jedis.AbstractRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.RedisConfig;
import org.nanoframework.orm.jedis.exception.RedisClientException;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisClusterException;

/**
 *
 * @author yanghe
 * @since 1.3.12
 */
public class RedisClusterClientImpl extends AbstractRedisClient implements RedisClient {
    protected JedisCluster cluster;

    public RedisClusterClientImpl(final String type) {
        super(type);
        cluster = POOL.getJedisCluster(type);
    }

    public RedisClusterClientImpl(final RedisConfig config) {
        super(config);
        cluster = POOL.appendJedisCluster(config);
    }

    @Override
    public List<Map<String, String>> info() {
        throw new JedisClusterException("No way to dispatch this command to Redis Cluster.");
    }

    @Override
    public List<Map<String, String>> info(String section) {
        throw new JedisClusterException("No way to dispatch this command to Redis Cluster.");
    }

    @Override
    public long del(final String... keys) {
        Assert.notEmpty(keys);
        try {
            long deleted = 0;
            for (String key : keys) {
                deleted += cluster.del(key);
            }

            return deleted;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(final String key) {
        Assert.hasText(key);
        try {
            return cluster.exists(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long expire(final String key, final int seconds) {
        Assert.hasText(key);
        try {
            return cluster.expire(key, seconds);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long ttl(final String key) {
        Assert.hasText(key);
        try {
            return cluster.ttl(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> keys(final String pattern) {
        throw new UnsupportedAccessException("Redis Cluster unsupported command 'keys'");
    }

    @Override
    public long append(final String key, final String value, final String separator) {
        Assert.hasText(key);
        Assert.hasText(value);
        final String newValue;
        if (StringUtils.isNotEmpty(separator) && exists(key)) {
            newValue = separator + value;
        } else {
            newValue = value;
        }

        try {
            return cluster.append(key, newValue);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String get(final String key) {
        Assert.hasText(key);
        try {
            return cluster.get(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> get(final String... keys) {
        Assert.notEmpty(keys);
        try {
            Map<String, String> values = Maps.newHashMap();
            for (String key : keys) {
                Assert.hasText(key);
                values.put(key, cluster.get(key));
            }

            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String getset(final String key, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);
        try {
            return cluster.getSet(key, value);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean set(final String key, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);
        try {
            return isOK(cluster.set(key, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Boolean> set(final Map<String, Object> map) {
        Assert.notEmpty(map);
        try {
            final Map<String, Boolean> response = Maps.newHashMap();
            for (Entry<String, Object> entry : map.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                response.put(key, set(key, value));
            }

            return response;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean setByNX(final String key, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);
        try {
            return isSuccess(cluster.setnx(key, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean setByNX(final String key, final String value, final int timeout) {
        return setByNX(key, value) && isSuccess(expire(key, timeout));
    }

    @Override
    public Map<String, Boolean> setByNX(final Map<String, Object> map) {
        Assert.notEmpty(map);
        try {
            final Map<String, Boolean> response = Maps.newHashMap();
            for (Entry<String, Object> entry : map.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                response.put(key, setByNX(key, value));
            }

            return response;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean setByEX(final String key, final String value, final int seconds) {
        Assert.hasText(key);
        Assert.hasText(value);
        try {
            return isOK(cluster.setex(key, seconds, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long strLen(final String key) {
        Assert.hasText(key);
        try {
            return cluster.strlen(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long incr(final String key) {
        Assert.hasText(key);
        try {
            final Long val = cluster.incr(key);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long incrBy(final String key, final long value) {
        Assert.hasText(key);
        try {
            final Long val = cluster.incrBy(key, value);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public double incrByFloat(final String key, final double value) {
        Assert.hasText(key);
        try {
            final Double val = cluster.incrByFloat(key, value);
            if (val == null) {
                return 0;
            }

            return val.doubleValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long decr(final String key) {
        Assert.hasText(key);
        try {
            final Long val = cluster.decr(key);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long decrBy(final String key, final long value) {
        Assert.hasText(key);
        try {
            final Long val = cluster.decrBy(key, value);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public ScanResult<String> scan(final long cursor, final ScanParams params) {
        try {
            return cluster.scan(String.valueOf(cursor), params);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long hdel(final String key, final String... fields) {
        Assert.hasText(key);
        Assert.notEmpty(fields);
        try {
            return cluster.hdel(key, fields);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean hexists(final String key, final String field) {
        Assert.hasText(key);
        Assert.hasText(field);
        try {
            return cluster.hexists(key, field);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String hget(final String key, final String field) {
        Assert.hasText(key);
        Assert.hasText(field);
        try {
            return cluster.hget(key, field);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> hmget(final String key, final String... fields) {
        Assert.hasText(key);
        Assert.notEmpty(fields);
        try {
            final List<String> values = cluster.hmget(key, fields);
            final Map<String, String> valuesMap = Maps.newHashMap();
            for (int idx = 0; idx < values.size(); idx++) {
                final String value = values.get(idx);
                if (value != null) {
                    valuesMap.put(fields[idx], value);
                }
            }

            return valuesMap;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> hgetAll(final String key) {
        Assert.hasText(key);
        try {
            return cluster.hgetAll(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> hkeys(final String key) {
        Assert.hasText(key);
        try {
            return cluster.hkeys(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long hlen(final String key) {
        Assert.hasText(key);
        try {
            return cluster.hlen(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean hset(final String key, final String field, final String value) {
        Assert.hasText(key);
        Assert.hasText(field);
        Assert.hasText(value);
        try {
            return isSuccess(cluster.hset(key, field, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean hmset(final String key, final Map<String, Object> map) {
        Assert.hasText(key);
        Assert.notEmpty(map);
        try {
            final Map<String, String> newMap = Maps.newHashMap();
            map.forEach((field, value) -> newMap.put(field, toJSONString(value)));
            return isOK(cluster.hmset(key, newMap));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean hsetByNX(final String key, final String field, final String value) {
        Assert.hasText(key);
        Assert.hasText(field);
        Assert.hasText(value);
        try {
            return isSuccess(cluster.hsetnx(key, field, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Boolean> hsetByNX(final String key, final Map<String, Object> map) {
        Assert.hasText(key);
        Assert.notEmpty(map);
        try {
            final Map<String, Boolean> response = Maps.newHashMap();
            for (Entry<String, Object> entry : map.entrySet()) {
                final String field = entry.getKey();
                final Object value = entry.getValue();
                response.put(field, hsetByNX(key, field, value));
            }

            return response;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> hvals(final String key) {
        Assert.hasText(key);
        try {
            return cluster.hvals(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(final String key, final long cursor, final ScanParams params) {
        Assert.hasText(key);
        Assert.notNull(params);
        try {
            return cluster.hscan(key, String.valueOf(cursor), params);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long hincrBy(final String key, final String field, final long value) {
        Assert.hasText(key);
        Assert.hasText(field);
        try {
            final Long val = cluster.hincrBy(key, field, value);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public double hincrByFloat(final String key, final String field, final double value) {
        Assert.hasText(key);
        Assert.hasText(field);
        try {
            final Double val = cluster.hincrByFloat(key, field, value);
            if (val == null) {
                return 0;
            }

            return val.doubleValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String bpop(final String key, final int timeout, final Mark pop) {
        Assert.hasText(key);
        Assert.notNull(pop);
        try {
            final List<String> values;
            switch (pop) {
                case LPOP:
                    values = cluster.blpop(timeout, key);
                    break;
                case RPOP:
                    values = cluster.brpop(timeout, key);
                    break;
                default:
                    throw new RedisClientException("Unknown Pop type");
            }

            if (!CollectionUtils.isEmpty(values)) {
                return values.get(1);
            }

            return null;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> bpop(final String[] keys, final Mark pop) {
        throw new UnsupportedAccessException("Redis Cluster does not support 'blpop' or 'brpop' calls without timeout parameters");
    }

    @Override
    public Map<String, String> bpop(final String[] keys, final int timeout, final Mark pop) {
        Assert.notEmpty(keys);
        Assert.notNull(pop);
        try {
            final List<String> values;
            switch (pop) {
                case LPOP:
                    values = cluster.blpop(timeout, keys);
                    break;
                case RPOP:
                    values = cluster.brpop(timeout, keys);
                    break;
                default:
                    throw new RedisClientException("Unknown Pop type");
            }

            if (!CollectionUtils.isEmpty(values)) {
                final Map<String, String> response = Maps.newHashMap();
                for (int idx = 0; idx < keys.length; idx++) {
                    final String key = keys[idx];
                    final String value = values.get(idx);
                    if (StringUtils.isNotBlank(value)) {
                        response.put(key, value);
                    }
                }

                return response;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String brpoplpush(final String source, final String destination, final int timeout) {
        Assert.hasText(source);
        Assert.hasText(destination);
        try {
            return cluster.brpoplpush(source, destination, timeout);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String lindex(final String key, final int index) {
        Assert.hasText(key);
        try {
            return cluster.lindex(key, index);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long linsert(final String key, final String pivot, final String value, final Mark position) {
        Assert.hasText(key);
        Assert.hasText(pivot);
        Assert.hasText(value);
        Assert.notNull(position);
        try {
            switch (position) {
                case BEFORE:
                    return cluster.linsert(key, LIST_POSITION.BEFORE, pivot, value);
                case AFTER:
                    return cluster.linsert(key, LIST_POSITION.AFTER, pivot, value);
                default:
                    throw new RedisClientException("Unknown pivot type");
            }
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long llen(final String key) {
        Assert.hasText(key);
        try {
            return cluster.llen(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String pop(final String key, final Mark pop) {
        Assert.hasText(key);
        Assert.notNull(pop);
        try {
            switch (pop) {
                case LPOP:
                    return cluster.lpop(key);
                case RPOP:
                    return cluster.rpop(key);
                default:
                    throw new RedisClientException("Unknown pop type");
            }
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> pop(final String key, int count) {
        Assert.hasText(key);

        final Mark pop;
        if (count >= 0)
            pop = Mark.LPOP;
        else {
            pop = Mark.RPOP;
            count = count * -1;
        }

        try {
            final Long len = llen(key);
            if (count > len) {
                count = len.intValue();
            }

            final List<String> values = Lists.newArrayList();
            switch (pop) {
                case LPOP:
                    for (int idx = 0; idx <= count; idx++) {
                        values.add(cluster.lpop(key));
                    }

                    break;
                case RPOP:
                    for (int idx = 0; idx <= count; idx++) {
                        values.add(cluster.rpop(key));
                    }

                    break;
                default:
                    throw new RedisClientException("Unknown pop type");
            }

            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean push(final String key, final String[] values, final Mark push) {
        Assert.hasText(key);
        Assert.notEmpty(values);
        Assert.notNull(push);
        try {
            switch (push) {
                case LPUSH:
                    cluster.lpush(key, values);
                    break;
                case RPUSH:
                    cluster.rpush(key, values);
                    break;
                default:
                    throw new RedisClientException("Unknown pop type");
            }

            return true;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean push(final String key, final String scanKey, final String value, final Mark push, final Mark policy) {
        Assert.hasText(key);
        Assert.hasText(scanKey);
        Assert.hasText(value);
        Assert.notNull(push);
        Assert.notNull(policy);

        try {
            switch (push) {
                case LPUSH:
                    switch (policy) {
                        case KEY:
                            cluster.lpush(key, scanKey);
                            break;
                        case VALUE:
                            cluster.lpush(key, value);
                            break;
                        default:
                            throw new RedisClientException("未知的策略(policy)类型");
                    }

                    break;
                case RPUSH:
                    switch (policy) {
                        case KEY:
                            cluster.rpush(key, scanKey);
                            break;
                        case VALUE:
                            cluster.rpush(key, value);
                            break;
                        default:
                            throw new RedisClientException("未知的策略(policy)类型");
                    }

                    break;
                default:
                    throw new RedisClientException("未知的写入(PUSH)类型");
            }

            return isOK(cluster.set(scanKey, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Boolean> push(final String key, final Map<String, Object> scanMap, final Mark push, final Mark policy) {
        Assert.hasText(key);
        Assert.notEmpty(scanMap);
        Assert.notNull(push);
        Assert.notNull(policy);

        try {
            final Map<String, Boolean> response = Maps.newHashMap();
            for (Entry<String, Object> entry : scanMap.entrySet()) {
                final String field = entry.getKey();
                final String value = toJSONString(entry.getValue());
                switch (push) {
                    case LPUSH:
                        switch (policy) {
                            case KEY:
                                cluster.lpush(key, field);
                                break;
                            case VALUE:
                                cluster.lpush(key, value);
                                break;
                            default:
                                throw new RedisClientException("未知的策略(policy)类型");
                        }

                        break;
                    case RPUSH:
                        switch (policy) {
                            case KEY:
                                cluster.rpush(key, field);
                                break;
                            case VALUE:
                                cluster.rpush(key, value);
                                break;
                            default:
                                throw new RedisClientException("未知的策略(policy)类型");
                        }

                        break;
                    default:
                        throw new RedisClientException("未知的写入(PUSH)类型");
                }

                response.put(field, isOK(cluster.set(field, value)));
            }

            return response;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long pushx(final String key, final String[] values, final Mark push) {
        Assert.hasText(key);
        Assert.notEmpty(values);
        Assert.notNull(push);

        try {
            long pushed = 0;
            switch (push) {
                case LPUSH:
                    for (String value : values) {
                        if (pushed == 0) {
                            pushed += cluster.lpushx(key, value);
                        } else if (cluster.lpushx(key, value) > 0) {
                            pushed++;
                        }
                    }
                    break;
                case RPUSH:
                    for (String value : values) {
                        if (pushed == 0) {
                            pushed += cluster.rpushx(key, value);
                        } else if (cluster.rpushx(key, value) > 0) {
                            pushed++;
                        }
                    }
                    break;
                default:
                    throw new RedisClientException("未知的写入(PUSH)类型");
            }

            return pushed;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> lrange(final String key, final int start, final int end) {
        Assert.hasText(key);
        try {
            return cluster.lrange(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> lrangeltrim(final String key, final int count) {
        Assert.hasText(key);
        try {
            final List<String> values = cluster.lrange(key, 0, count);
            if (!ltrim(key, count + 1, -1)) {
                throw new RedisClientException("lrangeltrim error");
            }

            return values;
        } catch (final Throwable e) {
            if (e instanceof RedisClientException) {
                throw e;
            }

            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long lrem(final String key, final int count, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);
        try {
            return cluster.lrem(key, count, value);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean lset(final String key, final int index, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);
        try {
            return isOK(cluster.lset(key, index, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean ltrim(final String key, final int start, final int end) {
        Assert.hasText(key);
        try {
            return isOK(cluster.ltrim(key, start, end));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long sadd(final String key, final String... members) {
        Assert.hasText(key);
        if (members.length == 0) {
            return 0;
        }

        try {
            return cluster.sadd(key, members);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long sreplace(final String key, final String[] oldMembers, final String[] newMembers) {
        Assert.hasText(key);
        try {
            if (!ArrayUtils.isEmpty(oldMembers)) {
                srem(key, oldMembers);
            }

            if (!ArrayUtils.isEmpty(newMembers)) {
                return cluster.sadd(key, newMembers);
            }

            return 0;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long scard(final String key) {
        Assert.hasText(key);
        try {
            return cluster.scard(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Long> scard(final String... keys) {
        if (keys.length == 0) {
            return Collections.emptyMap();
        }

        try {
            final Map<String, Long> response = Maps.newHashMap();
            for (String key : keys) {
                response.put(key, scard(key));
            }

            return response;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> sdiff(final String... keys) {
        throw new UnsupportedAccessException("In RedisCluster mode, sdiff operation is not supported");
    }

    @Override
    public long sdiffstore(final String destination, final String... keys) {
        throw new UnsupportedAccessException("In RedisCluster mode, sdiffstore operation is not supported");
    }

    @Override
    public Set<String> sinter(final String... keys) {
        throw new UnsupportedAccessException("In RedisCluster mode, sinter operation is not supported");
    }

    @Override
    public long sinterstore(final String destination, final String... keys) {
        throw new UnsupportedAccessException("In RedisCluster mode, sinterstore operation is not supported");
    }

    @Override
    public boolean sismember(final String key, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        try {
            return cluster.sismember(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> smembers(final String key) {
        Assert.hasText(key);
        try {
            return cluster.smembers(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean smove(final String source, final String destination, final String member) {
        throw new UnsupportedAccessException("In RedisCluster mode, smove operation is not supported");
    }

    @Override
    public String spop(final String key) {
        Assert.hasText(key);
        try {
            return cluster.spop(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> spop(final String key, final int count) {
        Assert.hasText(key);
        try {
            return cluster.spop(key, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public String srandmember(final String key) {
        Assert.hasText(key);
        try {
            return cluster.srandmember(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> srandmember(final String key, final int count) {
        Assert.hasText(key);
        try {
            return cluster.srandmember(key, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long srem(final String key, final String... members) {
        Assert.hasText(key);
        if (members.length == 0) {
            return 0;
        }

        try {
            return cluster.srem(key, members);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> sunion(final String... keys) {
        throw new UnsupportedAccessException("In RedisCluster mode, sunion operation is not supported");
    }

    @Override
    public long sunionstore(final String destination, final String... keys) {
        throw new UnsupportedAccessException("In RedisCluster mode, sunionstore operation is not supported");
    }
    
    @Override
    public ScanResult<String> sscan(final String key, final long cursor, final ScanParams params) {
        Assert.hasText(key);
        try {
            return cluster.sscan(key, String.valueOf(cursor), params);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zadd(final String key, final double score, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        try {
            return cluster.zadd(key, score, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zadd(final String key, final Map<Object, Double> values) {
        Assert.hasText(key);
        Assert.notEmpty(values);
        try {
            final Map<String, Double> scoreMembers = Maps.newHashMap();
            values.forEach((member, score) -> scoreMembers.put(toJSONString(member), score));
            return cluster.zadd(key, scoreMembers);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zcard(final String key) {
        Assert.hasText(key);
        try {
            return cluster.zcard(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zcount(final String key, final double min, final double max) {
        Assert.hasText(key);
        try {
            return cluster.zcount(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zcount(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        try {
            return cluster.zcount(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zlexcount(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        try {
            return cluster.zlexcount(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public double zincrby(final String key, final double increment, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        try {
            return cluster.zincrby(key, increment, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long end) {
        Assert.hasText(key);
        try {
            return cluster.zrange(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max, final int offset, final int count) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        try {
            return cluster.zrangeByLex(key, min, max, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public <T> Map<T, Double> zrangeWithScores(final String key, final long start, final long end, final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.notNull(type);
        try {
            final Set<Tuple> tuples = cluster.zrangeWithScores(key, start, end);
            if (!CollectionUtils.isEmpty(tuples)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple tuple : tuples) {
                    newValues.put(parseObject(tuple.getElement(), type), tuple.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        try {
            return cluster.zrangeByScore(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max, final int offset, final int count) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        try {
            return cluster.zrangeByScore(key, min, max, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public <T> Map<T, Double> zrangeByScoreWithScores(final String key, final String min, final String max, final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Assert.notNull(type);
        try {
            final Set<Tuple> tuples = cluster.zrangeByScoreWithScores(key, min, max);
            if (!CollectionUtils.isEmpty(tuples)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple tuple : tuples) {
                    newValues.put(parseObject(tuple.getElement(), type), tuple.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public <T> Map<T, Double> zrangeByScoreWithScores(final String key, final String min, final String max, final int offset, final int count,
            final TypeReference<T> type) {
        Assert.hasLength(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Assert.notNull(type);
        try {
            final Set<Tuple> tuples = cluster.zrangeByScoreWithScores(key, min, max, offset, count);
            if (!CollectionUtils.isEmpty(tuples)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple tuple : tuples) {
                    newValues.put(parseObject(tuple.getElement(), type), tuple.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zrank(final String key, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        try {
            return cluster.zrank(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zrem(final String key, final String... members) {
        Assert.hasText(key);
        if (members.length == 0) {
            return 0;
        }

        try {
            return cluster.zrem(key, members);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zremrangeByLex(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        try {
            return cluster.zremrangeByLex(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zremrangeByRank(final String key, final long start, final long end) {
        Assert.hasText(key);
        try {
            return cluster.zremrangeByRank(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zremrangeByScore(final String key, final String start, final String end) {
        Assert.hasText(key);
        Assert.hasText(start);
        Assert.hasText(end);
        try {
            return cluster.zremrangeByScore(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrevrange(final String key, final long start, final long end) {
        Assert.hasText(key);
        try {
            return cluster.zrevrange(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min) {
        Assert.hasText(key);
        Assert.hasText(max);
        Assert.hasText(min);
        try {
            return cluster.zrevrangeByLex(key, max, min);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min, final int offset, final int count) {
        Assert.hasText(key);
        Assert.hasText(max);
        Assert.hasText(min);
        try {
            return cluster.zrevrangeByLex(key, max, min, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public <T> Map<T, Double> zrevrangeWithScores(final String key, final long start, final long end, final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.notNull(type);
        try {
            final Set<Tuple> tuples = cluster.zrevrangeWithScores(key, start, end);
            if (!CollectionUtils.isEmpty(tuples)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple tuple : tuples) {
                    newValues.put(parseObject(tuple.getElement(), type), tuple.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        Assert.hasText(key);
        try {
            return cluster.zrevrangeByScore(key, max, min);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min, final int offset, final int count) {
        Assert.hasText(key);
        try {
            return cluster.zrevrangeByScore(key, max, min, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public <T> Map<T, Double> zrevrangeByScoreWithScores(final String key, final double max, final double min, final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.notNull(type);
        try {
            Set<Tuple> tuples = cluster.zrevrangeByScoreWithScores(key, max, min);
            if (!CollectionUtils.isEmpty(tuples)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple tuple : tuples) {
                    newValues.put(parseObject(tuple.getElement(), type), tuple.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public <T> Map<T, Double> zrevrangeByScoreWithScores(final String key, final double max, final double min, final int offset, final int count,
            final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.notNull(type);
        try {
            Set<Tuple> tuples = cluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
            if (!CollectionUtils.isEmpty(tuples)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple tuple : tuples) {
                    newValues.put(parseObject(tuple.getElement(), type), tuple.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public long zrevrank(final String key, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        try {
            return cluster.zrevrank(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public double zscore(final String key, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        try {
            return cluster.zscore(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    @Override
    public ScanResult<Entry<String, Double>> zscan(final String key, final long cursor, final ScanParams params) {
        Assert.hasText(key);
        Assert.notNull(params);

        try {
            final ScanResult<Tuple> res = cluster.zscan(key, String.valueOf(cursor), params);
            final List<Tuple> tuples = res.getResult();
            if (CollectionUtils.isEmpty(tuples)) {
                return new ScanResult<>(res.getStringCursor(), Collections.emptyList());
            }

            final List<Entry<String, Double>> newTuples = Lists.newArrayList();
            tuples.forEach(tuple -> newTuples.add(new AbstractMap.SimpleEntry<>(tuple.getElement(), tuple.getScore())));
            return new ScanResult<>(res.getStringCursor(), newTuples);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }
}
