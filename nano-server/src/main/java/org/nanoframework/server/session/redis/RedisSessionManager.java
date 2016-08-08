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

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static org.nanoframework.server.session.redis.RedisSessionIdManager.REDIS_SESSION_KEY;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.nanoframework.commons.util.SerializableUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.server.session.AbstractSessionManager;

import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.3.16
 */
public class RedisSessionManager extends AbstractSessionManager {
    static final Logger LOGGER = Log.getLogger("org.nanoframework.server.session");
    private static final String[] FIELDS = {"id", "created", "accessed", "lastNode", "expiryTime", "lastSaved", "lastAccessed", "maxIdle", "cookieSet", "attributes"};
    
    private final String redisType;
    private RedisClient client;
    
    public RedisSessionManager(final String redisType) {
        this.redisType = redisType;
    }
    
    /** only persist changes to session access times every 20 secs. */
    private long saveInterval = 20_000; 

    public void setSaveInterval(long saveInterval) {
        this.saveInterval = saveInterval;
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
    }

    @Override
    public void doStop() throws Exception {
        super.doStop();
    }

    @Override
    protected Session loadSession(final String clusterId, final Session current) {
        final long now = System.currentTimeMillis();
        RedisSession loaded;
        if (current == null) {
            LOGGER.debug("[RedisSessionManager] loadSession - No session found in cache, loading id={}", clusterId);
            loaded = loadFromStore(clusterId, (RedisSession) current);
        } else if (((RedisSession) current).requestStarted()) {
            LOGGER.debug("[RedisSessionManager] loadSession - Existing session found in cache, loading id={}", clusterId);
            loaded = loadFromStore(clusterId, (RedisSession) current);
        } else {
            loaded = (RedisSession) current;
        }
        
        if (loaded == null) {
            LOGGER.debug("[RedisSessionManager] loadSession - No session found in Redis for id={}", clusterId);
            if (current != null) {
                current.invalidate();
            }
        } else if (loaded == current) {
            LOGGER.debug("[RedisSessionManager] loadSession - No change found in Redis for session id={}", clusterId);
            return loaded;
        } else if (!StringUtils.equals(loaded.lastNode, getSessionIdManager().getWorkerName()) || current == null) {
            //if the session in the database has not already expired
            if (loaded.expiryTime * 1000 > now) {
                //session last used on a different node, or we don't have it in memory
                loaded.changeLastNode(getSessionIdManager().getWorkerName());
            } else {
                LOGGER.debug("[RedisSessionManager] loadSession - Loaded session has expired, id={}", clusterId);
                loaded = null;
            }
        }
        
        return loaded;
    }

    private RedisSession loadFromStore(final String clusterId, final RedisSession current) {
        final String key = REDIS_SESSION_KEY + clusterId;
        final Map<String, String> sessionMap;
        if (current == null) {
            sessionMap = getClient().exists(key) ? getClient().hmget(key, FIELDS) : null;
        } else {
            final String lastSaved = getClient().hget(key, "lastSaved");
            if (lastSaved == null) {
                // no session in store
                sessionMap = Collections.emptyMap();
            } else if (current.lastSaved != Long.parseLong(lastSaved)) {
                sessionMap = getClient().hmget(key, FIELDS);
            } else {
                sessionMap = null;
            }
        }
        
        if (sessionMap == null) {
            // case where session has not been modified
            return current;
        }
        
        if (sessionMap.isEmpty()) {
            // no session found in redis (no data)
            return null;
        }
        
        final String attrs = sessionMap.get("attributes");
        
        try {
            return new RedisSession(sessionMap, attrs == null ? Maps.newHashMap() : SerializableUtils.decode(attrs));
        } catch (final Throwable e) {
            LOGGER.debug(e.getMessage());
            getClient().del(key);
            return null;
        }
    }

    @Override
    protected void storeSession(final Session session) {
        final RedisSession redisSession = (RedisSession) session;
        
        if (!redisSession.redisMap.isEmpty()) {
            final SortedMap<String, Object> toStore = redisSession.redisMap.containsKey("attributes") ? redisSession.redisMap : new TreeMap<>(redisSession.redisMap);
            if (toStore.containsKey("attributes")) {
                toStore.put("attributes", SerializableUtils.encode(redisSession.getSessionAttributes()));
            }
            
            redisSession.lastSaved = System.currentTimeMillis();
            toStore.put("lastSaved", redisSession.lastSaved);
            
            LOGGER.debug("[RedisSessionManager] storeSession - Storing session id={}", session.getClusterId());
            
            final String key = REDIS_SESSION_KEY + session.getClusterId();
            getClient().hmset(key, toStore);
            final int ttl = session.getMaxInactiveInterval();
            if (ttl > 0) {
                getClient().expire(key, ttl);
            }
            
            redisSession.redisMap.clear();
        }
    }

    @Override
    protected RedisSession newSession(HttpServletRequest request) {
        return new RedisSession(request);
    }

    @Override
    protected void deleteSession(final Session session) {
        LOGGER.debug("[RedisSessionManager] deleteSession - Deleting from Redis session id={}", session.getClusterId());
        getClient().del(REDIS_SESSION_KEY + session.getClusterId());
    }

    final class RedisSession extends Session {
        private final SortedMap<String, Object> redisMap = Maps.newTreeMap();

        private long expiryTime;
        private long lastSaved;
        private String lastNode;
        private final ThreadLocal<Boolean> firstAccess = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.TRUE;
            }
        };

        private RedisSession(final HttpServletRequest request) {
            super(request);
            lastNode = getSessionIdManager().getWorkerName();
            final long ttl = getMaxInactiveInterval();
            expiryTime = ttl <= 0 ? 0 : System.currentTimeMillis() / 1000 + ttl;
            // new session so prepare redis map accordingly
            redisMap.put("id", getClusterId());
            redisMap.put("context", getCanonicalizedContext());
            redisMap.put("virtualHost", getVirtualHost());
            redisMap.put("created", getCreationTime());
            redisMap.put("lastNode", lastNode);
            redisMap.put("lastAccessed", getLastAccessedTime());
            redisMap.put("accessed", getAccessed());
            redisMap.put("expiryTime", expiryTime);
            redisMap.put("maxIdle", ttl);
            redisMap.put("cookieSet", getCookieSetTime());
            redisMap.put("attributes", StringUtils.EMPTY);
        }

        RedisSession(final Map<String, String> redisData, final Map<String, Object> attributes) {
            super(parseLong(redisData.get("created")), parseLong(redisData.get("accessed")), redisData.get("id"));
            lastNode = redisData.get("lastNode");
            expiryTime = parseLong(redisData.get("expiryTime"));
            lastSaved = parseLong(redisData.get("lastSaved"));
            super.setMaxInactiveInterval(parseInt(redisData.get("maxIdle")));
            setCookieSetTime(parseLong(redisData.get("cookieSet")));
            for (final Map.Entry<String, Object> entry : attributes.entrySet()) {
                super.doPutOrRemove(entry.getKey(), entry.getValue());
            }
            
            super.access(parseLong(redisData.get("lastAccessed")));
        }

        public void changeLastNode(final String lastNode) {
            this.lastNode = lastNode;
            redisMap.put("lastNode", lastNode);
        }

        @Override
        public void setAttribute(final String name, Object value) {
            super.setAttribute(name, value);
            redisMap.put("attributes", StringUtils.EMPTY);
        }

        @Override
        public void removeAttribute(final String name) {
            super.removeAttribute(name);
            redisMap.put("attributes", StringUtils.EMPTY);
        }

        public final Map<String, Object> getSessionAttributes() {
            final Map<String, Object> attrs = new LinkedHashMap<String, Object>();
            for (final String key : super.getNames()) {
                attrs.put(key, super.doGet(key));
            }
            return attrs;
        }

        @Override
        protected boolean access(final long time) {
            final boolean ret = super.access(time);
            firstAccess.remove();
            final int ttl = getMaxInactiveInterval();
            expiryTime = ttl <= 0 ? 0 : time / 1000 + ttl;
            // prepare serialization
            redisMap.put("lastAccessed", getLastAccessedTime());
            redisMap.put("accessed", getAccessed());
            redisMap.put("expiryTime", expiryTime);
            return ret;
        }

        @Override
        public void setMaxInactiveInterval(final int secs) {
            super.setMaxInactiveInterval(secs);
            // prepare serialization
            redisMap.put("maxIdle", secs);
        }

        @Override
        protected void cookieSet() {
            super.cookieSet();
            // prepare serialization
            redisMap.put("cookieSet", getCookieSetTime());
        }

        @Override
        protected void complete() {
            super.complete();
            if (!redisMap.isEmpty()
                && (redisMap.size() != 3
                || !redisMap.containsKey("lastAccessed")
                || !redisMap.containsKey("accessed")
                || !redisMap.containsKey("expiryTime")
                || getAccessed() - lastSaved >= saveInterval)) {
                try {
                    willPassivate();
                    storeSession(this);
                    didActivate();
                } catch (final Exception e) {
                    LOGGER.warn("[RedisSessionManager] complete - Problem persisting changed session data id=" + getId(), e);
                } finally {
                    redisMap.clear();
                }
            }
        }

        public boolean requestStarted() {
            final Boolean first = firstAccess.get();
            if (first != null && first.booleanValue()) {
                firstAccess.set(Boolean.FALSE);
            }
            
            return first;
        }
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
