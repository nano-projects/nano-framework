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
package org.nanoframework.web;

import java.io.IOException;
import java.util.Properties;

import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import junit.framework.TestCase;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:23:45
 */
public class PropertiesLoaderTest extends TestCase {

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoaderTest.class);

	public void test() throws LoaderException, IOException {
		Properties prop = PropertiesLoader.load(this.getClass().getResourceAsStream("/test-loader.properties"));
		
		assertNotNull("Prop is null.", prop);
		
		LOG.info("Prop size : " + prop.size());
		
	}

}
