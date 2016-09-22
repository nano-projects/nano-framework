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
package org.nanoframework.concurrent.scheduler.longwait;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.concurrent.scheduler.BaseScheduler;
import org.nanoframework.concurrent.scheduler.Scheduler;

/**
 *
 * @author yanghe
 * @since 1.3.16
 */
@Scheduler(parallel = 1, cron = "0 0 0 * * ?")
public class LongWaitScheduler extends BaseScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LongWaitScheduler.class);
    
    @Override
    public void before() {

    }

    @Override
    public void execute() {
        LOGGER.debug("Execute: {} at {}", getConfig().getId(), System.currentTimeMillis());
    }

    @Override
    public void after() {

    }

    @Override
    public void destroy() {
        LOGGER.debug("Closed: {}", getConfig().getId());
    }

}
