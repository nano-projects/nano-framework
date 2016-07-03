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
package org.nanoframework.web.entity;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;

import com.alibaba.fastjson.JSON;

import junit.framework.TestCase;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:23:24
 */
public class EntitySerialTest extends TestCase {

	private Logger LOG = LoggerFactory.getLogger(EntitySerialTest.class);
	
	public void test() {
		EntityTest test = new EntityTest();
		test.setId("123456");
		test.setName("Test");
		
		LOG.info("转换成JSON: " + JSON.toJSONString(test));

		LOG.info("获取属性: EntityTest.ID = " + test._getAttributeValue(EntityTest.ID));
		
		test._setAttributeValue(EntityTest.NAME, "Fuck you");
		LOG.info("设置属性: EntityTest.Name = Fuck you");
		
		LOG.info("获取新设置的属性值: EntityTest.Name = " + test._getAttributeValue(EntityTest.NAME));
		
		LOG.info("获取所有的属性名: " + StringUtils.join(test._getAttributeNames(), ", "));
		
	}
}
