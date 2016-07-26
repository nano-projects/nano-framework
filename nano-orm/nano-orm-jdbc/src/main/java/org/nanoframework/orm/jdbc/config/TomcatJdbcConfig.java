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
package org.nanoframework.orm.jdbc.config;

import java.util.Properties;

import org.nanoframework.commons.util.Assert;

/**
 *
 * @author yanghe
 * @since 1.3.6
 */
public class TomcatJdbcConfig extends JdbcConfig {
    private static final long serialVersionUID = -841473595983679697L;

    @Property("tomcat.jdbc.pool.initialSize")
    private Integer initialSize;

    @Property("tomcat.jdbc.pool.minIdle")
    private Integer minIdle;

    @Property("tomcat.jdbc.pool.maxWait")
    private Integer maxWait;

    @Property("tomcat.jdbc.pool.maxActive")
    private Integer maxActive;

    @Property("tomcat.jdbc.pool.testWhileIdle")
    private Boolean testWhileIdle;

    @Property("tomcat.jdbc.pool.testOnBorrow")
    private Boolean testOnBorrow;

    @Property("tomcat.jdbc.pool.validationQuery")
    private String validationQuery;

    @Property("tomcat.jdbc.pool.testOnReturn")
    private Boolean testOnReturn;

    @Property("tomcat.jdbc.pool.validationInterval")
    private Long validationInterval;

    @Property("tomcat.jdbc.pool.timeBetweenEvictionRunsMillis")
    private Integer timeBetweenEvictionRunsMillis;

    @Property("tomcat.jdbc.pool.logAbandoned")
    private Boolean logAbandoned;

    @Property("tomcat.jdbc.pool.removeAbandoned")
    private Boolean removeAbandoned;

    @Property("tomcat.jdbc.pool.removeAbandonedTimeout")
    private Integer removeAbandonedTimeout;

    @Property("tomcat.jdbc.pool.minEvictableIdleTimeMillis")
    private Integer minEvictableIdleTimeMillis;

    @Property("tomcat.jdbc.pool.jdbcInterceptors")
    private String jdbcInterceptors;

    @Property("tomcat.jdbc.pool.jmxEnabled")
    private Boolean jmxEnabled;

    public TomcatJdbcConfig() {
        
    }

    public TomcatJdbcConfig(Properties properties) {
        Assert.notNull(properties);
        this.setProperties(properties);
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(Boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public Long getValidationInterval() {
        return validationInterval;
    }

    public void setValidationInterval(Long validationInterval) {
        this.validationInterval = validationInterval;
    }

    public Integer getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Integer getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public Integer getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public Boolean getLogAbandoned() {
        return logAbandoned;
    }

    public void setLogAbandoned(Boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public Boolean getRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(Boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public String getJdbcInterceptors() {
        return jdbcInterceptors;
    }

    public void setJdbcInterceptors(String jdbcInterceptors) {
        this.jdbcInterceptors = jdbcInterceptors;
    }

    public Boolean getJmxEnabled() {
        return jmxEnabled;
    }

    public void setJmxEnabled(Boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
    }

}
