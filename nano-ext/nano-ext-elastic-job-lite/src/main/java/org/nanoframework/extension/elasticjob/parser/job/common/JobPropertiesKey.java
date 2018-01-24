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

/**
 * 基本作业的属性文件KEY.
 *
 * @author wangtong
 * @since 1.4.11
 */
public class JobPropertiesKey {

    public static final String ROOT = "ejob.conf.root";

    public static final String CONF = "ejob.conf.";

    public static final String REG_ROOT = "jobRegRoot";

    public static final String JOB_NAME = "jobName";

    public static final String CRON = "cron";

    public static final String SHARDING_TOTAL_COUNT = "shardingTotalCount";

    public static final String SHARDING_ITEM_PARAMETERS = "shardingItemParameters";

    public static final String JOB_PARAMETER = "jobParameter";

    public static final String FAILOVER = "failover";

    public static final String MISFIRE = "misfire";

    public static final String DESCRIPTION = "description";

    public static final String MONITOR_EXECUTION = "monitorExecution";

    public static final String MAX_TIME_DIFF_SECONDS = "maxTimeDiffSeconds";

    public static final String MONITOR_PORT = "monitorPort";

    public static final String RECONCILE_INTERVAL_MINUTES = "reconcileIntervalMinutes";

    public static final String DISABLED = "disabled";

    public static final String OVERWRITE = "overwrite";

    public static final String JOB_SHARDING_STRATEGY_CLASS = "jobShardingStrategyClass";

    public static final String JOB_EVENT_CONFIGURATION_CLASS = "jobEventConfigurationClass";

    public static final String JDBC_DATA_SOURCE_ID = "jdbcDataSourceId";

    public static final String JOB_EXCEPTION_HANDLER_CLASS = "jobExceptionHandlerClass";

    public static final String EXECUTOR_SERVICE_HANDLER_CLASS = "executorServiceHandlerClass";

    public static final String LISTENER_CLASS = "listenerClass";

    public static final String DISTRIBUTED_LISTENER_CLASS = "distributedListenerClass";
}
