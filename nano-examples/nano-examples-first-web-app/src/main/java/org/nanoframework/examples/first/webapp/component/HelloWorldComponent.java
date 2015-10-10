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
package org.nanoframework.examples.first.webapp.component;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.RequestParam;
import org.nanoframework.examples.first.webapp.component.impl.HelloWorldComponentImpl;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;

import com.google.inject.ImplementedBy;

/**
 * @author yanghe
 * @date 2015年10月9日 下午1:07:02
 */
@Component
@ImplementedBy(HelloWorldComponentImpl.class)
public interface HelloWorldComponent {
	
	@RequestMapping("/hello")
	Object hello();
	
	@RequestMapping(value = "/hello/{value}", method = RequestMethod.GET)
	Object byRestfulApiByGet(@PathVariable("value") String value);
	
	@RequestMapping(value = "/hello/{value}", method = RequestMethod.PUT)
	Object byRestfulApiByPut(@PathVariable("value") String value, @RequestParam(name = "name") String name);
	
	@RequestMapping(value = "/hello/{value}", method = RequestMethod.PATCH)
	Object byRestfulApiByPatch(@PathVariable("value") String value, @RequestParam(name = "name") String name);
	
	@RequestMapping("/hello/forward/{value}")
	View forward(@PathVariable("value") String value, Model model);
}
