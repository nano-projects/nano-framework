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
package org.nanoframework.extension.concurrent.scheduler;

import java.util.concurrent.ThreadFactory;

/**
 * 
 * @author yanghe
 * @since 1.3
 */
public class SchedulerThreadFactory implements ThreadFactory {
    private BaseScheduler baseScheduler;

    @Override
    public Thread newThread(Runnable runnable) {
        if (baseScheduler == null) {
            Thread thread = new Thread(runnable);
            thread.setName("Scheduler-Thread-" + System.currentTimeMillis());
            return thread;
        }

        Thread thread = new Thread(baseScheduler);
        thread.setName(baseScheduler.getConfig().getName());
        thread.setDaemon(baseScheduler.getConfig().getDaemon());
        return thread;
    }

    public void setBaseScheduler(BaseScheduler baseScheduler) {
        this.baseScheduler = baseScheduler;
    }
}