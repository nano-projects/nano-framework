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
package org.nanoframework.server.dao.impl;

import static org.nanoframework.orm.jdbc.binding.GlobalJdbcManager.get;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nanoframework.orm.jdbc.jstl.Result;
import org.nanoframework.server.dao.JdbcTestDao;
import org.nanoframework.server.domain.TestDomain;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:15:45
 */
public class JdbcTestDaoImpl implements JdbcTestDao {

	String select = "select waybill_no as waybillNo, source_org_code as sourceOrgCode, des_org_code as desOrgCode, source_org_city_code as sourceOrgCityCode, create_time as createTime from t_vw_fake where create_time <= ? limit ? ";
	String insert = "insert into t_vw_fake_his(waybill_no, source_org_code, des_org_code, source_org_city_code, create_time) values (?, ?, ?, ?, ?)";
	String deleteSeg = "delete from t_vw_fake where waybill_no in ( ";
	
	@SuppressWarnings("unchecked")
	@Override
	public List<TestDomain> select(Timestamp time, int limit) throws SQLException {
		List<Object> value = new ArrayList<>();
		value.add(time);
		value.add(limit);
		Result result = get("test").executeQuery(select, value);
		
		if(result != null && result.getRowCount() > 0) {
			List<TestDomain> fakeList = new ArrayList<>();
			Arrays.asList(result.getRows()).forEach(item -> fakeList.add(TestDomain._getMapToBean(item, TestDomain.class)));
			return fakeList;
		}
		
		return Collections.emptyList();
	}

	@Override
	public long move(List<TestDomain> fakeList) throws SQLException {
		List<List<Object>> batchValues = new ArrayList<>();
		fakeList.stream().map(item -> {
			List<Object> values = new ArrayList<>();
			values.add(item.getWaybillNo());
			values.add(item.getSourceOrgCode());
			values.add(item.getDesOrgCode());
			values.add(item.getSourceOrgCityCode());
			values.add(item.getCreateTime());
			return values;
		}).forEach(values -> batchValues.add(values));
		int[] changeds = get("test").executeBatchUpdate(insert, batchValues);
		long changed = 0;
		for(int c : changeds) {
			changed += c;
		}
		
		return changed;
	}

	@Override
	public long delete(List<String> waybillNos) throws SQLException {
		List<Object> values = new ArrayList<>();
		StringBuilder delete = new StringBuilder(deleteSeg);
		waybillNos.forEach(item -> {
			delete.append("?, ");
			values.add(item);
		});
		
		String newDelete = delete.substring(0, delete.length() - 2) + ")";
		return get("test").executeUpdate(newDelete, values);
	}

}
