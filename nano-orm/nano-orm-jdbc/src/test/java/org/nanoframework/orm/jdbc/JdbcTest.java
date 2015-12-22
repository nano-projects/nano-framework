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
package org.nanoframework.orm.jdbc;

import static org.nanoframework.orm.jdbc.JdbcAdapter.ADAPTER;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.config.C3P0JdbcConfig;
import org.nanoframework.orm.jdbc.config.DruidJdbcConfig;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.orm.jdbc.jstl.Result;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:14:31
 */
@JdbcCreater
public class JdbcTest {

	@Test
	public void test0() throws LoaderException, IOException, IllegalArgumentException, IllegalAccessException, PropertyVetoException, SQLException {
		List<JdbcConfig> configs = null;
		Properties prop = PropertiesLoader.load("/jdbc-test.properties");
		if(prop != null) {
			configs = new ArrayList<>();
			C3P0JdbcConfig config = new C3P0JdbcConfig(prop);
			configs.add(config);
		}
		
		JdbcAdapter.newInstance(configs, PoolType.C3P0, this.getClass());
		Connection conn = ADAPTER.getConnection("test");
		Result result = ADAPTER.executeQuery("select 1", conn);
		System.out.println(result.getRowCount());
		
		conn.close();
		ADAPTER.shutdown();
		
	}
	
	@Test
	public void test1() throws LoaderException, IOException, IllegalArgumentException, IllegalAccessException, PropertyVetoException, SQLException {
		List<JdbcConfig> configs = null;
		Properties prop = PropertiesLoader.load("/jdbc-test.properties");
		if(prop != null) {
			configs = new ArrayList<>();
			DruidJdbcConfig config = new DruidJdbcConfig(prop);
			configs.add(config);
		}
		
		JdbcAdapter.newInstance(configs, PoolType.DRUID, this.getClass());
		Connection conn = ADAPTER.getConnection("test");
		Result result = ADAPTER.executeQuery("select 1", conn);
		System.out.println(result.getRowCount());
		
		conn.close();
		ADAPTER.shutdown();
		
	}
}
