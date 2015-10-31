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
package org.nanoframework.core.plugins.defaults.module;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.plugins.PluginLoaderException;

/**
 * @author yanghe
 * @date 2015年10月31日 上午10:42:22
 */
public class DataSourceModule extends Module {

	@SuppressWarnings("unchecked")
	@Override
	public List<Module> load() throws Throwable {
		try {
			Class<?> DataSourceLoader = Class.forName("org.nanoframework.orm.DataSourceLoader");
			Object dsl = DataSourceLoader.newInstance();
			PropertiesLoader.PROPERTIES.putAll((Map<String, Properties>) DataSourceLoader.getMethod("getLoadProperties").invoke(dsl));
			modules.addAll((List<Module>) DataSourceLoader.getMethod("getModules").invoke(dsl));
			
		} catch(Exception e) {
			if(!(e instanceof ClassNotFoundException))
				throw new PluginLoaderException(e.getMessage(), e);
		}
		
		return modules;
	}

	@Override
	public void config(ServletConfig config) throws Throwable {

	}

	@Override
	protected void configure() {
		
	}

}
