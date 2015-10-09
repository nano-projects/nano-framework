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
package org.nanoframework.core.stereotype.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.core.component.stereotype.bind.MapperNode;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;

/**
 * @author yanghe
 * @date 2015年9月23日 下午9:29:00
 */
public class MapperNodeTest {

	@Test
	public void addNodeTest() {
		System.setProperty(Constants.CONTEXT_ROOT, "/jetty");
		Map<RequestMethod, RequestMapper> mapper = new HashMap<>();
		mapper.put(RequestMethod.GET, RequestMapper.create().setObject(this).setClz(this.getClass()));
		MapperNode.addLeaf("/jetty/test/{hello}/get", mapper);
		MapperNode.addLeaf("/jetty/test/{hello}", mapper);
		MapperNode.addLeaf("/jetty/test/{hello}/save", mapper);
		MapperNode.addLeaf("/jetty/test/{hello}/put", mapper);
		MapperNode.addLeaf("/jetty/test/{hello}/put/{id}", mapper);
		MapperNode.addLeaf("/jetty/test/hello/put/{id}", mapper);
		RequestMapper _mapper = MapperNode.get("/jetty/test/hello/put/123qweasdzxc", RequestMethod.GET);
		assertNotNull(_mapper);
		assertEquals("123qweasdzxc", _mapper.getParam().get("id"));
	}
}
