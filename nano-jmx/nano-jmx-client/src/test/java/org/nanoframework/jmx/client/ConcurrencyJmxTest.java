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

import java.rmi.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nanoframework.jmx.client.management.OperatingSystemMXBean;
import org.nanoframework.jmx.client.management.impl.OperatingSystemImpl;

/**
 * @author yanghe
 * @date 2015年12月2日 上午10:30:19
 */
public class ConcurrencyJmxTest {
	private static ExecutorService service = Executors.newCachedThreadPool();
	
	public static void main(String[] args) {
		for(int num = 0; num < 10; num ++) {
			service.execute(() -> {
				JmxClient client = null;
				for(;;) {
					try {
						client = JmxClientManager.get("192.168.180.137:10180", 3000);
						OperatingSystemMXBean operatingSystem = new OperatingSystemImpl(client);
				        System.out.println("CPU Ratio: " + operatingSystem.cpuRatio() + "%");
				        
					} catch(Exception e) {
						System.out.println(e.getMessage());
						if(e.getCause() != null && e.getCause() instanceof ConnectException) {
							try { client.reconnect(); } catch(ConnectException ex) { System.out.println(e.getMessage()); }
						}
						
						try { Thread.sleep(1000L); } catch(InterruptedException ex) { }
					}
				}
			});
		}
	}
	
	
}
