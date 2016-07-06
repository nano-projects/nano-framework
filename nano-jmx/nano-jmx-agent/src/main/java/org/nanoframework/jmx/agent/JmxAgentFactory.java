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
package org.nanoframework.jmx.agent;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.nanoframework.jmx.agent.exception.MBeanRegisterException;

/**
 * 
 * @author yanghe
 * @since 1.2
 */
public class JmxAgentFactory {
	private static MBeanServer mBeanServer = null;
	private static final Object LOCK = new Object();
	
	public static final synchronized ObjectInstance register(Object object, ObjectName name) {
		findMBeanServer();
		
		try {
			return mBeanServer.registerMBean(object, name);
		} catch(Throwable e) {
			throw new MBeanRegisterException(e.getMessage(), e);
		}
	}
	
	public static final synchronized ObjectInstance register(Object object, String name) {
		try {
			return register(object, new ObjectName(name));
		} catch(MalformedObjectNameException e) {
			throw new MBeanRegisterException(e.getMessage(), e);
		}
	}
	
	public static final MBeanServer findMBeanServer() {
		if(mBeanServer == null) {
			synchronized (LOCK) {
				if(mBeanServer == null) {
					List<MBeanServer> mbeanServers;
					if ((mbeanServers = MBeanServerFactory.findMBeanServer(null)).size() > 0) {
						mBeanServer = mbeanServers.get(0);
					} else {
						mBeanServer = ManagementFactory.getPlatformMBeanServer();
					}
				}
			}
		}
		
		return mBeanServer;
	}
}
