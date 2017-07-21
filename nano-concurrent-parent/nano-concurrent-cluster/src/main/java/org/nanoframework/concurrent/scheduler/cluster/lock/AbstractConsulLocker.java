/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.concurrent.scheduler.cluster.lock;

import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.concurrent.scheduler.cluster.consts.ConsulSources;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public abstract class AbstractConsulLocker {

    @Inject
    @Named(ConsulSources.SESSION_SCHEDULER_CLUSTER)
    private SessionClient sessionClient;

    @Inject
    @Named(ConsulSources.KV_SCHEDULER_CLUSTER)
    private KeyValueClient kvClient;

    private final String key;
    private final String sessionName;
    private String sessionId;

    public AbstractConsulLocker(final String key, final String sessionName) {
        this.key = key;
        this.sessionName = sessionName;
    }

    public boolean lock() {
        if (StringUtils.isBlank(sessionId)) {
            sessionId = createSession(sessionName);
        }

        return kvClient.acquireLock(key, sessionId);
    }

    public boolean unlock() {
        if (StringUtils.isBlank(sessionId)) {
            return false;
        }

        try {
            return kvClient.releaseLock(key, sessionId);
        } finally {
            sessionId = null;
        }
    }

    public boolean unlock(long delay) {
        try {
            Thread.sleep(delay);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }

        return unlock();
    }

    private String createSession(final String sessionName) {
        final Session session = ImmutableSession.builder().name(sessionName).ttl("60s").build();
        return sessionClient.createSession(session).getId();
    }

    public String getSessionId() {
        return sessionId;
    }
}
