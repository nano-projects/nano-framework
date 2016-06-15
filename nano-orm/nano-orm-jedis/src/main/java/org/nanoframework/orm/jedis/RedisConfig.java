/**
 * Copyright 2015- the original author or authors.
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
package org.nanoframework.orm.jedis;

import org.nanoframework.commons.entity.BaseEntity;

/**
 * RedisClient的连接配置
 * 
 * @author yanghe
 * @date 2015年7月27日 下午10:13:55 
 *
 */
public class RedisConfig extends BaseEntity {
	private static final long serialVersionUID = -6765559689700998419L;
	
	private String redisType;
	private String hostNames;
	private Integer maxTotal;
	private Integer maxIdle;
	private Integer timeOut;
	private Boolean testOnBorrow;
	private Integer expireTime;

	public static final String REDIS = "redis.";
	public static final String ROOT = "redis.root";
	public static final String REDIS_TYPE = "redisType";
	public static final String HOST_NAMES = "hostNames";
	public static final String MAX_TOTAL = "maxTotal";
	public static final String MAX_IDLE = "maxIdle";
	public static final String TIME_OUT = "timeOut";
	public static final String TEST_ON_BORROW = "testOnBorrow";
	public static final String EXPIRE_TIME = "expireTime";

	private RedisConfig() { }

	public static final RedisConfig newInstance() {
		return new RedisConfig();
	}

	public String getRedisType() {
		return redisType;
	}

	public void setRedisType(String redisType) {
		this.redisType = redisType;
	}

	public String getHostNames() {
		return hostNames;
	}

	public void setHostNames(String hostNames) {
		this.hostNames = hostNames;
	}

	public Integer getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Integer getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Integer timeOut) {
		this.timeOut = timeOut;
	}

	public Boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(Boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public Integer getExpireTime() {
		return expireTime == null ? Integer.valueOf(0) : expireTime;
	}

	public void setExpireTime(Integer expireTime) {
		this.expireTime = expireTime;
	}

}
