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
package org.nanoframework.orm.jdbc;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.orm.jdbc.config.C3P0JdbcConfig;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:14:24
 */
public class DBConfigInitTest {

	@Test
	public void test() throws LoaderException, IOException, IllegalArgumentException, IllegalAccessException {
		Properties prop = PropertiesLoader.load("classpath:jdbc-test.properties");
		if(prop != null) {
			C3P0JdbcConfig config = new C3P0JdbcConfig(prop);
			System.out.println(config._getBeanToMap());
			
		}
	}
}
