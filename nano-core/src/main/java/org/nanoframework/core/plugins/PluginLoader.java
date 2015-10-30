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
package org.nanoframework.core.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.orm.DataSourceLoader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author yanghe
 * @date 2015年10月30日 下午10:29:16
 */
public abstract class PluginLoader {
	private Logger LOG = LoggerFactory.getLogger(PluginLoader.class);
	
	private List<Module> modules = new LinkedList<>();
	private Set<String> properties = new LinkedHashSet<>();
	private List<Plugin> plugins = new LinkedList<>();
	private ServletConfig config;
	
	public void init(ServletConfig config) {
		Assert.notNull(config);
		this.config = config;
		
		try {
			initProperties();
			initModules();
			initPlugins();
			initComponent();
			
		} catch(Throwable e) {
			throw new PluginLoaderException(e.getMessage(), e);
		}
	}
	
	private void initProperties() {
		if(!properties.contains(Constants.MAIN_CONTEXT))
			properties.add(Constants.MAIN_CONTEXT);
		
		long time = System.currentTimeMillis();
		for(String path : properties) {
			InputStream input = this.getClass().getResourceAsStream(path);
		 	try { 
		 		PropertiesLoader.load(path, input, true); 
		 	} catch(Exception e) {
		 		throw new PluginLoaderException(e.getMessage(), e);
		 	}
		};
		
		LOG.info("加载属性文件完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	private void initModules() {
		long time = System.currentTimeMillis();
		configModules();
		DataSourceLoader loader = new DataSourceLoader();
		PropertiesLoader.PROPERTIES.putAll(loader.getLoadProperties());
		modules.addAll(loader.getModules());
		LOG.info("开始进行依赖注入");
		Globals.set(Injector.class, Guice.createInjector(modules));
		LOG.info("依赖注入完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	private void initComponent() throws LoaderException, IOException {
		long time = System.currentTimeMillis();
		LOG.info("开始加载组件服务");
		Components.load();
		LOG.info("加载组件服务结束, 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	private void initPlugins() throws Throwable {
		long time = System.currentTimeMillis();
		configPlugin();
		for(Plugin plugin : plugins) {
			plugin.config(config);
			plugin.load();
		}
		
		LOG.info("加载插件完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	public abstract void configProperties();
	public abstract void configModules();
	public abstract void configPlugin();
	
	protected void addProperties(String path) {
		Assert.hasLength(path);
		properties.add(path);
	}
	
	protected void addModule(Module module) {
		Assert.notNull(module);
		modules.add(module);
	}
	
	protected void addPlugin(Plugin plugin) {
		Assert.notNull(plugin);
		plugins.add(plugin);
	}
}
