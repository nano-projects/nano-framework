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

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.c3p0.C3p0DataSourceProvider;
import org.mybatis.guice.datasource.helper.JdbcHelper;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.orm.PoolTypes;
import org.nanoframework.orm.modules.provider.DruidDataSourceProvider;

import com.google.common.base.Preconditions;
import com.google.inject.name.Names;

/**
 * MySQL数据源及配置加载
 * 
 * @author yanghe
 * @date 2015年6月7日 下午5:10:29 
 *
 */
public final class DataSourceModule extends MyBatisModule {

	private String packageName;
	private Properties jdbc;
	private JdbcHelper helper = null;
	private boolean multible = false;
	private Set<Class<?>> classes;
	private PoolTypes poolType;
	
	public DataSourceModule(String mapperPackageName) {
		this.packageName = mapperPackageName;
		initJdbcProperties();
		
	}
	
	public DataSourceModule(String mapperPackageName, Properties jdbc) {
		this.packageName = mapperPackageName;
		this.jdbc = jdbc;
		initJdbcProperties();
		
	}
	
	public DataSourceModule(String mapperPackageName, Properties jdbc, JdbcHelper helper) {
		this.packageName = mapperPackageName;
		this.jdbc = jdbc;
		this.helper = helper;
		initJdbcProperties();
	}
	
	public DataSourceModule(String mapperPackageName, Properties jdbc, JdbcHelper helper, PoolTypes poolType) {
		this.packageName = mapperPackageName;
		this.jdbc = jdbc;
		this.helper = helper;
		this.poolType = poolType;
		initJdbcProperties();
	}
	
	public DataSourceModule(String mapperPackageName, Properties jdbc, JdbcHelper helper, boolean multible) {
		this.packageName = mapperPackageName;
		this.jdbc = jdbc;
		this.helper = helper;
		this.multible = multible;
		initJdbcProperties();
	}
	
	public DataSourceModule(String mapperPackageName, Properties jdbc, JdbcHelper helper, boolean multible, PoolTypes poolType) {
		this.packageName = mapperPackageName;
		this.jdbc = jdbc;
		this.helper = helper;
		this.multible = multible;
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
	protected void initialize() {
		if(helper != null)
			install(helper);
		
		if(poolType != null) {
			switch(poolType) {
				case C3P0: 
					bindDataSourceProviderType(C3p0DataSourceProvider.class); // c3p0连接池
					break;
				case DRUID: 
					bindDataSourceProviderType(DruidDataSourceProvider.class);
					break;
				default: 
					throw new IllegalArgumentException("Can not support this poolType: " + poolType);
			}
		} else {
			/** Druid is default dataSource provider */
			bindDataSourceProviderType(DruidDataSourceProvider.class);
		}
		
		bindTransactionFactoryType(JdbcTransactionFactory.class);
		
		if(classes != null && !classes.isEmpty())
			classes.clear();
		
		addMapperClasses(classes = getClasses(packageName));

		if(!multible) {
			Names.bindProperties(binder(), jdbc);
		}
		
	}
	
	public Set<Class<?>> getClasses() {
		return classes;
	}
	
	/**
     * Return a set of all classes contained in the given package.
     *
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(String packageName) {
        return getClasses(new ResolverUtil.IsA(Object.class), packageName);
    }

    /**
     * Return a set of all classes contained in the given package that match with
     * the given test requirement.
     *
     * @param test the class filter on the given package.
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(ResolverUtil.Test test, String packageName) {
    	Preconditions.checkArgument(test != null, "Parameter 'test' must not be null");
    	Preconditions.checkArgument(packageName != null, "Parameter 'packageName' must not be null");
        return new ResolverUtil<Object>().find(test, packageName).getClasses();
    }

}
