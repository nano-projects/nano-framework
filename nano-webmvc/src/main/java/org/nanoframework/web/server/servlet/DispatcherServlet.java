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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.component.aop.AOPModule;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.orm.DataSourceLoader;
import org.nanoframework.orm.jdbc.JdbcCreater;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * NanoFramework WebMVC启动服务入口，在web.xml中添加此类的servlet配置并设置
 * 默认的启动顺序。<br>
 * 启动过程如下: <br>
 * 1) 系统属性加载，默认会加载/context.properties的属性，同时加载context属性中
 * 配置的属性文件。<br>
 * 2) 加载Redis配置，默认加载/redis.properties中的属性。<br>
 * 3) 加载组件AOP Module及数据源Module<br>
 * 4) 加载组件服务，根据属性文件中配置的{context.component-scan.base-package}属性加载
 * 实现了{@link org.nanoframework.web.component.stereotype.Component}
 * 注解的所有的类。<br>
 * 5) 加载任务调度，根据属性文件中配置的{context.quartz-scan.base-package}属性加载
 * 实现了{@link org.nanoframework.web.concurrent.quartz.Quartz}
 * 注解并继承了{@link org.nanoframework.web.concurrent.quartz.BaseQuartz}
 * 类的所有的类。<br>
 * 6) 启动所有的任务调度缓冲区中的调度服务。<br>
 * 7) 启动WebSocket服务。<br>
 * 
 * @author yanghe
 * @date 2015年7月24日 下午9:41:03 
 *
 * @see org.nanoframework.web.concurrent.quartz.QuartzFactory#load()
 * @see org.nanoframework.web.concurrent.quartz.QuartzFactory#startAll()
 */
@JdbcCreater
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 2341783013239890497L;

	private Logger LOG = LoggerFactory.getLogger(DispatcherServlet.class);
	
	/**
	 * 初始化服务项
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		
		try {
			initProperties();
			initRedis();
			initModules();
			initComponent();
			initQuartz();
			initWebSocket();
			
		} catch(Throwable e) {
			LOG.error(e.getMessage(), e);
			System.exit(1);
		}
	}
	
	/**
	 * 加载属性文件，默认加载/context.properties路径的属性文件<br>
	 * 可以通过Servlet启动参数{ context }指定属性文件的路径
	 * 
	 * @throws LoaderException 属性文件加载异常，当属性文件流为null时抛出此异常
	 * @throws IOException 文件加载异常
	 */
	private void initProperties() throws LoaderException, IOException {
		String context = this.getInitParameter("context");
		if(StringUtils.isEmpty(context))
			context = Constants.MAIN_CONTEXT;
		
		InputStream input = this.getClass().getResourceAsStream(context);
	 	long time = System.currentTimeMillis();
	 	PropertiesLoader.load(context, input, true);
		LOG.info("加载系统属性, 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	/**
	 * 加载Redis配置，默认加载/redis.properties路径的属性文件<br>
	 * 可以通过Servlet启动参数{ redis }指定属性文件的路径，<br>
	 * 支持加载多个属性文件配置。<br>
	 * 例如: /redis1.properties;/redis2.properties
	 *  
	 * @throws LoaderException 属性文件加载异常，当属性文件流为null时抛出此异常
	 * @throws IOException 文件加载异常
	 */
	private void initRedis() throws LoaderException, IOException {
		try {
			Class<?> redisClientPool = Class.forName("org.nanoframework.orm.jedis.RedisClientPool");
			long time = System.currentTimeMillis();
			String redis = this.getInitParameter("redis");
			List<Properties> propList = Collections.emptyList();
			if(StringUtils.isNotBlank(redis)) {
				propList = new ArrayList<>();
				String[] paths = redis.split(";");
				for(String path : paths) {
					propList.add(PropertiesLoader.load(DispatcherServlet.class.getResourceAsStream(path)));
				}
			}
			
			Object pool = redisClientPool.getField("POOL").get(redisClientPool);
			pool.getClass().getMethod("initRedisConfig", List.class).invoke(pool, propList);
			pool.getClass().getMethod("createJedis").invoke(pool);
			
			LOG.info("加载Redis配置, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		} catch(ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			if(!(e instanceof ClassNotFoundException))
				throw new RuntimeException(e);
		}
	}
	
	/**
	 * 加载Guice Module <br>
	 * 1) 加载组件服务的AOP注入，对实现了{@link org.nanoframework.web.component.stereotype.invoke.ComponentInvoke}、
	 * {@link org.nanoframework.web.component.stereotype.invoke.ServiceInvoke}注解的
	 * 类进行AOP的支持<br>
	 * 
	 * @throws Exception 加载数据源异常时抛出此异常
	 * @see org.nanoframework.web.server.servlet.DispatcherServlet#loadDataSource(List, long)
	 * @see org.nanoframework.web.globals.Globals#set(Class, Object)
	 */
	private void initModules() throws Throwable {
		long time = System.currentTimeMillis();
		List<Module> modules = new ArrayList<>();
		modules.add(new AOPModule());
		LOG.info("创建组件依赖注入模块, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		
		DataSourceLoader loader = new DataSourceLoader();
		PropertiesLoader.PROPERTIES.putAll(loader.getLoadProperties());
		modules.addAll(loader.getModules());
		
		String moduleClasses = this.getInitParameter("module-classes");
		if(StringUtils.isNotEmpty(moduleClasses)) {
			try {
				String[] classes = moduleClasses.split(";");
				for(String className : classes) {
					modules.add((Module) Class.forName(className).newInstance());
				}
			} catch(Exception e) {
				throw new ServletException("module-classes没有配置正确，此处的类必须为全路径，且必须实现com.google.inject.Module接口或者继承com.google.inject.AbstractModule");
			}
		}
		
		time = System.currentTimeMillis();
		LOG.info("开始进行依赖注入");
		Globals.set(Injector.class, Guice.createInjector(modules));
		LOG.info("依赖注入完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	/**
	 * 初始化组件服务
	 * 
	 * @throws LoaderException 属性文件加载异常，当属性文件流为null时抛出此异常
	 * @throws IOException 文件加载异常
	 * 
	 * @see org.nanoframework.web.component.Components#load()
	 */
	private void initComponent() throws LoaderException, IOException {
		long time = System.currentTimeMillis();
		LOG.info("开始加载组件服务");
		Components.load();
		LOG.info("加载组件服务结束, 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	/**
	 * 初始化任务调度至缓冲区
	 * 
	 * @throws IllegalArgumentException 传入的参数不符合要求时抛出此异常
	 * @throws IllegalAccessException 安全权限异常，如果放射调用的属性为private时抛出此异常
	 * 
	 * @see org.nanoframework.web.concurrent.quartz.QuartzFactory#load()
	 */
	private void initQuartz() throws IllegalArgumentException, IllegalAccessException {
		try {
			Class<?> quartzFactory = Class.forName("org.nanoframework.extension.concurrent.quartz.QuartzFactory");
			long time = System.currentTimeMillis();
			LOG.info("开始加载任务调度");
			quartzFactory.getMethod("load").invoke(quartzFactory);
			quartzFactory.getMethod("startAll").invoke(quartzFactory);
			LOG.info("加载任务调度结束, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		} catch(Exception e) {
			if(!(e instanceof ClassNotFoundException))
				throw new RuntimeException(e);
			
		}
	}
	
	/**
	 * 初始化WebSocket服务
	 * 
	 * @throws ClassNotFoundException
	 * @throws CertificateException
	 * @throws SSLException
	 * @throws InterruptedException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void initWebSocket() throws ClassNotFoundException, CertificateException, SSLException, InterruptedException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		try {
			Class<?> websocket = Class.forName("org.nanoframework.extension.websocket.WebSocketFactory");
			long time = System.currentTimeMillis();
			LOG.info("开始加载WebSocket服务");
			websocket.getMethod("load").invoke(websocket);
			LOG.info("加载WebSocket服务完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		} catch(ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			if(!(e instanceof ClassNotFoundException))
				throw new RuntimeException(e);
			
		}
	}
}
