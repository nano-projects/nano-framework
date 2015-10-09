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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.nanoframework.orm.jdbc.jstl.Result;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:11:33
 */
public interface DefaultSqlExecutor {
	void commit(Connection conn) throws SQLException;
	
	void rollback(Connection conn) throws SQLException;
	
	Result executeQuery(String sql, Connection conn) throws SQLException;
	
	int executeUpdate(String sql, Connection conn) throws SQLException;
	
	Result executeQuery(String sql, List<Object> values, Connection conn) throws SQLException;
	
	int executeUpdate(String sql, List<Object> values, Connection conn) throws SQLException;
	
	int[] executeBatchUpdate(String sql, List<List<Object>> batchValues, Connection conn) throws SQLException;
	
}
