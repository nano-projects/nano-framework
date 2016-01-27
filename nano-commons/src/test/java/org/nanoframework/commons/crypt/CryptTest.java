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
package org.nanoframework.commons.crypt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * @author yanghe
 * @date 2015年10月8日 下午1:58:16
 */
public class CryptTest {
	private Logger LOG = LoggerFactory.getLogger(CryptTest.class);
	
	@Test
	public void cryptByDefaultTest() {
		String testString = "test crypt encrypt";
		String encode = CryptUtil.encrypt(testString);
		String decode = CryptUtil.decrypt(encode);
		LOG.debug("encode: " + encode);
		assertEquals(testString, decode);
	}
	
	@Test
	public void cryptByUsePasswdTest() {
		String testString = "test crypt encrypt by use passwd";
		String encode = CryptUtil.encrypt(testString, "use passwd");
		String decode = CryptUtil.decrypt(encode, "use passwd");
		LOG.debug("encode: " + encode);
		assertEquals(testString, decode);
	}
}
