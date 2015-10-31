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
package org.nanoframework.web.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.core.plugins.PluginLoader;

public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 2341783013239890497L;
	private Logger LOG = LoggerFactory.getLogger(DispatcherServlet.class);
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			String pluginLoader = this.getInitParameter(Constants.PLUGIN_LOADER);
			if(StringUtils.isBlank(pluginLoader))
				throw new IllegalArgumentException("请在DispacherServlet中添加PluginLoader设置");
			
			Class<?> cls = Class.forName(pluginLoader);
			if(PluginLoader.class.isAssignableFrom(cls)) {
				((PluginLoader) cls.newInstance()).init(this.getServletConfig());
				
			} else 
				throw new IllegalArgumentException("pluginLoader配置的类必须继承PluginLoader类");
			
		} catch(Throwable e) {
			LOG.error(e.getMessage(), e);
			System.exit(1);
		}
	}
	
}
