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

import org.nanoframework.core.component.aop.Before;
import org.nanoframework.core.component.aop.BeforeMore;
import org.nanoframework.core.status.ResultMap;
import org.nanoframework.examples.first.webapp.aop.Examples2AOP;
import org.nanoframework.examples.first.webapp.aop.ExamplesAOP;
import org.nanoframework.examples.first.webapp.component.MybatisExampleComponent;
import org.nanoframework.examples.first.webapp.domain.Test;
import org.nanoframework.examples.first.webapp.mapper.ExampleMapper;

import com.google.inject.Inject;

/**
 * @author yanghe
 * @date 2015年10月13日 下午3:11:32
 */
public class MybatisExampleComponentImpl implements MybatisExampleComponent {

	@Inject
	private ExampleMapper exampleMapper;
	
//	@Before(classType = ExamplesAOP.class, methodName = "before")
	@BeforeMore(value = { @Before(classType = ExamplesAOP.class), @Before(classType = Examples2AOP.class) })
	@Override
	public Object findAll() {
		List<Test> testList = exampleMapper.select();
		Map<String, Object> map = ResultMap.create(200, "Find all Test", "SUCCESS")._getBeanToMap();
		map.put("records", testList.size());
		map.put("rows", testList);
		return map;
	}

}
