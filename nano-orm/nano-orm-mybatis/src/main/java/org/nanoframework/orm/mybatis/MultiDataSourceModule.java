/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.orm.mybatis;

import static com.google.inject.util.Providers.guicify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.inject.Provider;
import javax.sql.DataSource;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.nanoframework.commons.io.ClassPathResource;
import org.nanoframework.commons.io.Resource;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ResourceUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * MyBatis XML模式读取数据源，并通过jdbc属性文件进行配置<br>
 * 将创建的SqlSessionFactory写入至全局管理类
 * 
 * @author yanghe
 * @since 1.2
 */
public class MultiDataSourceModule extends AbstractModule {

	private String envId;
	private Properties jdbc;
	private String mybatisConfigPath;
	private String[] mapperPackageName;
	
	public MultiDataSourceModule(DataSourceConfig conf) {
		Assert.notNull(this.jdbc = conf.getJdbc());
		Assert.hasLength(this.envId = conf.getEnvId());
		Assert.hasLength(this.mybatisConfigPath = conf.getMybatisConfigPath());
		Assert.notEmpty(this.mapperPackageName = conf.getMapperPackageName());
	}
	
	@Override
	protected void configure() {
		Reader reader = null;
		try {
			InputStream input;
			try {
				Resource resource = new ClassPathResource(mybatisConfigPath);
				input = resource.getInputStream();
				if(input == null) 
					input = new FileInputStream(ResourceUtils.getFile(mybatisConfigPath));
				
			} catch(IOException e) {
				throw new LoaderException("加载文件异常: " + e.getMessage());
			}

			reader = new InputStreamReader(input);
			SqlSessionFactory sessionFactory;
	        SqlSessionManager sessionManager = SqlSessionManager.newInstance(sessionFactory = new SqlSessionFactoryBuilder().build(reader, envId, jdbc));
	        GlobalSqlSession.set(envId, sessionManager);
	        
	        Configuration configuration = sessionFactory.getConfiguration();
	        
	        MapperRegistry registry = configuration.getMapperRegistry();
	        for(String pkg : mapperPackageName) {
	        	Set<Class<?>> classes = getClasses(pkg);
	        	if(!CollectionUtils.isEmpty(classes)) {
	        		for(Class<?> cls : classes) {
	        			if(!registry.hasMapper(cls))
	        				registry.addMapper(cls);
	        		}
	        	}
	        }
	        
	        // bind mappers
            Collection<Class<?>> mapperClasses = registry.getMappers();
            for (Class<?> mapperType : mapperClasses) {
                bindMapper(mapperType, sessionManager);
            }
		} finally {
			if(reader != null) {
				try { reader.close(); } catch (IOException e) {  }
			}
		}
	}
	
	/**
     * Set the DataSource Provider type has to be bound.
     *
     * @param dataSourceProviderType the DataSource Provider type
     */
    protected final void bindDataSourceProviderType(Class<? extends Provider<DataSource>> dataSourceProviderType) {
        Assert.notNull(dataSourceProviderType, "Parameter 'dataSourceProviderType' must be not null");
        bind(DataSource.class).toProvider(dataSourceProviderType).in(Scopes.SINGLETON);
    }
	
    /**
    *
    * @param <T>
    * @param mapperType
    */
   final <T> void bindMapper(Class<T> mapperType, SqlSessionManager sessionManager) {
       bind(mapperType).toProvider(guicify(new MapperProvider<T>(mapperType, sessionManager))).in(Scopes.SINGLETON);
   }
   
   /**
    * Return a set of all classes contained in the given package.
    *
    * @param packageName the package has to be analyzed.
    * @return a set of all classes contained in the given package.
    */
   final Set<Class<?>> getClasses(String packageName) {
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
   final Set<Class<?>> getClasses(ResolverUtil.Test test, String packageName) {
       Assert.notNull(test, "Parameter 'test' must not be null");
       Assert.notNull(packageName, "Parameter 'packageName' must not be null");
       return new ResolverUtil<Object>().find(test, packageName).getClasses();
   }
	
}
