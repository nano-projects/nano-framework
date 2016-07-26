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
package org.nanoframework.orm.jdbc;

import java.beans.PropertyVetoException;
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
import org.nanoframework.commons.util.CollectionUtils;
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
 * JDBC适配器，基础JDBC处理对象，实例化需要实现JdbcCreater注解.
 * 
 * @author yanghe
 * @since 1.3.6
 */
public class JdbcAdapter implements DefaultSqlExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcAdapter.class);
    private static final Object LOCK = new Object();
    private static final AtomicBoolean INIT = new AtomicBoolean(false);
    private static JdbcAdapter INSTANCE;
    
    /**
     * 
     * @deprecated 使用 INSTANCE 替代 ADAPTER，外部使用时使用静态方法 adapter() 获取全局实例.
     */
    @Deprecated
    public static JdbcAdapter ADAPTER;
    
    private Pool pool;

    private JdbcAdapter(final Collection<JdbcConfig> configs, final PoolType poolType) throws PropertyVetoException, SQLException {
        Assert.notNull(poolType);
        if (INIT.get()) {
            throw new SQLException("数据源已经加载");
        }

        switch (poolType) {
            case C3P0:
                pool = new C3P0Pool(configs);
                break;
            case DRUID:
                pool = new DruidPool(configs);
                break;
            case TOMCAT_JDBC_POOL:
                pool = new TomcatJdbcPool(configs);
                break;
            default:
                throw new DataSourceException("无效的PoolType");
        }

        INIT.set(true);
    }

    protected static final JdbcAdapter newInstance(final Collection<JdbcConfig> configs, final PoolType poolType, final Object obj) {
        try {
            Assert.notNull(obj);
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new JdbcAdapter(configs, poolType);
                    ADAPTER = INSTANCE;
                } else {
                    INSTANCE.shutdown();
                    return newInstance(configs, poolType, obj);
                }
            }

            return INSTANCE;
        } catch (final SQLException | PropertyVetoException e) {
            throw new DataSourceException(e.getMessage());
        }
    }

    public static final JdbcAdapter adapter() {
        return INSTANCE;
    }

    public Connection getConnection(final String dataSource) throws SQLException {
        try {
            return pool.getPool(dataSource).getConnection();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public void commit(final Connection conn) throws SQLException {
        Assert.notNull(conn);
        if (isTxInit(conn)) {
            conn.commit();
        }
    }

    public void rollback(final Connection conn) throws SQLException {
        Assert.notNull(conn);
        if (isTxInit(conn)) {
            conn.rollback();
        }
    }

    public boolean isTxInit(final Connection conn) throws SQLException {
        Assert.notNull(conn);
        return !conn.getAutoCommit();
    }

    public final Statement getStatement(final Connection conn) throws SQLException {
        Assert.notNull(conn);
        return conn.createStatement();
    }

    public final PreparedStatement getPreparedStmt(final Connection conn, final String sql, final List<Object> values) throws SQLException {
        Assert.notNull(conn);
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        setValues(pstmt, values);
        return pstmt;
    }

    public final PreparedStatement getPreparedStmtForBatch(final Connection conn, final String sql, final List<List<Object>> batchValues) throws SQLException {
        Assert.notNull(conn);
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        if (batchValues != null && batchValues.size() > 0) {
            for (List<Object> values : batchValues) {
                setValues(pstmt, values);
                pstmt.addBatch();
            }
        }

        return pstmt;
    }

    public Result executeQuery(final String sql, final Connection conn) throws SQLException {
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
        } finally {
            close(rs, stmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Query SQL ]: {} cost [ {}ms ]", sql, System.currentTimeMillis() - start);
            }
        }

        return result;
    }

    public int executeUpdate(final String sql, final Connection conn) throws SQLException {
        Assert.notNull(conn);
        final long start = System.currentTimeMillis();
        int result = 0;
        Statement stmt = null;
        try {
            stmt = getStatement(conn);
            stmt.setQueryTimeout(60);
            result = stmt.executeUpdate(sql);
        } finally {
            close(stmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Update/Insert SQL ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
            }
        }

        return result;
    }

    public Result executeQuery(final String sql, final List<Object> values, final Connection conn) throws SQLException {
        Assert.notNull(conn);
        final long start = System.currentTimeMillis();
        Result result = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = getPreparedStmt(conn, sql, values);
            pstmt.setQueryTimeout(60);
            rs = pstmt.executeQuery();
            rs.setFetchSize(rs.getRow());
            result = ResultSupport.toResult(rs);
        } finally {
            close(rs, pstmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Query SQL ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
                LOGGER.debug("[ Execute Parameter ]: {}", JSON.toJSONString(values, SerializerFeature.WriteDateUseDateFormat));
            }
        }

        return result;
    }

    public int executeUpdate(final String sql, final List<Object> values, final Connection conn) throws SQLException {
        Assert.notNull(conn);
        long start = System.currentTimeMillis();
        PreparedStatement pstmt = null;

        try {
            pstmt = getPreparedStmt(conn, sql, values);
            pstmt.setQueryTimeout(60);
            return pstmt.executeUpdate();
        } finally {
            close(pstmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Update/Insert SQL ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
                LOGGER.debug("[ Execute Parameter ]: {}", JSON.toJSONString(values, SerializerFeature.WriteDateUseDateFormat));
            }
        }

    }

    public int[] executeBatchUpdate(final String sql, final List<List<Object>> batchValues, final Connection conn) throws SQLException {
        Assert.notNull(conn);
        if (CollectionUtils.isEmpty(batchValues)) {
            return new int[0];
        }

        final long start = System.currentTimeMillis();
        PreparedStatement pstmt = null;
        try {
            pstmt = getPreparedStmtForBatch(conn, sql, batchValues);
            pstmt.setQueryTimeout(60);
            return pstmt.executeBatch();
        } finally {
            close(pstmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Update/Insert SQL ] : {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
                LOGGER.debug("[ Execute Parameter ]: {}", JSON.toJSONString(batchValues, SerializerFeature.WriteDateUseDateFormat));
            }
        }
    }
    
    @Override
    public boolean execute(final String sql, final Connection conn) throws SQLException {
        Assert.notNull(conn);
        
        final long start = System.currentTimeMillis();
        Statement stmt = null;
        try {
            stmt = getStatement(conn);
            return stmt.execute(sql);
        } finally {
            close(stmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
            }
        }
    }

    private void setValues(final PreparedStatement pstmt, final List<Object> values) throws SQLException {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values.get(i));
            } else if (values.get(i) instanceof Long) {
                pstmt.setLong(i + 1, (Long) values.get(i));
            } else if (values.get(i) instanceof String) {
                pstmt.setString(i + 1, (String) values.get(i));
            } else if (values.get(i) instanceof Double) {
                pstmt.setDouble(i + 1, (Double) values.get(i));
            } else if (values.get(i) instanceof Float) {
                pstmt.setFloat(i + 1, (Float) values.get(i));
            } else if (values.get(i) instanceof Timestamp) {
                pstmt.setTimestamp(i + 1, (Timestamp) values.get(i));
            } else if (values.get(i) instanceof java.util.Date) {
                java.util.Date tempDate = (java.util.Date) values.get(i);
                pstmt.setDate(i + 1, new Date(tempDate.getTime()));
            } else {
                pstmt.setObject(i + 1, values.get(i));
            }
        }
    }

    public void close(final Object... jdbcObj) {
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
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public void shutdown() {
        pool.closeAndClear();
        pool = null;
        INIT.set(false);
        INSTANCE = null;
        ADAPTER = null;
    }

}
