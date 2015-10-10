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

import org.nanoframework.core.status.ResultMap;
import org.nanoframework.examples.first.webapp.component.HelloWorldComponent;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;
import org.nanoframework.web.server.mvc.support.RedirectView;

/**
 * @author yanghe
 * @date 2015年10月9日 下午1:07:52
 */
public class HelloWorldComponentImpl implements HelloWorldComponent {

	@Override
	public Object hello() {
		return "Hello Nano Framework!";
	}
	
	@Override
	public Object byRestfulApiByGet(String value) {
		return "Hello Nano Framework by Restful API and GET method, this value is " + value;
	}

	@Override
	public Object byRestfulApiByGetParam(String value, String name) {
		return "Hello Nano Framework by Restful API and GET method, this value is " + value + " and name is " + name;
	}
	
	@Override
	public Object byRestfulApiByPut(String value, String name) {
		return ResultMap.create(200, "Hello Nano Framework by Restful API and PUT method, this value is " + value + " and name is " + name, "SUCCESS");
	}
	
	@Override
	public Object byRestfulApiByPatch(String value, String name) {
		return ResultMap.create(200, "Hello Nano Framework by Restful API and PATCH method, this value is " + value + " and name is " + name, "SUCCESS");
	}
	
	@Override
	public View forward(String value, Model model) {
		model.addAttribute("value", "Hello Nano Framework by Forward API, and value is " + value);
		return new ForwardView("/pages/forword.jsp", true);
	}
	
	@Override
	public View redirect(String value, Model model) {
		model.addAttribute("value", "Hello Nano Framework by Redirect API, and value is " + value);
		return new RedirectView("/pages/redirect.jsp");
	}
}
