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
package org.nanoframework.orm;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.orm.jdbc.DataSourceException;
import org.nanoframework.orm.jdbc.binding.JdbcModule;
import org.nanoframework.orm.jdbc.config.C3P0JdbcConfig;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;

import com.alibaba.fastjson.JSON;
import com.google.inject.Module;

/**
 * @author yanghe
 * @date 2015年10月13日 下午2:06:58
 */
@SuppressWarnings("unchecked")
public class DataSourceLoader {
	private Logger LOG = LoggerFactory.getLogger(DataSourceLoader.class);
	
	private List<Module> modules = new ArrayList<>();
	private long time;
	private Map<String, Properties> newLoadProperties = new HashMap<>();
	private Map<String, JdbcConfig> configs = new HashMap<>();
	private List<Object> dsConf = new ArrayList<>();
	private Set<PoolTypes> poolTypes = new HashSet<>();
	
	private static Class<?> DATASOURCE_CONFIG;
	private static Class<?> MULTI_DATASOURCE_MODULE;
	private static Class<? extends Module> MULTI_TRANSACTIONAL_MODULE;
	private static boolean hasMybatisPlugin = false;
	private final String MYBATIS_ENVIRONMENT_ID = "MYBATIS_ENVIRONMENT_ID";
	
	static {
		try {
			DATASOURCE_CONFIG = Class.forName("org.nanoframework.orm.mybatis.DataSourceConfig");
			MULTI_DATASOURCE_MODULE = Class.forName("org.nanoframework.orm.mybatis.MultiDataSourceModule");
			MULTI_TRANSACTIONAL_MODULE = (Class<? extends Module>) Class.forName("org.nanoframework.orm.mybatis.MultiTransactionalModule");
			hasMybatisPlugin = true;
		} catch(Exception e) { }
	}
	
	public DataSourceLoader() {
		this.time = System.currentTimeMillis();
		
		try {
			if(hasMybatisPlugin) {
				String envId = (String) DATASOURCE_CONFIG.getField(MYBATIS_ENVIRONMENT_ID).get(DATASOURCE_CONFIG);
				for(Properties prop : PropertiesLoader.PROPERTIES.values()) {
					String mapperPackageName;
					String mapperPackageRoot;
					if(StringUtils.isNotBlank(mapperPackageName = prop.getProperty(Constants.MAPPER_PACKAGE_NAME)) || StringUtils.isNotBlank(prop.getProperty(Constants.MAPPER_PACKAGE_JDBC))) {
						readJdbcPlan1(prop, envId, mapperPackageName);
					} else if(StringUtils.isNotBlank(mapperPackageRoot = prop.getProperty(Constants.MAPPER_PACKAGE_ROOT))) {
						readJdbcPlan2(prop, envId, mapperPackageRoot);
					}
				}
				
				if(poolTypes.size() > 1) {
					throw new IllegalArgumentException("不支持多个连接池配置: " + JSON.toJSONString(poolTypes));
				}
				
				addMybatisModules(envId);
			} else {
				loadJdbcConfig();
			}
			
			/** 读取方式三 */
			if(!configs.isEmpty()) {
				/** 默认的连接池采用DRUID */
				if(poolTypes.size() > 0)
					this.modules.add(new JdbcModule(configs, poolTypes.iterator().next()));
				else 
					this.modules.add(new JdbcModule(configs, PoolTypes.DRUID));
			}
		} catch(Throwable e) {
			throw new DataSourceLoaderException(e.getMessage(), e);
		}
	}
	
	private void readJdbcPlan1(Properties prop, String envId, String mapperPackageName) throws Throwable {
		/** 读取方式一 */
		Properties jdbc = findJdbcProperties(prop.getProperty(Constants.MAPPER_PACKAGE_JDBC));
		if(StringUtils.isNotBlank(jdbc.getProperty(envId))) {
			PoolTypes poolType = setPoolTypes(jdbc.getProperty(Constants.JDBC_POOL_TYPE));
			addDataSourceConfig(mapperPackageName, jdbc, poolType);
		} else {
			addJdbcConfig(jdbc);
		}
	}
	
	private void readJdbcPlan2(Properties prop, String envId, String mapperPackageRoot) throws Throwable {
		/** 读取方式二 */
		String[] roots = mapperPackageRoot.split(",");
		for(String root : roots) {
			String mapperPackageNameByRoot;
			if(StringUtils.isNotBlank(mapperPackageNameByRoot = prop.getProperty(Constants.MAPPER_PACKAGE_NAME + "." + root)) || 
					StringUtils.isNotBlank(prop.getProperty(Constants.MAPPER_PACKAGE_JDBC + "." + root))) {
				
				Properties jdbc = findJdbcProperties(prop.getProperty(Constants.MAPPER_PACKAGE_JDBC + "." + root));
				if(StringUtils.isNotBlank(jdbc.getProperty(envId))) {
					PoolTypes poolType = setPoolTypes(jdbc.getProperty(Constants.JDBC_POOL_TYPE));
					addDataSourceConfig(mapperPackageNameByRoot, jdbc, poolType);
				} else{
					addJdbcConfig(jdbc);
				}
			} 
		}
	}

	private PoolTypes setPoolTypes(String poolTypeAlias) throws Throwable {
		PoolTypes poolType = null;
		if(StringUtils.isNotBlank(poolTypeAlias)) {
			poolType = (PoolTypes) PoolTypes.class.getField(poolTypeAlias).get(PoolTypes.class);
			if(poolType == null)
				throw new IllegalArgumentException("无效的Pool类型名称: " + poolTypeAlias);
			
			poolTypes.add(poolType);
		}
		
		return poolType;
	}
	
	private void addDataSourceConfig(String mapperPackageName, Properties jdbc, PoolTypes poolType) throws Throwable {
		Constructor<?> constructor = DATASOURCE_CONFIG.getConstructor(String.class, Properties.class, PoolTypes.class);
		if(StringUtils.isNotBlank(mapperPackageName)) {
			dsConf.add(constructor.newInstance(mapperPackageName, jdbc, poolType));
		} else if(StringUtils.isNotBlank((mapperPackageName = jdbc.getProperty(Constants.MAPPER_PACKAGE_NAME)))) {
			dsConf.add(constructor.newInstance(mapperPackageName, jdbc, poolType));
		} else 
			throw new DataSourceException("没有配置Mapper包路径");
		
		LOG.info("创建数据源依赖注入模块, Mapper包路径: " + mapperPackageName + ", 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	private void addMybatisModules(String envId) throws Throwable {
		if(!dsConf.isEmpty()) {
			for(Object conf : dsConf) {
				Constructor<? extends Module> pdsmConstructor = (Constructor<? extends Module>) MULTI_DATASOURCE_MODULE.getConstructor(conf.getClass());
				modules.add(pdsmConstructor.newInstance(conf));
			}
			
			modules.add(MULTI_TRANSACTIONAL_MODULE.newInstance());
		}
	}
	
	private void loadJdbcConfig() throws Throwable {
		for(Properties prop : PropertiesLoader.PROPERTIES.values()) {
			String jdbcURI = prop.getProperty(Constants.MAPPER_PACKAGE_JDBC);
			String mapperPackageRoot;
			if(StringUtils.isNotBlank(jdbcURI)) {
				addJdbcConfig(findJdbcProperties(jdbcURI));
			} else if(StringUtils.isNotBlank(mapperPackageRoot = prop.getProperty(Constants.MAPPER_PACKAGE_ROOT))) {
				String[] roots = mapperPackageRoot.split(",");
				for(String root : roots) {
					addJdbcConfig(findJdbcProperties(prop.getProperty(Constants.MAPPER_PACKAGE_JDBC + "." + root)));
				}
			}
			
		}
	}
	
	private Properties findJdbcProperties(String jdbcURI) throws LoaderException, IOException {
		if(StringUtils.isNotBlank(jdbcURI)) {
			Properties jdbc = PropertiesLoader.PROPERTIES.get(jdbcURI);
			if(jdbc == null) {
				jdbc = PropertiesLoader.load(DataSourceLoader.class.getResourceAsStream(jdbcURI));
				if(jdbc != null)
					newLoadProperties.put(jdbcURI, jdbc);
				else 
					throw new DataSourceException("数据源没有配置或配置错误: " + jdbcURI);
			}
			
			return jdbc;
		}
		
		return null;
	}
	
	private void addJdbcConfig(Properties jdbc) throws Throwable {
		if(StringUtils.isNotBlank(jdbc.getProperty(JdbcConfig.JDBC_ENVIRONMENT_ID))) {
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
	
	public List<Module> getModules() {
		return modules;
	}
	
	public Map<String, Properties> getLoadProperties() {
		return newLoadProperties;
	}
}
