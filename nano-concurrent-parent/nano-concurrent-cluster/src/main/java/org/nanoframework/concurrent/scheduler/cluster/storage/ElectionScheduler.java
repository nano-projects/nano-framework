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
package org.nanoframework.concurrent.scheduler.cluster.storage;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.concurrent.scheduler.BaseScheduler;
import org.nanoframework.concurrent.scheduler.SchedulerConfig;
import org.nanoframework.concurrent.scheduler.SchedulerFactory;
import org.nanoframework.concurrent.scheduler.cluster.config.Election;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
@Singleton
public class ElectionScheduler extends BaseScheduler {
    private static final long VOTE_WAIT_TIME = 5000;

    private final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    @Inject
    private Election election;

    public ElectionScheduler() {
        final SchedulerConfig conf = new SchedulerConfig();
        final String group = ElectionScheduler.class.getSimpleName();
        conf.setGroup(group);
        conf.setId(group + '-' + getIndex(group));
        conf.setName(SchedulerFactory.DEFAULT_SCHEDULER_NAME_PREFIX + conf.getId());
        conf.setService(service);
        conf.setDaemon(Boolean.TRUE);
        conf.setRunNumberOfTimes(1);
        setConfig(conf);
    }

    public void start() {
        service.execute(this);
    }

    @Override
    public void before() {
        thisWait(VOTE_WAIT_TIME);
        election.vote();
    }

    @Override
    public void execute() {
        if (election.isInitiator()) {
            thisWait(VOTE_WAIT_TIME);
            final String leader = election.calc();
            if (StringUtils.isNotBlank(leader) && election.hasVoter(leader)) {
                election.newLeader(leader);
            }

            election.clear();
        }

        election.end();
    }

    @Override
    public void after() {

    }

    @Override
    public void destroy() {

    }

}
