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

import org.nanoframework.commons.annatations.Property;
import org.nanoframework.commons.util.Assert;

/**
 * 数据源配置类
 * 
 * @author yanghe
 * @date 2015年7月20日 下午3:27:09 
 *
 */
public class C3P0JdbcConfig extends JdbcConfig {
	private static final long serialVersionUID = 8176998886713632074L;

	/** 当连接池中的连接耗尽的时候c3p0一次同时获取的连接数。Default: 3 */
	@Property(name = "c3p0.acquireIncrement")
	private Integer acquireIncrement;
	
	/** 定义在从数据库获取新连接失败后重复尝试的次数。Default: 30 */
	@Property(name = "c3p0.acquireRetryAttempts")
	private Integer acquireRetryAttempts;
	
	/** 两次连接中间隔时间，单位毫秒。Default: 1000 */
	@Property(name = "c3p0.acquireRetryDelay")
	private Integer acquireRetryDelay;
	
	/** 连接关闭时默认将所有未提交的操作回滚。Default: false */
	@Property(name = "c3p0.autoCommitOnClose")
	private Boolean autoCommitOnClose;
	
	/** c3p0将建一张名为Test的空表，并使用其自带的查询语句进行测试。
	*	如果定义了这个参数那么属性preferredTestQuery将被忽略。
	*	你不能在这张Test表上进行任何操作，它将只供c3p0测试使用。Default: null */
	@Property(name = "c3p0.automaticTestTable")
	private String automaticTestTable;
	
	/** 获取连接失败将会引起所有等待连接池来获取连接的线程抛出异常。
	*	但是数据源仍有效保留，并在下次调用getConnection()的时候继续尝试获取连接。
	*	如果设为true，那么在尝试获取连接失败后该数据源将申明已断开并永久关闭。Default: false */
	@Property(name = "c3p0.breakAfterAcquireFailure")
	private Boolean breakAfterAcquireFailure;
	
	/** 当连接池用完时客户端调用getConnection()后等待获取新连接的时间，超时后将抛出SQLException。
	*	如设为0则无限期等待, 单位毫秒, Default: 0 */
	@Property(name = "c3p0.checkoutTimeout")
	private Integer checkoutTimeout;
	
	/** 通过实现ConnectionTester或QueryConnectionTester的类来测试连接。
	*	类名需制定全路径。Default: com.mchange.v2.c3p0.impl.DefaultConnectionTester */
	@Property(name = "c3p0.connectionTesterClassName")
	private String connectionTesterClassName;
	
	/** 指定c3p0 libraries的路径，如果（通常都是这样）在本地即可获得那么无需设置，默认null即可 Default: null */
	@Property(name = "c3p0.factoryClassLocation")
	private String factoryClassLocation;
	
	/** Strongly disrecommended. Setting this to true may lead to subtle and bizarre bugs.（文档原文）作者强烈建议不使用的一个属性 */
	/** private boolean forceIgnoreUnresolvedTransactions; */
	
	/** 每隔多少秒检查一次所有连接池中的空闲连接。Default: 0 */
	@Property(name = "c3p0.idleConnectionTestPeriod")
	private Integer idleConnectionTestPeriod;

	/** 初始化时获取三个连接，取值应在minPoolSize与maxPoolSize之间。Default: 3 */
	@Property(name = "c3p0.initialPoolSize")
	private Integer initialPoolSize;
	
	/** 最大空闲时间,隔多少秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0 */
	@Property(name = "c3p0.maxIdleTime")
	private Integer maxIdleTime;
	
	/** 连接池中保留的最大连接数。Default: 15 */
	@Property(name = "c3p0.maxPoolSize")
	private Integer maxPoolSize;
	
	/** JDBC的标准参数，用以控制数据源内加载的PreparedStatements数量。
	*	但由于预缓存的statements属于单个connection而不是整个连接池。
	*	所以设置这个参数需要考虑到多方面的因素。如果maxStatements与maxStatementsPerConnection均为0，则缓存被关闭。Default: 0 */
	@Property(name = "c3p0.maxStatements")
	private Integer maxStatements;
	
	/** maxStatementsPerConnection定义了连接池内单个连接所拥有的最大缓存statements数。Default: 0 */
	@Property(name = "c3p0.maxStatementsPerConnection")
	private Integer maxStatementsPerConnection;
	
	/** c3p0是异步操作的，缓慢的JDBC操作通过帮助进程完成。扩展这些操作可以有效的提升性能通过多线程实现多个操作同时被执行。Default: 3 */
	@Property(name = "c3p0.numHelperThreads")
	private Integer numHelperThreads;
	
	/** 当用户调用getConnection()时使root用户成为去获取连接的用户。主要用于连接池连接非c3p0的数据源时。Default: null */
	@Property(name = "c3p0.overrideDefaultUser")
	private String overrideDefaultUser;
	
	/** 与overrideDefaultUser参数对应使用的一个参数。Default: null */
	@Property(name = "c3p0.overrideDefaultPassword")
	private String overrideDefaultPassword;
	
	/** 定义所有连接测试都执行的测试语句。在使用连接测试的情况下这个一显著提高测试速度。注意：测试的表必须在初始数据源的时候就存在。Default: null */
	@Property(name = "c3p0.preferredTestQuery")
	private String preferredTestQuery;
	
	/** 用户修改系统配置参数执行前最多等待300秒。Default: 300 */
	@Property(name = "c3p0.propertyCycle")
	private Integer propertyCycle;
	
	/** 因性能消耗大请只在需要的时候使用它。
	*	如果设为true那么在每个connection提交的时候都将校验其有效性。
	*	建议使用idleConnectionTestPeriod或automaticTestTable等方法来提升连接测试的性能。Default: false */
	@Property(name = "c3p0.testConnectionOnCheckout")
	private Boolean testConnectionOnCheckout;
	
	/** 如果设为true那么在取得连接的同时将校验连接的有效性。Default: false */
	@Property(name = "c3p0.testConnectionOnCheckin")
	private Boolean testConnectionOnCheckin;

	public C3P0JdbcConfig() {
		
	}
	
	public C3P0JdbcConfig(Properties properties) {
		Assert.notNull(properties);
		this.setProperties(properties);
	}
	
	public Integer getAcquireIncrement() {
		return acquireIncrement;
	}

	public void setAcquireIncrement(Integer acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	public Integer getAcquireRetryAttempts() {
		return acquireRetryAttempts;
	}

	public void setAcquireRetryAttempts(Integer acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	public Integer getAcquireRetryDelay() {
		return acquireRetryDelay;
	}

	public void setAcquireRetryDelay(Integer acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}

	public Boolean getAutoCommitOnClose() {
		return autoCommitOnClose;
	}

	public void setAutoCommitOnClose(Boolean autoCommitOnClose) {
		this.autoCommitOnClose = autoCommitOnClose;
	}

	public String getAutomaticTestTable() {
		return automaticTestTable;
	}

	public void setAutomaticTestTable(String automaticTestTable) {
		this.automaticTestTable = automaticTestTable;
	}

	public Boolean getBreakAfterAcquireFailure() {
		return breakAfterAcquireFailure;
	}

	public void setBreakAfterAcquireFailure(Boolean breakAfterAcquireFailure) {
		this.breakAfterAcquireFailure = breakAfterAcquireFailure;
	}

	public Integer getCheckoutTimeout() {
		return checkoutTimeout;
	}

	public void setCheckoutTimeout(Integer checkoutTimeout) {
		this.checkoutTimeout = checkoutTimeout;
	}

	public String getConnectionTesterClassName() {
		return connectionTesterClassName;
	}

	public void setConnectionTesterClassName(String connectionTesterClassName) {
		this.connectionTesterClassName = connectionTesterClassName;
	}

	public String getFactoryClassLocation() {
		return factoryClassLocation;
	}

	public void setFactoryClassLocation(String factoryClassLocation) {
		this.factoryClassLocation = factoryClassLocation;
	}

	public Integer getIdleConnectionTestPeriod() {
		return idleConnectionTestPeriod;
	}

	public void setIdleConnectionTestPeriod(Integer idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
	}

	public Integer getInitialPoolSize() {
		return initialPoolSize;
	}

	public void setInitialPoolSize(Integer initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	public Integer getMaxIdleTime() {
		return maxIdleTime;
	}

	public void setMaxIdleTime(Integer maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}

	public Integer getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(Integer maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public Integer getMaxStatements() {
		return maxStatements;
	}

	public void setMaxStatements(Integer maxStatements) {
		this.maxStatements = maxStatements;
	}

	public Integer getMaxStatementsPerConnection() {
		return maxStatementsPerConnection;
	}

	public void setMaxStatementsPerConnection(Integer maxStatementsPerConnection) {
		this.maxStatementsPerConnection = maxStatementsPerConnection;
	}

	public Integer getNumHelperThreads() {
		return numHelperThreads;
	}

	public void setNumHelperThreads(Integer numHelperThreads) {
		this.numHelperThreads = numHelperThreads;
	}

	public String getOverrideDefaultUser() {
		return overrideDefaultUser;
	}

	public void setOverrideDefaultUser(String overrideDefaultUser) {
		this.overrideDefaultUser = overrideDefaultUser;
	}

	public String getOverrideDefaultPassword() {
		return overrideDefaultPassword;
	}

	public void setOverrideDefaultPassword(String overrideDefaultPassword) {
		this.overrideDefaultPassword = overrideDefaultPassword;
	}

	public String getPreferredTestQuery() {
		return preferredTestQuery;
	}

	public void setPreferredTestQuery(String preferredTestQuery) {
		this.preferredTestQuery = preferredTestQuery;
	}

	public Integer getPropertyCycle() {
		return propertyCycle;
	}

	public void setPropertyCycle(Integer propertyCycle) {
		this.propertyCycle = propertyCycle;
	}

	public Boolean getTestConnectionOnCheckout() {
		return testConnectionOnCheckout;
	}

	public void setTestConnectionOnCheckout(Boolean testConnectionOnCheckout) {
		this.testConnectionOnCheckout = testConnectionOnCheckout;
	}

	public Boolean getTestConnectionOnCheckin() {
		return testConnectionOnCheckin;
	}

	public void setTestConnectionOnCheckin(Boolean testConnectionOnCheckin) {
		this.testConnectionOnCheckin = testConnectionOnCheckin;
	}
	
	
}
