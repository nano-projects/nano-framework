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
package org.nanoframework.core.plugins.defaults.plugin;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.plugins.Plugin;

/**
 * @author yanghe
 * @date 2015年10月30日 下午11:44:49
 */
public class WebSocketPlugin implements Plugin {

    private Logger LOG = LoggerFactory.getLogger(WebSocketPlugin.class);

    @Override
    public boolean load() throws Throwable {
        try {
            Class<?> websocket = Class.forName("org.nanoframework.extension.websocket.WebSocketFactory");
            long time = System.currentTimeMillis();
            LOG.info("开始加载WebSocket服务");
            websocket.getMethod("load").invoke(websocket);
            LOG.info("加载WebSocket服务完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            if (!(e instanceof ClassNotFoundException))
                throw new RuntimeException(e);

            return false;
        }

        return true;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {

    }

}
