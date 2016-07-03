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
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.jmx.client.management.AbstractMXBean;
import org.nanoframework.jmx.client.management.ClassLoadingMXBean;
import org.nanoframework.jmx.client.management.CompilationMXBean;
import org.nanoframework.jmx.client.management.GarbageCollectorMXBean;
import org.nanoframework.jmx.client.management.MemoryMXBean;
import org.nanoframework.jmx.client.management.MemoryManagerMXBean;
import org.nanoframework.jmx.client.management.ObjectNames;
import org.nanoframework.jmx.client.management.OperatingSystemMXBean;
import org.nanoframework.jmx.client.management.RuntimeMXBean;
import org.nanoframework.jmx.client.management.impl.ClassLoadingImpl;
import org.nanoframework.jmx.client.management.impl.CompilationImpl;
import org.nanoframework.jmx.client.management.impl.GarbageCollectorImpl;
import org.nanoframework.jmx.client.management.impl.MemoryImpl;
import org.nanoframework.jmx.client.management.impl.MemoryManagerImpl;
import org.nanoframework.jmx.client.management.impl.OperatingSystemImpl;
import org.nanoframework.jmx.client.management.impl.RuntimeImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:07:55
 */
@Ignore
public class JmxClientTest {

	private Logger LOG = LoggerFactory.getLogger(JmxClientTest.class);
	JmxClient client;
	
	@Before
	public void init() {
		try {
			client = JmxClientManager.get("192.168.180.137:10180");
		} catch(Exception e) { }
	}
	
	@Test
	public void runttimeTest() throws IOException {
		try {
			RuntimeMXBean runtime = new RuntimeImpl(client);
			print(runtime);
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	@Test
	public void classLoadingTest() {
		try {
			ClassLoadingMXBean classLoading = new ClassLoadingImpl(client);
			print(classLoading);
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	@Test
	public void compilationTest() {
		try {
			CompilationMXBean compilation = new CompilationImpl(client);
			print(compilation);
		} catch(Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	@Test
	public void memoryTest() {
		try {
			MemoryMXBean memory = new MemoryImpl(client);
			print(memory);
			
			memory.gc();
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	@Test
	public void operatingSystemTest() {
		try {
			OperatingSystemMXBean operatingSystem = new OperatingSystemImpl(client);
			print(operatingSystem);
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	@Test
	public void memoryManagerTest() {
		try {
			MemoryManagerMXBean memoryManager = new MemoryManagerImpl(client, ObjectNames.MetaspaceManager);
			LOG.debug("Query Names: " + ((AbstractMXBean) memoryManager).queryNames());
			print(memoryManager);
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	@Test
	public void garbageCollectorTest() {
		try {
			GarbageCollectorMXBean garbageCollector = new GarbageCollectorImpl(client, ObjectNames.ConcurrentMarkSweep);
			LOG.debug("Query Names: " + ((AbstractMXBean) garbageCollector).queryNames());
			print(garbageCollector);
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	private void print(Object obj) {
		Map<String, Object> map = JSON.parseObject(JSON.toJSONString(obj), new TypeReference<Map<String, Object>>() { });
		LOG.debug("Class Type: " + obj.getClass().getName());
		map.forEach((key, value) -> LOG.debug(key + ": " + value));
		LOG.debug("");
	}
}
