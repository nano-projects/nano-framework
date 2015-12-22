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
package org.nanoframework.orm.mybatis;

import java.util.Properties;

import org.mybatis.guice.datasource.helper.JdbcHelper;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.DataSourceLoader;
import org.nanoframework.orm.PoolType;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:12:26
 */
public class DataSourceConfig extends BaseEntity {
	private static final long serialVersionUID = -3733512377087727530L;
	
	public static final String DEFAULT_MYBATIS_CONFIG_PATH = "/mybatis-config-";
	public static final String XML_SUFFIX = ".xml";

	private String[] mapperPackageName;
	private Properties jdbc;
	private JdbcHelper helper;
	private String envId;
	private String mybatisConfigPath;
	private PoolType poolType;

	public DataSourceConfig(String[] mapperPackageName, Properties jdbc, PoolType poolType) {
		Assert.notNull(jdbc);
		Assert.hasLength(this.envId = jdbc.getProperty(DataSourceLoader.MYBATIS_ENVIRONMENT_ID));
		Assert.notNull(poolType);
		this.mapperPackageName = mapperPackageName;
		this.jdbc = jdbc;
		this.poolType = poolType;

		/** 现在使用poolType进行匹配MyBatis-config配置文件，而非MYBATIS_ENVIRONMENT_PATH属性 */
		this.mybatisConfigPath = DEFAULT_MYBATIS_CONFIG_PATH + poolType.name().toLowerCase() + XML_SUFFIX;
	}

	public String[] getMapperPackageName() {
		return mapperPackageName;
	}

	public void setMapperPackageName(String[] mapperPackageName) {
		this.mapperPackageName = mapperPackageName;
	}

	public Properties getJdbc() {
		return jdbc;
	}

	public void setJdbc(Properties jdbc) {
		this.jdbc = jdbc;
	}

	public JdbcHelper getHelper() {
		return helper;
	}

	public void setHelper(JdbcHelper helper) {
		this.helper = helper;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
	}

	public String getMybatisConfigPath() {
		return mybatisConfigPath;
	}

	public void setMybatisConfigPath(String mybatisConfigPath) {
		this.mybatisConfigPath = mybatisConfigPath;
	}

	public PoolType getPoolType() {
		return poolType;
	}

	public void setPoolType(PoolType poolType) {
		this.poolType = poolType;
	}

}
