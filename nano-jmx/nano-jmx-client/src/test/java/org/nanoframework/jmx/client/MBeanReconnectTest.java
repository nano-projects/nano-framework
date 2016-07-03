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
import java.lang.management.RuntimeMXBean;

import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.jmx.client.management.AbstractMXBean;
import org.nanoframework.jmx.client.management.impl.RuntimeImpl;

/**
 * @author yanghe
 * @date 2015年8月19日 下午2:09:38
 */
@Ignore
public class MBeanReconnectTest {
	private Logger LOG = LoggerFactory.getLogger(MBeanReconnectTest.class);
	
	@Test
	public void test0() throws IOException {
		try {
			JmxClient client = JmxClientManager.get("192.168.180.137:10180");
			RuntimeMXBean runtime = new RuntimeImpl(client);
			LOG.debug("runtime.Name: " + runtime.getName());
			
			((AbstractMXBean) runtime).close();
			
			try {
				LOG.debug("runtime.Name: " + runtime.getName());
			} catch(Exception e) {
				LOG.error(e.getMessage());
			}
			
			((AbstractMXBean) runtime).connect();
			
			try {
				LOG.debug("runtime.Name: " + runtime.getName());
			} catch(Exception e) {
				LOG.error(e.getMessage());
			}
			
			((AbstractMXBean) runtime).close();
		} catch(Exception e) { }
	}
	
	@Test
	public void test1() throws IOException, InterruptedException {
		try {
			JmxClient client = JmxClientManager.get("192.168.180.137:10180");
			RuntimeMXBean runtime = new RuntimeImpl(client);
			LOG.debug("runtime.Name: " + runtime.getName());
			try {
				LOG.debug("runtime.Name: " + runtime.getName());
			} catch(Exception e) {
				LOG.error(e.getMessage());
				LOG.debug("Reconnect to JMX: ");
				((AbstractMXBean) runtime).reConnect();
			}
			
			LOG.debug("runtime.Name: " + runtime.getName());
			
			((AbstractMXBean) runtime).close();
		} catch(Exception e) { }
	}
}
