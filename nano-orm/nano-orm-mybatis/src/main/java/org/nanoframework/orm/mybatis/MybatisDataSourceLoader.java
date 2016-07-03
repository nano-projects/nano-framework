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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.DataSourceLoader;
import org.nanoframework.orm.ORMType;

/**
 * @author yanghe
 * @date 2015年12月22日 上午10:13:35
 */
public class MybatisDataSourceLoader extends DataSourceLoader {
	private Logger LOG = LoggerFactory.getLogger(MybatisDataSourceLoader.class);
	
	private long time = System.currentTimeMillis();
	private List<DataSourceConfig> dsc = new ArrayList<>();
	
	public MybatisDataSourceLoader() {
		load();
		toModule();
	}
	
	@Override
	public void load() {
		load0(ORMType.MYBATIS);
	}
	
	@Override
	public void toConfig(Properties properties) {
		Assert.notNull(properties, "数据源属性文件不能为空");
		String[] mapperPackageName = properties.getProperty(MAPPER_PACKAGE_NAME, "NULL").split(",");
		DataSourceConfig config = new DataSourceConfig(mapperPackageName, properties, poolType(properties));
		dsc.add(config);
		LOG.info("创建数据源依赖注入模块, Mapper包路径: " + mapperPackageName + ", 耗时: " + (System.currentTimeMillis() - time) + "ms");
	}

	@Override
	public void toModule() {
		for(DataSourceConfig config : dsc) {
			MultiDataSourceModule module = new MultiDataSourceModule(config);
			modules.add(module);
		}
		
		modules.add(new MultiTransactionalModule());
	}

}
