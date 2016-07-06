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
package org.nanoframework.extension.websocket;

import static org.nanoframework.core.context.ApplicationContext.WEBSOCKET_BASE_PACKAGE;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.core.component.scan.ComponentScan;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.globals.Globals;

import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 1.1
 */
public class WebSocketFactory {
	private static Logger LOG = LoggerFactory.getLogger(WebSocketFactory.class);
	private static boolean isLoaded = false;
	
	private static final ConcurrentMap<String, WebSocketServer> handlerMap = new ConcurrentHashMap<>();
	
	public static final void load() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, CertificateException, SSLException, InterruptedException {
		if(isLoaded) {
			throw new LoaderException("WebSocket已经加载，这里不再进行重复的加载，如需重新加载请调用reload方法");
		}

		if(PropertiesLoader.PROPERTIES.size() == 0) {
			throw new LoaderException("没有加载任何的属性文件, 无法加载组件.");
		}
		
		PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.getProperty(WEBSOCKET_BASE_PACKAGE) != null).forEach(item -> {
			ComponentScan.scan(item.getProperty(WEBSOCKET_BASE_PACKAGE));
		});
		
		Set<Class<?>> componentClasses = ComponentScan.filter(WebSocket.class);
		LOG.info("WebSocket size: " + componentClasses.size());
		
		if(componentClasses.size() > 0) {
			for(Class<?> clz : componentClasses) {
				if(AbstractWebSocketHandler.class.isAssignableFrom(clz)) {
					LOG.info("Inject WebSocket Class: " + clz.getName());
					WebSocket websocket = clz.getAnnotation(WebSocket.class);
					String webSocketName = websocket.value();
					if(StringUtils.isBlank(websocket.value())) {
//						throw new WebSocketException("WebSocket名不能为空, 类名 [ " + clz.getName()+ " ]");
						webSocketName = clz.getSimpleName();
					}
					
					String host = null;
					Integer port = null;
					Integer proxyPort = null;
					Boolean ssl = null;
					String location = null;
					for(Properties properties : PropertiesLoader.PROPERTIES.values()) {
						if(StringUtils.isNotBlank(websocket.hostProperty())) {
							String _host = properties.getProperty(websocket.hostProperty());
							if(StringUtils.isNotBlank(_host)) {
								host = _host;
							}
						}
						
						if(StringUtils.isNotBlank(websocket.portProperty())) {
							String _port = properties.getProperty(websocket.portProperty());
							if(StringUtils.isNotBlank(_port)) {
								port = Integer.valueOf(_port);
							}
						}
						
						if(StringUtils.isNotBlank(websocket.proxyPortProperty())) {
							String _proxyPort = properties.getProperty(websocket.proxyPortProperty());
							if(StringUtils.isNotBlank(_proxyPort)) {
								proxyPort = Integer.valueOf(_proxyPort);
							}
						}
						
						if(StringUtils.isNotBlank(websocket.sslProperty())) {
							String _ssl = properties.getProperty(websocket.sslProperty());
							if(StringUtils.isNotBlank(_ssl)) {
								ssl = Boolean.valueOf(_ssl);
							}
						}
						
						if(StringUtils.isNotBlank(websocket.locationProperty())) {
							String _location = properties.getProperty(websocket.locationProperty());
							if(StringUtils.isNotBlank(_location)) {
								location = _location;
							}
						}
					}
					
					
					if(StringUtils.isBlank(host)) {
						host = websocket.host();
					}
					
					if(port == null) {
						port = websocket.port();
					}
					
					if(proxyPort == null)
						proxyPort = websocket.proxyPort();
					
					if(ssl == null) {
						ssl = websocket.ssl();
					}
					
					if(StringUtils.isBlank(location))
						location = websocket.location();
					
					Assert.hasLength(location);
					location = System.getProperty(ApplicationContext.CONTEXT_ROOT) + location;
					
					AbstractWebSocketHandler handler = (AbstractWebSocketHandler) Globals.get(Injector.class).getInstance(clz);
					handler.setLocation(location);
					handlerMap.put(webSocketName, WebSocketServer.create(host, port, proxyPort, ssl, location, handler));
					
				} else 
					throw new WebSocketException("必须继承: [ "+AbstractWebSocketHandler.class.getName()+" ]");
				
			}
			
		}
		
		isLoaded = true;
	}
	
	public static final WebSocketServer get(String name) {
		return handlerMap.get(name);
	}
}
