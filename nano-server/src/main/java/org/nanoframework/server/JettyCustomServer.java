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
package org.nanoframework.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.commons.util.RuntimeUtil;
import org.nanoframework.server.exception.JettyServerException;
import org.nanoframework.server.exception.ReadXMLException;

/**
 * Jetty Server
 * 
 * @author yanghe
 * @date 2015年7月11日 下午5:19:20 
 *
 */
public class JettyCustomServer extends Server {

	private static Logger LOG = LoggerFactory.getLogger(JettyCustomServer.class);
	
	private static Properties CONTEXT;
	public static String DEFAULT_RESOURCE_BASE = "./webRoot";
	
	static {
		try {
			CONTEXT = PropertiesLoader.load(JettyCustomServer.class.getResourceAsStream(Constants.MAIN_CONTEXT));
			LOG.info("Runtime path: " + RuntimeUtil.getPath(JettyCustomServer.class));
		} catch(LoaderException | IOException e) { }
	}
	
	public static String DEFAULT_WEB_XML_PATH = DEFAULT_RESOURCE_BASE + "/WEB-INF/web.xml";
	
	public static String WEB_DEFAULT = DEFAULT_RESOURCE_BASE + "/WEB-INF/webdefault.xml";
	
	public static String DEFAULT_JETTY_CONFIG = DEFAULT_RESOURCE_BASE + "/WEB-INF/jetty.xml";
	
	public static JettyCustomServer DEFAULT;
	static {
		try {
			DEFAULT = new JettyCustomServer();
		} catch(Exception e) { }
	}
	public JettyCustomServer() {
		this(DEFAULT_JETTY_CONFIG, CONTEXT.getProperty(Constants.CONTEXT_ROOT), null, null, null);
	}
	
	public JettyCustomServer(String mainContext) {
		Assert.hasLength(mainContext, "未设置CONTEXT属性文件路径");
		try {
			CONTEXT = PropertiesLoader.load(JettyCustomServer.class.getResourceAsStream(mainContext));
			LOG.info("Runtime path: " + RuntimeUtil.getPath(JettyCustomServer.class));
		} catch(LoaderException | IOException e) { 
			throw new JettyServerException(e.getMessage(), e);
		}
		
		readXmlConfig(DEFAULT_JETTY_CONFIG);
		applyHandle(CONTEXT.getProperty(Constants.CONTEXT_ROOT), null);
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
			super.start();
			LOG.info("current thread:" + super.getThreadPool().getThreads() + "| idle thread:" + super.getThreadPool().getIdleThreads());
			super.join();
		} catch (Exception e) {
			throw new JettyServerException(e.getMessage(), e);
		}
	}

}