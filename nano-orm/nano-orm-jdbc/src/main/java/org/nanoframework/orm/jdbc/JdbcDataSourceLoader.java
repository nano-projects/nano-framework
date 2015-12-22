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
package org.nanoframework.orm.jdbc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.DataSourceLoader;
import org.nanoframework.orm.ORMType;
import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.binding.JdbcModule;
import org.nanoframework.orm.jdbc.config.C3P0JdbcConfig;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;

/**
 * @author yanghe
 * @date 2015年12月22日 上午8:52:47
 */
public class JdbcDataSourceLoader extends DataSourceLoader {
	
	private Map<PoolType, Map<String, JdbcConfig>> configAggs = new HashMap<>();
	
	public JdbcDataSourceLoader() {
		load();
		toModule();
	}
	
	@Override
	public void load() {
		load0(ORMType.JDBC);
	}
	
	@Override
	public void toConfig(Properties properties) {
		Assert.notNull(properties, "数据源属性文件不能为空");
		PoolType poolType = poolType(properties);
		switch(poolType) {
			case C3P0: 
				Map<String, JdbcConfig> configs;
				if((configs = configAggs.get(poolType)) == null) {
					configs = new LinkedHashMap<>();
					JdbcConfig config = new C3P0JdbcConfig(properties);
					configs.put(config.getEnvironmentId(), config);
					configAggs.put(poolType, configs);
				} else {
					JdbcConfig config = new C3P0JdbcConfig(properties);
					configs.put(config.getEnvironmentId(), config);
				}
				
				break;
			case DRUID: 
				if((configs = configAggs.get(poolType)) == null) {
					configs = new LinkedHashMap<>();
					JdbcConfig config = new DruidJdbcConfig(properties);
					configs.put(config.getEnvironmentId(), config);
					configAggs.put(poolType, configs);
				} else {
					JdbcConfig config = new DruidJdbcConfig(properties);
					configs.put(config.getEnvironmentId(), config);
				}
				
				break;
		}
	}
	
	@Override
	public void toModule() {
		for(Entry<PoolType, Map<String, JdbcConfig>> item : configAggs.entrySet()) {
			modules.add(new JdbcModule(item.getValue(), item.getKey()));
		}
	}
}
