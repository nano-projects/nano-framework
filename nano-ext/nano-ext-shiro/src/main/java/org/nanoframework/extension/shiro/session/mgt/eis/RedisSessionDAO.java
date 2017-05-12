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
package org.nanoframework.extension.shiro.session.mgt.eis;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.SerializableUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

/**
 * @author yanghe
 * @since 1.2
 */
public class RedisSessionDAO extends CachingSessionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionDAO.class);

    protected static final String DEFAULT_REDIS_SOURCE_NAME = "shiro";
    protected static final String DEFAULT_SESSION_NAME = "SHIRO_SESSION_";
    protected static final PersistType DEFAULT_PERSIST_TYPE = PersistType.SET;
    protected static final int DEFAULT_SESSION_EXPIRE = 1800;

    /** 支持主备读取，持久化操作只对主节点进行，读取操作时，如果主节点宕机，可以自动对备用节点进行操作 */
    protected String redisSourceNames = DEFAULT_REDIS_SOURCE_NAME;
    protected Map<String, RedisClient> sessions;

    /** 持久化只Redis时使用的方式(SET, HSET), 默认使用SET方式，默认的持久化名称为'SHIRO_SESSION_' + sessionId, 默认的超时时间为1800秒 */
    protected String sessionName = DEFAULT_SESSION_NAME;
    /** HSET不支持Session过期自动删除功能，这可能会导致大量的Session持久化至Redis而不被自动删除 */
    protected PersistType persistType = DEFAULT_PERSIST_TYPE;
    protected int sessionExpire = DEFAULT_SESSION_EXPIRE;

    protected enum PersistType {
        SET, HSET
    }

    @Override
    protected Serializable doCreate(Session session) {
        initRedisClient();
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        RedisClient client = sessions.values().iterator().next();
        switch (persistType) {
            case SET:
                String id;
                client.set((id = sessionName + sessionId), SerializableUtils.encode(session));
                client.expire(id, sessionExpire);
                break;

            case HSET:
                client.hset(sessionName, (String) sessionId, SerializableUtils.encode(session));
                break;
        }

        return session.getId();
    }

    @Override
    protected void doUpdate(Session session) {
        if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
            return;
        }

        initRedisClient();
        RedisClient client = sessions.values().iterator().next();
        switch (persistType) {
            case SET:
                String id;
                client.set((id = sessionName + session.getId()), SerializableUtils.encode(session));
                client.expire(id, sessionExpire);
                break;

            case HSET:
                client.hset(sessionName, (String) session.getId(), SerializableUtils.encode(session));
                break;
        }
    }

    @Override
    protected void doDelete(Session session) {
        initRedisClient();
        RedisClient client = sessions.values().iterator().next();
        switch (persistType) {
            case SET:
                client.del(sessionName + session.getId());
                break;
            case HSET:
                client.hdel(sessionName, JSON.toJSONString(session.getId()));
                break;
        }
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        initRedisClient();
        String value = null;
        for (Entry<String, RedisClient> item : sessions.entrySet()) {
            try {
                RedisClient client = item.getValue();
                switch (persistType) {
                    case SET:
                        value = client.get(sessionName + sessionId);
                        break;
                    case HSET:
                        value = client.hget(sessionName, (String) sessionId);
                        break;
                }

                break;
            } catch (Exception e) {
                LOGGER.error("读取Session异常[" + item.getKey() + "]: " + e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(value)) {
            return SerializableUtils.decode(value);
        }

        return null;
    }

    @Override
    public Collection<Session> getActiveSessions() {
        return super.getActiveSessions();
    }

    public void setRedisSourceNames(String jedisSourceNames) {
        this.redisSourceNames = jedisSourceNames;
        if (StringUtils.isNotBlank(jedisSourceNames)) {
            String[] names = jedisSourceNames.split(",");
            Map<String, RedisClient> sessionMap = Maps.newLinkedHashMap();
            for (String name : names) {
                if (StringUtils.isBlank(name)) {
                    continue;
                }

                RedisClient client;
                if ((client = GlobalRedisClient.get(name)) != null) {
                    sessionMap.put(name, client);
                }
            }

            this.sessions = sessionMap;
        }
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public void setPersistType(String persistType) {
        this.persistType = PersistType.valueOf(persistType);
    }

    public void setSessionExpire(int sessionExpire) {
        this.sessionExpire = sessionExpire;
    }

    private void initRedisClient() {
        if (CollectionUtils.isEmpty(sessions)) {
            setRedisSourceNames(redisSourceNames);
        }
    }
}
