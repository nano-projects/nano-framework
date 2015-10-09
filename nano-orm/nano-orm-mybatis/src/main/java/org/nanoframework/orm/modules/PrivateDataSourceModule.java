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
package org.nanoframework.orm.modules;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.mybatis.guice.datasource.helper.JdbcHelper;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.PoolTypes;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:12:03
 */
public class PrivateDataSourceModule extends PrivateModule {

	private String mapperPackageName;
	private Properties jdbc;
	private JdbcHelper helper = JdbcHelper.MySQL;
	private PoolTypes poolType = PoolTypes.DRUID;
	
	private Logger LOG = LoggerFactory.getLogger(PrivateDataSourceModule.class);
	
	public PrivateDataSourceModule(String mapperPackageName) {
		this.mapperPackageName = mapperPackageName;
		initJdbcProperties();
		
	}
	
	public PrivateDataSourceModule(String mapperPackageName, Properties jdbc) {
		this.mapperPackageName = mapperPackageName;
		this.jdbc = jdbc;
		initJdbcProperties();
		
	}
	
	public PrivateDataSourceModule(String mapperPackageName, Properties jdbc, JdbcHelper helper) {
		this.mapperPackageName = mapperPackageName;
		this.jdbc = jdbc;
		this.helper = helper;
		initJdbcProperties();
		
	}
	
	public PrivateDataSourceModule(String mapperPackageName, Properties jdbc, JdbcHelper helper, PoolTypes poolType) {
		this.mapperPackageName = mapperPackageName;
		this.jdbc = jdbc;
		this.helper = helper;
		this.poolType = poolType;
		initJdbcProperties();
	}
	
	/**
	 * 如果没有外部数据源配置则加载此包内的默认数据源配置文件
	 * 
	 * @throws LoaderException
	 * @throws IOException
	 */
	private void initJdbcProperties() {
		if(this.jdbc == null) {
			try {
				this.jdbc = PropertiesLoader.load(this.getClass().getResourceAsStream("/jdbc.properties"));
			} catch(LoaderException | IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
	
	@Override
	protected void configure() {
		DataSourceModule module;
		install(module = new DataSourceModule(mapperPackageName, jdbc, helper, true, poolType));
		
		Names.bindProperties(binder(), jdbc);
		Set<Class<?>> mapperClasses = module.getClasses();
		if(mapperClasses != null && mapperClasses.size() > 0) {
			mapperClasses.forEach(clz -> { 
				expose(clz);
				if(LOG.isDebugEnabled())
					LOG.debug("Expose Mapper Class: " + clz.getName());
				
			});
		}
		
	}
	
}
