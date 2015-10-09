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
package org.nanoframework.server.component.impl;

import org.nanoframework.core.component.aop.After;
import org.nanoframework.core.component.aop.Before;
import org.nanoframework.core.component.aop.BeforeAndAfter;
import org.nanoframework.server.component.TestComponent;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:15:28
 */
public class TestComponentImpl implements TestComponent {

	@Override
	public Object hello(String name) {
		return "hello, " + name;
	}

	@Override
	public Object each() {
		System.out.println("AOP Each");
		return "each world";
	}
	
	@Override
	@BeforeAndAfter(before = @Before(classType = TestComponentImpl.class, methodName = "each"), after = @After(classType = TestComponentImpl.class, methodName = "each"))
	public Object get(String id) {
		return "get id: " + id;
	}

	@Override
	public Object get(String id, String name) {
		return "id: " + id + ", name: " + name;
	}
	
	@Override
	public Object getName(String id, String name) {
		return "id: " + id + ", name: " + name;
	}
	
	@Override
	public Object getNameByGET(String id, String name) {
		return "id: " + id + ", name: " + name + " to GET Request";
	}
	
	@Override
	public Object getNameByPOST(String id, String name) {
		return "id: " + id + ", name: " + name + " to POST Request";
	}

	@Override
	public Object getNameByHEAD(String id, String name) {
		return "id: " + id + ", name: " + name + " to HEAD Request";
	}

	@Override
	public Object getNameByPUT(String id, String name) {
		return "id: " + id + ", name: " + name + " to PUT Request";
	}

	@Override
	public Object getNameByDELETE(String id, String name) {
		return "id: " + id + ", name: " + name + " to DELETE Request";
	}

	@Override
	public Object getNameByOPTIONS(String id, String name) {
		return "id: " + id + ", name: " + name + " to OPTIONS Request";
	}

	@Override
	public Object getNameByTRACE(String id, String name) {
		return "id: " + id + ", name: " + name + " to TRACE Request";
	}
	
	@Override
	public Object getNameByPATCH(String id, String name) {
		return "id: " + id + ", name: " + name + " to PATCH Request";
	}
}
