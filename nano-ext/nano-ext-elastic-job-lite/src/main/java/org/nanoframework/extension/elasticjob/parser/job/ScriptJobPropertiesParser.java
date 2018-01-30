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
package org.nanoframework.extension.elasticjob.parser.job;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.google.inject.Injector;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.extension.elasticjob.AbstractScriptJob;
import org.nanoframework.extension.elasticjob.parser.job.common.AbstractJobPropertiesParser;

/**
 * 脚本作业类型解析器.
 *
 * @author wangtong
 * @since 1.4.11
 */
public final class ScriptJobPropertiesParser extends AbstractJobPropertiesParser {
    private static final ScriptJobPropertiesParser INSTANCE = new ScriptJobPropertiesParser();

    private ScriptJobPropertiesParser() {
    }

    public static ScriptJobPropertiesParser getInstance() {
        return INSTANCE;
    }

    @Override
    protected JobTypeConfiguration getJobTypeConfiguration(final JobCoreConfiguration jobCoreConfiguration, final Class<?> clz) throws Exception {
        final Injector injector = Globals.get(Injector.class);
        final AbstractScriptJob scriptJob = (AbstractScriptJob) injector.getInstance(clz);
        return new ScriptJobConfiguration(jobCoreConfiguration, scriptJob.execute());
    }
}
