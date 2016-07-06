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
package org.nanoframework.orm.mybatis.plugin;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * @author yanghe
 * @since 1.2
 */
public class AbstractDataSourceFactory implements DataSourceFactory {
	protected DataSource dataSource;
	
	@Override
	public void setProperties(Properties properties) {
		Properties driverProperties = new Properties();
		MetaObject metaDataSource = SystemMetaObject.forObject(dataSource);
		for (Object key : properties.keySet()) {
			String propertyName = (String) key;
			if (metaDataSource.hasSetter(propertyName)) {
				String value = (String) properties.get(propertyName);
				/**　对没有设置值的属性跳过设置 */
				if(StringUtils.isNotEmpty(value) && value.startsWith("${") && value.endsWith("}")) {
					continue ;
				}
				
				Object convertedValue = convertValue(metaDataSource, propertyName, value);
				metaDataSource.setValue(propertyName, convertedValue);
			} else {
				throw new DataSourceException("Unknown DataSource property: " + propertyName);
			}
		}
		
		if (driverProperties.size() > 0) {
			metaDataSource.setValue("driverProperties", driverProperties);
		}
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	private Object convertValue(MetaObject metaDataSource, String propertyName, String value) {
		Object convertedValue = value;
		Class<?> targetType = metaDataSource.getSetterType(propertyName);
		if (targetType == Integer.class || targetType == int.class) {
			convertedValue = Integer.valueOf(value);
		} else if (targetType == Long.class || targetType == long.class) {
			convertedValue = Long.valueOf(value);
		} else if (targetType == Boolean.class || targetType == boolean.class) {
			convertedValue = Boolean.valueOf(value);
		}
		return convertedValue;
	}

}
