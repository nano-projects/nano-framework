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
package org.nanoframework.web.server.listener;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * Web服务启动上下文监听器
 * 
 * @author yanghe
 * @date 2015年7月25日 下午8:28:47 
 *
 */
public class ContextLoaderListener implements ServletContextListener {

	private Logger LOG = LoggerFactory.getLogger(ContextLoaderListener.class);
	
	public void contextInitialized(ServletContextEvent sce) {
		try {
			Class.forName("org.nanoframework.extension.concurrent.queue.BlockingQueueFactory");
			Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
		} catch(ClassNotFoundException e) {
			LOG.error("没有加载Websocket扩展或调用异常: " + e.getMessage());
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		try {
			Class<?> websocket = Class.forName("org.nanoframework.extension.websocket.WebSocketServer");
			websocket.getMethod("closeAll").invoke(websocket);
		} catch(ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			LOG.error("没有加载Websocket扩展或调用异常: " + e.getMessage());
		}
	}

}
