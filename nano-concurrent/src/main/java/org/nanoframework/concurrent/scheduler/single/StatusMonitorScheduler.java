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
package org.nanoframework.concurrent.scheduler.single;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.nanoframework.concurrent.scheduler.BaseScheduler;
import org.nanoframework.concurrent.scheduler.SchedulerConfig;
import org.nanoframework.concurrent.scheduler.SchedulerThreadFactory;
import org.nanoframework.concurrent.scheduler.exception.SchedulerException;

import com.google.common.collect.Maps;

/**
 * 
 *
 * @author yanghe
 * @since 1.4.8
 */
public class StatusMonitorScheduler extends BaseScheduler {
    private final ConcurrentMap<String, BaseScheduler> closed;
    private final SchedulerThreadFactory threadFactory;
    private final ConcurrentMap<String, BaseScheduler> stoppingScheduler;
    private final ConcurrentMap<String, BaseScheduler> stoppedScheduler;

    public StatusMonitorScheduler(final SchedulerThreadFactory threadFactory, final ConcurrentMap<String, BaseScheduler> stoppingScheduler,
            final ConcurrentMap<String, BaseScheduler> stoppedScheduler) {
        this.threadFactory = threadFactory;
        this.stoppingScheduler = stoppingScheduler;
        this.stoppedScheduler = stoppedScheduler;
        init();
        closed = Maps.newConcurrentMap();
    }

    protected void init() {
        final SchedulerConfig conf = new SchedulerConfig();
        conf.setId("StatusMonitorScheduler-0");
        conf.setName("StatusMonitorScheduler");
        conf.setGroup("StatusMonitorScheduler");
        threadFactory.setBaseScheduler(this);
        conf.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
        conf.setInterval(50L);
        conf.setTotal(1);
        conf.setDaemon(Boolean.TRUE);
        setConfig(conf);
        setClose(false);
    }

    @Override
    public void before() throws SchedulerException {
        stoppingScheduler.forEach((id, scheduler) -> {
            if (scheduler.isClosed()) {
                closed.put(id, scheduler);
            }
        });
    }

    @Override
    public void execute() throws SchedulerException {
        closed.forEach((id, scheduler) -> {
            if (!scheduler.isRemove()) {
                stoppedScheduler.put(id, scheduler);
            }

            stoppingScheduler.remove(id, scheduler);
        });
    }

    @Override
    public void after() throws SchedulerException {
        closed.clear();
    }

    @Override
    public void destroy() throws SchedulerException {

    }

}