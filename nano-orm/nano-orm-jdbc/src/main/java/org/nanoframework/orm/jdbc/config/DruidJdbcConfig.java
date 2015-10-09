/**
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.orm.jdbc.config;

import java.util.Properties;

import org.nanoframework.commons.annatations.Property;
import org.nanoframework.commons.util.Assert;

/**
 * @author yanghe
 * @date 2015年9月30日 下午3:17:56
 */
public class DruidJdbcConfig extends JdbcConfig {

	@Property(name = "druid.initialSize")
	private Integer initialSize;

	@Property(name = "druid.maxActive")
	private Integer maxActive;

	@Property(name = "druid.maxIdle")
	private Integer maxIdle;

	@Property(name = "druid.minIdle")
	private Integer minIdle;

	@Property(name = "druid.maxWait")
	private Long maxWait;

	@Property(name = "druid.removeAbandoned")
	private Boolean removeAbandoned;

	@Property(name = "druid.removeAbandonedTimeout")
	private Integer removeAbandonedTimeout;

	@Property(name = "druid.timeBetweenEvictionRunsMillis")
	private Long timeBetweenEvictionRunsMillis;

	@Property(name = "druid.minEvictableIdleTimeMillis")
	private Long minEvictableIdleTimeMillis;

	@Property(name = "druid.validationQuery")
	private String validationQuery;

	@Property(name = "druid.testWhileIdle")
	private Boolean testWhileIdle;

	@Property(name = "druid.testOnBorrow")
	private Boolean testOnBorrow;

	@Property(name = "druid.testOnReturn")
	private Boolean testOnReturn;

	@Property(name = "druid.poolPreparedStatements")
	private Boolean poolPreparedStatements;

	@Property(name = "druid.maxPoolPreparedStatementPerConnectionSize")
	private Integer maxPoolPreparedStatementPerConnectionSize;

	@Property(name = "druid.filters")
	private String filters;

	public DruidJdbcConfig() {

	}

	public DruidJdbcConfig(Properties properties) throws IllegalArgumentException, IllegalAccessException {
		Assert.notNull(properties);
		this.setProperties(properties, this.getClass());
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public Long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(Long maxWait) {
		this.maxWait = maxWait;
	}

	public Boolean getRemoveAbandoned() {
		return removeAbandoned;
	}

	public void setRemoveAbandoned(Boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
	}

	public Integer getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}

	public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
		this.removeAbandonedTimeout = removeAbandonedTimeout;
	}

	public Long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public Long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
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

	public Boolean getTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(Boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public Boolean getPoolPreparedStatements() {
		return poolPreparedStatements;
	}

	public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
		this.poolPreparedStatements = poolPreparedStatements;
	}

	public Integer getMaxPoolPreparedStatementPerConnectionSize() {
		return maxPoolPreparedStatementPerConnectionSize;
	}

	public void setMaxPoolPreparedStatementPerConnectionSize(Integer maxPoolPreparedStatementPerConnectionSize) {
		this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

}
