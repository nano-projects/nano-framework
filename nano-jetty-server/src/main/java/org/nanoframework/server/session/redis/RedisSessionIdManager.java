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
package org.nanoframework.server.session.redis;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.server.session.AbstractSessionIdManager;

import com.google.common.collect.Lists;

/**
 *
 * @author yanghe
 * @since 1.3.16
 */
public class RedisSessionIdManager extends AbstractSessionIdManager {
    final static Logger LOGGER = Log.getLogger("org.nanoframework.server.session");
    static final String REDIS_SESSION_KEY = "JETTY-SESSION-";
    
    private final String redisType;
    private RedisClient client;
    
    public RedisSessionIdManager(final Server server, final String redisType) {
        super(server);
        this.redisType = redisType;
    }

    @Override
    protected void deleteClusterId(final String clusterId) {

    }

    @Override
    protected void storeClusterId(final String clusterId) {

    }

    @Override
    protected boolean hasClusterId(final String clusterId) {
        return getClient().exists(REDIS_SESSION_KEY + clusterId);
    }

    @Override
    protected List<String> scavenge(final List<String> clusterIds) {
        final List<String> notExistsIds = Lists.newArrayList();
        clusterIds.stream().filter(clusterId -> !getClient().exists(REDIS_SESSION_KEY + clusterId)).forEach(clusterId -> notExistsIds.add(clusterId));
        return notExistsIds;
    }
    
    /**
     * @return the client
     */
    protected RedisClient getClient() {
        if (client == null && StringUtils.isNotBlank(redisType)) {
            client = GlobalRedisClient.get(redisType);
        }
        
        return client;
    }
}
