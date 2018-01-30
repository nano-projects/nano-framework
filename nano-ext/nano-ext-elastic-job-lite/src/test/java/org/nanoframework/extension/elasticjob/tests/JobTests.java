/*
 * Copyright © 2015-2017 the original author or authors.
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
package org.nanoframework.extension.elasticjob.tests;

import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.extension.elasticjob.tests.job.TestDataflowJob;
import org.nanoframework.extension.elasticjob.tests.job.TestScriptJob;
import org.nanoframework.extension.elasticjob.tests.job.TestSimpleJob;

/**
 * @author wangtong
 * @since 1.4.11
 */
public class JobTests extends PluginLoaderInit {

    @Test
    public void jobTest() throws InterruptedException {
        Thread.sleep(1500);

        final Injector injector = Globals.get(Injector.class);

        TestSimpleJob testSimpleJob = injector.getInstance(TestSimpleJob.class);
        Assert.assertTrue(testSimpleJob.completed);

        TestDataflowJob testDataflowJob = injector.getInstance(TestDataflowJob.class);
        Assert.assertTrue(testDataflowJob.completed);

        TestScriptJob testScriptJob = injector.getInstance(TestScriptJob.class);
        Assert.assertTrue(testScriptJob.completed);
    }
}
