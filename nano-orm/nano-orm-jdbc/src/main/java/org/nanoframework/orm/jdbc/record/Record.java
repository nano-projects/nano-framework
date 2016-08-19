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
import java.util.List;

import org.nanoframework.orm.jdbc.record.script.SQLScript;
import org.nanoframework.orm.jdbc.record.script.SQLScriptBatch;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public interface Record<T> {

    List<T> select() throws SQLException;
    
    List<T> select(String[] where, Object... values) throws SQLException;
    
    List<T> select(List<String> where, Object... values) throws SQLException;
    
    List<T> select(String[] fields, String[] where, Object... values) throws SQLException;
    
    List<T> select(List<String> fields, List<String> where, Object... values) throws SQLException;
    
    List<T> select(String sql, Object... values) throws SQLException;

    List<T> select(String sql, List<Object> values) throws SQLException;

    List<T> select(SQLScript select) throws SQLException;
    
    T selectOne(String[] where, Object... values) throws SQLException;
    
    T selectOne(List<String> where, Object... values) throws SQLException;
    
    T selectOne(String[] fields, String[] where, Object... values) throws SQLException;
    
    T selectOne(List<String> fields, List<String> where, Object... values) throws SQLException;

    T selectOne(String sql, Object... values) throws SQLException;

    T selectOne(String sql, List<Object> values) throws SQLException;

    T selectOne(SQLScript select) throws SQLException;
    
    long selectCount() throws SQLException;
    
    long selectCount(String[] where, Object... values) throws SQLException;
    
    long selectCount(List<String> where, Object... values) throws SQLException;
    
    long selectCount(String sql, Object... values) throws SQLException;

    long selectCount(String sql, List<Object> values) throws SQLException;

    long selectCount(SQLScript select) throws SQLException;

    int insert(T entity) throws SQLException;

    int insert(T entity, boolean ignoreNull) throws SQLException;

    int insert(SQLScript insert) throws SQLException;

    int[] insertBatch(List<T> entitys) throws SQLException;
    
    int[] insertBatch(SQLScriptBatch insertBatch) throws SQLException;

    int update(T entity) throws SQLException;
    
    int update(T entity, String[] fields, String[] where) throws SQLException;
    
    int update(T entity, List<String> fields, List<String> where) throws SQLException;
    
    int update(SQLScript update) throws SQLException;
    
    int[] updateBatch(List<T> entitys) throws SQLException;
    
    int[] updateBatch(List<T> entitys, String[] fields, String[] where) throws SQLException;
    
    int[] updateBatch(List<T> entitys, List<String> fields, List<String> where) throws SQLException;
    
    int[] updateBatch(SQLScriptBatch updateBatch) throws SQLException;
    
    int delete(T entity) throws SQLException;
    
    int delete(T entity, String... where) throws SQLException;
    
    int delete(T entity, List<String> where) throws SQLException;
    
    int delete(SQLScript delete) throws SQLException;
    
    int[] deleteBatch(List<T> entitys) throws SQLException;
    
    int[] deleteBatch(List<T> entitys, String... where) throws SQLException;
    
    int[] deleteBatch(List<T> entitys, List<String> where) throws SQLException;
    
    int[] delete(SQLScriptBatch delete) throws SQLException;
}
