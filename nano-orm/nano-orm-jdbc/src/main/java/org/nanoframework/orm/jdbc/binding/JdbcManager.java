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
package org.nanoframework.orm.jdbc.binding;

import static org.nanoframework.orm.jdbc.JdbcAdapter.adapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.jdbc.DataSourceException;
import org.nanoframework.orm.jdbc.DefaultSqlExecutor;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.orm.jdbc.jstl.Result;

/**
 * 
 * @author yanghe
 * @date 2015年7月30日 下午4:19:56 
 *
 */
public class JdbcManager implements SqlExecutor {

	private DataSource dataSource;
	private String envId;
	private final DefaultSqlExecutor sqlExecutorProxy;
	
	private ThreadLocal<Connection> localConnection = new ThreadLocal<Connection>();
	
	public static JdbcManager newInstance(JdbcConfig config, DataSource dataSource) {
		return new JdbcManager(config, dataSource);
	}

	private JdbcManager(JdbcConfig config, DataSource dataSource) {
		this.dataSource = dataSource;
		this.envId = config.getEnvironmentId();
		this.sqlExecutorProxy = (DefaultSqlExecutor) Proxy.newProxyInstance(JdbcManager.class.getClassLoader(), new Class[]{ DefaultSqlExecutor.class }, new SqlExecutorInterceptor());
		
	}

	protected void startManagedSession() throws SQLException {
		this.localConnection.set(dataSource.getConnection());
	}

	protected void startManagedSession(boolean autoCommit) throws SQLException {
		Connection conn;
		Assert.notNull(conn = dataSource.getConnection());
		conn.setAutoCommit(autoCommit);
		
		this.localConnection.set(conn);
	}

	protected boolean isManagedSessionStarted() {
		return this.localConnection.get() != null;
	}

	protected Connection getConnection() {
		final Connection conn = this.localConnection.get();
		if (conn == null)
			throw new DataSourceException("数据源没有设置，无法获取Connection连接");
		
		return conn;
	}

	@Override
	public void commit() throws SQLException {
	    adapter().commit(this.localConnection.get());
		
	}

	@Override
	public void rollback() throws SQLException {
		adapter().rollback(this.localConnection.get());
	}

	@Override
	public void close() {
		try {
			adapter().close(this.localConnection.get());
			
		} finally {
			this.localConnection.set(null);
		}
	}

	@Override
	public Result executeQuery(String sql) throws SQLException {
		return sqlExecutorProxy.executeQuery(sql, this.localConnection.get());
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		return sqlExecutorProxy.executeUpdate(sql, this.localConnection.get());
	}

	@Override
	public Result executeQuery(String sql, List<Object> values) throws SQLException {
		return sqlExecutorProxy.executeQuery(sql, values, this.localConnection.get());
	}

	@Override
	public int executeUpdate(String sql, List<Object> values) throws SQLException {
		return sqlExecutorProxy.executeUpdate(sql, values, this.localConnection.get());
	}

	@Override
	public int[] executeBatchUpdate(String sql, List<List<Object>> batchValues) throws SQLException {
		return sqlExecutorProxy.executeBatchUpdate(sql, batchValues, this.localConnection.get());
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	private class SqlExecutorInterceptor implements InvocationHandler {
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			final Connection conn = JdbcManager.this.localConnection.get();
			if (conn != null) {
				return method.invoke(adapter(), args);
			} else {
				final Connection newConn = adapter().getConnection(JdbcManager.this.envId);
				try {
					Parameter[] parameters = method.getParameters();
					int idx = 0;
					for(Parameter param : parameters) {
						if(param.getType() == Connection.class) {
							args[idx] = newConn;
							break;
						}
						
						idx ++;
					}
					
					final Object result = method.invoke(adapter(), args);
					adapter().commit(newConn);
					return result;
				} catch (Throwable t) {
					adapter().rollback(newConn);
					throw t;
				} finally {
					adapter().close(newConn);
				}
			}
		}
	}

}
