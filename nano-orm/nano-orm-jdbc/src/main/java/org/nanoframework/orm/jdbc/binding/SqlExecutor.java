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

import java.io.Closeable;
import java.sql.SQLException;
import java.util.List;

import org.nanoframework.orm.jdbc.jstl.Result;

/**
 * 
 * @author yanghe
 * @since 1.2
 */
public interface SqlExecutor extends Closeable {
	public void commit() throws SQLException;
	
	public void rollback() throws SQLException;
	
	public Result executeQuery(String sql) throws SQLException;
	
	public int executeUpdate(String sql) throws SQLException;
	
	public Result executeQuery(String sql, List<Object> values) throws SQLException;
	
	public int executeUpdate(String sql, List<Object> values) throws SQLException;
	
	public int[] executeBatchUpdate(String sql, List<List<Object>> batchValues) throws SQLException;
	
}
