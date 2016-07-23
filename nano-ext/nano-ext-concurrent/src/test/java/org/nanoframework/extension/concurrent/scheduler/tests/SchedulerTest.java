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
package org.nanoframework.extension.concurrent.scheduler.tests;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.extension.concurrent.scheduler.BaseScheduler;
import org.nanoframework.extension.concurrent.scheduler.PluginLoaderInit;
import org.nanoframework.extension.concurrent.scheduler.SchedulerConfig;
import org.nanoframework.extension.concurrent.scheduler.SchedulerFactory;
import org.nanoframework.extension.concurrent.scheduler.TestScheduler;
import org.nanoframework.extension.concurrent.scheduler.TestScheduler2;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class SchedulerTest extends PluginLoaderInit {

    @Test
    public void schedulerTest() throws InterruptedException {
        // wait scheduler to running status
        Thread.sleep(1500);
        
        final SchedulerFactory factory = SchedulerFactory.getInstance();
        Assert.assertEquals(factory.getStartedSchedulerSize(), 4);
        final BaseScheduler scheduler = factory.getStartedScheduler().iterator().next();
        final SchedulerConfig conf = scheduler.getConfig();
        
        Assert.assertTrue(scheduler instanceof TestScheduler || scheduler instanceof TestScheduler2);
        Assert.assertFalse(scheduler.isClose());
        Assert.assertFalse(scheduler.isClosed());
        
        factory.close(conf.getId());
        
        Assert.assertTrue(scheduler.isClose());
        
        // wait scheduler to closed status
        Thread.sleep(1000);
        
        Assert.assertTrue(scheduler.isClosed());
        Assert.assertFalse(scheduler.isRemove());
        
        factory.removeScheduler(scheduler);
        
        // If the list is only one scheduler, do not delete the scheduler
        Assert.assertTrue(scheduler.isRemove());
        
        factory.append(conf.getGroup(), 1, true);
        factory.startAll();
        Thread.sleep(1000);
        
        factory.closeGroup(conf.getGroup());
        
        // wait scheduler group to closed status
        Thread.sleep(5000);
        
        Assert.assertEquals(factory.getStartedSchedulerSize(), 2);
        
        factory.startAll();
        
        Thread.sleep(2000);
        
        Assert.assertEquals(factory.getStartedSchedulerSize(), 4);
        
        factory.removeGroup(conf.getGroup());
        
        Assert.assertEquals(factory.getStartedSchedulerSize(), 2);
        Assert.assertEquals(factory.getStoppedSchedulerSize(), 0);
        
        factory.append(conf.getGroup(), 1, true);
        factory.startAll();
        Thread.sleep(1000);
        
        factory.closeAll();
        
        // wait scheduler to closed status
        Thread.sleep(5000);
        
        Assert.assertEquals(factory.getStartedSchedulerSize(), 0);
        
        factory.startGroup(conf.getGroup());
        // wait scheduler to running status
        Thread.sleep(1500);
        
        Assert.assertEquals(factory.getStartedSchedulerSize(), 2);
    }
    
}
