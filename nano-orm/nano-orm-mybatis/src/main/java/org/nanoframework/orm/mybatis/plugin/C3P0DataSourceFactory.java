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
package org.nanoframework.orm.mybatis.plugin;

import javax.sql.DataSource;

/**
 * C3P0连接池工厂类
 * 
 * @author yanghe
 * @date 2015年7月29日 下午2:45:06 
 *
 */
public class C3P0DataSourceFactory extends AbstractDataSourceFactory {

	public C3P0DataSourceFactory() {
		try {
			Class<?> ComboPooledDataSource = Class.forName("com.mchange.v2.c3p0.ComboPooledDataSource");
			this.dataSource = (DataSource) ComboPooledDataSource.newInstance();
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) { }
	}
	
}
