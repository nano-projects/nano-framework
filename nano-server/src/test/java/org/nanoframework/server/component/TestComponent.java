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
package org.nanoframework.server.component;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.RequestParam;
import org.nanoframework.server.component.impl.TestComponentImpl;

import com.google.inject.ImplementedBy;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:15:34
 */
@Component
@ImplementedBy(TestComponentImpl.class)
@RequestMapping(value = "/test")
public interface TestComponent {
	@RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
	public Object hello(@PathVariable("name") String name);
	
	@RequestMapping("/each")
	public Object each();
	
	@RequestMapping(value = "/get")
	public Object get(@RequestParam(name = "id") String id);
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public Object get(@RequestParam(name = "id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}/name/{name}")
	public Object getName(@PathVariable("id") String id, @PathVariable("name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
	public Object getNameByGET(@PathVariable("id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.POST)
	public Object getNameByPOST(@PathVariable("id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.HEAD)
	public Object getNameByHEAD(@PathVariable("id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.PUT)
	public Object getNameByPUT(@PathVariable("id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.DELETE)
	public Object getNameByDELETE(@PathVariable("id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.OPTIONS)
	public Object getNameByOPTIONS(@PathVariable("id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.TRACE)
	public Object getNameByTRACE(@PathVariable("id") String id, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/get/{id}", method = RequestMethod.PATCH)
	public Object getNameByPATCH(@PathVariable("id") String id, @RequestParam(name = "name") String name);
}
