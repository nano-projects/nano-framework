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
import java.net.Socket;
import java.rmi.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.RuntimeUtil;
import org.nanoframework.jmx.client.exception.MXBeanException;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class JmxClient {
	public static final String DEFAULT_CONTENT = "jmxrmi";
	private static final ThreadPoolExecutor service = new ThreadPoolExecutor(0, RuntimeUtil.AVAILABLE_PROCESSORS, 10L, TimeUnit.SECONDS, 
		new SynchronousQueue<Runnable>(),
        new ThreadFactory() {
    		private AtomicLong id = new AtomicLong(0);
    		@Override
    		public Thread newThread(Runnable r) {
    			Thread thread = new Thread(r);
    			thread.setName("JMX-CONNECT-POOL-" + id.getAndIncrement());
    			thread.setDaemon(true);
    			return thread;
    		}
    	}
	);
	
	private String host;
	private Integer port;
	private String content;
	private JMXServiceURL jmxServiceUrl;
	private JMXConnector connector;
	private MBeanServerConnection connection;
	private AtomicBoolean closed = new AtomicBoolean(true);
	private Socket socket;
	
	public JmxClient(String host, Integer port) {
		this(host, port, DEFAULT_CONTENT);
	}
	
	public JmxClient(String host, Integer port, long timeout) {
		this(host, port, DEFAULT_CONTENT, timeout);
	}
	
	public JmxClient(String host, Integer port, String content) {
		this(host, port, content, true);
	}
	
	public JmxClient(String host, Integer port, String content, boolean autoConnect) {
		init(host, port, content);
		
		if(autoConnect)
			connect();
	}
	
	public JmxClient(String host, Integer port, String content, long timeout) {
		this(host, port, content, true, timeout);
	}
	
	public JmxClient(String host, Integer port, String content, boolean autoConnect, long timeout) {
		init(host, port, content);
		
		if(autoConnect)
			connect(timeout);
	}
	
	private void init(String host, Integer port, String content) {
		Assert.hasLength(host);
		Assert.notNull(port);
		Assert.hasLength(content);
		this.host = host;
		this.port = port;
		this.content = content;
		
		try {
			jmxServiceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ':' + port + '/' + content);
		} catch(IOException e) {
			throw new MXBeanException(e.getMessage(), e);
		}
	}
	
	public JmxClient connect() {
		try {
			socket = new Socket(host, port);
			if(socket.isConnected()) {
				socket.close();
				
				connector = JMXConnectorFactory.connect(jmxServiceUrl);
				closed.set(false);
			}
		} catch(IOException e) {
			throw new MXBeanException(e.getMessage(), e);
		} finally {
			socket = null;
		}
		
		return this;
	}
	
	public JmxClient connect(long timeout) {
		try {
			socket = new Socket(host, port);
			if(socket.isConnected()) {
				socket.close();
				
				Future<JMXConnector> future = service.submit(() -> JMXConnectorFactory.connect(jmxServiceUrl));
				connector = future.get(timeout, TimeUnit.MILLISECONDS);
				closed.set(false);
			}
		} catch(InterruptedException | ExecutionException | TimeoutException | IOException e) {
			throw new MXBeanException(e.getMessage(), e);
		} finally {
			socket = null;
		}
		
		return this;
	}
	
	public synchronized void reconnect() throws ConnectException {
		reconnect(0);
	}
	
	public synchronized void reconnect(long timeout) throws ConnectException {
		if(isClosed())
			return ;
		
		try {
			close();
			if(timeout <= 0)
				connect();
			else
				connect(timeout);
		} catch(MXBeanException e) {
			if(e.getCause() != null && e.getCause() instanceof IOException)
				throw new ConnectException(e.getMessage(), e);

			throw e;
		}
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
