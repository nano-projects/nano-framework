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
package org.nanoframework.ssh.exec;

import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.ssh.FindProcessExecutor;
import org.nanoframework.extension.ssh.GanymedSSH;
import org.nanoframework.extension.ssh.ShellExecutor;
import org.nanoframework.jmx.client.JmxClientManager;
import org.nanoframework.jmx.client.management.impl.RuntimeImpl;

/**
 * 
 * @author yanghe
 * @date 2015年8月21日 上午11:01:35
 * @since 1.1
 */
public class SSHTest {

	private Logger LOG = LoggerFactory.getLogger(SSHTest.class);
	
	private String host = "192.168.180.137";
	private String username = "yanghe";
	private String passwd = "MzZGRTBFQTFFNDVGNjZENDk3QjVCNzkyMDIwNDlFQ0Q1";
	private int port = 10180;
	private GanymedSSH danymedSSH;
	private Socket socket;
	private ShellExecutor ssh;
	private FindProcessExecutor callSsh;
	
	@Ignore
	@Test
	public void test0() throws InterruptedException, MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
		try {
			danymedSSH = GanymedSSH.newInstance(host, username, passwd);
			ssh = ShellExecutor.newInstance(danymedSSH);
			callSsh = FindProcessExecutor.newInstance(danymedSSH);
			
			ssh.execute("/etc/profile", "/home/yanghe/test/nano-manager-quartz-server-1.0.0-SNAPSHOT/bin/startup.sh");
			
			String pid;
			for(;;) {
				try {
					socket = new Socket(host, port);
					boolean run = socket.isConnected();
					if(run) {
						socket.close();
						LOG.info("Server is started on port: " + port);
						
						RuntimeMXBean runtime = new RuntimeImpl(JmxClientManager.get(host + ":" + port));
						pid = runtime.getName().split("@")[0];
						
						try {
							Boolean ret = callSsh.get(callSsh.submit(pid), 3000, TimeUnit.MILLISECONDS, true);
							LOG.info("Exists PID: " + ret);
						} catch(Exception e) {
							LOG.error(e.getMessage(), e);
						}
						
						Thread.sleep(5000);
						ssh.execute("/etc/profile", "/home/yanghe/test/nano-manager-quartz-server-1.0.0-SNAPSHOT/bin/shutdown.sh");
						break;
					}
				} catch(IOException e) {
					Thread.sleep(1000);
				}
			}
			
			for(;;) {
				try {
					socket = new Socket(host, port);
					boolean run = socket.isConnected();
					if(!run) {
						LOG.info("Server is stoped on port: " + port);
						break;
					}
				} catch(IOException e) {
					LOG.info("Server is stoped on port: " + port);
					break;
				} finally {
					Thread.sleep(1000);
				}
			}
			
			Boolean ret = callSsh.get(callSsh.submit(pid), 3000, TimeUnit.MILLISECONDS, true);
			LOG.info("Exists PID: " + ret);
			Thread.sleep(5000);
		} catch(Exception e) { LOG.error(e.getMessage(), e); }
	}
	
}
