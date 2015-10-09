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
package org.nanoframework.server.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.nanoframework.core.component.exception.ServiceInvokeException;
import org.nanoframework.orm.jdbc.binding.JdbcTransactional;
import org.nanoframework.server.dao.JdbcTestDao;
import org.nanoframework.server.domain.TestDomain;
import org.nanoframework.server.service.JdbcTestService;

import com.google.inject.Inject;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:16:15
 */
public class JdbcTestServiceImpl implements JdbcTestService {

	@Inject
	private JdbcTestDao testDao;
	
	@JdbcTransactional(envId = { "test" })
	@Override
	public long exchanged(Timestamp time, int limit) {
		try {
			List<TestDomain> fakeList = testDao.select(time, limit);
			long changed = 0;
			if(fakeList != null && !fakeList.isEmpty()) {
				changed = testDao.move(fakeList);
				List<String> waybillNos = new ArrayList<>();
				fakeList.forEach(fake -> waybillNos.add(fake.getWaybillNo()));
				if(testDao.delete(waybillNos) != changed)
					throw new ServiceInvokeException("迁移的数据与删除的数据不一致");
			}
			
			return changed;
			
		} catch(Throwable e) {
			throw new ServiceInvokeException(e.getMessage(), e);
			
		}
	}

	

}
