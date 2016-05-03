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
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.orm.jdbc.config.TomcatJdbcConfig;

import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 1.3.6
 */
public class TomcatJdbcPool implements Pool {
    private final Logger logger = LoggerFactory.getLogger(TomcatJdbcPool.class);
    private final ConcurrentMap<String, DataSource> dataSources = new ConcurrentHashMap<>();
    
    Class<?> TomcatJdbcDataSource; {
        try {
            TomcatJdbcDataSource = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
        } catch(ClassNotFoundException e) {
            logger.warn("Unload class [ org.apache.tomcat.jdbc.pool.DataSource ]");
        }
    }
    
    public TomcatJdbcPool(Collection<JdbcConfig> configs) {
        Assert.notNull(configs);
        Assert.notEmpty(configs);
        
        if(dataSources != null && !dataSources.isEmpty()) {
            closeAndClear();
        }
        
        List<TomcatJdbcConfig> tomcatJdbcConfigs = Lists.newArrayList();
        configs.forEach(config -> tomcatJdbcConfigs.add((TomcatJdbcConfig) config));
        for(TomcatJdbcConfig config : tomcatJdbcConfigs) {
            DataSource dataSource;
            try {
                dataSource = (DataSource) TomcatJdbcDataSource.newInstance();
            } catch(final Throwable e) {
                throw new RuntimeException("Instance Constructor Exception: " + TomcatJdbcDataSource.getName());
            }
            
            try {
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
            } catch(final Throwable e) {
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
            try { TomcatJdbcDataSource.getMethod("close").invoke(dataSource); } catch(Exception e) { }
        });
        
        dataSources.clear();
    }

    @Override
    public DataSource getPool(String envId) {
        return dataSources.get(envId);
    }

}
