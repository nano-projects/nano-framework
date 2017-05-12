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

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.core.component.scan.ClassScanner;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.globals.Globals;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 1.1
 */
public class WebSocketFactory {
	private static Logger LOGGRE = LoggerFactory.getLogger(WebSocketFactory.class);
	private static boolean LOADED = false;
	private static final ConcurrentMap<String, WebSocketServer> HANDLER = Maps.newConcurrentMap();
	
	public static final void load() throws Throwable {
		if(LOADED) {
			throw new LoaderException("WebSocket已经加载，这里不再进行重复的加载，如需重新加载请调用reload方法");
		}

		if(PropertiesLoader.PROPERTIES.size() == 0) {
			throw new LoaderException("没有加载任何的属性文件, 无法加载组件.");
		}
		
		PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.getProperty(WEBSOCKET_BASE_PACKAGE) != null).forEach(item -> {
			ClassScanner.scan(item.getProperty(WEBSOCKET_BASE_PACKAGE));
		});
		
		final Set<Class<?>> classes = ClassScanner.filter(WebSocket.class);
		LOGGRE.info("WebSocket size: " + classes.size());
		
		if(classes.size() > 0) {
			for(final Class<?> cls : classes) {
				if(AbstractWebSocketHandler.class.isAssignableFrom(cls)) {
					LOGGRE.info("Inject WebSocket Class: " + cls.getName());
					final WebSocket websocket = cls.getAnnotation(WebSocket.class);
					String webSocketName = websocket.value();
					if(StringUtils.isBlank(websocket.value())) {
						webSocketName = cls.getSimpleName();
					}
					
					String host = null;
					Integer port = null;
					Integer proxyPort = null;
					Boolean ssl = null;
					String location = null;
					for(final Properties properties : PropertiesLoader.PROPERTIES.values()) {
						if(StringUtils.isNotBlank(websocket.hostProperty())) {
							final String h = properties.getProperty(websocket.hostProperty());
							if(StringUtils.isNotBlank(h)) {
								host = h;
							}
						}
						
						if(StringUtils.isNotBlank(websocket.portProperty())) {
						    final String p = properties.getProperty(websocket.portProperty());
							if(StringUtils.isNotBlank(p)) {
								port = Integer.valueOf(p);
							}
						}
						
						if(StringUtils.isNotBlank(websocket.proxyPortProperty())) {
						    final String pp = properties.getProperty(websocket.proxyPortProperty());
							if(StringUtils.isNotBlank(pp)) {
								proxyPort = Integer.valueOf(pp);
							}
						}
						
						if(StringUtils.isNotBlank(websocket.sslProperty())) {
						    final String s = properties.getProperty(websocket.sslProperty());
							if(StringUtils.isNotBlank(s)) {
								ssl = Boolean.valueOf(s);
							}
						}
						
						if(StringUtils.isNotBlank(websocket.locationProperty())) {
						    final String l = properties.getProperty(websocket.locationProperty());
							if(StringUtils.isNotBlank(l)) {
								location = l;
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
					
					if(StringUtils.isBlank(location)) {
						location = websocket.location();
					}
					
					Assert.hasLength(location);
					location = System.getProperty(ApplicationContext.CONTEXT_ROOT) + location;
					
					final AbstractWebSocketHandler handler = (AbstractWebSocketHandler) Globals.get(Injector.class).getInstance(cls);
					handler.setLocation(location);
					HANDLER.put(webSocketName, WebSocketServer.create(host, port, proxyPort, ssl, location, handler));
				} else {
					throw new WebSocketException("必须继承: [ "+AbstractWebSocketHandler.class.getName()+" ]");
				}
			}
		}
		
		LOADED = true;
	}
	
	public static final WebSocketServer get(final String name) {
		return HANDLER.get(name);
	}
}
