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
package org.nanoframework.orm.jdbc.pool;

import java.beans.PropertyVetoException;
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
import org.nanoframework.orm.jdbc.config.C3P0JdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;

/**
 * C3P0连接池管理类
 * 
 * @author yanghe
 * @since 1.2
 */
public class C3P0Pool implements Pool {
	private Logger LOG = LoggerFactory.getLogger(C3P0Pool.class);
	private ConcurrentMap<String, DataSource> dataSources = new ConcurrentHashMap<>();

	Class<?> ComboPooledDataSource; { 
		try { 
			ComboPooledDataSource = Class.forName("com.mchange.v2.c3p0.ComboPooledDataSource");
		} catch(ClassNotFoundException e) {
			LOG.warn("Unload class [ com.mchange.v2.c3p0.ComboPooledDataSource ]");
		}
	}
	
	public C3P0Pool(Collection<JdbcConfig> configs) throws PropertyVetoException {
		Assert.notNull(configs);
		Assert.notEmpty(configs);
		
		if(dataSources != null && !dataSources.isEmpty())
			closeAndClear();
		
		List<C3P0JdbcConfig> _configs = new ArrayList<>();
		configs.forEach(config -> _configs.add((C3P0JdbcConfig) config));
		
		for(C3P0JdbcConfig config : _configs) {
			DataSource dataSource;
			try {
				dataSource = (DataSource) ComboPooledDataSource.getConstructor(String.class).newInstance(config.getEnvironmentId());
			} catch (Exception e) {
				throw new RuntimeException("Instance Constructor Exception: " + ComboPooledDataSource.getName());
			}
			
			try { 
				ComboPooledDataSource.getMethod("setDriverClass", String.class).invoke(dataSource, config.getDriver());
				ComboPooledDataSource.getMethod("setJdbcUrl", String.class).invoke(dataSource, config.getUrl());
				ComboPooledDataSource.getMethod("setUser", String.class).invoke(dataSource, config.getUserName());
				ComboPooledDataSource.getMethod("setPassword", String.class).invoke(dataSource, config.getPasswd());
				
				if(config.getAcquireIncrement() != null)
					ComboPooledDataSource.getMethod("setAcquireIncrement", int.class).invoke(dataSource, config.getAcquireIncrement());
				
				if(config.getAcquireRetryAttempts() != null)
					ComboPooledDataSource.getMethod("setAcquireRetryAttempts", int.class).invoke(dataSource, config.getAcquireRetryAttempts());
				
				if(config.getAcquireRetryDelay() != null)
					ComboPooledDataSource.getMethod("setAcquireRetryDelay", int.class).invoke(dataSource, config.getAcquireRetryDelay());
				
				if(config.getAutoCommitOnClose() != null)
					ComboPooledDataSource.getMethod("setAcquireRetryDelay", boolean.class).invoke(dataSource, config.getAutoCommitOnClose());
				
				if(config.getAutomaticTestTable() != null)
					ComboPooledDataSource.getMethod("setAutomaticTestTable", String.class).invoke(dataSource, config.getAutomaticTestTable());
				
				if(config.getBreakAfterAcquireFailure() != null)
					ComboPooledDataSource.getMethod("setBreakAfterAcquireFailure", boolean.class).invoke(dataSource, config.getBreakAfterAcquireFailure());
				
				if(config.getCheckoutTimeout() != null)
					ComboPooledDataSource.getMethod("setCheckoutTimeout", int.class).invoke(dataSource, config.getCheckoutTimeout());
				
				if(config.getConnectionTesterClassName() != null)
					ComboPooledDataSource.getMethod("setConnectionTesterClassName", String.class).invoke(dataSource, config.getConnectionTesterClassName());
				
				if(config.getFactoryClassLocation() != null)
					ComboPooledDataSource.getMethod("setFactoryClassLocation", String.class).invoke(dataSource, config.getFactoryClassLocation());
				
				if(config.getIdleConnectionTestPeriod() != null)
					ComboPooledDataSource.getMethod("setIdleConnectionTestPeriod", int.class).invoke(dataSource, config.getIdleConnectionTestPeriod());
				
				if(config.getInitialPoolSize() != null)
					ComboPooledDataSource.getMethod("setInitialPoolSize", int.class).invoke(dataSource, config.getInitialPoolSize());
				
				if(config.getMaxIdleTime() != null)
					ComboPooledDataSource.getMethod("setMaxIdleTime", int.class).invoke(dataSource, config.getMaxIdleTime());
				
				if(config.getMaxPoolSize() != null)
					ComboPooledDataSource.getMethod("setMaxPoolSize", int.class).invoke(dataSource, config.getMaxPoolSize());
				
				if(config.getMaxStatements() != null)
					ComboPooledDataSource.getMethod("setMaxStatements", int.class).invoke(dataSource, config.getMaxStatements());
				
				if(config.getMaxStatementsPerConnection() != null)
					ComboPooledDataSource.getMethod("setMaxStatementsPerConnection", int.class).invoke(dataSource, config.getMaxStatementsPerConnection());
				
				if(config.getNumHelperThreads() != null)
					ComboPooledDataSource.getMethod("setNumHelperThreads", int.class).invoke(dataSource, config.getNumHelperThreads());
				
				if(config.getOverrideDefaultUser() != null)
					ComboPooledDataSource.getMethod("setOverrideDefaultUser", String.class).invoke(dataSource, config.getOverrideDefaultUser());
				
				if(config.getOverrideDefaultPassword() != null)
					ComboPooledDataSource.getMethod("setOverrideDefaultPassword", String.class).invoke(dataSource, config.getOverrideDefaultPassword());
				
				if(config.getPreferredTestQuery() != null)
					ComboPooledDataSource.getMethod("setPreferredTestQuery", String.class).invoke(dataSource, config.getPreferredTestQuery());
				
				if(config.getPropertyCycle() != null)
					ComboPooledDataSource.getMethod("setPropertyCycle", int.class).invoke(dataSource, config.getPropertyCycle());
				
				if(config.getTestConnectionOnCheckout() != null)
					ComboPooledDataSource.getMethod("setTestConnectionOnCheckout", boolean.class).invoke(dataSource, config.getTestConnectionOnCheckout());
				
				if(config.getTestConnectionOnCheckin() != null)
					ComboPooledDataSource.getMethod("setTestConnectionOnCheckin", boolean.class).invoke(dataSource, config.getTestConnectionOnCheckin());
				
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
			try { ComboPooledDataSource.getMethod("close").invoke(dataSource); } catch(Exception e) { }
		});
		
		dataSources.clear();
		
	}
	
	@Override
	public DataSource getPool(String envId) {
		return dataSources.get(envId);
	}
	
}
