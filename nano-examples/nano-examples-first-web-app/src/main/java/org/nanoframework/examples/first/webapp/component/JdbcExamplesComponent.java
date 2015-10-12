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
import org.nanoframework.core.component.stereotype.bind.RequestParam;
import org.nanoframework.examples.first.webapp.component.impl.JdbcExamplesComponentImpl;

import com.google.inject.ImplementedBy;

/**
 * @author yanghe
 * @date 2015年10月12日 上午11:03:20
 */
@Component
@ImplementedBy(JdbcExamplesComponentImpl.class)
@RequestMapping("/jdbc")
public interface JdbcExamplesComponent {
	@RequestMapping("/persist")
	Object persist(@RequestParam(name = "id") Integer id, @RequestParam(name = "name") String name);
	
	@RequestMapping("/find/all")
	Object findAll();
	
	@RequestMapping("/find/{id}")
	Object findById(@PathVariable("id") Integer id);
	
	@RequestMapping("/persist/move/{id}")
	Object move(@PathVariable("id") Integer id);
}
