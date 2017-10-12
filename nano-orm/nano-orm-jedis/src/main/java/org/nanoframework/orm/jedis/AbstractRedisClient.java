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
package org.nanoframework.orm.jedis;

import static org.nanoframework.orm.jedis.RedisClientPool.POOL;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 *
 * @author yanghe
 * @since 1.3.12
 */
public abstract class AbstractRedisClient implements RedisClient {
    /** 默认分隔符 */
    public static final String DEFAULT_SEPARATOR = ",";

    /** 部分操作的返回结果，表示操作成功 */
    public static final String OK = "OK";

    /** 部分操作的返回结果，表示操作成功 */
    public static final long SUCCESS = 1;

    public static final String INF0 = "-inf";
    public static final String INF1 = "+inf";

    protected RedisConfig config;

    public AbstractRedisClient(final String type) {
        config = POOL.getRedisConfig(type);
    }

    public AbstractRedisClient(final RedisConfig config) {
        this.config = config;
    }

    /**
     * FastJson Object to JsonString
     * 
     * @param value
     * @return
     * 
     * @see com.alibaba.fastjson.JSON#toJSONString(Object, SerializerFeature...)
     */
    protected String toJSONString(final Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return JSON.toJSONString(value, SerializerFeature.WriteDateUseDateFormat);
    }

    protected String[] toJSONString(final Object... values) {
        if (values.length == 0) {
            return new String[0];
        }

        final List<String> newValues = Lists.newArrayList();
        for (Object value : values) {
            String jsonValue;
            if ((jsonValue = toJSONString(value)) != null) {
                newValues.add(jsonValue);
            }
        }

        return newValues.toArray(new String[newValues.size()]);
    }

    /**
     * FastJson parse String to Object by TypeReference
     * 
     * @param value
     * @param type
     * @return
     * 
     * @see com.alibaba.fastjson.TypeReference
     * @see com.alibaba.fastjson.JSON#parseObject(String, TypeReference, com.alibaba.fastjson.parser.Feature...)
     */
    @SuppressWarnings("unchecked")
    protected <T> T parseObject(final String value, final TypeReference<T> type) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        if (type.getType() == String.class) {
            return (T) value;
        }

        return JSON.parseObject(value, type);
    }

    protected boolean isOK(final String value) {
        return OK.equals(value);
    }

    protected boolean isSuccess(final long value) {
        return value == SUCCESS ? true : false;
    }

    @Override
    public RedisConfig getConfig() {
        return config;
    }

    protected Map<String, String> info0(final String info) {
        final String[] attributes = info.split("\n");
        final Map<String, String> decodeInfo = Maps.newLinkedHashMap();
        for (final String attribute : attributes) {
            if (!StringUtils.isEmpty(StringUtils.trim(attribute)) && !StringUtils.startsWith(attribute, "#")) {
                final String[] keyvalue = attribute.substring(0, attribute.length() - 1).split(":");
                if (keyvalue.length == 2) {
                    final String key = keyvalue[0];
                    final String value = StringUtils.endsWith(keyvalue[1], "\r") ? StringUtils.substring(keyvalue[1], 0, keyvalue[1].length() - 1)
                            : keyvalue[1];
                    decodeInfo.put(key, value);
                } else {
                    decodeInfo.put(keyvalue[0], "");
                }
            }
        }

        return decodeInfo;
    }

    @Override
    public long del(final List<String> keys) {
        Assert.notNull(keys);
        if (keys.isEmpty()) {
            return 0;
        }

        return del(keys.toArray(new String[keys.size()]));
    }

    @Override
    public long expire(final String key) {
        Assert.hasText(key);
        return expire(key, config.getExpireTime());
    }

    @Override
    public long expireat(final String key, final long timestamp) {
        Assert.hasText(key);
        Long time = (timestamp - System.currentTimeMillis()) / 1000;
        return expire(key, time.intValue());
    }

    @Override
    public long append(final String key, final String value) {
        return append(key, value, DEFAULT_SEPARATOR);
    }

    @Override
    public long append(final String key, final Object value, final String separator) {
        return append(key, toJSONString(value), separator);
    }

    public long append(final String key, final Object value) {
        return append(key, toJSONString(value), DEFAULT_SEPARATOR);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(final String key, final TypeReference<T> type) {
        final String value = get(key);
        if (StringUtils.isBlank(value)) {
            if (String.class.getName().equals(type.getType().getTypeName())) {
                return (T) value;
            }

            return null;
        }

        return parseObject(value, type);
    }

    @Override
    public <T> Map<String, T> get(final String[] keys, final TypeReference<T> type) {
        Assert.notNull(type);

        final Map<String, String> values = get(keys);
        if (values.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, T> newValues = Maps.newHashMap();
        for (Entry<String, String> entry : values.entrySet()) {
            newValues.put(entry.getKey(), parseObject(entry.getValue(), type));
        }

        return newValues;
    }

    @Override
    public <T> Map<String, T> get(final List<String> keys, final TypeReference<T> type) {
        Assert.notEmpty(keys);
        return get(keys.toArray(new String[keys.size()]), type);
    }

    @Override
    public boolean set(final String key, final Object value) {
        Assert.notNull(value);
        return set(key, toJSONString(value));
    }

    @Override
    public boolean setByNX(final String key, final Object value) {
        return setByNX(key, toJSONString(value));
    }

    @Override
    public boolean setByNX(final String key, final Object value, final int timeout) {
        return setByNX(key, toJSONString(value), timeout);
    }

    @Override
    public boolean setByEX(final String key, final String value) {
        return setByEX(key, value, config.getExpireTime());
    }

    @Override
    public boolean setByEX(final String key, final Object value) {
        return setByEX(key, toJSONString(value), config.getExpireTime());
    }

    @Override
    public boolean setByEX(final String key, final Object value, final int seconds) {
        return setByEX(key, toJSONString(value), seconds);
    }

    @Override
    public <T> T hget(final String key, final String field, final TypeReference<T> type) {
        Assert.notNull(type);
        return parseObject(hget(key, field), type);
    }

    @Override
    public <T> Map<String, T> hmget(final String key, final String[] fields, final TypeReference<T> type) {
        Assert.notNull(type);

        final Map<String, String> map = hmget(key, fields);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, T> values = Maps.newHashMap();
        for (Entry<String, String> entry : map.entrySet()) {
            values.put(entry.getKey(), parseObject(entry.getValue(), type));
        }

        return values;
    }

    @Override
    public <T> Map<String, T> hgetAll(final String key, final TypeReference<T> type) {
        Assert.notNull(type);

        final Map<String, String> map = hgetAll(key);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, T> values = Maps.newHashMap();
        for (Entry<String, String> entry : map.entrySet()) {
            values.put(entry.getKey(), parseObject(entry.getValue(), type));
        }

        return values;
    }

    @Override
    public <T> Set<T> hkeys(final String key, final TypeReference<T> type) {
        final Set<String> keys = hkeys(key);
        if (!CollectionUtils.isEmpty(keys)) {
            final Set<T> sets = Sets.newHashSet();
            keys.forEach(item -> sets.add(parseObject(item, type)));
            return sets;
        }

        return Collections.emptySet();
    }

    @Override
    public boolean hset(final String key, final String field, final Object value) {
        return hset(key, field, toJSONString(value));
    }

    @Override
    public boolean hsetByNX(final String key, final String field, final Object value) {
        return hsetByNX(key, field, toJSONString(value));
    }

    @Override
    public <T> List<T> hvals(final String key, final TypeReference<T> type) {
        List<String> vals = hvals(key);
        if (!CollectionUtils.isEmpty(vals)) {
            List<T> lists = Lists.newArrayList();
            vals.forEach(item -> lists.add(parseObject(item, type)));
            return lists;
        }

        return Collections.emptyList();
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(String key, long cursor) {
        return hscan(key, String.valueOf(cursor));
    }

    @Override
    public <T> ScanResult<Entry<String, T>> hscan(String key, long cursor, TypeReference<T> type) {
        return hscan(key, String.valueOf(cursor), type);
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(final String key, final String cursor) {
        return hscan(key, cursor, new ScanParams());
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(String key, long cursor, ScanParams params) {
        return hscan(key, String.valueOf(cursor), params);
    }

    @Override
    public <T> ScanResult<Entry<String, T>> hscan(final String key, final String cursor, final TypeReference<T> type) {
        return hscan(key, cursor, new ScanParams(), type);
    }

    @Override
    public <T> ScanResult<Entry<String, T>> hscan(final String key, long cursor, final ScanParams params, final TypeReference<T> type) {
        return hscan(key, String.valueOf(cursor), new ScanParams(), type);
    }

    @Override
    public <T> ScanResult<Entry<String, T>> hscan(final String key, final String cursor, final ScanParams params, final TypeReference<T> type) {
        final ScanResult<Entry<String, String>> result = hscan(key, cursor, params);
        final List<Entry<String, String>> entrys = result.getResult();
        if (CollectionUtils.isEmpty(entrys)) {
            return new ScanResult<>(result.getStringCursor(), Lists.newArrayList());
        }

        final List<Entry<String, T>> newEntrys = Lists.newArrayList();
        entrys.forEach(entry -> newEntrys.add(new AbstractMap.SimpleEntry<String, T>(entry.getKey(), JSON.parseObject(entry.getValue(), type))));
        return new ScanResult<>(result.getStringCursor(), newEntrys);
    }

    @Override
    public long hincr(final String key, String field) {
        return hincrBy(key, field, 1);
    }

    @Override
    public String bpop(final String key, final Mark pop) {
        return bpop(key, 0, pop);
    }

    @Override
    public <T> T bpop(final String key, final Mark pop, final TypeReference<T> type) {
        return bpop(key, 0, pop, type);
    }

    @Override
    public <T> T bpop(final String key, final int timeout, final Mark pop, final TypeReference<T> type) {
        final String value = bpop(key, timeout, pop);
        if (StringUtils.isNotEmpty(value)) {
            return parseObject(value, type);
        }

        return null;
    }

    @Override
    public <T> Map<String, T> bpop(final String[] keys, final Mark pop, final TypeReference<T> type) {
        final Map<String, String> valuesMap = bpop(keys, pop);
        if (valuesMap != null && !valuesMap.isEmpty()) {
            final Map<String, T> newMap = Maps.newHashMap();
            for (Entry<String, String> entry : valuesMap.entrySet()) {
                newMap.put(entry.getKey(), parseObject(entry.getValue(), type));
            }

            return newMap;
        }

        return Collections.emptyMap();
    }

    @Override
    public <T> Map<String, T> bpop(final String[] keys, final int timeout, final Mark pop, final TypeReference<T> type) {
        final Map<String, String> values = bpop(keys, timeout, pop);
        if (values != null && !values.isEmpty()) {
            final Map<String, T> newMap = Maps.newHashMap();
            for (Entry<String, String> entry : values.entrySet()) {
                newMap.put(entry.getKey(), parseObject(entry.getValue(), type));
            }

            return newMap;
        }

        return Collections.emptyMap();
    }

    @Override
    public String brpoplpush(final String source, final String destination) {
        return brpoplpush(source, destination, 0);
    }

    @Override
    public <T> T brpoplpush(final String source, final String destination, final TypeReference<T> type) {
        return brpoplpush(source, destination, 0, type);
    }

    @Override
    public <T> T brpoplpush(final String source, final String destination, final int timeout, final TypeReference<T> type) {
        final String value = brpoplpush(source, destination, timeout);
        if (StringUtils.isNotEmpty(value)) {
            return parseObject(value, type);
        }

        return null;
    }

    @Override
    public <T> T lindex(final String key, final int index, final TypeReference<T> type) {
        final String value = lindex(key, index);
        if (StringUtils.isNotEmpty(value)) {
            return parseObject(value, type);
        }

        return null;
    }

    @Override
    public long linsert(final String key, final String pivot, final Object value, final Mark position) {
        return linsert(key, pivot, toJSONString(value), position);
    }

    @Override
    public <T> T pop(final String key, final Mark pop, final TypeReference<T> type) {
        final String value = pop(key, pop);
        if (StringUtils.isNotEmpty(value)) {
            return parseObject(value, type);
        }

        return null;
    }

    @Override
    public <T> List<T> pop(final String key, int count, final TypeReference<T> type) {
        Assert.notNull(type);
        final List<String> values = pop(key, count);
        if (!values.isEmpty()) {
            final List<T> newValues = Lists.newArrayList();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptyList();
    }

    @Override
    public boolean push(final String key, final String value, final Mark push) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }

        return push(key, new String[] { value }, push);
    }

    @Override
    public boolean push(final String key, final Object[] values, final Mark push) {
        if (ArrayUtils.isEmpty(values)) {
            return false;
        }

        final List<String> newValues = Lists.newArrayList();
        for (Object value : values) {
            if (value == null) {
                continue;
            }

            newValues.add(toJSONString(value));
        }

        return push(key, newValues.toArray(new String[newValues.size()]), push);
    }

    @Override
    public boolean push(final String key, final Object value, final Mark push) {
        if (value == null) {
            return false;
        }

        return push(key, new String[] { toJSONString(value) }, push);
    }

    @Override
    public boolean push(final String key, final List<Object> values, final Mark push) {
        if (CollectionUtils.isEmpty(values)) {
            return false;
        }

        final List<String> newValues = Lists.newArrayList();
        for (Object value : values) {
            if (value == null) {
                continue;
            }

            newValues.add(toJSONString(value));
        }

        return push(key, newValues.toArray(new String[newValues.size()]), push);
    }

    @Override
    public boolean push(final String key, final String scanKey, final Object value, final Mark push, final Mark policy) {
        return push(key, scanKey, toJSONString(value), push, policy);
    }

    @Override
    public long pushx(final String key, final String value, final Mark push) {
        Assert.hasText(value);
        return pushx(key, new String[] { value }, push);
    }

    @Override
    public long pushx(final String key, final Object[] values, final Mark push) {
        Assert.notEmpty(values);
        final List<String> newValues = Lists.newArrayList();
        for (Object value : values) {
            if (value == null) {
                continue;
            }

            newValues.add(toJSONString(value));
        }

        return pushx(key, newValues.toArray(new String[newValues.size()]), push);
    }

    @Override
    public long pushx(final String key, final Object value, final Mark push) {
        Assert.notNull(value);
        return pushx(key, toJSONString(value), push);
    }

    @Override
    public List<String> lrange(final String key, final int count) {
        return lrange(key, 0, count);
    }

    @Override
    public List<String> lrange(final String key) {
        return lrange(key, 0, -1);
    }

    @Override
    public <T> List<T> lrange(final String key, final int start, final int end, final TypeReference<T> type) {
        Assert.notNull(type);
        final List<String> values = lrange(key, start, end);
        if (!CollectionUtils.isEmpty(values)) {
            final List<T> newValues = Lists.newArrayList();
            for (String value : values) {
                if (StringUtils.isEmpty(value)) {
                    continue;
                }

                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptyList();
    }

    @Override
    public <T> List<T> lrange(final String key, final int count, final TypeReference<T> type) {
        return lrange(key, 0, count, type);
    }

    @Override
    public <T> List<T> lrange(final String key, final TypeReference<T> type) {
        return lrange(key, 0, -1, type);
    }

    @Override
    public <T> List<T> lrangeltrim(final String key, final int count, final TypeReference<T> type) {
        Assert.notNull(type);
        final List<String> values = lrangeltrim(key, count);
        if (!CollectionUtils.isEmpty(values)) {
            final List<T> newValues = Lists.newArrayList();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptyList();
    }

    @Override
    public long lrem(final String key, final String value) {
        return lrem(key, 0, value);
    }

    @Override
    public long lrem(final String key, final int count, final Object value) {
        return lrem(key, count, toJSONString(value));
    }

    @Override
    public long lrem(final String key, final Object value) {
        return lrem(key, 0, value);
    }

    @Override
    public boolean lset(final String key, final int index, final Object value) {
        return lset(key, index, toJSONString(value));
    }

    @Override
    public long sadd(final String key, final Object... members) {
        if (members.length == 0) {
            return 0;
        }

        final List<String> newMembers = Lists.newArrayList();
        for (Object member : members) {
            if (member != null) {
                newMembers.add(toJSONString(member));
            }
        }

        return sadd(key, newMembers.toArray(new String[newMembers.size()]));
    }

    @Override
    public long sreplace(final String key, final Object[] oldMembers, final Object[] newMembers) {
        return sreplace(key, toJSONString(oldMembers), toJSONString(newMembers));
    }

    @Override
    public long sreplace(final String key, final Collection<Object> oldMembers, final Collection<Object> newMembers) {
        final Object[] olds = oldMembers == null || oldMembers.size() == 0 ? new Object[0] : oldMembers.toArray(new Object[oldMembers.size()]);
        final Object[] news = newMembers == null || newMembers.size() == 0 ? new Object[0] : newMembers.toArray(new Object[newMembers.size()]);
        return sreplace(key, olds, news);
    }

    @Override
    public Map<String, Long> scard(final Collection<String> keys) {
        Assert.notEmpty(keys);
        return scard(keys.toArray(new String[keys.size()]));
    }

    @Override
    public <T> Set<T> sdiff(final String[] keys, final TypeReference<T> type) {
        Assert.notNull(type);
        final Set<String> values = sdiff(keys);
        if (!values.isEmpty()) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> sdiff(final Collection<String> keys, final TypeReference<T> type) {
        Assert.notEmpty(keys);
        return sdiff(keys.toArray(new String[keys.size()]), type);
    }

    @Override
    public <T> Set<T> sinter(final String[] keys, final TypeReference<T> type) {
        Assert.notNull(type);
        final Set<String> interSet = sinter(keys);
        if (!interSet.isEmpty()) {
            final Set<T> newInterSet = Sets.newLinkedHashSet();
            for (String value : interSet) {
                newInterSet.add(parseObject(value, type));
            }

            return newInterSet;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> sinter(final Collection<String> keys, final TypeReference<T> type) {
        Assert.notEmpty(keys);
        return sinter(keys.toArray(new String[keys.size()]), type);
    }

    @Override
    public boolean sismember(final String key, final Object member) {
        Assert.notNull(member);
        return sismember(key, toJSONString(member));
    }

    @Override
    public <T> Set<T> smembers(final String key, final TypeReference<T> type) {
        Assert.notNull(type);
        final Set<String> members = smembers(key);
        if (!members.isEmpty()) {
            final Set<T> newMembers = Sets.newLinkedHashSet();
            for (String member : members) {
                newMembers.add(parseObject(member, type));
            }

            return newMembers;
        }

        return Collections.emptySet();
    }

    @Override
    public Map<String, Boolean> smove(final String source, final String destination, final String... members) {
        Assert.notEmpty(members);
        final Map<String, Boolean> values = Maps.newHashMap();
        for (String member : members) {
            values.put(member, smove(source, destination, member));
        }

        return values;
    }

    @Override
    public boolean smove(final String source, final String destination, final Object member) {
        Assert.notNull(member);
        return smove(source, destination, toJSONString(member));
    }

    @Override
    public Map<Object, Boolean> smove(final String source, final String destination, final Object... members) {
        Assert.notEmpty(members);
        final Map<Object, Boolean> values = Maps.newHashMap();
        for (Object member : members) {
            values.put(member, smove(source, destination, member));
        }

        return values;
    }

    @Override
    public <T> T spop(final String key, final TypeReference<T> type) {
        Assert.notNull(type);
        return parseObject(spop(key), type);
    }

    @Override
    public <T> Set<T> spop(final String key, final int count, final TypeReference<T> type) {
        Assert.notNull(type);
        final Set<String> values = spop(key, count);
        if (!values.isEmpty()) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> T srandmember(final String key, final TypeReference<T> type) {
        Assert.notNull(type);
        return parseObject(srandmember(key), type);
    }

    @Override
    public <T> List<T> srandmember(final String key, final int count, final TypeReference<T> type) {
        Assert.notNull(type);
        final List<String> values = srandmember(key, count);
        if (!values.isEmpty()) {
            final List<T> newValues = Lists.newArrayList();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptyList();
    }

    @Override
    public long srem(final String key, final Object... members) {
        if (members.length == 0) {
            return 0;
        }

        final Set<String> newMembers = Sets.newLinkedHashSet();
        for (Object member : members) {
            newMembers.add(toJSONString(member));
        }

        return srem(key, newMembers.toArray(new String[newMembers.size()]));
    }

    @Override
    public <T> Set<T> sunion(final String[] keys, final TypeReference<T> type) {
        Assert.notNull(type);
        final Set<String> values = sunion(keys);
        if (!values.isEmpty()) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public long zadd(final String key, final double score, final Object member) {
        Assert.notNull(member);
        return zadd(key, score, toJSONString(member));
    }

    @Override
    public long zcount(final String key) {
        return zcount(key, INF0, INF1);
    }

    @Override
    public <T> double zincrby(final String key, final double increment, final T member) {
        Assert.notNull(member);
        return zincrby(key, increment, toJSONString(member));
    }

    @Override
    public Set<String> zrange(final String key, final long end) {
        return zrange(key, 0, end);
    }

    @Override
    public Set<String> zrange(final String key) {
        return zrange(key, 0, -1);
    }

    @Override
    public <T> Set<T> zrange(final String key, final long start, final long end, final TypeReference<T> type) {
        Assert.notNull(type);
        final Set<String> values = zrange(key, start, end);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> zrange(final String key, final long end, final TypeReference<T> type) {
        return zrange(key, 0, end, type);
    }

    @Override
    public <T> Set<T> zrange(final String key, final TypeReference<T> type) {
        return zrange(key, 0, -1, type);
    }

    @Override
    public <T> Set<T> zrangeByLex(final String key, final String min, final String max, final int offset, final int count,
            final TypeReference<T> type) {
        final Set<String> values = zrangeByLex(key, min, max, offset, count);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Map<T, Double> zrangeWithScores(final String key, final long end, final TypeReference<T> type) {
        return zrangeWithScores(key, 0, end, type);
    }

    @Override
    public <T> Map<T, Double> zrangeWithScores(final String key, final TypeReference<T> type) {
        return zrangeWithScores(key, 0, -1, type);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        return zrangeByScore(key, String.valueOf(min), String.valueOf(max));
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max, final int offset, final int count) {
        return zrangeByScore(key, String.valueOf(min), String.valueOf(max), offset, count);
    }

    @Override
    public <T> Set<T> zrangeByScore(final String key, final double min, final double max, final TypeReference<T> type) {
        return zrangeByScore(key, String.valueOf(min), String.valueOf(max), type);
    }

    @Override
    public <T> Set<T> zrangeByScore(final String key, final double min, final double max, final int offset, final int count,
            final TypeReference<T> type) {
        return zrangeByScore(key, String.valueOf(min), String.valueOf(max), offset, count, type);
    }

    @Override
    public <T> Set<T> zrangeByScore(final String key, final String min, final String max, final TypeReference<T> type) {
        final Set<String> values = zrangeByScore(key, min, max);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> zrangeByScore(final String key, final String min, final String max, final int offset, final int count,
            final TypeReference<T> type) {
        final Set<String> values = zrangeByScore(key, min, max, offset, count);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Map<T, Double> zrangeByScoreWithScores(final String key, final double min, final double max, final TypeReference<T> type) {
        return zrangeByScoreWithScores(key, String.valueOf(min), String.valueOf(max), type);
    }

    @Override
    public <T> Map<T, Double> zrangeByScoreWithScores(final String key, final double min, final double max, final int offset, final int count,
            final TypeReference<T> type) {
        return zrangeByScoreWithScores(key, String.valueOf(min), String.valueOf(max), offset, count, type);
    }

    @Override
    public <T> long zrank(final String key, T member) {
        return zrank(key, toJSONString(member));
    }

    @Override
    public <T> long zrem(final String key, @SuppressWarnings("unchecked") final T... members) {
        return zrem(key, toJSONString(members));
    }

    @Override
    public long zremrangeByScore(final String key, final double min, final double max) {
        return zremrangeByScore(key, String.valueOf(min), String.valueOf(max));
    }

    @Override
    public Set<String> zrevrange(final String key, final long end) {
        return zrevrange(key, 0, end);
    }

    @Override
    public Set<String> zrevrange(final String key) {
        return zrevrange(key, 0, -1);
    }

    @Override
    public <T> Set<T> zrevrange(final String key, final long start, final long end, final TypeReference<T> type) {
        final Set<String> values = zrevrange(key, start, end);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> zrevrange(final String key, final long end, final TypeReference<T> type) {
        return zrevrange(key, 0, end, type);
    }

    @Override
    public <T> Set<T> zrevrange(final String key, final TypeReference<T> type) {
        return zrevrange(key, 0, -1, type);
    }

    @Override
    public <T> Set<T> zrevrangeByLex(final String key, final String max, final String min, final TypeReference<T> type) {
        final Set<String> values = zrevrangeByLex(key, max, min);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> zrevrangeByLex(final String key, final String max, final String min, final int offset, final int count,
            final TypeReference<T> type) {
        final Set<String> values = zrevrangeByLex(key, max, min, offset, count);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Map<T, Double> zrevrangeWithScores(final String key, final long end, final TypeReference<T> type) {
        return zrevrangeWithScores(key, 0, end, type);
    }

    @Override
    public <T> Map<T, Double> zrevrangeWithScores(final String key, final TypeReference<T> type) {
        return zrevrangeWithScores(key, 0, -1, type);
    }

    @Override
    public <T> Set<T> zrevrangeByScore(final String key, final double max, final double min, final TypeReference<T> type) {
        final Set<String> values = zrevrangeByScore(key, max, min);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> Set<T> zrevrangeByScore(final String key, final double max, final double min, final int offset, final int count,
            final TypeReference<T> type) {
        final Set<String> values = zrevrangeByScore(key, max, min, offset, count);
        if (!CollectionUtils.isEmpty(values)) {
            final Set<T> newValues = Sets.newLinkedHashSet();
            for (String value : values) {
                newValues.add(parseObject(value, type));
            }

            return newValues;
        }

        return Collections.emptySet();
    }

    @Override
    public <T> long zrevrank(final String key, final T member) {
        return zrevrank(key, toJSONString(member));
    }

    @Override
    public <T> double zscore(final String key, final T member) {
        return zscore(key, toJSONString(member));
    }

}
