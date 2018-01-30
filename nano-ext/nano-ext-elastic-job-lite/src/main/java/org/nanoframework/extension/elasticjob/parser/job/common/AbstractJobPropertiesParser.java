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
package org.nanoframework.extension.elasticjob.parser.job.common;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.api.strategy.impl.AverageAllocationJobShardingStrategy;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.extension.elasticjob.exception.JobException;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Properties;

import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.CONF;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.CRON;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.DESCRIPTION;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.DISABLED;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.DISTRIBUTED_LISTENER_CLASS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.EXECUTOR_SERVICE_HANDLER_CLASS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.FAILOVER;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.JDBC_DATA_SOURCE_ID;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.JOB_EVENT_CONFIGURATION_CLASS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.JOB_EXCEPTION_HANDLER_CLASS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.JOB_NAME;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.JOB_PARAMETER;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.JOB_SHARDING_STRATEGY_CLASS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.LISTENER_CLASS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.MAX_TIME_DIFF_SECONDS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.MISFIRE;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.MONITOR_EXECUTION;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.MONITOR_PORT;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.OVERWRITE;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.RECONCILE_INTERVAL_MINUTES;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.SHARDING_ITEM_PARAMETERS;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.SHARDING_TOTAL_COUNT;

/**
 * 基本作业的属性文件解析器.
 *
 * @author wangtong
 * @since 1.4.11
 */
public abstract class AbstractJobPropertiesParser {

    protected abstract JobTypeConfiguration getJobTypeConfiguration(final JobCoreConfiguration jobCoreConfiguration, final Class<?> clz) throws Exception;

    public LiteJobConfiguration createLiteJobConfiguration(final Properties properties, final String confRoot, final Class<?> clz) {
        return createLiteJobConfiguration(properties, confRoot, createJobCoreConfiguration(properties, confRoot), clz);
    }

    private LiteJobConfiguration createLiteJobConfiguration(final Properties properties, final String confRoot, final JobCoreConfiguration jobCoreConfiguration, final Class<?> clz) {
        try {
            Constructor<LiteJobConfiguration> con = LiteJobConfiguration.class.getDeclaredConstructor(
                    JobTypeConfiguration.class, boolean.class, int.class, int.class, String.class, int.class, boolean.class, boolean.class);
            con.setAccessible(true);
            return con.newInstance(getJobTypeConfiguration(jobCoreConfiguration, clz),
                    Boolean.valueOf(properties.getProperty(CONF + confRoot + '.' + MONITOR_EXECUTION, "true")),
                    Integer.valueOf(properties.getProperty(CONF + confRoot + '.' + MAX_TIME_DIFF_SECONDS, "-1")),
                    Integer.valueOf(properties.getProperty(CONF + confRoot + '.' + MONITOR_PORT, "-1")),
                    properties.getProperty(CONF + confRoot + '.' + JOB_SHARDING_STRATEGY_CLASS,
                            AverageAllocationJobShardingStrategy.class.getName()),
                    Integer.valueOf(properties.getProperty(CONF + confRoot + '.' + RECONCILE_INTERVAL_MINUTES, "10")),
                    Boolean.valueOf(properties.getProperty(CONF + confRoot + '.' + DISABLED, "false")),
                    Boolean.valueOf(properties.getProperty(CONF + confRoot + '.' + OVERWRITE, "false")));
        } catch (Exception e) {
            throw new JobException(e.getMessage(), e);
        }
    }

    private JobCoreConfiguration createJobCoreConfiguration(final Properties properties, final String confRoot) {
        try {
            final String jobName = properties.getProperty(CONF + confRoot + '.' + JOB_NAME);
            final String cron = properties.getProperty(CONF + confRoot + '.' + CRON);
            final int shardingTotalCount = Integer.parseInt(properties.getProperty(CONF + confRoot + '.' + SHARDING_TOTAL_COUNT));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(cron), "cron can not be empty.");
            Preconditions.checkArgument(shardingTotalCount > 0, "shardingTotalCount should larger than zero.");
            Constructor<JobCoreConfiguration> con = JobCoreConfiguration.class.getDeclaredConstructor(
                    String.class, String.class, int.class, String.class, String.class, boolean.class, boolean.class, String.class, JobProperties.class);
            con.setAccessible(true);
            return con.newInstance(jobName, cron, shardingTotalCount,
                    properties.getProperty(CONF + confRoot + '.' + SHARDING_ITEM_PARAMETERS, ""),
                    properties.getProperty(CONF + confRoot + '.' + JOB_PARAMETER, ""),
                    Boolean.valueOf(properties.getProperty(CONF + confRoot + '.' + FAILOVER, "false")),
                    Boolean.valueOf(properties.getProperty(CONF + confRoot + '.' + MISFIRE, "true")),
                    properties.getProperty(CONF + confRoot + '.' + DESCRIPTION, ""),
                    createJobProperties(properties, confRoot));
        } catch (Exception e) {
            throw new JobException(e.getMessage(), e);
        }
    }

    private JobProperties createJobProperties(final Properties properties, final String confRoot) {
        final JobProperties jobProperties = new JobProperties();
        jobProperties.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(),
                properties.getProperty(CONF + confRoot + '.' + JOB_EXCEPTION_HANDLER_CLASS,
                        DefaultJobExceptionHandler.class.getName()));
        jobProperties.put(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(),
                properties.getProperty(CONF + confRoot + '.' + EXECUTOR_SERVICE_HANDLER_CLASS,
                        DefaultExecutorServiceHandler.class.getName()));
        return jobProperties;
    }

    public JobEventConfiguration createJobEventConfig(final Properties properties, final String confRoot) {
        try {
            if (properties.containsKey(CONF + confRoot + '.' + JOB_EVENT_CONFIGURATION_CLASS)) {
                final String eventClass = properties.getProperty(CONF + confRoot + '.' + JOB_EVENT_CONFIGURATION_CLASS);
                if (eventClass.equals(JobEventRdbConfiguration.class.getCanonicalName())) {
                    return new JobEventRdbConfiguration(GlobalJdbcManager.get(CONF + confRoot + '.' + JDBC_DATA_SOURCE_ID).getDataSource());
                }
                return (JobEventConfiguration) Globals.get(Injector.class).getInstance(Class.forName(eventClass));
            }
            return null;
        } catch (Exception e) {
            throw new JobException(e.getMessage(), e);
        }
    }

    public ElasticJobListener[] createJobListeners(final Properties properties, final String confRoot) {
        try {
            final List<ElasticJobListener> listeners = Lists.newArrayList();
            if (properties.containsKey(CONF + confRoot + '.' + LISTENER_CLASS)) {
                listeners.add((ElasticJobListener) Globals.get(Injector.class).getInstance(
                        Class.forName(properties.getProperty(CONF + confRoot + '.' + LISTENER_CLASS))));
            }
            if (properties.containsKey(CONF + confRoot + '.' + DISTRIBUTED_LISTENER_CLASS)) {
                listeners.add((ElasticJobListener) Globals.get(Injector.class).getInstance(
                        Class.forName(properties.getProperty(CONF + confRoot + '.' + DISTRIBUTED_LISTENER_CLASS))));
            }
            final ElasticJobListener[] result = new ElasticJobListener[listeners.size()];
            for (int i = 0; i < listeners.size(); i++) {
                result[i] = listeners.get(i);
            }
            return result;
        } catch (Exception e) {
            throw new JobException(e.getMessage(), e);
        }
    }
}
