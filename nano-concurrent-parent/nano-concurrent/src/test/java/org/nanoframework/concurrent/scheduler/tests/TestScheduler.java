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
package org.nanoframework.concurrent.scheduler.tests;

import org.nanoframework.concurrent.scheduler.BaseScheduler;
import org.nanoframework.concurrent.scheduler.Scheduler;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
@Scheduler(parallel = 2, interval = 10, lazy = true, daemon = true)
public class TestScheduler extends BaseScheduler {

    @Override
    public void before() {

    }

    @Override
    public void execute() {

    }

    @Override
    public void after() {

    }

    @Override
    public void destroy() {

    }

}
