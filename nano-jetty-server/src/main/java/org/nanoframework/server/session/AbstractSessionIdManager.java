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

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.3.16
 */
public abstract class AbstractSessionIdManager extends org.eclipse.jetty.server.session.AbstractSessionIdManager {

    final static Logger LOGGER = Log.getLogger("org.nanoframework.server.session");
    // for a session id in the whole jetty, each webapp can have different sessions for the same id
    protected final ConcurrentMap<String, Object> sessions = Maps.newConcurrentMap();

    private final Server server;

    private long scavengerInterval = 60 * 1000; // 1min
    private ScheduledFuture<?> scavenger;
    private ScheduledExecutorService executorService;

    protected AbstractSessionIdManager(final Server server) {
        this.server = server;
    }

    public final void setScavengerInterval(final long scavengerInterval) {
        this.scavengerInterval = scavengerInterval;
    }

    @Override
    protected final void doStart() throws Exception {
        sessions.clear();
        if (scavenger != null) {
            scavenger.cancel(true);
            scavenger = null;
        }
        
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        
        if (scavengerInterval > 0) {
            executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
                final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName("RedisSessionIdManager-ScavengerThread");
                return thread;
            });
            
            scavenger = executorService.scheduleWithFixedDelay(() -> {
                if (!sessions.isEmpty()) {
                    try {
                        final List<String> expired = scavenge(Lists.newArrayList((sessions.keySet())));
                        for (final String clusterId : expired) {
                            sessions.remove(clusterId);
                        }
                        
                        forEachSessionManager(sessionManager -> sessionManager.expire(expired));
                    } catch (final Exception e) {
                        LOGGER.warn("Scavenger thread failure: " + e.getMessage(), e);
                    }
                }
            }, scavengerInterval, scavengerInterval, TimeUnit.MILLISECONDS);
        }
        
        super.doStart();
    }

    @Override
    protected final void doStop() throws Exception {
        sessions.clear();
        if (scavenger != null) {
            scavenger.cancel(true);
            scavenger = null;
        }
        
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        
        super.doStop();
    }

    @Override
    public final String getClusterId(final String nodeId) {
        final int dot = nodeId.lastIndexOf('.');
        return dot > 0 ? nodeId.substring(0, dot) : nodeId;
    }

    @Override
    public final String getNodeId(final String clusterId, final HttpServletRequest request) {
        if (_workerName != null) {
            return clusterId + '.' + _workerName;
        }
        
        return clusterId;
    }

    @Override
    public final boolean idInUse(final String id) {
        final String cid = getClusterId(id);
        return id != null && (sessions.containsKey(cid) || hasClusterId(cid));
    }

    @Override
    public final void addSession(final HttpSession session) {
        final String clusterId = getClusterId(session.getId());
        storeClusterId(clusterId);
        sessions.putIfAbsent(clusterId, Void.class);
    }

    @Override
    public final void removeSession(final HttpSession session) {
        final String clusterId = getClusterId(session.getId());
        if (sessions.containsKey(clusterId)) {
            sessions.remove(clusterId);
            deleteClusterId(clusterId);
        }
    }

    @Override
    public final void invalidateAll(final String clusterId) {
        if (sessions.containsKey(clusterId)) {
            sessions.remove(clusterId);
            deleteClusterId(clusterId);
            forEachSessionManager(sessionManager -> sessionManager.invalidateSession(clusterId));
        }
    }

    protected abstract void deleteClusterId(String clusterId);

    protected abstract void storeClusterId(String clusterId);

    protected abstract boolean hasClusterId(String clusterId);

    protected abstract List<String> scavenge(List<String> clusterIds);

    private void forEachSessionManager(final SessionManagerCallback callback) {
        Handler[] contexts = server.getChildHandlersByClass(ContextHandler.class);
        for (int i = 0; contexts != null && i < contexts.length; i++) {
            final SessionHandler sessionHandler = ((ContextHandler) contexts[i]).getChildHandlerByClass(SessionHandler.class);
            if (sessionHandler != null) {
                final SessionManager manager = sessionHandler.getSessionManager();
                if (manager != null && manager instanceof AbstractSessionManager) {
                    callback.execute((AbstractSessionManager) manager);
                }
            }
        }
    }

    /**
     * @author Mathieu Carbou (mathieu.carbou@gmail.com)
     */
    private static interface SessionManagerCallback {
        void execute(AbstractSessionManager sessionManager);
    }

}
