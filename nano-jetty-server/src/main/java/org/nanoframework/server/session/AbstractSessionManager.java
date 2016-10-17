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
package org.nanoframework.server.session;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.server.session.AbstractSession;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.3.16
 */
public abstract class AbstractSessionManager extends org.eclipse.jetty.server.session.AbstractSessionManager {
    private final static Logger LOGGER = Log.getLogger("org.nanoframework.server.session");
    private static final Field COOKIE_SET;

    static {
        try {
            COOKIE_SET = AbstractSession.class.getDeclaredField("_cookieSet");
            COOKIE_SET.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private final ConcurrentMap<String, Session> sessions = Maps.newConcurrentMap();

    @Override
    public void doStart() throws Exception {
        sessions.clear();
        super.doStart();
    }

    @Override
    public void doStop() throws Exception {
        sessions.clear();
        super.doStop();
    }

    @Override
    protected final void addSession(final AbstractSession session) {
        if (isRunning()) {
            final String clusterId = getClusterId(session);
            sessions.put(clusterId, (Session) session);
            session.willPassivate();
            storeSession((Session) session);
            session.didActivate();
        }
    }

    @Override
    public final void removeSession(final AbstractSession session, boolean invalidate) {
        final String clusterId = getClusterId(session);
        final boolean removed = removeSession(clusterId);
        if (removed) {
            _sessionsStats.decrement();
            _sessionTimeStats.set(Math.round((System.currentTimeMillis() - session.getCreationTime()) / 1000.0));
            _sessionIdManager.removeSession(session);
            if (invalidate) {
                _sessionIdManager.invalidateAll(session.getClusterId());
            }

            if (invalidate && _sessionListeners != null) {
                final HttpSessionEvent event = new HttpSessionEvent(session);
                for (int i = LazyList.size(_sessionListeners); i-- > 0;) {
                    ((HttpSessionListener) LazyList.get(_sessionListeners, i)).sessionDestroyed(event);
                }
            }

            if (!invalidate) {
                session.willPassivate();
            }
        }
    }

    @Override
    protected final boolean removeSession(final String clusterId) {
        final Session session = sessions.remove(clusterId);
        try {
            if (session != null) {
                deleteSession(session);
            }
        } catch (final Exception e) {
            LOGGER.warn("Problem deleting session id=" + clusterId, e);
        }

        return session != null;
    }

    @Override
    public final Session getSession(final String clusterId) {
        final Session current = sessions.get(clusterId);
        final Session loaded = loadSession(clusterId, current);
        if (loaded != null) {
            sessions.put(clusterId, loaded);
            if (current != loaded) {
                loaded.didActivate();
            }
        }
        
        return loaded;
    }

    @Override
    @Deprecated
    public final Map<String, Session> getSessionMap() {
        return Collections.unmodifiableMap(sessions);
    }

    @Override
    protected final void invalidateSessions() {
        //Do nothing - we don't want to remove and
        //invalidate all the sessions because this
        //method is called from doStop(), and just
        //because this context is stopping does not
        //mean that we should remove the session from
        //any other nodes
    }

    public final void invalidateSession(final String clusterId) {
        final AbstractSession session = sessions.get(clusterId);
        if (session != null) {
            session.invalidate();
        }
    }

    public final void expire(final List<String> expired) {
        if (isStopping() || isStopped()) {
            return;
        }
        
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            for (final String expiredClusterId : expired) {
                LOGGER.debug("[SessionManagerSkeleton] Expiring session id={}", expiredClusterId);
                final Session session = sessions.get(expiredClusterId);
                if (session != null) {
                    session.timeout();
                }
            }
        } catch (final Throwable t) {
            if (t instanceof ThreadDeath) {
                throw ((ThreadDeath) t);
            } else {
                LOGGER.warn("Problem expiring sessions", t);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    public final void setSessionPath(final String path) {
        getSessionCookieConfig().setPath(path);
    }

    public final void setMaxCookieAge(final int seconds) {
        getSessionCookieConfig().setMaxAge(seconds);
    }

    public void setSessionDomain(final String domain) {
        setDomain(domain);
    }

    public void setDomain(final String domain) {
        getSessionCookieConfig().setDomain(domain);
    }

    protected final String getVirtualHost() {
        final String vhost = "0.0.0.0";
        if (_context == null) {
            return vhost;
        }
        
        final String[] vhosts = _context.getContextHandler().getVirtualHosts();
        if (vhosts == null || vhosts.length == 0 || vhosts[0] == null) {
            return vhost;
        }
        
        return vhosts[0];
    }

    protected final String getCanonicalizedContext() {
        if (_context.getContextPath() == null) {
            return "";
        }
        
        return _context.getContextPath().replace('/', '_').replace('.', '_').replace('\\', '_');
    }

    protected abstract void storeSession(Session session);

    protected abstract void deleteSession(Session session);

    protected abstract Session loadSession(String clusterId, Session current);

    public abstract class Session extends AbstractSession {

        public Session(final HttpServletRequest request) {
            super(AbstractSessionManager.this, request);
            super.setMaxInactiveInterval(AbstractSessionManager.this._dftMaxIdleSecs > 0 ? AbstractSessionManager.this._dftMaxIdleSecs : -1);
        }

        public Session(final long created, final long accessed, final String clusterId) {
            super(AbstractSessionManager.this, created, accessed, clusterId);
        }

        @Override
        public void timeout() throws IllegalStateException {
            LOGGER.debug("Timing out session id={}", getClusterId());
            super.timeout();
        }

        protected void setCookieSetTime(final long time) {
            try {
                COOKIE_SET.set(this, time);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

    }
}
