/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.commons.util;

import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * @author yanghe
 * @date 2016年1月19日 上午9:05:43
 */
public class ObjectCompareTest {
	private Logger LOG = LoggerFactory.getLogger(ObjectCompareTest.class);
	
	@Test
	public void test0() {
		LOG.debug("Test reg empty: " + ObjectCompare.isInListByRegEx("Test", ""));
	}
}
