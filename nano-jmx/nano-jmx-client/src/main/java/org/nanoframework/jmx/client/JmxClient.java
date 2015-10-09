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
package org.nanoframework.jmx.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.jmx.client.exception.MXBeanException;

/**
 * 
 * @author yanghe
 * @date 2015年8月18日 下午5:22:57 
 * @since 1.1
 */
public class JmxClient {
	public static final String DEFAULT_CONTENT = "jmxrmi";
	private static final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			return thread;
		}
	});
	
	private String host;
	private Integer port;
	private String content;
	private JMXServiceURL jmxServiceUrl;
	private JMXConnector connector;
	private MBeanServerConnection connection;
	private AtomicBoolean closed = new AtomicBoolean(true);
	
	public JmxClient(String host, Integer port) {
		this(host, port, DEFAULT_CONTENT);
	}
	
	public JmxClient(String host, Integer port, String content) {
		Assert.hasLength(host);
		Assert.notNull(port);
		Assert.hasLength(content);
		this.host = host;
		this.port = port;
		this.content = content;
		
		try {
			jmxServiceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/" + content);
		} catch(IOException e) {
			throw new MXBeanException(e.getMessage(), e);
		}
		
		connect();
	}
	
	public JmxClient connect() {
		try {
			Future<JMXConnector> future = service.submit(() -> JMXConnectorFactory.connect(jmxServiceUrl));
			connector = future.get(3000, TimeUnit.MILLISECONDS);
			closed.set(false);
		} catch(InterruptedException | ExecutionException | TimeoutException e) {
			throw new MXBeanException(e.getMessage(), e);
		}
		
		return this;
	}
	
	public MBeanServerConnection getConnection() {
		if(connection != null)
			return connection;
		
		try {
			if(connector == null)
				connect();
			
			return connection = connector.getMBeanServerConnection();
		} catch(IOException e) {
			throw new MXBeanException(e.getMessage(), e);
		}
	}
	
	public synchronized void close() {
		if(connector != null) {
			try { connector.close(); } catch(Exception e) {  }
			connector = null;
			connection = null;
			closed.set(true);
		}
	}
	
	/**
	 * if closed then true, else then false
	 * @return
	 */
	public boolean isClosed() {
		return closed.get();
	}

	public String getHost() {
		return host;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public String getContent() {
		return content;
	}
}
