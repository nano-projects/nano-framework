/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
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
 * @date 2016年3月25日 下午3:44:51
 */
public final class ApplicationContext {
	/** 应用标识，即 WebContext */
	public static final String CONTEXT_ROOT = "context.root";
	public static final String CONTEXT_FILTER = "context.filter";
	@Deprecated
	public static final String CONTEXT_SECURITY_FILTER = "context.security.filter";
	public static final String CONTEXT_SUFFIX_FILTER = "context.suffix.filter";
	
	/** 组件扫描属性 */
	public static final String COMPONENT_BASE_PACKAGE = "context.component-scan.base-package";
	
	/**
	 * Quartz属性列表
	 * @author yanghe
	 * @date 2016年3月25日 下午4:18:22
	 */
	@Deprecated
	public static final class Quartz {
		/** 任务扫描属性 */
		public static final String BASE_PACKAGE = "context.quartz-scan.base-package";
		/** 任务自动运行标识属性 */
		public static final String AUTO_RUN = "context.quartz.run.auto";
		/** 导入任务列表属性 */
		public static final String INCLUDES = "context.quartz.group.includes";
		/** 过滤任务列表属性 */
		public static final String EXCLUSIONS = "context.quartz.group.exclusions";
		/** 停止超时时间属性 */
		public static final String SHUTDOWN_TIMEOUT = "context.quartz.shutdown.timeout";
		/** ETCD同步属性 */
		public static final String ETCD_ENABLE = "context.quartz.etcd.enable";
		/** ETCD服务地址属性 */
		public static final String ETCD_URI = "context.quartz.etcd.uri";
		/** ETCD用户名属性 */
		public static final String ETCD_USER = "context.quartz.etcd.username";
		/** ETCD用户校验码属性 */
		public static final String ETCD_CLIENT_ID = "context.quartz.etcd.clientid";
		/** 应用名属性 */
		public static final String ETCD_APP_NAME = "context.quartz.app.name";
		/** ETCD失败最大重试次数属性 */
		public static final String ETCD_MAX_RETRY_COUNT = "context.quartz.etcd.max.retry.count";
		/** JMX同步ETCD速率属性 */
		public static final String SCHEDULER_APP_JMX_RATE = "context.quartz.app.jmx.rate";
		/** JMX同步ETCD属性 */
		public static final String SCHEDULER_APP_JMX_ENABLE = "context.quartz.app.jmx.enable";
	}
	
	/**
	 * Scheduler属性列表
	 * @author yanghe
	 * @date 2016年3月25日 下午4:22:53
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
		/**
		 * @see org.nanoframework.extension.etcd.etcd4j.transport.EtcdNettyConfig#CONNECT_TIMEOUT
		 */
		public static final String ETCD_CONNECT_TIMEOUT = "context.scheduler.etcd.connect.timeout";
		/**
		 * @see org.nanoframework.extension.etcd.etcd4j.transport.EtcdNettyConfig#FRAME_SIZE
		 */
		public static final String ETCD_FRAME_SIZE = "context.scheduler.etcd.max.frame.size";
	}
	
	/** WebSocket服务扫描属性 */
	public static final String WEBSOCKET_BASE_PACKAGE = "context.websocket-scan.base-package";
	
}
