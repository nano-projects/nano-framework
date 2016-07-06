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
package org.nanoframework.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.RuntimeUtil;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.server.exception.JettyServerException;
import org.nanoframework.server.exception.ReadXMLException;

/**
 * Jetty Server
 * 
 * @author yanghe
 * @since 1.0
 */
public class JettyCustomServer extends Server {
	private static final Logger LOGGER = LoggerFactory.getLogger(JettyCustomServer.class);
	
	private static Properties CONTEXT;
	private static String DEFAULT_RESOURCE_BASE = "./webRoot";
	
	static {
		try {
			CONTEXT = PropertiesLoader.load(ApplicationContext.MAIN_CONTEXT);
			LOGGER.info("Runtime path: " + RuntimeUtil.getPath(JettyCustomServer.class));
		} catch(LoaderException e) { }
	}
	
	private static String DEFAULT_WEB_XML_PATH = DEFAULT_RESOURCE_BASE + "/WEB-INF/web.xml";
	
	private static String WEB_DEFAULT = DEFAULT_RESOURCE_BASE + "/WEB-INF/webdefault.xml";
	
	private static String DEFAULT_JETTY_CONFIG = DEFAULT_RESOURCE_BASE + "/WEB-INF/jetty.xml";
	
	public static JettyCustomServer DEFAULT;
	static {
		try {
			DEFAULT = new JettyCustomServer();
		} catch(Exception e) { }
	}
	
	private static final String JETTY_PID_FILE = "jetty.pid";
	
	static final String[] CMD = new String[] {
		"start", 
		"stop"
	};
	
	public JettyCustomServer() {
		this(DEFAULT_JETTY_CONFIG, CONTEXT.getProperty(ApplicationContext.CONTEXT_ROOT), null, null, null);
	}
	
	public JettyCustomServer(String mainContext) {
		Assert.hasLength(mainContext, "未设置CONTEXT属性文件路径");
		try {
			CONTEXT = PropertiesLoader.load(mainContext);
			LOGGER.info("Runtime path: " + RuntimeUtil.getPath(JettyCustomServer.class));
		} catch(LoaderException e) { 
			throw new JettyServerException(e.getMessage(), e);
		}
		
		readXmlConfig(DEFAULT_JETTY_CONFIG);
		applyHandle(CONTEXT.getProperty(ApplicationContext.CONTEXT_ROOT), null);
	}
	
	public JettyCustomServer(String xmlConfigPath, String contextPath, String resourceBase, String webXmlPath) {
		this(xmlConfigPath, contextPath, resourceBase, webXmlPath, null);
	}

	public JettyCustomServer(String xmlConfigPath, String contextPath) {
		this(xmlConfigPath, contextPath, null, null, null);
	}
	
	public JettyCustomServer(String xmlConfigPath, String contextPath, String warPath) {
		this(xmlConfigPath, contextPath, null, null, warPath);
	}

	public JettyCustomServer(String xmlConfigPath, String contextPath, String resourceBase, String webXmlPath, String warPath) {
		super();
		if (StringUtils.isNotBlank(xmlConfigPath)) {
			DEFAULT_JETTY_CONFIG = xmlConfigPath;
			readXmlConfig(xmlConfigPath);
		}

		if (StringUtils.isNotEmpty(warPath) && StringUtils.isNotEmpty(contextPath)) {
			applyHandle(contextPath, warPath);
		} else {
			if (StringUtils.isNotEmpty(resourceBase))
				DEFAULT_RESOURCE_BASE = resourceBase;
			
			if (StringUtils.isNotEmpty(webXmlPath))
				DEFAULT_WEB_XML_PATH = webXmlPath;
			
			if (StringUtils.isNotBlank(contextPath)) 
				applyHandle(contextPath, warPath);
		}
	}
	
	private void readXmlConfig(String configPath) {
		try {
			XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(configPath));
			configuration.configure(this);
		} catch(Exception e) {
			throw new ReadXMLException(e.getMessage(), e);
		}
	}

	public void applyHandle(String contextPath, String warPath) {
		ContextHandlerCollection handler = new ContextHandlerCollection();
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(contextPath);
		webapp.setDefaultsDescriptor(WEB_DEFAULT);
		if (StringUtils.isEmpty(warPath)) {
			webapp.setResourceBase(DEFAULT_RESOURCE_BASE);
			webapp.setDescriptor(DEFAULT_WEB_XML_PATH);
		} else 
			webapp.setWar(warPath);

		handler.addHandler(webapp);
		super.setHandler(handler);
	}

	public void startServer() {
		try {
			writePID2File();
			super.start();
			LOGGER.info("Current thread: {} | Idle thread: {}", super.getThreadPool().getThreads(), super.getThreadPool().getIdleThreads());
			super.join();
		} catch (Throwable e) {
			throw new JettyServerException(e.getMessage(), e);
		}
	}
	
	/**
	 * 根据PID优雅停止进程
	 * 
	 * @since 1.2.15
	 */
	public void stopServer() {
		try {
			String pid = readPID();
			if(StringUtils.isNotBlank(pid)) {
				if(RuntimeUtil.exsitsProcess(pid)) {
					RuntimeUtil.exitProcess(pid);
					delPID();
					return ;
				} else {
					return ;
				}
			}
			
			throw new JettyServerException("Not found jetty.pid");
		} catch(Throwable e) {
			throw new JettyServerException("Stop Server error: " + e.getMessage());
		}
	}
	
	public void startServerDaemon() {
		Executors.newFixedThreadPool(1, (runnable) -> {
			Thread jetty = new Thread(runnable);
			jetty.setName("Jetty Server Deamon: " + System.currentTimeMillis());
			return jetty;
		}).execute(() -> startServer());
	}

	public void writePID2File() {
		try {
			final String pid = RuntimeUtil.PID;
			File file = new File(JETTY_PID_FILE);
			if(!file.exists())
				file.createNewFile();
		
			try(FileWriter writer = new FileWriter(file, false)) {
				writer.write(pid);
				writer.flush();
			}
		} catch(Throwable e) {
			throw new JettyServerException(e.getMessage(), e);
		}
	}
	
	public String readPID() {
		try {
			File file = new File(JETTY_PID_FILE);
			if(file.exists()) {
				try (InputStream input = new FileInputStream(file)) {
					try (Scanner scanner = new Scanner(input)) {
						StringBuilder builder = new StringBuilder();
						while(scanner.hasNextLine()) {
							builder.append(scanner.nextLine());
						}
						
						return builder.toString();
					}
				}
			}
			
			return StringUtils.EMPTY;
		} catch(Throwable e) {
			throw new JettyServerException("Read PID file error: " + e.getMessage());
		}
	}
	
	public void delPID() {
		try {
			File file = new File(JETTY_PID_FILE);
			if(file.exists()) 
				file.delete();
			
		} catch(Throwable e) {
			throw new JettyServerException("Del PID file error: " + e.getMessage());
		}
	}
	
	public final void bootstrap(String[] args) {
		if(args.length > 0) {
			if(StringUtils.equals(args[0], CMD[0])) {
				startServerDaemon();
				
			} else if(StringUtils.equals(args[0], CMD[1])) {
				stopServer();
				
			} else {
				throw new JettyServerException("Unknown command in args list");
				
			}
		} else {
			startServerDaemon();
		}
	}
}