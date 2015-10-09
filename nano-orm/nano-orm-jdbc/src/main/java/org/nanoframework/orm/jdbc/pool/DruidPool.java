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
package org.nanoframework.orm.jdbc.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;
import org.nanoframework.orm.jdbc.binding.JdbcManager;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;

/**
 * @author yanghe
 * @date 2015年9月30日 下午3:50:14
 */
public class DruidPool implements Pool {
	private Logger LOG = LoggerFactory.getLogger(DruidPool.class);
	private ConcurrentMap<String, DataSource> dataSources = new ConcurrentHashMap<>();

	Class<?> DruidDataSource; { 
		try { 
			DruidDataSource = Class.forName("com.alibaba.druid.pool.DruidDataSource");
		} catch(ClassNotFoundException e) {
			LOG.warn("Unload class [ com.alibaba.druid.pool.DruidDataSource ]");
		}
	}
	
	public DruidPool(Collection<JdbcConfig> configs) {
		Assert.notNull(configs);
		Assert.notEmpty(configs);
		
		if(dataSources != null && !dataSources.isEmpty())
			closeAndClear();
		
		List<DruidJdbcConfig> _configs = new ArrayList<>();
		configs.forEach(config -> _configs.add((DruidJdbcConfig) config));
		for(DruidJdbcConfig config : _configs) {
			DataSource dataSource;
			try {
				dataSource = (DataSource) DruidDataSource.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Instance Constructor Exception: " + DruidDataSource.getName());
			}
			
			try { 
				DruidDataSource.getMethod("setDriverClassName", String.class).invoke(dataSource, config.getDriver());
				DruidDataSource.getMethod("setUrl", String.class).invoke(dataSource, config.getUrl());
				DruidDataSource.getMethod("setUsername", String.class).invoke(dataSource, config.getUserName());
				DruidDataSource.getMethod("setPassword", String.class).invoke(dataSource, config.getPasswd());
			
				if(config.getInitialSize() != null)
					DruidDataSource.getMethod("setInitialSize", int.class).invoke(dataSource, config.getInitialSize());
				
				if(config.getMaxActive() != null)
					DruidDataSource.getMethod("setMaxActive", int.class).invoke(dataSource, config.getMaxActive());
				
				if(config.getMaxIdle() != null)
					DruidDataSource.getMethod("setMaxIdle", int.class).invoke(dataSource, config.getMaxIdle());
				
				if(config.getMinIdle() != null)
					DruidDataSource.getMethod("setMinIdle", int.class).invoke(dataSource, config.getMinIdle());
				
				if(config.getMaxWait() != null) 
					DruidDataSource.getMethod("setMaxWait", long.class).invoke(dataSource, config.getMaxWait());
				
				if(config.getRemoveAbandoned() != null)
					DruidDataSource.getMethod("setRemoveAbandoned", boolean.class).invoke(dataSource, config.getRemoveAbandoned());
				
				if(config.getRemoveAbandonedTimeout() != null)
					DruidDataSource.getMethod("setRemoveAbandonedTimeout", int.class).invoke(dataSource, config.getRemoveAbandonedTimeout());
				
				if(config.getTimeBetweenEvictionRunsMillis() != null)
					DruidDataSource.getMethod("setTimeBetweenEvictionRunsMillis", long.class).invoke(dataSource, config.getTimeBetweenEvictionRunsMillis());
				
				if(config.getMinEvictableIdleTimeMillis() != null)
					DruidDataSource.getMethod("setMinEvictableIdleTimeMillis", long.class).invoke(dataSource, config.getMinEvictableIdleTimeMillis());
				
				if(config.getValidationQuery() != null)
					DruidDataSource.getMethod("setValidationQuery", String.class).invoke(dataSource, config.getValidationQuery());
				
				if(config.getTestWhileIdle() != null) 
					DruidDataSource.getMethod("setTestWhileIdle", boolean.class).invoke(dataSource, config.getTestWhileIdle());
				
				if(config.getTestOnBorrow() != null)
					DruidDataSource.getMethod("setTestOnBorrow", boolean.class).invoke(dataSource, config.getTestOnBorrow());
				
				if(config.getTestOnReturn() != null)
					DruidDataSource.getMethod("setTestOnReturn", boolean.class).invoke(dataSource, config.getTestOnReturn());
				
				if(config.getPoolPreparedStatements() != null)
					DruidDataSource.getMethod("setPoolPreparedStatements", boolean.class).invoke(dataSource, config.getPoolPreparedStatements());
				
				if(config.getMaxPoolPreparedStatementPerConnectionSize() != null) 
					DruidDataSource.getMethod("setMaxPoolPreparedStatementPerConnectionSize", int.class).invoke(dataSource, config.getMaxPoolPreparedStatementPerConnectionSize());
				
				if(config.getFilters() != null)
					DruidDataSource.getMethod("setFilters", String.class).invoke(dataSource, config.getFilters());
				
			} catch(Exception e) {
				throw new IllegalArgumentException("设置参数异常: " + e.getMessage());
			}
			
			dataSources.put(config.getEnvironmentId(), dataSource);
			
			/** 创建并设置全局Jdbc管理类 */
			GlobalJdbcManager.set(config.getEnvironmentId(), JdbcManager.newInstance(config, dataSource));
		}
	}
	
	@Override
	public void closeAndClear() {
		dataSources.forEach((envId, dataSource) -> { 
			try { DruidDataSource.getMethod("close").invoke(dataSource); } catch(Exception e) { }
		});
		
		dataSources.clear();
		
	}
	
	@Override
	public DataSource getPool(String envId) {
		return dataSources.get(envId);
	}
}
