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
import java.util.List;

import org.nanoframework.examples.first.webapp.constant.DataSource;
import org.nanoframework.examples.first.webapp.dao.JdbcExamplesMoveDao;
import org.nanoframework.examples.first.webapp.domain.Test;

/**
 * @author yanghe
 * @date 2015年10月12日 下午3:28:47
 */
public class JdbcExamplesMoveDaoImpl implements JdbcExamplesMoveDao {

	private final String insert = "INSERT INTO T_NANO_TEST(ID, NAME) VALUES (?, ?) ";
	
	@Override
	public long insert(Test test) throws SQLException {
		List<Object> values = new ArrayList<>();
		values.add(test.getId());
		values.add(test.getName());
		return get(DataSource.EXAMPLES2).executeUpdate(insert, values);
	}
}
