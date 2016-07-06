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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.orm.jedis.cluster.RedisClusterClientImpl;
import org.nanoframework.orm.jedis.exception.RedisClientException;
import org.nanoframework.orm.jedis.sharded.RedisClientImpl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

/**
 * RedisClient连接池管理类.
 * 
 * @author yanghe
 * @since 1.0
 */
public class RedisClientPool {
    /** RedisClientPool. */
    public static final RedisClientPool POOL = new RedisClientPool();

    public static final String MAIN_REDIS = "/redis.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClientPool.class);

    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_MAX_REDIRECTIONS = 5;
    private static final int DEFAULT_MAX_TOTAL = 100;
    private static final int DEFAULT_MAX_IDLE = 30;
    private static final int DEFAULT_MIN_IDLE = 10;
    private static final Boolean DEFAULT_TEST_ON_BORROW = Boolean.FALSE;
    
    // REDIS连接池，可以对应的是操作类型
    private Map<String, ShardedJedisPool> jedisPool = Maps.newHashMap();

    private Map<String, JedisCluster> jedisClusterPool = Maps.newHashMap();

    private Map<String, RedisConfig> redisConfigs = Maps.newLinkedHashMap();
    
    public RedisClientPool initRedisConfig(final List<Properties> redis) throws LoaderException, IOException {
        if (redis == null || redis.isEmpty()) {
            return this;
        }

        return initRedisConfig(redis.toArray(new Properties[redis.size()]));
    }

    public RedisClientPool initRedisConfig(final Properties... redis) throws LoaderException, IOException {
        List<Properties> redises = new ArrayList<>();
        if (redis == null || redis.length == 0) {
            redises.add(PropertiesLoader.load(MAIN_REDIS));
        } else {
            redises.addAll(Arrays.asList(redis));
        }

        /** 修正了因有多个idx导致的config加载的BUG. */
        for (Properties rds : redises) {
            final String root = rds.getProperty(RedisConfig.ROOT);
            if (StringUtils.isNotEmpty(root)) {
                final Map<String, RedisConfig> confs = Maps.newHashMap();
                final String[] idxs = root.split(",");
                for (String idx : idxs) {
                    final RedisConfig conf = RedisConfig.newInstance();
                    for (String name : conf.attributeNames()) {
                        if (RedisConfig.REDIS_TYPE.equals(name)) {
                            Assert.hasLength(rds.getProperty(RedisConfig.REDIS + idx + '.' + name));
                        } else if (RedisConfig.EXTEND_PROPERTIES.equals(name)) {
                            continue;
                        }

                        conf.setAttributeValue(name, rds.getProperty(RedisConfig.REDIS + idx + '.' + name));
                    }

                    confs.put(conf.getRedisType(), conf);
                }

                redisConfigs.putAll(confs);
            }
        }

        return this;
    }

    // 初始化连接池
    public void createJedis() {
        redisConfigs.values().stream().filter(conf -> conf.getCluster() == null || !conf.getCluster())
        .forEach(conf -> jedisPool.put(conf.getRedisType(), createJedisPool(conf)));

        redisConfigs.values().stream().filter(conf -> conf.getCluster() != null && conf.getCluster())
        .forEach(conf -> jedisClusterPool.put(conf.getRedisType(), createJedisClusterPool(conf)));
    }
    
    public ShardedJedisPool appendJedis(final RedisConfig conf) {
        Assert.notNull(conf);
        Assert.hasLength(conf.getRedisType());

        if(conf.getCluster() == null || !conf.getCluster()) {
            if (!jedisPool.containsKey(conf.getRedisType())) {
                redisConfigs.put(conf.getRedisType(), conf);
                final ShardedJedisPool pool;
                jedisPool.put(conf.getRedisType(), pool = createJedisPool(conf));
                bindGlobal(conf);
                return pool;
            }
            
            return jedisPool.get(conf.getRedisType());
        }

        throw new RedisClientException("Can't append ShardedJedis, this is a redis cluster config");
    }
    
    public JedisCluster appendJedisCluster(RedisConfig conf) {
        Assert.notNull(conf);
        Assert.hasLength(conf.getRedisType());
        
        if(conf.getCluster() != null && conf.getCluster()) {
            if(!jedisClusterPool.containsKey(conf.getRedisType())) {
                redisConfigs.put(conf.getRedisType(), conf);
                final JedisCluster cluster;
                jedisClusterPool.put(conf.getRedisType(), cluster = createJedisClusterPool(conf));
                bindGlobal(conf);
                return cluster;
            }
        }
        
        throw new RedisClientException("Can't append JedisCluster, this is a redis sharded config");
    }

    public void bindGlobal() {
        for (Entry<String, RedisConfig> entry : redisConfigs.entrySet()) {
            bindGlobal(entry.getValue());
        }

        LOGGER.info("RedisClient Pools: " + GlobalRedisClient.keys());
    }
    
    public void bindGlobal(final RedisConfig conf) {
        final RedisClient redisClient;
        final String extend = conf.getExtend();
        if(conf.getCluster() == null || !conf.getCluster()) {
            if (StringUtils.isNotBlank(extend)) {
                try {
                    final Class<?> cls = Class.forName(extend);
                    if(RedisClientImpl.class.isAssignableFrom(cls)) {
                        redisClient = ReflectUtils.newInstance(extend, conf.getRedisType());
                    } else {
                        throw new RedisClientException("The extend class must inherit <" + RedisClientImpl.class.getName() + '>');
                    }
                } catch(final ClassNotFoundException e) {
                    throw new RedisClientException(e);
                }
            } else {
                redisClient = new RedisClientImpl(conf.getRedisType());
            }
        } else {
            if(StringUtils.isNotBlank(extend)) {
                try {
                    final Class<?> cls = Class.forName(extend);
                    if(RedisClusterClientImpl.class.isAssignableFrom(cls)) {
                        redisClient = ReflectUtils.newInstance(extend, conf.getRedisType());
                    } else {
                        throw new RedisClientException("The extend class must inherit <" + RedisClusterClientImpl.class.getName() + '>');
                    }
                } catch(final ClassNotFoundException e) {
                    throw new RedisClientException(e);
                }
            } else {
                redisClient = new RedisClusterClientImpl(conf.getRedisType());
            }
        }
        
        GlobalRedisClient.set(conf.getRedisType(), redisClient);
    }

    // 根据连接池名，取得连接
    public ShardedJedis getJedis(final String poolName) {
        Assert.hasText(poolName);
        ShardedJedis shardedJedis = null;
        try {
            final ShardedJedisPool pool = jedisPool.get(poolName);
            if (pool != null) {
                shardedJedis = pool.getResource();
            }

            Assert.notNull(shardedJedis, "Not found ShardedJedis.");
            return shardedJedis;
        } catch (final Throwable e) {
            close(shardedJedis);
            throw new RedisClientException(e.getMessage(), e);
        }
    }
    
    public JedisCluster getJedisCluster(final String poolName) {
        Assert.hasText(poolName);
        final JedisCluster cluster = jedisClusterPool.get(poolName);
        Assert.notNull(cluster, "Not found JedisCluster.");
        return cluster;
    }

    // 创建连接池
    private ShardedJedisPool createJedisPool(final RedisConfig conf) {
        Assert.notNull(conf);
        try {
            final String[] hostAndports = conf.getHostNames().split(";");
            final List<String> redisHosts = Lists.newArrayList();
            final List<Integer> redisPorts = Lists.newArrayList();
            for (int i = 0; i < hostAndports.length; i++) {
                final String[] hostPort = hostAndports[i].split(":");
                redisHosts.add(hostPort[0]);
                redisPorts.add(Integer.valueOf(hostPort[1]));
            }

            final List<JedisShardInfo> shards = Lists.newArrayList();
            for (int i = 0; i < redisHosts.size(); i++) {
                final String host = (String) redisHosts.get(i);
                final Integer port = (Integer) redisPorts.get(i);
                Integer timeout = conf.getTimeOut();
                if (timeout == null || timeout < 0) {
                    timeout = DEFAULT_TIMEOUT;
                }
                
                JedisShardInfo si = new JedisShardInfo(host, port.intValue(), timeout);
                shards.add(si);
            }

            return new ShardedJedisPool(getJedisPoolConfig(conf), shards, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    private JedisCluster createJedisClusterPool(final RedisConfig config) {
        Assert.notNull(config);
        try {
            final String[] hostAndports = config.getHostNames().split(";");
            final List<String> redisHosts = Lists.newArrayList();
            final List<Integer> redisPorts = Lists.newArrayList();
            for (int i = 0; i < hostAndports.length; i++) {
                final String[] hostPort = hostAndports[i].split(":");
                redisHosts.add(hostPort[0]);
                redisPorts.add(Integer.valueOf(hostPort[1]));
            }

            final Set<HostAndPort> nodes = Sets.newLinkedHashSet();
            for (int i = 0; i < redisHosts.size(); i++) {
                final String host = (String) redisHosts.get(i);
                final int port = (Integer) redisPorts.get(i);
                nodes.add(new HostAndPort(host, port));
            }

            Integer timeout = config.getTimeOut();
            if (timeout == null || timeout < 0) {
                timeout = DEFAULT_TIMEOUT;
            }

            Integer maxRedirections = config.getMaxRedirections();
            if (maxRedirections == null || maxRedirections < 0) {
                maxRedirections = DEFAULT_MAX_REDIRECTIONS;
            }

            return new JedisCluster(nodes, timeout, maxRedirections, getJedisPoolConfig(config));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    // 设置redis连接池的属性
    private JedisPoolConfig getJedisPoolConfig(final RedisConfig conf) {
        Assert.notNull(conf);

        Integer maxTotal = conf.getMaxTotal();
        if(maxTotal == null) {
            maxTotal = DEFAULT_MAX_TOTAL;
        }
        
        Integer maxIdle = conf.getMaxIdle();
        if(maxIdle == null) {
            maxIdle = DEFAULT_MAX_IDLE;
        }
        
        Integer minIdle = conf.getMinIdle();
        if(minIdle == null) {
            minIdle = DEFAULT_MIN_IDLE;
        }
        
        Boolean testOnBorrow = conf.getTestOnBorrow();
        if(testOnBorrow == null) {
            testOnBorrow = DEFAULT_TEST_ON_BORROW;
        }
        
        final JedisPoolConfig poolConf = new JedisPoolConfig();
        poolConf.setMaxTotal(maxTotal);
        poolConf.setMaxIdle(maxIdle);
        poolConf.setTestOnBorrow(testOnBorrow);
        poolConf.setMinIdle(minIdle);
        return poolConf;
    }

    public Map<String, RedisConfig> getRedisConfigs() {
        return redisConfigs;
    }

    public RedisConfig getRedisConfig(final String redisType) {
        Assert.hasLength(redisType);
        final RedisConfig conf;
        if ((conf = redisConfigs.get(redisType)) == null) {
            throw new RedisClientException("无效的RedisType");
        }

        return conf;
    }

    public void close(final ShardedJedis shardedJedis) {
        if (shardedJedis == null) {
            return;
        }

        shardedJedis.close();
    }
}
