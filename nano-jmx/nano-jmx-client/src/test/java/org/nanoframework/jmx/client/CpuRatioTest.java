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

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.jmx.client.management.impl.OperatingSystemImpl;

/**
 * @author yanghe
 * @date 2015年9月2日 下午5:35:54
 */
@Ignore
public class CpuRatioTest {
	private Logger LOG = LoggerFactory.getLogger(CpuRatioTest.class);
	JmxClient client;
	
	
	@Before
	public void init() {
		try {
			client = JmxClientManager.get("192.168.180.137:10180");
		} catch(Exception e) { }
	}
	
	@SuppressWarnings("restriction")
	@Test
	public void test0() {
		try {
			com.sun.management.OperatingSystemMXBean operatingSystemMXBean = new OperatingSystemImpl(client);
			int count = 5;
			for(;count > 0; count --) {
				Long start = System.currentTimeMillis();  
		        long startT = operatingSystemMXBean.getProcessCpuTime();  
		        /**    Collect data every 1 seconds      */  
		        try {  
		            TimeUnit.SECONDS.sleep(1);  
		        } catch (InterruptedException e) {  
		            LOG.error("InterruptedException occurred while MemoryCollector sleeping...");  
		        }  
		        
		        Long end = System.currentTimeMillis();  
		        long endT = operatingSystemMXBean.getProcessCpuTime();  
		        double ratio = (endT-startT)/1000000.0/(end-start)/operatingSystemMXBean.getAvailableProcessors();
		        LOG.debug("CPU Ratio: " + (ratio * 100) + "%");
			}
		} catch(Exception e) { }
	}
}
