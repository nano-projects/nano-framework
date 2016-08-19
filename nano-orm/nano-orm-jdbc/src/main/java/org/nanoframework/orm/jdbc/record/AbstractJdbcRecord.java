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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.entity.EntityException;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;
import org.nanoframework.orm.jdbc.binding.JdbcManager;
import org.nanoframework.orm.jdbc.jstl.Result;
import org.nanoframework.orm.jdbc.record.annotation.Column;
import org.nanoframework.orm.jdbc.record.annotation.Id;
import org.nanoframework.orm.jdbc.record.annotation.Table;
import org.nanoframework.orm.jdbc.record.script.SQLScript;
import org.nanoframework.orm.jdbc.record.script.SQLScriptBatch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public abstract class AbstractJdbcRecord<T extends BaseEntity> {
    protected JdbcManager manager;
    protected Class<T> entity;
    protected String tableName;
    protected String idColumn;
    protected String idField;
    protected Map<String, Field> columnMapper;
    protected Map<String, String> fieldColumnMapper;
    
    private BaseEntity instance;
    
    public AbstractJdbcRecord(final String dataSourceName) {
        this(GlobalJdbcManager.get(dataSourceName));
    }

    public AbstractJdbcRecord(final JdbcManager manager) {
        initEntity();
        initTableName();
        initIdColumn();
        initColumnNames();
        
        this.manager = manager;
        Assert.notNull(manager, "无效的JdbcManager对象");
    }
    
    @SuppressWarnings("unchecked")
    protected void initEntity() {
        final Type superClass = getClass().getGenericSuperclass();
        this.entity = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
        this.instance = ReflectUtils.newInstance(this.entity);
    }
    
    protected void initTableName() {
        if (this.entity.isAnnotationPresent(Table.class)) {
            final Table table = this.entity.getAnnotation(Table.class);
            this.tableName = table.value();
        }
        
        Assert.hasText(tableName, "无效的Record实体[ " + this.entity.getName() + " ], 必须添加类级注解@Table");
    }
    
    protected void initIdColumn() {
        final Collection<Field> fields = instance.fields();
        for(Field field : fields) {
            if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                idColumn = field.getAnnotation(Column.class).value();
                idField = field.getName();
                break;
            }
        }
    }
    
    protected void initColumnNames() {
        final Collection<Field> fields = instance.fields();
        if(Result.JDBC_JSTL_CASE_INSENSITIVE_ORDER) {
            columnMapper = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);
            fieldColumnMapper = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);
        } else { 
            columnMapper = Maps.newTreeMap();
            fieldColumnMapper = Maps.newTreeMap();
        }
        
        fields.stream().filter(field -> field.isAnnotationPresent(Column.class))
        .forEach(field -> {
            final String columnName = field.getAnnotation(Column.class).value();
            columnMapper.put(columnName, field);
            fieldColumnMapper.put(field.getName(), columnName);
        });
        
        Assert.notEmpty(columnMapper, "无效的Record实体[ " + this.entity.getName() + " ], 必须添加属性级注解@Column");
    }
    
    @SuppressWarnings("unchecked")
    protected List<T> toBeans(final SortedMap<?, ?>[] beanMaps) {
        if (ArrayUtils.isEmpty(beanMaps)) {
            return Collections.emptyList();
        }

        final List<T> beans = Lists.newArrayList();
        for (SortedMap<?, ?> beanMap : beanMaps) {
            beans.add(toBean((Map<String, Object>) beanMap));
        }

        return beans;
    }
    
    protected T toBean(Map<String, Object> beanMap) {
        if (CollectionUtils.isEmpty(beanMap)) {
            return null;
        }

        try {
            final T bean = this.entity.newInstance();
            beanMap.forEach((key, value) -> {
                final Field field = columnMapper.get(key);
                if (field != null) {
                    final String fieldName = field.getName();
                    bean.setAttributeValue(fieldName, value);
                }
            });
            
            return bean;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new EntityException(e.getMessage(), e);
        }
    }
    
    protected SQLScript createSelectStatement(final List<String> fields, final List<String> where, final Object... values) {
        final StringBuilder sqlBuilder = new StringBuilder("select ");
        final StringBuilder whereBuilder = new StringBuilder();
        if (CollectionUtils.isEmpty(fields)) {
            fieldColumnMapper.keySet().forEach(attribute -> {
                sqlBuilder.append(fieldColumnMapper.get(attribute));
                sqlBuilder.append(", ");
            });
        } else {
            fields.forEach(attribute -> {
                sqlBuilder.append(fieldColumnMapper.get(attribute));
                sqlBuilder.append(", ");
            });
        }
        
        if (!CollectionUtils.isEmpty(where)) {
            whereBuilder.append(" where 1 = 1 ");
            where.forEach(attribute -> {
                whereBuilder.append("and ");
                whereBuilder.append(fieldColumnMapper.get(attribute));
                whereBuilder.append(" = ? ");
            });
        }
        
        return SQLScript.create(sqlBuilder.substring(0, sqlBuilder.length() - 2) + " from " + tableName + whereBuilder.toString(), Lists.newArrayList(values));
    }
    
    protected SQLScript createSelectOneStatement(final List<String> fields, final List<String> where, final Object... values) {
        Assert.notEmpty(where);
        return createSelectStatement(fields, where, values);
    }
    
    protected SQLScript createSelectCountStatement(final List<String> where, final Object... values) {
        final StringBuilder sqlBuilder = new StringBuilder("select count(1) as count from ");
        final StringBuilder whereBuilder = new StringBuilder();
        if (!CollectionUtils.isEmpty(where)) {
            whereBuilder.append(" where 1 = 1 ");
            where.forEach(attribute -> {
                whereBuilder.append("and ");
                whereBuilder.append(fieldColumnMapper.get(attribute));
                whereBuilder.append(" = ? ");
            });
        }
        
        return SQLScript.create(sqlBuilder.toString() + tableName + whereBuilder.toString(), Lists.newArrayList(values));
    }
    
    protected SQLScript createInsertStatement(final T entity) {
        return createInsertStatement(entity, true);
    }
    
    protected SQLScript createInsertStatement(final T entity, final boolean ignoreNull) {
        Assert.notNull(entity);
        final List<Object> values = Lists.newArrayList();
        final StringBuilder sqlBuilder = new StringBuilder("insert into ");
        final StringBuilder valueBuilder = new StringBuilder(" values ( ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" ( ");
        Arrays.asList(entity.attributeNames()).stream()
        .filter(attribute -> entity.attributeValue(attribute) != null && ignoreNull)
        .filter(attribute -> fieldColumnMapper.containsKey(attribute))
        .forEach(attribute -> {
            sqlBuilder.append(fieldColumnMapper.get(attribute));
            sqlBuilder.append(", ");
            valueBuilder.append("?, ");
            values.add(entity.attributeValue(attribute));
        });
        
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        
        return SQLScript.create(createInsertSQL(sqlBuilder, valueBuilder), values);
    }
    
    protected SQLScriptBatch createInsertBatchStatement(final List<T> entitys) {
        Assert.notEmpty(entitys);
        
        final Set<String> attributeNames = fieldColumnMapper.keySet();
        final StringBuilder sqlBuilder = new StringBuilder("insert into ");
        final StringBuilder valueBuilder = new StringBuilder(" values ( ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" ( ");
        attributeNames.forEach(attribute -> {
            sqlBuilder.append(fieldColumnMapper.get(attribute));
            sqlBuilder.append(", ");
            valueBuilder.append("?, ");
        });
        
        final List<List<Object>> batchValues = Lists.newArrayList();
        entitys.stream().filter(entity -> entity != null).forEach(entity -> {
            final List<Object> values = Lists.newArrayList();
            attributeNames.forEach(attributeName -> values.add(entity.attributeValue(attributeName)));
            batchValues.add(values);
        });
        
        return SQLScriptBatch.create(createInsertSQL(sqlBuilder, valueBuilder), batchValues);
    }
    
    private String createInsertSQL(final StringBuilder sqlBuilder, final StringBuilder valueBuilder) {
        return sqlBuilder.substring(0, sqlBuilder.length() - 2) + " ) " + valueBuilder.substring(0, valueBuilder.length() - 2) + " ) ";
    }
    
    protected SQLScript createUpdateStatement(final T entity, final List<String> fields, final List<String> where) {
        Assert.notNull(entity);
        Assert.notEmpty(fields);
        Assert.notEmpty(where);
        final List<Object> values = Lists.newArrayList();
        final StringBuilder sqlBuilder = new StringBuilder("update ");
        final StringBuilder whereBuilder = new StringBuilder(" where 1 = 1 ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" set ");
        fields.forEach(attribute -> {
            sqlBuilder.append(fieldColumnMapper.get(attribute));
            sqlBuilder.append(" = ?, ");
            values.add(entity.attributeValue(attribute));
        });
        
        where.forEach(attribute -> {
            whereBuilder.append(" and ");
            whereBuilder.append(fieldColumnMapper.get(attribute));
            whereBuilder.append(" = ? ");
            values.add(entity.attributeValue(attribute));
        });
        
        return SQLScript.create(createUpdateSQL(sqlBuilder, whereBuilder), values);
    }
    
    private String createUpdateSQL(final StringBuilder sqlBuilder, final StringBuilder whereBuilder) {
        return sqlBuilder.substring(0, sqlBuilder.length() - 2) + whereBuilder.toString();
    }
    
    protected SQLScript createDeleteStatement(final T entity, final List<String> where) {
        Assert.notNull(entity);
        Assert.notEmpty(where);
        final List<Object> values = Lists.newArrayList();
        final StringBuilder sqlBuilder = new StringBuilder("delete from ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" where 1 = 1 ");
        where.forEach(attribute -> {
            sqlBuilder.append("and ");
            sqlBuilder.append(fieldColumnMapper.get(attribute));
            sqlBuilder.append(" = ? ");
            values.add(entity.attributeValue(attribute));
        });
        
        return SQLScript.create(sqlBuilder.toString(), values);
    }
    
    protected SQLScriptBatch createDeleteBatchStatement(final List<T> entitys, final List<String> where) {
        Assert.notEmpty(entitys);
        Assert.notEmpty(where);
        final StringBuilder sqlBuilder = new StringBuilder("delete from ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" where 1 = 1 ");
        where.forEach(attribute -> {
            sqlBuilder.append("and ");
            sqlBuilder.append(fieldColumnMapper.get(attribute));
            sqlBuilder.append(" = ? ");
        });
        
        List<List<Object>> batchValues = Lists.newArrayList();
        entitys.forEach(entity -> {
            List<Object> values = Lists.newArrayList();
            where.forEach(attribute -> values.add(entity.attributeValue(attribute)));
            batchValues.add(values);
        });
        
        return SQLScriptBatch.create(sqlBuilder.toString(), batchValues);
    }
}
