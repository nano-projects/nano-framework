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
package org.nanoframework.orm.mybatis.plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.datasource.DataSourceException;
import org.nanoframework.orm.jdbc.config.TomcatJdbcConfig;

import com.google.common.collect.Maps;

/**
 * 
 *
 * @author yanghe
 * @since 1.3.6
 */
public class TomcatJdbcPoolDataSourceFactory extends AbstractDataSourceFactory {
    private Class<?> TomcatJdbcDataSource;
    
	public TomcatJdbcPoolDataSourceFactory() {
		try {
		    TomcatJdbcDataSource = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
		    this.dataSource = (DataSource) TomcatJdbcDataSource.newInstance();
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
		    throw new DataSourceException(e.getMessage(), e);
		}
	}
	
	@Override
	public void setProperties(Properties properties) {
	    try {
	        Map<String, Object> map = Maps.newHashMap();
	        properties.forEach((key, value) -> {
	            if(StringUtils.isNotEmpty((String) value) && ((String) value).startsWith("${") && ((String) value).endsWith("}")) {
                    return;
                }
	            
	            map.put((String) key, value);   
	        });
	        
	        TomcatJdbcConfig config = TomcatJdbcConfig.mapToBean(map, TomcatJdbcConfig.class);
    	    if(TomcatJdbcDataSource != null) {
    	        TomcatJdbcDataSource.getMethod("setDriverClassName", String.class).invoke(dataSource, config.getDriver());
                TomcatJdbcDataSource.getMethod("setUrl", String.class).invoke(dataSource, config.getUrl());
                TomcatJdbcDataSource.getMethod("setUsername", String.class).invoke(dataSource, config.getUserName());
                TomcatJdbcDataSource.getMethod("setPassword", String.class).invoke(dataSource, config.getPasswd());
                
                if(config.getInitialSize() != null) {
                    TomcatJdbcDataSource.getMethod("setInitialSize", int.class).invoke(dataSource, config.getInitialSize());
                }
                
                if(config.getMinIdle() != null) {
                    TomcatJdbcDataSource.getMethod("setMinIdle", int.class).invoke(dataSource, config.getMinIdle());
                }
                
                if(config.getMaxWait() != null) {
                    TomcatJdbcDataSource.getMethod("setMaxWait", int.class).invoke(dataSource, config.getMaxWait());
                }
                
                if(config.getMaxActive() != null) {
                    TomcatJdbcDataSource.getMethod("setMaxActive", int.class).invoke(dataSource, config.getMaxActive());
                }
                
                if(config.getTestWhileIdle() != null) {
                    TomcatJdbcDataSource.getMethod("setTestWhileIdle", boolean.class).invoke(dataSource, config.getTestWhileIdle());
                }
                
                if(config.getTestOnBorrow() != null) {
                    TomcatJdbcDataSource.getMethod("setTestOnBorrow", boolean.class).invoke(dataSource, config.getTestOnBorrow());
                }
                
                if(config.getValidationInterval() != null) {
                    TomcatJdbcDataSource.getMethod("setValidationInterval", long.class).invoke(dataSource, config.getValidationInterval());
                }
                
                if(config.getTimeBetweenEvictionRunsMillis() != null) {
                    TomcatJdbcDataSource.getMethod("setTimeBetweenEvictionRunsMillis", int.class).invoke(dataSource, config.getTimeBetweenEvictionRunsMillis());
                }
                
                if(config.getLogAbandoned() != null) {
                    TomcatJdbcDataSource.getMethod("setLogAbandoned", boolean.class).invoke(dataSource, config.getLogAbandoned());
                }
                
                if(config.getRemoveAbandoned() != null) {
                    TomcatJdbcDataSource.getMethod("setRemoveAbandoned", boolean.class).invoke(dataSource, config.getRemoveAbandoned());
                }
                
                if(config.getRemoveAbandonedTimeout() != null) {
                    TomcatJdbcDataSource.getMethod("setRemoveAbandonedTimeout", int.class).invoke(dataSource, config.getRemoveAbandonedTimeout());
                }
                
                if(config.getMinEvictableIdleTimeMillis() != null) {
                    TomcatJdbcDataSource.getMethod("setMinEvictableIdleTimeMillis", int.class).invoke(dataSource, config.getMinEvictableIdleTimeMillis());
                }
                
                if(config.getJdbcInterceptors() != null) {
                    TomcatJdbcDataSource.getMethod("setJdbcInterceptors", String.class).invoke(dataSource, config.getJdbcInterceptors());
                }
                
                if(config.getJmxEnabled() != null) {
                    TomcatJdbcDataSource.getMethod("setJmxEnabled", boolean.class).invoke(dataSource, config.getJmxEnabled());
                }
    	    } else {
    	        throw new DataSourceException("Unknown class [ org.apache.tomcat.jdbc.pool.DataSource ]");
    	    }
	    } catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
	        throw new DataSourceException(e.getMessage(), e);
	    }
	}
}
