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

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.jmx.client.management.impl.ThreadImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:08:02
 */
@Ignore
public class ThreadingTest {

	private Logger LOG = LoggerFactory.getLogger(ThreadingTest.class);
	JmxClient client;
	
	@Before
	public void init() {
		try {
			client = JmxClientManager.get("192.168.180.137:10180");
		} catch(Exception e) { }
	}
	
	@Test
	public void threadingTest() {
		
		try {
			ThreadMXBean thread = new ThreadImpl(client);
			print(thread);
			long[] ids = thread.getAllThreadIds();
			for(long id : ids) {
				LOG.debug("Thread info: " + thread.getThreadInfo(id));
				LOG.debug("Thraed cpu time: " + thread.getThreadCpuTime(id));
				LOG.debug("Thraed user time: " + thread.getThreadUserTime(id));
			}
			
			LOG.debug("All thread info: ");
			ThreadInfo[] threadInfos = thread.getThreadInfo(ids);
			for(ThreadInfo threadInfo : threadInfos) {
				LOG.debug("Thread info: " + threadInfo);
			}
			
			LOG.debug("All thread info on lock: ");
			threadInfos = thread.getThreadInfo(ids, true, true);
			for(ThreadInfo threadInfo : threadInfos) {
				LOG.debug("Thread info: " + threadInfo);
			}
			
			LOG.debug("Dump all threads: ");
			threadInfos = thread.dumpAllThreads(false, false);
			for(ThreadInfo threadInfo : threadInfos) {
				LOG.debug("Thread info: " + threadInfo);
			}
			
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
