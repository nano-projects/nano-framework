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
package org.nanoframework.commons.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.nanoframework.commons.util.URLContext;

/**
 * @author yanghe
 * @date 2015年10月8日 下午3:21:16
 */
public class StringFormatTest {
	@Test
	public void formatURLTest() {
		String url = "/web/url/test?param1=123&param2=456&param3[]=a&param3[]=b&param3[]=c&param3[]=a";
		URLContext context = StringFormat.formatURL(url);
		assertEquals("/web/url/test", context.getContext());
		assertNotNull(context.getParameter());
		assertEquals("123", context.getParameter().get("param1"));
		assertEquals("456", context.getParameter().get("param2"));
		assertEquals(String[].class, context.getParameter().get("param3[]").getClass());
	}
}
