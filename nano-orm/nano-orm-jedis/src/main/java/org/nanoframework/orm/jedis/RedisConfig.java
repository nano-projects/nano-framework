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

import java.util.Properties;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.jedis.exception.NotFoundExtendException;

/**
 * RedisClient的连接配置.
 * @author yanghe
 * @since 1.0
 */
public class RedisConfig extends BaseEntity {
    public static final String REDIS = "redis.";
    public static final String ROOT = "redis.root";
    public static final String REDIS_TYPE = "redisType";
    public static final String HOST_NAMES = "hostNames";
    public static final String MAX_TOTAL = "maxTotal";
    public static final String MAX_IDLE = "maxIdle";
    public static final String MIN_IDLE = "minIdle";
    public static final String TIME_OUT = "timeOut";
    public static final String TEST_ON_BORROW = "testOnBorrow";
    public static final String EXPIRE_TIME = "expireTime";
    public static final String EXTEND = "extend";
    public static final String EXTEND_RESOURCE = "extendResource";
    public static final String EXTEND_PROPERTIES = "extendProperties";
    public static final String CLUSTER = "cluster";
    public static final String MAX_REDIRECTIONS = "maxRedirections";
    public static final String LOCK_TIMEOUT = "lockTimeout";
    public static final String LOCK_HASH = "lockGroup";

    private static final long serialVersionUID = -6765559689700998419L;

    private String redisType;
    private String hostNames;
    private Integer maxTotal;
    private Integer maxIdle;
    private Integer minIdle;
    private Integer timeOut;
    private Boolean testOnBorrow;
    private Integer expireTime;

    /**
     * RedisClient扩展.
     * @since 1.3.10
     */
    private String extend;

    /**
     * RedisClient扩展属性路径.
     * @since 1.3.10
     */
    private String extendResource;

    /**
     * RedisClient扩展属性.
     * @since 1.3.10
     */
    private Properties extendProperties;

    /**
     * JedisCluster模式.
     * @since 1.3.12
     */
    private Boolean cluster;

    /**
     * 
     * @since 1.3.12
     */
    private Integer maxRedirections;

    /**
     * 
     * @since 1.4.9
     */
    private Integer lockTimeout;

    /**
     * 
     * @since 1.4.9
     */
    private String lockGroup;

    private RedisConfig() {
    }

    public static final RedisConfig newInstance() {
        return new RedisConfig();
    }

    public String getRedisType() {
        return redisType;
    }

    public void setRedisType(final String redisType) {
        this.redisType = redisType;
    }

    public String getHostNames() {
        return hostNames;
    }

    public void setHostNames(final String hostNames) {
        this.hostNames = hostNames;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(final Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(final Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(final Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(final Integer timeOut) {
        this.timeOut = timeOut;
    }

    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(final Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public Integer getExpireTime() {
        return expireTime == null ? Integer.valueOf(0) : expireTime;
    }

    public void setExpireTime(final Integer expireTime) {
        this.expireTime = expireTime;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(final String extend) {
        if (StringUtils.isNotBlank(extend)) {
            try {
                Class.forName(extend);
            } catch (final ClassNotFoundException e) {
                throw new NotFoundExtendException(e.getMessage(), e);
            }
        }

        this.extend = extend;
    }

    public String getExtendResource() {
        return extendResource;
    }

    public void setExtendResource(final String extendResource) {
        this.extendResource = extendResource;
        if (StringUtils.isNotBlank(extendResource)) {
            Properties extendProperties = PropertiesLoader.PROPERTIES.get(extendResource);
            if (extendProperties == null) {
                extendProperties = PropertiesLoader.load(extendResource);
            }

            if (extendProperties != null) {
                setExtendProperties(extendProperties);
            }
        }
    }

    public Properties getExtendProperties() {
        return extendProperties;
    }

    public void setExtendProperties(final Properties extendProperties) {
        this.extendProperties = extendProperties;
    }

    public Boolean getCluster() {
        return cluster;
    }

    public void setCluster(final Boolean cluster) {
        this.cluster = cluster;
    }

    public Integer getMaxRedirections() {
        return maxRedirections;
    }

    public void setMaxRedirections(final Integer maxRedirections) {
        this.maxRedirections = maxRedirections;
    }

    public Integer getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(final Integer lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public String getLockGroup() {
        return lockGroup;
    }

    public void setLockGroup(final String lockGroup) {
        this.lockGroup = lockGroup;
    }
}
