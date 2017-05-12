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
package org.nanoframework.extension.shiro.listener;

import java.util.List;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListenerAdapter;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.extension.shiro.web.component.SSOComponent;
import org.nanoframework.orm.jedis.RedisClient;

import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class SSOSessionListener extends SessionListenerAdapter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SSOSessionListener.class);
    
    @Override
    public void onStop(final Session session) {
        unbindSession((String) session.getId());
    }

    @Override
    public void onExpiration(final Session session) {
        unbindSession((String) session.getId());
    }
    
    protected void unbindSession(final String sessionId) {
        final int errorRetry = SSOComponent.ERROR_RETRY;
        for(int count = 0; count < errorRetry; count++) {
            try {
                unbindSession0(sessionId);
                return ;
            } catch(final Throwable e) {
                LOGGER.error("unbindSession Error: {}, retry {}...", e.getMessage(), count + 1);
            }
        }
    }
    
    protected void unbindSession0(final String sessionId) {
        final String shiroSessionListenerPrefix = SSOComponent.SHIRO_SESSION_LISTENER_PREFIX;
        final String shiroClientSessionPrefix = SSOComponent.SHIRO_CLIENT_SESSION_PREFIX;
        final RedisClient shiro = SSOComponent.SHIRO;
        final String sessionListenerKey = shiroSessionListenerPrefix + sessionId;
        
        Set<String> clientSessionIds = shiro.smembers(sessionListenerKey);
        if(!CollectionUtils.isEmpty(clientSessionIds)) {
            List<String> delKeys = Lists.newArrayList();
            clientSessionIds.forEach(clientSessionId -> delKeys.add(shiroClientSessionPrefix + clientSessionId));
            delKeys.add(sessionListenerKey);
            
            shiro.del(delKeys);
        }
    }

}
