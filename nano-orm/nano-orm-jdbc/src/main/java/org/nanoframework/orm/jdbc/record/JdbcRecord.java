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
package org.nanoframework.orm.jdbc.record;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.orm.jdbc.record.exception.MultiRecordException;
import org.nanoframework.orm.jdbc.record.script.SQLScript;
import org.nanoframework.orm.jdbc.record.script.SQLScriptBatch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class JdbcRecord<T extends BaseEntity> extends AbstractJdbcRecord<T> implements Record<T> {
    
    public JdbcRecord(final String dataSourceName) {
        super(dataSourceName);
    }
    
    @Override
    public List<T> select() throws SQLException {
        return select(Collections.emptyList());
    }
    
    @Override
    public List<T> select(final String[] where, final Object... values) throws SQLException {
        return select(Lists.newArrayList(where), values);
    }
    
    @Override
    public List<T> select(final List<String> where, final Object... values) throws SQLException {
        return select(Lists.newArrayList(fieldColumnMapper.keySet()), where, values);
    }
    
    @Override
    public List<T> select(final String[] fields, final String[] where, final Object... values) throws SQLException {
        return select(Lists.newArrayList(fields), Lists.newArrayList(where), values);
    }
    
    @Override
    public List<T> select(final List<String> fields, final List<String> where, final Object... values) throws SQLException {
        return select(createSelectStatement(fields, where, values));
    }

    @Override
    public List<T> select(final String sql, final Object... values) throws SQLException {
        if (values.length == 0) {
            return select(sql, (List<Object>) null);
        }
        
        return select(sql, Lists.newArrayList(values));
    }
    
    @Override
    public List<T> select(final String sql, final List<Object> values) throws SQLException {
        return select(SQLScript.create(sql, values));
    }
    
    @Override
    public List<T> select(final SQLScript select) throws SQLException {
        return toBeans(manager.executeQuery(select.sql, select.values).getRows());
    }
    
    @Override
    public T selectOne(final String[] where, final Object... values) throws SQLException {
        return selectOne(Lists.newArrayList(where), values);
    }
    
    @Override
    public T selectOne(final List<String> where, final Object... values) throws SQLException {
        return selectOne(Lists.newArrayList(fieldColumnMapper.keySet()), where, values);
    }
    
    @Override
    public T selectOne(final String[] fields, final String[] where, final Object... values) throws SQLException {
        return selectOne(Lists.newArrayList(fields), Lists.newArrayList(where), values);
    }
    
    @Override
    public T selectOne(final List<String> fields, final List<String> where, final Object... values) throws SQLException {
        return selectOne(createSelectOneStatement(fields, where, values));
    }
    
    @Override
    public T selectOne(final String sql, final Object... values) throws SQLException {
        if (values.length == 0) {
            return selectOne(sql, (List<Object>) null);
        }
        
        return selectOne(SQLScript.create(sql, Lists.newArrayList(values)));
    }
    
    @Override
    public T selectOne(final String sql, final List<Object> values) throws SQLException {
        return selectOne(SQLScript.create(sql, values));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T selectOne(final SQLScript select) throws SQLException {
        final SortedMap<?, ?>[] rows = manager.executeQuery(select.sql, select.values).getRows();
        if (!ArrayUtils.isEmpty(rows)) {
            if (rows.length > 1) {
                throw new MultiRecordException();
            }
            
            return toBean((Map<String, Object>) rows[0]);
        }
        
        return null;
    }
    
    @Override
    public int insert(final T entity) throws SQLException {
        return insert(entity, true);
    }
    
    @Override
    public int insert(final T entity, final boolean ignoreNull) throws SQLException {
        final SQLScript insert = createInsertStatement(entity, ignoreNull);
        return insert(insert);
    }
    
    @Override
    public int insert(final SQLScript insert) throws SQLException {
        return manager.executeUpdate(insert.sql, insert.values);
    }
    
    @Override
    public int[] insertBatch(final List<T> entitys) throws SQLException {
        return insertBatch(createInsertBatchStatement(entitys));
    }
    
    @Override
    public int[] insertBatch(final SQLScriptBatch insertBatch) throws SQLException {
        return manager.executeBatchUpdate(insertBatch.sql, insertBatch.batchValues);
    }
    
    @Override
    public int update(final T entity) throws SQLException {
        final Set<String> fields = Sets.newLinkedHashSet(fieldColumnMapper.keySet());
        fields.remove(idField);
        return update(entity, Lists.newArrayList(fields), Lists.newArrayList(idField));
    }
    
    @Override
    public int update(final T entity, final String[] fields, final String[] where) throws SQLException {
        return update(entity, Lists.newArrayList(fields), Lists.newArrayList(where));
    }
    
    @Override
    public int update(final T entity, final List<String> fields, final List<String> where) throws SQLException {
        return update(createUpdateStatement(entity, fields, where));
    }
    
    @Override
    public int update(final SQLScript update) throws SQLException {
        return manager.executeUpdate(update.sql, update.values);
    }
    
    @Override
    public int[] updateBatch(final List<T> entitys) throws SQLException {
        if (CollectionUtils.isEmpty(entitys)) {
            return new int[0];
        }
        
        boolean managedSessionStarted = false;
        if (!manager.isManagedSessionStarted()) {
            managedSessionStarted = true;
            manager.startManagedSession(false);
        }
        
        try {
            final int[] rets = new int[entitys.size()];
            for(int idx = 0, length = entitys.size(); idx < length; idx++) {
                rets[idx] = update(entitys.get(idx));
            }
            
            if (managedSessionStarted) {
                manager.commit();
            }
            
            return rets;
        } catch (final Throwable e) {
            if (managedSessionStarted) {
                manager.rollback();
            }
            
            throw e;
        } finally {
            if (managedSessionStarted) {
                manager.close();
            }
        }
        
    }
    
    @Override
    public int[] updateBatch(final List<T> entitys, final String[] fields, final String[] where) throws SQLException {
        return updateBatch(entitys, Lists.newArrayList(fields), Lists.newArrayList(where));
    }
    
    @Override
    public int[] updateBatch(final List<T> entitys, final List<String> fields, final List<String> where) throws SQLException {
        if (CollectionUtils.isEmpty(entitys)) {
            return new int[0];
        }
        
        final StringBuilder sqlBuilder = new StringBuilder();
        final List<List<Object>> batchValues = Lists.newArrayList();
        entitys.forEach(entity -> {
            final SQLScript script = createUpdateStatement(entity, fields, where);
            if (StringUtils.isEmpty(sqlBuilder)) {
                sqlBuilder.append(script.sql);
            }
            
            batchValues.add(script.values);
        });
        
        return updateBatch(SQLScriptBatch.create(sqlBuilder.toString(), batchValues));
    }
    
    @Override
    public int[] updateBatch(final SQLScriptBatch updateBatch) throws SQLException {
        return manager.executeBatchUpdate(updateBatch.sql, updateBatch.batchValues);
    }
    
    @Override
    public int delete(final T entity) throws SQLException {
        return delete(entity, idField);
    }
    
    @Override
    public int delete(final T entity, final String... where) throws SQLException {
        return delete(entity, Lists.newArrayList(where));
    }
    
    @Override
    public int delete(final T entity, final List<String> where) throws SQLException {
        return delete(createDeleteStatement(entity, where));
    }
    
    @Override
    public int delete(final SQLScript delete) throws SQLException {
        return manager.executeUpdate(delete.sql, delete.values);
    }
    
    @Override
    public int[] deleteBatch(final List<T> entitys) throws SQLException {
        return deleteBatch(entitys, idField);
    }
    
    @Override
    public int[] deleteBatch(final List<T> entitys, final String... where) throws SQLException {
        return deleteBatch(entitys, Lists.newArrayList(where));
    }
    
    @Override
    public int[] deleteBatch(final List<T> entitys, final List<String> where) throws SQLException {
        return delete(createDeleteBatchStatement(entitys, where));
    }
    
    @Override
    public int[] delete(final SQLScriptBatch delete) throws SQLException {
        return manager.executeBatchUpdate(delete.sql, delete.batchValues);
    }
}
