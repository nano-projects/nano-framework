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
package org.nanoframework.jmx.agent;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.nanoframework.jmx.agent.mbean.HelloWorld;
import org.nanoframework.jmx.agent.mbean.HelloWorld2;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:05:43
 */
public class JmxAgentTest {

	public static void main(String... args) throws IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, InterruptedException {
		LocateRegistry.createRegistry(10180);
		
		final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:10180/jmxrmi");
		MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer(JmxAgentTest.class.getName());
		JMXConnectorServer jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbeanServer);
		mbeanServer.registerMBean(new HelloWorld(), new ObjectName("org.nanoframework:type=HelloWorld"));
		jmxConnectorServer.start();
		
		final JMXServiceURL url2 = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:10180/jmxrmi2");
		MBeanServer mbeanServer2 = MBeanServerFactory.createMBeanServer(JmxAgentTest.class.getName());
		JMXConnectorServer jmxConnectorServer2 = JMXConnectorServerFactory.newJMXConnectorServer(url2, null, mbeanServer2);
		mbeanServer2.registerMBean(new HelloWorld2(), new ObjectName("org.nanoframework:type=HelloWorld2"));
		jmxConnectorServer2.start();
		
		Thread.sleep(120000L);
	}
}
