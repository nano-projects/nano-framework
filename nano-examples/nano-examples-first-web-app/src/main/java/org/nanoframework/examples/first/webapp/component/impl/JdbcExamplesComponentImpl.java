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
package org.nanoframework.examples.first.webapp.component.impl;

import java.util.List;
import java.util.Map;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.component.exception.ComponentInvokeException;
import org.nanoframework.web.server.http.status.ResultMap;
import org.nanoframework.examples.first.webapp.component.JdbcExamplesComponent;
import org.nanoframework.examples.first.webapp.constant.DataSource;
import org.nanoframework.examples.first.webapp.dao.JdbcExamplesDao;
import org.nanoframework.examples.first.webapp.dao.JdbcExamplesMoveDao;
import org.nanoframework.examples.first.webapp.domain.Test;
import org.nanoframework.orm.jdbc.binding.JdbcTransactional;

import com.google.inject.Inject;

/**
 * @author yanghe
 * @date 2015年10月12日 上午11:03:47
 */
public class JdbcExamplesComponentImpl implements JdbcExamplesComponent {
	private Logger LOG = LoggerFactory.getLogger(JdbcExamplesComponentImpl.class);
	
	@Inject
	private JdbcExamplesDao examplsDao;
	
	@Inject
	private JdbcExamplesMoveDao examplesMoveDao;
	
	@JdbcTransactional(envId = DataSource.EXAMPLES)
	@Override
	public Object persist(Integer id, String name) {
		Test test = new Test(id, name);
		try { 
			long changed = examplsDao.insert(test);
			if(changed > 0)
				return ResultMap.create(200, "写入数据库成功", "SUCCESS");
			else 
				return ResultMap.create(200, "写入数据库失败", "ERROR");
		} catch(Exception e) {
			LOG.error("写入数据库异常: " + e.getMessage(), e);
			return ResultMap.create(500, "写入数据库异常: " + e.getMessage(), e.getClass().getName());
		}
	}

	@Override
	public Object findAll() {
		try {
			List<Test> testList = examplsDao.select();
			Map<String, Object> map = ResultMap.create(200, "OK", "SUCCESS")._getBeanToMap();
			map.put("records", testList.size());
			map.put("rows", testList);
			return map;
		} catch(Exception e) {
			LOG.error("查询数据异常: " + e.getMessage(), e);
			return ResultMap.create(500, "查询数据异常: " + e.getMessage() , e.getClass().getName());
		}
	}

	@Override
	public Object findById(Integer id) {
		try {
			Test test = examplsDao.select(id);
			Map<String, Object> map = ResultMap.create(200, "OK", "SUCCESS")._getBeanToMap();
			map.put("data", test);
			return map;
		} catch(Exception e) {
			LOG.error("查询数据异常: " + e.getMessage(), e);
			return ResultMap.create(500, "查询数据异常: " + e.getMessage() , e.getClass().getName());
		}
	}

	@JdbcTransactional(envId = {DataSource.EXAMPLES, DataSource.EXAMPLES2})
	@Override
	public Object move(Integer id) {
		try {
			Test test = examplsDao.select(id);
			if(test == null) {
				return ResultMap.create(200, "Not Found Data", "WARNING");
			} else {
				if(examplesMoveDao.insert(test) > 0) {
					examplsDao.delete(id);
				}
			}
		} catch(Exception e) {
			throw new ComponentInvokeException(e.getMessage(), e);
		}
		
		return ResultMap.create(200, "OK", "SUCCESS");
	}
}
