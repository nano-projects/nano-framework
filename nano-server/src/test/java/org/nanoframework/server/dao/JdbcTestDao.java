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
package org.nanoframework.server.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.nanoframework.server.dao.impl.JdbcTestDaoImpl;
import org.nanoframework.server.domain.TestDomain;

import com.google.inject.ImplementedBy;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:15:51
 */
@ImplementedBy(JdbcTestDaoImpl.class)
public interface JdbcTestDao {
	public List<TestDomain> select(Timestamp time, int limit) throws SQLException;
	public long move(List<TestDomain> fakeList) throws SQLException;
	public long delete(List<String> waybillNos) throws SQLException;
	
}
