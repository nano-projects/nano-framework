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

import javax.servlet.ServletConfig;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.plugins.Plugin;

/**
 * @author yanghe
 * @since 1.1
 */
public class WebSocketPlugin implements Plugin {
    private Logger LOGGER = LoggerFactory.getLogger(WebSocketPlugin.class);

    @Override
    public boolean load() throws Throwable {
        try {
            long time = System.currentTimeMillis();
            LOGGER.info("开始加载WebSocket服务");
            WebSocketFactory.load();
            LOGGER.info("加载WebSocket服务完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
        } catch (final Throwable e) {
            throw new WebSocketException(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {

    }

}
