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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.jdbc.DataSourceException;

import com.google.inject.Module;

/**
 * @author yanghe
 * @date 2015年12月22日 上午9:16:30
 */
public abstract class DataSourceLoader {
	private Logger LOG = LoggerFactory.getLogger(DataSourceLoader.class);
	
	public static final String JDBC_ENVIRONMENT_ID = "JDBC.environment.id";
	public static final String MYBATIS_ENVIRONMENT_ID = "mybatis.environment.id";
	
	public static final String MAPPER_PACKAGE_ROOT = "mapper.package.root";
	public static final String MAPPER_PACKAGE_NAME = "mapper.package.name";
	public static final String MAPPER_PACKAGE_JDBC = "mapper.package.jdbc";
	public static final String MAPPER_PACKAGE_HELPER = "mapper.package.helper";
	public static final String JDBC_POOL_TYPE = "JDBC.pool.type";
	
	protected List<Module> modules = new ArrayList<>();
	protected Map<String, Properties> newLoadProperties = new HashMap<>();
	
	public abstract void load();
	public abstract void toConfig(Properties properties);
	public abstract void toModule();
	
	protected void load0(ORMType ormType) {
		for(Properties properties : PropertiesLoader.PROPERTIES.values()) {
			Properties source = getProperties(properties.getProperty(MAPPER_PACKAGE_JDBC), ormType);
			if(source != null) {
				toConfig(source);
			}
			
			String mapperPackageRoot;
			if(StringUtils.isNotBlank(mapperPackageRoot = properties.getProperty(MAPPER_PACKAGE_ROOT))) {
				String[] roots = mapperPackageRoot.split(",");
				for(String root : roots) {
					Properties _source = getProperties(properties.getProperty(MAPPER_PACKAGE_JDBC + "." + root), ormType);
					if(_source != null) {
						toConfig(_source);
					}
				}
			}
		}
	}
	
	protected ORMType ormType(Properties properties) {
		Assert.notNull(properties, "数据源属性文件不能为空");
		
		ORMType type = null;
		if(StringUtils.isNotBlank(properties.getProperty(JDBC_ENVIRONMENT_ID))) 
			type = ORMType.JDBC;
		
		if(StringUtils.isNotBlank(properties.getProperty(MYBATIS_ENVIRONMENT_ID))) 
			type = ORMType.MYBATIS;
		
		return type;
	}
	
	protected PoolType poolType(Properties properties) {
		PoolType poolType;
		try {
			poolType = PoolType.valueOf(properties.getProperty(JDBC_POOL_TYPE));
		} catch(Exception e) {
			LOG.warn("未设置有效的JDBC.pool.type, 现在使用默认的连接池: DRUID");
			poolType = PoolType.DRUID;
		}
		
		return poolType;
	}
	
	protected Properties getProperties(String path, ORMType ormType) {
		if(StringUtils.isNotBlank(path)) {
			Properties properties = PropertiesLoader.PROPERTIES.get(path);
			if(properties == null) {
				properties = PropertiesLoader.load(path);
				if(properties != null) 
					newLoadProperties.put(path, properties);
				else 
					throw new DataSourceException("数据源没有配置或配置错误: " + path);
				
			}
			
			ORMType type = ormType(properties);
			if(type == null)
				throw new DataSourceException("未知的数据源类型配置: " + path);
			
			if(type == ormType) 
				return properties;
			
		}
		
		return null;
	}
	
	public Map<String, Properties> getLoadProperties() {
		return newLoadProperties;
	}
	
	public List<Module> getModules() {
		return modules;
	}
}
