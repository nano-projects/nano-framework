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
package org.nanoframework.jmx.client;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:07:48
 */
@Ignore
public class JmxAgentClientTest {
	private Logger LOG = LoggerFactory.getLogger(JmxAgentClientTest.class);
	
	@Test
	public void test0() throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		try {
			JmxClient client = JmxClientManager.get("localhost:10180");
			ObjectName objectName = new ObjectName("org.nanoframework:type=HelloWorld");
			System.out.println(client.getConnection().getAttribute(objectName, "Name"));
			
			JmxClient client2 = JmxClientManager.get("localhost:10180", "jmxrmi2");
			ObjectName objectName2 = new ObjectName("org.nanoframework:type=HelloWorld2");
			System.out.println(client2.getConnection().getAttribute(objectName2, "Name"));
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
