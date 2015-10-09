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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import org.nanoframework.orm.PoolTypes;
import org.nanoframework.orm.jdbc.DataSourceException;
import org.nanoframework.orm.jdbc.JdbcCreater;
import org.nanoframework.orm.jdbc.binding.JdbcModule;
import org.nanoframework.orm.jdbc.config.C3P0JdbcConfig;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;

import com.alibaba.fastjson.JSON;
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
	private void initModules() throws Exception {
		long time = System.currentTimeMillis();
		List<Module> modules = new ArrayList<>();
		modules.add(new AOPModule());
		LOG.info("创建组件依赖注入模块, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		
		time = System.currentTimeMillis();
		PropertiesLoader.PROPERTIES.putAll(loadDataSource(modules, time));
		
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
	 * 加载数据源配置<br>
	 * 读取方式一: <br>
	 * 根据全局属性列表获取{ mapper.package.name }、{ mapper.package.jdbc }及{ mapper.package.helper }
	 * 属性，如果设置了{ mapper.package.name }属性，则加载{ mapper.package.jdbc }设置的属性文件。
	 * 如果设置了{ mapper.package.helper }的话，则使用JdbcHelper中的枚举项，此属性的名称必须在JdbcHelper中存在。<br>
	 * JDBC属性文件中需要配置{ mybatis.environment.id }属性以标示其为MyBatis的数据源。<br>
	 * 以此方式加载的数据源为MyBatis的模式<br>
	 * 
	 * <br>
	 * 读取方式二: <br>
	 * 根据全局属性列表获取{ mapper.package.root }属性，支持多项，以","分隔，以下称为"{root}"<br>
	 * 以此属性为基础，读取{ mapper.package.name }、{ mapper.package.jdbc }及{ mapper.package.helper }
	 * 再加上".{root}"，读取方式同"读取方式一"。<br>
	 * JDBC属性文件中需要配置{ mybatis.environment.id }属性以标示其为MyBatis的数据源。<br>
	 * 以此方式加载的数据源为MyBatis的模式<br>
	 * <br>
	 * 读取方式三: <br>
	 * 基于读取方式一和读取方式二中获取到的DBConfig集合来创建JDBC连接池管理类。<br>
	 * JDBC属性文件中需要配置{ JDBC.environment.id }属性以标示其为原生JDBC的数据源。<br>
	 * 以此方式加载的数据源为JDBC的模式<br>
	 * 
	 * @since 1.2 将ORM作为插件包来使用，默认使用ORM-JDBC来加载，需要使用Mybatis时增加对于的maven依赖即可自动进行切换
	 * 
	 * @param modules Guice Module列表
	 * @param time 系统时间
	 * @return 返回属性文件路径与属性文件映射的Map对象
	 * @throws LoaderException 属性文件加载异常，当属性文件流为null时抛出此异常
	 * @throws IOException 文件加载异常
	 * @throws IllegalArgumentException 传入的参数不符合要求时抛出此异常
	 * @throws IllegalAccessException 安全权限异常，如果放射调用的属性为private时抛出此异常
	 * @throws NoSuchFieldException 根据{ mapper.package.helper }属性获取到的值不存在于JdbcHelper中时抛出此异常
	 * @throws SecurityException 安全权限异常
	 * 
	 * @see org.nanoframework.orm.jdbc.JdbcAdapter#newInstance(java.util.Collection, Object)
	 * @see org.mybatis.guice.datasource.helper.JdbcHelper
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Properties> loadDataSource(List<Module> modules, long time) throws LoaderException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Map<String, Properties> newLoadProperties = new HashMap<>();
		Map<String, JdbcConfig> configs = new HashMap<>();
		List dsConf = new ArrayList<>();
		Set<PoolTypes> poolTypes = new HashSet<>();
		try {
			Class<?> dataSourceConfig = Class.forName("org.nanoframework.orm.mybatis.DataSourceConfig");
			Class<?> jdbcHelper = Class.forName("org.mybatis.guice.datasource.helper.JdbcHelper");
			Constructor<?> constructor = dataSourceConfig.getConstructor(String.class, Properties.class, jdbcHelper, PoolTypes.class);
			String envId = (String) dataSourceConfig.getField("MYBATIS_ENVIRONMENT_ID").get(dataSourceConfig);
			for(Properties prop : PropertiesLoader.PROPERTIES.values()) {
				String mapperPackageName;
				String mapperPackageRoot;
				
				if(StringUtils.isNotBlank(mapperPackageName = prop.getProperty(Constants.MAPPER_PACKAGE_NAME)) || 
						StringUtils.isNotBlank(prop.getProperty(Constants.MAPPER_PACKAGE_JDBC))) {
					/** 读取方式一 */
					String jdbcURI = prop.getProperty(Constants.MAPPER_PACKAGE_JDBC);
					Properties jdbc = null;
					if(StringUtils.isNotBlank(jdbcURI)) {
						jdbc = PropertiesLoader.PROPERTIES.get(jdbcURI);
						if(jdbc == null) {
							jdbc = PropertiesLoader.load(DispatcherServlet.class.getResourceAsStream(jdbcURI));
							
							if(jdbc != null)
								newLoadProperties.put(jdbcURI, jdbc);
							else 
								throw new DataSourceException("数据源没有配置或配置错误: " + jdbcURI);
							
						}
					}
					
					if(StringUtils.isNotBlank(jdbc.getProperty(envId))) {
						String helperAlias = prop.getProperty(Constants.MAPPER_PACKAGE_HELPER);
						Object helper = null;
						if(StringUtils.isNotBlank(helperAlias)) 
							helper = jdbcHelper.getField(helperAlias).get(jdbcHelper);
						
						String poolTypeAlias = jdbc.getProperty(Constants.JDBC_POOL_TYPE);
						PoolTypes poolType = null;
						if(StringUtils.isNotBlank(poolTypeAlias)) {
							poolType = (PoolTypes) PoolTypes.class.getField(poolTypeAlias).get(PoolTypes.class);
							if(poolType == null)
								throw new IllegalArgumentException("无效的Pool类型名称: " + poolTypeAlias);
							
							poolTypes.add(poolType);
						}
						
						if(StringUtils.isNotBlank(mapperPackageName)) {
							dsConf.add(constructor.newInstance(mapperPackageName, jdbc, helper, poolType));
						} else if(StringUtils.isNotBlank((mapperPackageName = jdbc.getProperty(Constants.MAPPER_PACKAGE_NAME)))) {
							dsConf.add(constructor.newInstance(mapperPackageName, jdbc, helper, poolType));
						} else 
							throw new DataSourceException("没有配置Mapper包路径，请配置属性{ " + Constants.MAPPER_PACKAGE_NAME + " }，推荐配置在jdbc属性文件中。");
						
						LOG.info("创建数据源依赖注入模块, Mapper包路径: " + mapperPackageName + ", 耗时: " + (System.currentTimeMillis() - time) + "ms");
					} else if(StringUtils.isNotBlank(jdbc.getProperty(JdbcConfig.JDBC_ENVIRONMENT_ID))) {
						String poolTypeAlias = jdbc.getProperty(Constants.JDBC_POOL_TYPE);
						PoolTypes poolType = null;
						if(StringUtils.isNotBlank(poolTypeAlias)) {
							poolType = (PoolTypes) PoolTypes.class.getField(poolTypeAlias).get(PoolTypes.class);
							if(poolType == null)
								throw new IllegalArgumentException("无效的Pool类型名称: " + poolTypeAlias);
							
							poolTypes.add(poolType);
						}
						
						JdbcConfig config;
						if(poolType != null) {
							switch(poolType) {
							case C3P0: 
								config = new C3P0JdbcConfig(jdbc);
								break;
							case DRUID: 
								config = new DruidJdbcConfig(jdbc);
								break;
								default: 
									throw new IllegalArgumentException("Can not support this poolType: " + poolType);
							}
						} else {
							config = new DruidJdbcConfig(jdbc);
						}
						
						configs.put(config.getEnvironmentId(), config);
					}
				} else if(StringUtils.isNotBlank(mapperPackageRoot = prop.getProperty(Constants.MAPPER_PACKAGE_ROOT))) {
					/** 读取方式二 */
					String[] roots = mapperPackageRoot.split(",");
					for(String root : roots) {
						String mapperPackageNameByRoot;
						if(StringUtils.isNotBlank(mapperPackageNameByRoot = prop.getProperty(Constants.MAPPER_PACKAGE_NAME + "." + root)) || 
								StringUtils.isNotBlank(prop.getProperty(Constants.MAPPER_PACKAGE_JDBC + "." + root))) {
							
							String jdbcURI = prop.getProperty(Constants.MAPPER_PACKAGE_JDBC + "." + root);
							Properties jdbc = null;
							if(StringUtils.isNotBlank(jdbcURI)) {
								jdbc = PropertiesLoader.PROPERTIES.get(jdbcURI);
								if(jdbc == null) {
									jdbc = PropertiesLoader.load(DispatcherServlet.class.getResourceAsStream(jdbcURI));
									
									if(jdbc != null)
										newLoadProperties.put(jdbcURI, jdbc);
									else 
										throw new DataSourceException("数据源没有配置或配置错误: " + jdbcURI);
									
								}
							}
							
							if(StringUtils.isNotBlank(jdbc.getProperty(envId))) {
								String helperAlias = prop.getProperty(Constants.MAPPER_PACKAGE_HELPER + "." + root);
								Object helper = null;
								if(StringUtils.isNotBlank(helperAlias)) 
									helper = jdbcHelper.getField(helperAlias).get(jdbcHelper);
								
								String poolTypeAlias = jdbc.getProperty(Constants.JDBC_POOL_TYPE);
								PoolTypes poolType = null;
								if(StringUtils.isNotBlank(poolTypeAlias)) {
									poolType = (PoolTypes) PoolTypes.class.getField(poolTypeAlias).get(PoolTypes.class);
									if(poolType == null)
										throw new IllegalArgumentException("无效的Pool类型名称: " + poolTypeAlias);
									
									poolTypes.add(poolType);
								}
								
								if(StringUtils.isNotBlank(mapperPackageNameByRoot)) {
									dsConf.add(constructor.newInstance(mapperPackageNameByRoot, jdbc, helper, poolType));
								} else if(StringUtils.isNotBlank((mapperPackageNameByRoot = jdbc.getProperty(Constants.MAPPER_PACKAGE_NAME)))) {
									dsConf.add(constructor.newInstance(mapperPackageNameByRoot, jdbc, helper, poolType));
								} else 
									throw new DataSourceException("没有配置Mapper包路径，在jdbc属性文件中配置属性{ " + Constants.MAPPER_PACKAGE_NAME + " }或者在配置了属性{ " + Constants.MAPPER_PACKAGE_ROOT + " }的属性文件中配置属性{ " + Constants.MAPPER_PACKAGE_NAME + "." + root + " }，推荐配置在jdbc属性文件中。");
								
								LOG.info("创建数据源依赖注入模块, Mapper包路径: " + mapperPackageNameByRoot + ", 耗时: " + (System.currentTimeMillis() - time) + "ms");
							} else if(StringUtils.isNotBlank(jdbc.getProperty(JdbcConfig.JDBC_ENVIRONMENT_ID))) {
								String poolTypeAlias = jdbc.getProperty(Constants.JDBC_POOL_TYPE);
								PoolTypes poolType = null;
								if(StringUtils.isNotBlank(poolTypeAlias)) {
									poolType = (PoolTypes) PoolTypes.class.getField(poolTypeAlias).get(PoolTypes.class);
									if(poolType == null)
										throw new IllegalArgumentException("无效的Pool类型名称: " + poolTypeAlias);
									
									poolTypes.add(poolType);
								}
								
								JdbcConfig config;
								if(poolType != null) {
									switch(poolType) {
									case C3P0: 
										config = new C3P0JdbcConfig(jdbc);
										break;
									case DRUID: 
										config = new DruidJdbcConfig(jdbc);
										break;
										default: 
											throw new IllegalArgumentException("Can not support this poolType: " + poolType);
									}
								} else {
									config = new DruidJdbcConfig(jdbc);
								}
								configs.put(config.getEnvironmentId(), config);
							}
						} 
					}
				}
			}
			
			if(poolTypes.size() > 1) {
				throw new IllegalArgumentException("不支持多个连接池配置: " + JSON.toJSONString(poolTypes));
			}
			
			if(!dsConf.isEmpty()) {
				if(dsConf.size() == 1) {
					Object conf = dsConf.get(0);
					String mapperPackageName = (String) conf.getClass().getMethod("getMapperPackageName").invoke(conf);
					Properties jdbc = (Properties) conf.getClass().getMethod("getJdbc").invoke(conf);
					Object helper = conf.getClass().getMethod("getHelper").invoke(conf);
					Object poolType = conf.getClass().getMethod("getPoolType").invoke(conf);
					Class<?> dataSourceModule = Class.forName("org.nanoframework.orm.modules.DataSourceModule");
					Constructor<? extends Module> dsmConstructor = (Constructor<? extends Module>) dataSourceModule.getConstructor(String.class, Properties.class, jdbcHelper, PoolTypes.class);
					modules.add(dsmConstructor.newInstance(mapperPackageName, jdbc, helper, poolType));
				} else {
					boolean hasXmlDataSource = false;
					for(Object conf : dsConf) {
						/**  */
						String property = (String) dataSourceConfig.getField("PROPERTY").get(dataSourceConfig);
						String xml = (String) dataSourceConfig.getField("XML").get(dataSourceConfig);
						String type = (String) dataSourceConfig.getMethod("getType").invoke(conf);
						if(property.equals(type)) {
							Class<?> privateDataSourceModule = Class.forName("org.nanoframework.orm.modules.PrivateDataSourceModule");
							Constructor<? extends Module> pdsmConstructor = (Constructor<? extends Module>) privateDataSourceModule.getConstructor(String.class, Properties.class, jdbcHelper, PoolTypes.class);
							String mapperPackageName = (String) conf.getClass().getMethod("getMapperPackageName").invoke(conf);
							Properties jdbc = (Properties) conf.getClass().getMethod("getJdbc").invoke(conf);
							Object helper = conf.getClass().getMethod("getHelper").invoke(conf);
							Object poolType = conf.getClass().getMethod("getPoolType").invoke(conf);
							modules.add(pdsmConstructor.newInstance(mapperPackageName, jdbc, helper, poolType));
							LOG.warn("目前MyBatis-Guice针对多数据源时不能使用事务进行处理，开发过程中如果需要对多数据源进行事务处理，推荐使用Jdbc或者MyBatis的XML模式");
						} else if(xml.equals(type)) {
							Class<?> multiDataSourceModule = Class.forName("org.nanoframework.orm.mybatis.MultiDataSourceModule");
							Constructor<? extends Module> pdsmConstructor = (Constructor<? extends Module>) multiDataSourceModule.getConstructor(conf.getClass());
							modules.add(pdsmConstructor.newInstance(conf));
							hasXmlDataSource = true;
							
						} else 
							throw new IllegalArgumentException("未知的数据源类型{ " + envId + " }");
							
					}
					
					if(hasXmlDataSource) {
						Class<? extends Module> multiDataSourceModule = (Class<? extends Module>) Class.forName("org.nanoframework.orm.mybatis.MultiTransactionalModule");
						modules.add(multiDataSourceModule.newInstance());
					}
				}
			}
		} catch(Exception e) {
			if(e instanceof ClassNotFoundException) {
				for(Properties prop : PropertiesLoader.PROPERTIES.values()) {
					String jdbcURI = prop.getProperty(Constants.MAPPER_PACKAGE_JDBC);
					Properties jdbc = null;
					if(StringUtils.isNotBlank(jdbcURI)) {
						jdbc = PropertiesLoader.PROPERTIES.get(jdbcURI);
						if(jdbc == null) {
							jdbc = PropertiesLoader.load(DispatcherServlet.class.getResourceAsStream(jdbcURI));
							
							if(jdbc != null)
								newLoadProperties.put(jdbcURI, jdbc);
							else 
								throw new DataSourceException("数据源没有配置或配置错误: " + jdbcURI);
							
						}
					}
					
					if(jdbc != null && StringUtils.isNotBlank(jdbc.getProperty(JdbcConfig.JDBC_ENVIRONMENT_ID))) {
						String poolTypeAlias = jdbc.getProperty(Constants.JDBC_POOL_TYPE);
						PoolTypes poolType = null;
						if(StringUtils.isNotBlank(poolTypeAlias)) {
							poolType = (PoolTypes) PoolTypes.class.getField(poolTypeAlias).get(PoolTypes.class);
							if(poolType == null)
								throw new IllegalArgumentException("无效的Pool类型名称: " + poolTypeAlias);
							
							poolTypes.add(poolType);
						}
						
						JdbcConfig config;
						if(poolType != null) {
							switch(poolType) {
							case C3P0: 
								config = new C3P0JdbcConfig(jdbc);
								break;
							case DRUID: 
								config = new DruidJdbcConfig(jdbc);
								break;
								default: 
									throw new IllegalArgumentException("Can not support this poolType: " + poolType);
							}
						} else {
							config = new DruidJdbcConfig(jdbc);
						}
						configs.put(config.getEnvironmentId(), config);
					} else {
						LOG.warn("数据源没有配置或配置错误: " + jdbcURI);
					}
				}
			} else 
				throw new RuntimeException(e);
		}
		
		/** 读取方式三 */
		if(!configs.isEmpty()) {
			/** 默认的连接池采用DRUID */
			if(poolTypes.size() > 0)
				modules.add(new JdbcModule(configs, poolTypes.iterator().next()));
			else 
				modules.add(new JdbcModule(configs, PoolTypes.DRUID));
		}
		
		return newLoadProperties;
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
