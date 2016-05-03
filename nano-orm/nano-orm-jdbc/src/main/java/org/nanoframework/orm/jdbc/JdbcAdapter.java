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
package org.nanoframework.orm.jdbc;

import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.orm.jdbc.jstl.Result;
import org.nanoframework.orm.jdbc.jstl.ResultSupport;
import org.nanoframework.orm.jdbc.pool.C3P0Pool;
import org.nanoframework.orm.jdbc.pool.DruidPool;
import org.nanoframework.orm.jdbc.pool.Pool;
import org.nanoframework.orm.jdbc.pool.TomcatJdbcPool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JDBC适配器，基础JDBC处理对象，实例化需要实现JdbcCreater注解
 * 
 * @author yanghe
 * @date 2015年7月21日 下午9:44:58 
 *
 */
public class JdbcAdapter implements DefaultSqlExecutor {

	private Logger LOG = LoggerFactory.getLogger(JdbcAdapter.class);
	
	private Pool pool;
	
	private static Object LOCK = new Object();
	private static AtomicBoolean init = new AtomicBoolean(false);
	
	public static JdbcAdapter ADAPTER;
	
	private JdbcAdapter(Collection<JdbcConfig> configs, PoolType poolType) throws PropertyVetoException, SQLException {
		Assert.notNull(poolType);
		if(init.get()) {
			throw new SQLException("数据源已经加载");
		}
		
		switch(poolType) {
			case C3P0:
				pool = new C3P0Pool(configs);
				break;
			case DRUID: 
				pool = new DruidPool(configs);
				break;
			case TOMCAT_JDBC_POOL:
			    pool = new TomcatJdbcPool(configs);
			    break;
		}
		
		init.set(true);
	}
	
	public static final JdbcAdapter newInstance(Collection<JdbcConfig> configs, PoolType poolType, Object obj) {
		try {
			Assert.notNull(obj);
			if(obj instanceof Class<?>) {
				if(!((Class<?>) obj).isAnnotationPresent(JdbcCreater.class))
					throw new DataSourceException("只有实现JdbcCreater注解的类或方法才可以实例化此对象");
			} else if(obj instanceof Method) {
				if(!((Method) obj).isAnnotationPresent(JdbcCreater.class))
					throw new DataSourceException("只有实现JdbcCreater注解的类或方法才可以实例化此对象");
			} else {
				throw new DataSourceException("只有实现JdbcCreater注解的类或方法才可以实例化此对象");
			}
			
			synchronized (LOCK) {
    			if(ADAPTER == null) {
    				ADAPTER = new JdbcAdapter(configs, poolType);
    			} else {
    				ADAPTER.shutdown();
    				ADAPTER = null;
    				return newInstance(configs, poolType, obj);
    			}
			}
			
			return ADAPTER;
		} catch(SQLException | PropertyVetoException e) {
			throw new DataSourceException(e.getMessage());
		}
	}
	
    /**
     * 获取连接
     *
     * getConnection
     *
     * @param dataSource 数据源
     * @return Connection
     * @throws java.sql.SQLException 数据库异常
     */
    public Connection getConnection(String dataSource) throws SQLException {
        try {
            Connection conn = pool.getPool(dataSource).getConnection();
            return conn;
            
        } catch(Exception e) {
            LOG.error(e.getMessage() , e);
        }

        return null;
    }
    
    public void commit(Connection conn) throws SQLException {
    	Assert.notNull(conn);
    	
    	if(isTxInit(conn))
    		conn.commit();
    }
    
    public void rollback(Connection conn) throws SQLException {
    	Assert.notNull(conn);
    	
    	if(isTxInit(conn))
    		conn.rollback();
    }
    
    public boolean isTxInit(Connection conn) throws SQLException {
    	Assert.notNull(conn);
		return !conn.getAutoCommit();
    }

    public final Statement getStatement(Connection conn) throws SQLException {
    	Assert.notNull(conn);
        return conn.createStatement();
    }
    
    public final PreparedStatement getPreparedStmt(Connection conn, String sql, List<Object> values) throws SQLException {
    	Assert.notNull(conn);

        PreparedStatement pstmt = conn.prepareStatement(sql);
        setValues(pstmt, values);
        return pstmt;
    }
    
    public final PreparedStatement getPreparedStmtForBatch(Connection conn, String sql, List<List<Object>> batchValues) throws SQLException {
    	Assert.notNull(conn);
    	
    	PreparedStatement pstmt = conn.prepareStatement(sql);
        if(batchValues != null && batchValues.size() > 0) {
	        for(List<Object> values : batchValues) {
	            setValues(pstmt, values);
	            pstmt.addBatch();
	        }
        }

        return pstmt;
    }
    
    /**
	 * 执行查询
	 * 
	 * @param String
	 * @return Result
	 * @throws Exception
	 */
	public Result executeQuery(String sql, Connection conn) throws SQLException {
		Assert.notNull(conn);
		
		long start = System.currentTimeMillis();
		Result result = null;
		ResultSet rs = null;
		Statement stmt = null;
		
		try {
			stmt = getStatement(conn);
			stmt.setQueryTimeout(60);
			rs = stmt.executeQuery(sql);
			rs.setFetchSize(rs.getRow());
			result = ResultSupport.toResult(rs);
			
		} catch (SQLException e) {
			throw e;
			
		} finally{
			close(rs, stmt);
			if(LOG.isDebugEnabled())
				LOG.debug("[ Execute Query SQL ]: " + sql + " cost [ "+(System.currentTimeMillis() - start)+"ms ]");
			
		}
		
		return result;
	}

	/**
	 * 执行更新
	 * 
	 * @param String
	 * @return void
	 * @throws Exception
	 */
	public int executeUpdate(String sql, Connection conn) throws SQLException {
		Assert.notNull(conn);
		
		long start = System.currentTimeMillis();
		int result = 0;
		Statement stmt = null;
		try {
			stmt = getStatement(conn);
			stmt.setQueryTimeout(60);
			result = stmt.executeUpdate(sql);
			
		} catch (SQLException e) {
			throw e;
			
		} finally{
			close(stmt);
			if(LOG.isDebugEnabled())
				LOG.debug("[ Execute Update/Insert SQL ]: " + sql + " [cost " + (System.currentTimeMillis() - start) + "]");
			
		}
		
		return result;
	}
	
	/**
	 * 执行有参数查询
	 * @param sql 有参数的SQL
	 * @param values 参数值
	 */
	public Result executeQuery(String sql, List<Object> values, Connection conn) throws SQLException {
		Assert.notNull(conn);
		
		long start = System.currentTimeMillis();
		Result result = null;
		ResultSet rs = null;
		PreparedStatement preStmt = null;
		
		try {
			preStmt = getPreparedStmt(conn, sql, values);
			preStmt.setQueryTimeout(60);
			rs = preStmt.executeQuery();
			rs.setFetchSize(rs.getRow());
			result = ResultSupport.toResult(rs);
			
		} catch (SQLException e) {
			throw e;
			
		} finally{
			close(rs , preStmt);
			if(LOG.isDebugEnabled()) {
				LOG.debug("[ Execute Query SQL ]: " + sql + " [cost " + (System.currentTimeMillis() - start) + "]");
				LOG.debug("[ Execute Parameter ]: " + JSON.toJSONString(values, SerializerFeature.WriteDateUseDateFormat));
				
			}
			
		}
		
		return result;
	}
	
	/**
	 * 执行更新
	 * @param String 要执行的sql
	 * @param List<Object> values 参数列表
	 * @return Integer 返回更新的影响行数
	 * @throws Exception
	 * 
	 */
	public int executeUpdate(String sql, List<Object> values , Connection conn) throws SQLException {
		Assert.notNull(conn);
		
		long start = System.currentTimeMillis();
		Integer result = 0;
		PreparedStatement preStmt = null;
		
		try {
			preStmt = getPreparedStmt(conn, sql, values);
			preStmt.setQueryTimeout(60);
			result = preStmt.executeUpdate();
			
		} catch (SQLException e) {
			throw e;
			
		} finally{
			close(preStmt);
			if(LOG.isDebugEnabled()) {
				LOG.debug("[ Execute Update/Insert SQL ]: " + sql + " [cost " + (System.currentTimeMillis() - start) + "]");
				LOG.debug("[ Execute Parameter ]: " + JSON.toJSONString(values, SerializerFeature.WriteDateUseDateFormat));
			}
		}
		
		return result;
	}
	
	/**
	 * 执行更新
	 * @param String 要执行的sql
	 * @param List<Object> values 参数列表
	 * @return Integer 返回更新的影响行数
	 * @throws Exception
	 * 
	 */
	public int[] executeBatchUpdate(String sql, List<List<Object>> batchValues , Connection conn) throws SQLException {
		Assert.notNull(conn);
		
		if(batchValues == null || batchValues.size() == 0)
			return new int[]{};
		
		long start = System.currentTimeMillis();
		int[] result = new int[]{};
		PreparedStatement preStmt = null;
		try {
			preStmt = getPreparedStmtForBatch(conn, sql, batchValues);
			preStmt.setQueryTimeout(60);
			result = preStmt.executeBatch();
			
		} catch (SQLException e) {
			throw e;
			
		} finally{
			close(preStmt);
			if(LOG.isDebugEnabled()) {
				LOG.debug("[ Execute Update/Insert SQL ] : " + sql + " [cost " + (System.currentTimeMillis() - start) + "]");
				LOG.debug("[ Execute Parameter ]: " + JSON.toJSONString(batchValues, SerializerFeature.WriteDateUseDateFormat));
			}
			
		}
		
		return result;
	}
	
    /**
     * 执行有参数查询
     *
     * @param preStmt 要设备参数据的Statement对象
     * @param values 参数值
     * @throws java.sql.SQLException SQL异常
     */
    private void setValues(PreparedStatement preStmt, List<Object> values) throws SQLException {
        try {
        	if(values == null || values.size() == 0)
        		return ;
        	
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) instanceof Integer) {
                    //设置整数值
                    preStmt.setInt(i + 1, (Integer) values.get(i));

                } else if (values.get(i) instanceof Long) {
                    //设置长数值
                    preStmt.setLong(i + 1, (Long) values.get(i));

                } else if (values.get(i) instanceof String) {
                    //设置字符串
                    preStmt.setString(i + 1, (String) values.get(i));

                } else if (values.get(i) instanceof Double) {
                    //设置双精度浮点数
                    preStmt.setDouble(i + 1, (Double) values.get(i));

                } else if (values.get(i) instanceof Float) {
                    //设置单精度浮点数
                    preStmt.setFloat(i + 1, (Float) values.get(i));

                } else if (values.get(i) instanceof Timestamp) {
                    //设置时间Timestamp
                    preStmt.setTimestamp(i + 1, (Timestamp) values.get(i));

                } else if (values.get(i) instanceof java.util.Date) {
                    //设置日期
                    //两个日期类型的转换
                    java.util.Date tempDate = new java.util.Date();
                    tempDate = (java.util.Date) values.get(i);
                    preStmt.setDate(i + 1, new Date(tempDate.getTime()));

                } else {
                    preStmt.setObject(i + 1, values.get(i));

                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void close(Object... jdbcObj) {

        if (jdbcObj != null && jdbcObj.length > 0) {

            for (Object obj : jdbcObj) {
            	try {
	                if (obj != null) {
	                    if (obj instanceof ResultSet) {
	                        ((ResultSet) obj).close();
	                        obj = null;
	
	                    } else if (obj instanceof Statement) {
	                        ((Statement) obj).close();
	                        obj = null;
	
	                    } else if (obj instanceof PreparedStatement) {
	                        ((PreparedStatement) obj).close();
	                        obj = null;
	
	                    } else if (obj instanceof Connection) {
	                        ((Connection) obj).close();
	                        obj = null;
	                    }
	
	                }
            	} catch(SQLException e) {
            		LOG.error(e.getMessage() , e);

            	}
            }

        }

    }
    
    public void shutdown() {
    	pool.closeAndClear();
    	pool = null;
    	init.set(false);
    }

}
