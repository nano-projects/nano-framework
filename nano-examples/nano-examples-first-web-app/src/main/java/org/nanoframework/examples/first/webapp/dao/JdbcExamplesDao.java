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
package org.nanoframework.examples.first.webapp.dao;

import java.sql.SQLException;
import java.util.List;

import org.nanoframework.examples.first.webapp.dao.impl.JdbcExamplesDaoImpl;
import org.nanoframework.examples.first.webapp.domain.Test;

import com.google.inject.ImplementedBy;

/**
 * @author yanghe
 * @date 2015年10月12日 上午10:42:40
 */
@ImplementedBy(JdbcExamplesDaoImpl.class)
public interface JdbcExamplesDao {
	long insert(Test test) throws SQLException;
	List<Test> select() throws SQLException;
	Test select(int id) throws SQLException;
}
