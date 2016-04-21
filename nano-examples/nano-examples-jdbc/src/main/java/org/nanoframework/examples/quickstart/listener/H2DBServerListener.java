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
package org.nanoframework.examples.quickstart.listener;

import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.h2.tools.Server;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
*
* @author yanghe
* @since 1.3.5
*/
public class H2DBServerListener implements ServletContextListener {
    private Logger LOG = LoggerFactory.getLogger(H2DBServerListener.class);
    private Server server;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {  
            LOG.info("正在启动H2数据库...");
            server = Server.createTcpServer().start(); 
            LOG.info("H2数据库启动完成");
        } catch (SQLException e) {  
            throw new RuntimeException("启动H2数据库出错：" + e.getMessage(), e);  
        }  
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if(server != null) {
            server.stop();
            server = null;
        }
    }

}