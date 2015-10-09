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
package org.nanoframework.orm.modules.provider;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author yanghe
 * @date 2015年9月30日 下午2:06:05
 */
public class DruidDataSourceProvider implements Provider<DataSource> {

	/**
     * The DruidDataSource reference.
     */
	private final DruidDataSource dataSource = new DruidDataSource();
	
	 /**
     * Creates a new DruidDataSourceProvider using the needed parameter.
     *
     * @param driver The JDBC driver class.
     * @param url the database URL of the form <code>jdbc:subprotocol:subname</code>.
     * @param username the database user.
     * @param password the user's password.
     */
    @Inject
    public DruidDataSourceProvider(@Named("JDBC.driver") final String driver, @Named("JDBC.url") final String url) {
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
    }

    /**
     *
     * @param username
     */
    @com.google.inject.Inject(optional = true)
    public void setUser(@Named("JDBC.username") final String username) {
        dataSource.setUsername(username);
    }

    /**
     *
     * @param password
     */
    @com.google.inject.Inject(optional = true)
    public void setPassword(@Named("JDBC.password") final String password) {
        dataSource.setPassword(password);
    }
    
    /** 初始化连接数量 */
    @com.google.inject.Inject(optional = true)
    public void setInitialSize(@Named("druid.initialSize") final int initialSize) {
    	dataSource.setInitialSize(initialSize);
    }
    
    /** 最大并发连接数 */
    @com.google.inject.Inject(optional = true)
    public void setMaxActive(@Named("druid.maxActive") final int maxActive) {
    	dataSource.setMaxActive(maxActive);
    }
    
    /** 最大空闲连接数 */
    /** Druid maxIdle is Deprecated */
    @Deprecated
    @com.google.inject.Inject(optional = true)
    public void setMaxIdle(@Named("druid.maxIdle") final int maxIdle) {
    	dataSource.setMaxIdle(maxIdle);
    }
    
    /** 最小空闲连接数 */
    @com.google.inject.Inject(optional = true)
    public void setMinIdle(@Named("druid.minIdle") final int minIdle) {
    	dataSource.setMinIdle(minIdle);
    }
    
    /** 配置获取连接等待超时的时间 */
    @com.google.inject.Inject(optional = true)
    public void setMaxWait(@Named("druid.maxWait") final long maxWait) {
    	dataSource.setMaxWait(maxWait);
    }
    
    /** 超过时间限制是否回收 */
    @com.google.inject.Inject(optional = true)
    public void setRemoveAbandoned(@Named("druid.removeAbandoned") final boolean removeAbandoned) {
    	dataSource.setRemoveAbandoned(removeAbandoned);
    }
    
    /** 超过时间限制多长 */
    @com.google.inject.Inject(optional = true)
    public void setRemoveAbandonedTimeout(@Named("druid.removeAbandonedTimeout") final int removeAbandonedTimeout) {
    	dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
    }
    
    /** 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 */
    @com.google.inject.Inject(optional = true)
    public void setTimeBetweenEvictionRunsMillis(@Named("druid.timeBetweenEvictionRunsMillis") final long timeBetweenEvictionRunsMillis) {
    	dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    }
    
    /** 配置一个连接在池中最小生存的时间，单位是毫秒 */
    @com.google.inject.Inject(optional = true)
    public void setMinEvictableIdleTimeMillis(@Named("druid.minEvictableIdleTimeMillis") final long minEvictableIdleTimeMillis) {
    	dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    }
    
    /** 用来检测连接是否有效的sql，要求是一个查询语句 */
    @com.google.inject.Inject(optional = true)
    public void setValidationQuery(@Named("druid.validationQuery") final String validationQuery) {
    	dataSource.setValidationQuery(validationQuery);
    }
    
    /** 申请连接的时候检测 */
    @com.google.inject.Inject(optional = true)
    public void setTestWhileIdle(@Named("druid.testWhileIdle") final boolean testWhileIdle) {
    	dataSource.setTestWhileIdle(testWhileIdle);
    }
    
    /** 申请连接时执行validationQuery检测连接是否有效，配置为true会降低性能 */
    @com.google.inject.Inject(optional = true)
    public void setTestOnBorrow(@Named("druid.testOnBorrow") final boolean testOnBorrow) {
    	dataSource.setTestOnBorrow(testOnBorrow);
    }
    
    /** 归还连接时执行validationQuery检测连接是否有效，配置为true会降低性能 */
    @com.google.inject.Inject(optional = true)
    public void setTestOnReturn(@Named("druid.testOnReturn") final boolean testOnReturn) {
    	dataSource.setTestOnReturn(testOnReturn);
    }
    
    /** 打开PSCache，并且指定每个连接上PSCache的大小 */
    @com.google.inject.Inject(optional = true)
    public void setPoolPreparedStatements(@Named("druid.poolPreparedStatements") final boolean poolPreparedStatements) {
    	dataSource.setPoolPreparedStatements(poolPreparedStatements);
    }
    
    @com.google.inject.Inject(optional = true)
    public void setMaxPoolPreparedStatementPerConnectionSize(@Named("druid.maxPoolPreparedStatementPerConnectionSize") final int maxPoolPreparedStatementPerConnectionSize) {
    	dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
    }
    
    /** 属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有：<br>
     * 监控统计用的filter:stat <br>
     * 日志用的filter:log4j <br>
     * 防御SQL注入的filter:wall <br>
     */
    @com.google.inject.Inject(optional = true)
    public void setFilters(@Named("druid.filters") final String filters) throws SQLException {
    	dataSource.setFilters(filters);
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public DataSource get() {
		return dataSource;
	}
}
