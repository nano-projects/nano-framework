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
package org.nanoframework.concurrent.scheduler.cluster.config;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.SEPARATOR_CHAR;
import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.ELECTION;
import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.VOTERS;
import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.VOTES;
import static org.nanoframework.concurrent.scheduler.cluster.storage.listener.SchedulerListener.LEADER;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.concurrent.scheduler.cluster.consts.ConsulSources;
import org.nanoframework.concurrent.scheduler.cluster.lock.ElectionLocker;
import org.nanoframework.concurrent.scheduler.cluster.storage.ElectionScheduler;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.orbitz.consul.KeyValueClient;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
@Singleton
public class Election extends BaseEntity {
    private static final long serialVersionUID = 3325800338729604902L;
    private static final long delay = 5000;

    @Inject
    private Configure configure;

    @Inject
    private ElectionScheduler scheduler;

    @Inject
    @Named(ConsulSources.KV_SCHEDULER_CLUSTER)
    private KeyValueClient kvClient;

    @Inject
    private SecureRandom random;

    @Inject
    private ElectionLocker locker;

    private String initiator;
    private final Map<String, AtomicInteger> votes = Maps.newHashMap();
    private final Set<String> voters = Sets.newHashSet();
    private boolean pushed;

    public void start() {
        scheduler.start();
    }

    public void end() {
        locker.unlock(delay);
    }

    public boolean isInitiator() {
        if (StringUtils.isBlank(initiator)) {
            return false;
        }

        final Node node = configure.getCurrentNode();
        if (node != null && StringUtils.equals(node.getId(), initiator)) {
            return true;
        }

        return false;
    }

    public void setInitiator(final String initiator) {
        this.initiator = initiator;
        if (StringUtils.isNotBlank(initiator)) {
            final String clusterId = configure.getClusterId();
            final String curNodeId = configure.getCurrentNode().getId();
            kvClient.putValue(clusterId + SEPARATOR_CHAR + VOTERS + SEPARATOR_CHAR + curNodeId + SEPARATOR_CHAR);
            start();
        }
    }

    public void vote() {
        if (!CollectionUtils.isEmpty(voters)) {
            final int voterSize = this.voters.size();
            final String[] voters = this.voters.toArray(new String[voterSize]);
            final int randomVoter = random.nextInt(voterSize);
            final String vote = voters[randomVoter];

            final String clusterId = configure.getClusterId();
            final String curNodeId = configure.getCurrentNode().getId();
            kvClient.putValue(clusterId + SEPARATOR_CHAR + VOTES + SEPARATOR_CHAR + curNodeId, vote);
        }
    }

    public void addVote(final String voter) {
        if (votes.containsKey(voter)) {
            votes.get(voter).incrementAndGet();
        } else {
            votes.put(voter, new AtomicInteger(1));
        }
    }

    public void clearVotes() {
        votes.clear();
    }

    public void addVoter(final String voter) {
        voters.add(voter);
    }

    public boolean hasVoter(final String voter) {
        return voters.contains(voter);
    }

    public void clearVoters() {
        voters.clear();
    }

    public String calc() {
        String leader = EMPTY;
        int maxVote = 0;
        for (final Entry<String, AtomicInteger> entry : votes.entrySet()) {
            final String voter = entry.getKey();
            final AtomicInteger vote = entry.getValue();
            final int v = vote.get();
            if (v > maxVote) {
                maxVote = v;
                leader = voter;
            }
        }

        return leader;
    }

    public void newLeader(final String leader) {
        final String clusterId = configure.getClusterId();
        kvClient.putValue(clusterId + SEPARATOR_CHAR + LEADER, leader);
    }

    public void clear() {
        final String clusterId = configure.getClusterId();
        kvClient.deleteKeys(clusterId + SEPARATOR_CHAR + VOTES);
        kvClient.deleteKeys(clusterId + SEPARATOR_CHAR + VOTERS);
        kvClient.deleteKeys(clusterId + SEPARATOR_CHAR + ELECTION);
        pushed = false;
    }

    public void push() {
        if (!pushed) {
            final String clusterId = configure.getClusterId();
            final String nodeId = configure.getCurrentNode().getId();
            kvClient.putValue(clusterId + SEPARATOR_CHAR + ELECTION, nodeId);
            pushed = true;
        }
    }
}
