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
package org.nanoframework.examples.first.webapp.dao.impl;

import static org.nanoframework.orm.jdbc.binding.GlobalJdbcManager.get;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nanoframework.examples.first.webapp.constant.DataSource;
import org.nanoframework.examples.first.webapp.dao.JdbcExamplesDao;
import org.nanoframework.examples.first.webapp.domain.Test;
import org.nanoframework.orm.jdbc.jstl.Result;

/**
 * @author yanghe
 * @date 2015年10月12日 上午10:47:06
 */
public class JdbcExamplesDaoImpl implements JdbcExamplesDao {

	private final String insert = "INSERT INTO T_NANO_TEST(ID, NAME) VALUES (?, ?) ";
	private final String select = "SELECT ID, NAME FROM T_NANO_TEST ";
	private final String selectById = "SELECT ID, NAME FROM T_NANO_TEST WHERE ID = ? ";
	
	@Override
	public long insert(Test test) throws SQLException {
		List<Object> values = new ArrayList<>();
		values.add(test.getId());
		values.add(test.getName());
		return get(DataSource.EXAMPLES.value()).executeUpdate(insert, values);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Test> select() throws SQLException {
		Result result = get(DataSource.EXAMPLES.value()).executeQuery(select);
		if(result.getRowCount() > 0) {
			List<Test> tests = new ArrayList<>();
			Arrays.asList(result.getRows()).forEach(row -> tests.add(Test._getMapToBean(row, Test.class)));
			return tests;
		} 
		
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Test select(int id) throws SQLException {
		List<Object> values = new ArrayList<>();
		values.add(id);
		Result result = get(DataSource.EXAMPLES.value()).executeQuery(selectById, values);
		if(result.getRowCount() > 0) {
			return Test._getMapToBean(result.getRows()[0], Test.class);
		}
		
		return null;
	}

}
