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
package org.nanoframework.core.context;

/**
 * @author yanghe
 * @since 1.0
 */
public final class ApplicationContext {
    public static final String CONTEXT = "context";
    public static final String PLUGIN_LOADER = "pluginLoader";
    public static final String MAIN_CONTEXT = "/context.properties";
    
    /**
     * NanoFramework版本号.
     * @since 1.3.14
     */
    public static final String FRAMEWORK_VERSION = "1.3.16";
    
    /**
     * 项目版本号属性.
     * @since 1.3.14
     */
    public static final String VERSION = "context.version";
    /**
     * 项目运行模式属性.
     * @since 1.3.14
     */
    public static final String MODE = "context.mode";
    
    /** 应用标识，即 WebContext */
    public static final String CONTEXT_ROOT = "context.root";
    @Deprecated
    public static final String CONTEXT_FILTER = "context.filter";
    @Deprecated
    public static final String CONTEXT_SECURITY_FILTER = "context.security.filter";
    @Deprecated
    public static final String CONTEXT_SUFFIX_FILTER = "context.suffix.filter";

    /** 组件扫描属性 */
    public static final String COMPONENT_BASE_PACKAGE = "context.component-scan.base-package";
    
    /**
     * Redis Configure to context.properties.
     * @since 1.3.16
     */
    public static final String CONTEXT_REDIS = "context.redis";
    
    /**
     *
     * @author yanghe
     * @since 1.3.16 
     */
    public static final class JettyRedisSession {
        /** 默认集群节点名称. */
        public static final String DEFAULT_JETTY_CLUSTER_WORKER_NAME = "_";
        /** 默认过期Session定期扫描任务时间间隔. */
        public static final String DEFAULT_SCAVENGER_INTERVAL = "30000";
        /** 默认存储Session时间间隔. */
        public static final String DEFAULT_SESSION_SAVE_INTERVAL = "20000";
        
        /** 集群模式下使用的RedisType名称属性. */
        public static final String JETTY_CLUSTER = "context.jetty.cluster";
        /** 集群节点名称属性. */
        public static final String JETTY_CLUSTER_WORKER_NAME = "context.jetty.cluster.worker.name";
        /** 过期Session定期扫描任务时间间隔属性. */
        public static final String JETTY_SESSION_SCAVENGER_INTERVAL = "context.jetty.scavenger.interval";
        /** 存储Session时间间隔. */
        public static final String JETTY_SESSION_SAVE_INTERVAL = "context.jetty.session.save.interval";
    }

    /**
     * Scheduler属性列表
     * @author yanghe
     * @since 1.3
     */
    public static final class Scheduler {
        /** 任务扫描属性 */
        public static final String BASE_PACKAGE = "context.scheduler-scan.base-package";
        /** 任务自动运行标识属性 */
        public static final String AUTO_RUN = "context.scheduler.run.auto";
        /** 导入任务列表属性 */
        public static final String INCLUDES = "context.scheduler.group.includes";
        /** 过滤任务列表属性 */
        public static final String EXCLUSIONS = "context.scheduler.group.exclusions";
        /** 停止超时时间属性 */
        public static final String SHUTDOWN_TIMEOUT = "context.scheduler.shutdown.timeout";
        /** ETCD同步属性 */
        public static final String ETCD_ENABLE = "context.scheduler.etcd.enable";
        /** ETCD服务地址属性 */
        public static final String ETCD_URI = "context.scheduler.etcd.uri";
        /** ETCD用户名属性 */
        public static final String ETCD_USER = "context.scheduler.etcd.username";
        /** ETCD用户校验码属性 */
        public static final String ETCD_CLIENT_ID = "context.scheduler.etcd.clientid";
        /** 应用名属性 */
        public static final String ETCD_APP_NAME = "context.scheduler.app.name";
        /** ETCD失败最大重试次数属性 */
        public static final String ETCD_MAX_RETRY_COUNT = "context.scheduler.etcd.max.retry.count";
        /** 任务执行统计同步ETCD属性 */
        public static final String ETCD_SCHEDULER_ANALYSIS = "context.scheduler.analysis.enable";
        /** JMX同步ETCD速率属性 */
        public static final String SCHEDULER_APP_JMX_RATE = "context.scheduler.app.jmx.rate";
        /** JMX同步ETCD属性 */
        public static final String SCHEDULER_APP_JMX_ENABLE = "context.scheduler.app.jmx.enable";
        /** ETCD元素过期时间 */
        public static final String ETCD_KEY_TTL = "context.scheduler.etcd.key.ttl";

        public static final String ETCD_CONNECT_TIMEOUT = "context.scheduler.etcd.connect.timeout";
        
        public static final String ETCD_FRAME_SIZE = "context.scheduler.etcd.max.frame.size";
    }

    /** WebSocket服务扫描属性 */
    public static final String WEBSOCKET_BASE_PACKAGE = "context.websocket-scan.base-package";

}
