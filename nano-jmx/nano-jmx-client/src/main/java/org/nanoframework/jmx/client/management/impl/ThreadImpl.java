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
package org.nanoframework.jmx.client.management.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.management.AbstractMXBean;
import org.nanoframework.jmx.client.management.ThreadMXBean;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class ThreadImpl extends AbstractMXBean implements ThreadMXBean {
	public static final String OBJECT_NAME = ManagementFactory.THREAD_MXBEAN_NAME;
	public static final String THREAD_COUNT = "ThreadCount";
	public static final String PEAK_THREAD_COUNT = "PeakThreadCount";
	public static final String TOTAL_STARTED_THREAD_COUNT = "TotalStartedThreadCount";
	public static final String DAEMON_THREAD_COUNT = "DaemonThreadCount";
	public static final String ALL_THREAD_IDS = "AllThreadIds";
	public static final String THREAD_INFO = "ThreadInfo";
	public static final String GET_THREAD_INFO = "getThreadInfo";
	public static final String THREAD_CONTENTION_MONITORING_SUPPORTED = "ThreadContentionMonitoringSupported";
	public static final String THREAD_CONTENTION_MONITORING_ENABLED = "ThreadContentionMonitoringEnabled";
	public static final String CURRENT_THREAD_CPU_TIME = "CurrentThreadCpuTime";
	public static final String CURRENT_THREAD_USER_TIME = "CurrentThreadUserTime";
	public static final String THREAD_CPU_TIME = "ThreadCpuTime";
	public static final String GET_THREAD_CPU_TIME = "getThreadCpuTime";
	public static final String THREAD_USER_TIME = "ThreadUserTime";
	public static final String GET_THREAD_USER_TIME = "getThreadUserTime";
	public static final String THREAD_CPU_TIME_SUPPORTED = "ThreadCpuTimeSupported";
	public static final String CURRENT_THREAD_CPU_TIME_SUPPORTED = "CurrentThreadCpuTimeSupported";
	public static final String THREAD_CPU_TIME_ENABLED = "ThreadCpuTimeEnabled";
	public static final String FIND_MONITOR_DEADLOCKED_THREADS = "findMonitorDeadlockedThreads";
	public static final String RESET_PEAK_THREAD_COUNT = "resetPeakThreadCount";
	public static final String FIND_DEADLOCKED_THREADS = "findDeadlockedThreads";
	public static final String OBJECT_MONITOR_USAGE_SUPPORTED = "ObjectMonitorUsageSupported";
	public static final String SYNCHRONIZER_USAGE_SUPPORTED = "SynchronizerUsageSupported";
	public static final String DUMP_ALL_THREADS = "dumpAllThreads";
	
	public ThreadImpl(JmxClient client) throws MalformedObjectNameException {
        this(client, new ObjectName(OBJECT_NAME));
	}
	
	public ThreadImpl(JmxClient client, ObjectName objectName) {
		init(client, objectName);
	}
	
	@Override
	public ObjectName getObjectName() {
		return objectName;
	}

	@Override
	public int getThreadCount() {
		return getAttribute(THREAD_COUNT);
	}

	@Override
	public int getPeakThreadCount() {
		return getAttribute(PEAK_THREAD_COUNT);
	}

	@Override
	public long getTotalStartedThreadCount() {
		return getAttribute(TOTAL_STARTED_THREAD_COUNT);
	}

	@Override
	public int getDaemonThreadCount() {
		return getAttribute(DAEMON_THREAD_COUNT);
	}

	@Override
	public long[] getAllThreadIds() {
		return getAttribute(ALL_THREAD_IDS);
	}

	@Override
	public ThreadInfo getThreadInfo(long id) {
	    final String longName = long.class.getName();
		return ThreadInfo.from(invoke(GET_THREAD_INFO, new Object[] { id }, new String[] { longName }));
	}

	@Override
	public ThreadInfo[] getThreadInfo(long[] ids) {
	    final String longArrayName = long[].class.getName();
		return from(invoke(GET_THREAD_INFO, new Object[] { ids }, new String[] { longArrayName }));
	}

	@Override
	public ThreadInfo getThreadInfo(long id, int maxDepth) {
	    final String longName = long.class.getName();
	    final String intName = int.class.getName();
		return ThreadInfo.from(invoke(GET_THREAD_INFO, new Object[] { id, maxDepth }, new String[] { longName, intName }));
	}

	@Override
	public ThreadInfo[] getThreadInfo(long[] ids, int maxDepth) {
	    final String longArrayName = long[].class.getName();
	    final String intName = int.class.getName();
		return from(invoke(GET_THREAD_INFO, new Object[] { ids, maxDepth }, new String[] { longArrayName, intName }));
	}

	@Override
	public boolean isThreadContentionMonitoringSupported() {
		return getAttribute(THREAD_CONTENTION_MONITORING_SUPPORTED);
	}

	@Override
	public boolean isThreadContentionMonitoringEnabled() {
		return getAttribute(THREAD_CONTENTION_MONITORING_ENABLED);
	}

	@Override
	public void setThreadContentionMonitoringEnabled(boolean enable) {
		setAttribute(THREAD_CONTENTION_MONITORING_ENABLED, enable);
	}

	@Override
	public long getCurrentThreadCpuTime() {
		return getAttribute(CURRENT_THREAD_CPU_TIME);
	}

	@Override
	public long getCurrentThreadUserTime() {
		return getAttribute(CURRENT_THREAD_USER_TIME);
	}

	@Override
	public long getThreadCpuTime(long id) {
	    final String longName = long.class.getName();
		return invoke(GET_THREAD_CPU_TIME, new Object[] { id }, new String[] { longName });
	}

	@Override
	public long getThreadUserTime(long id) {
	    final String longName = long.class.getName();
		return invoke(GET_THREAD_USER_TIME, new Object[] { id }, new String[] { longName });
	}

	@Override
	public boolean isThreadCpuTimeSupported() {
		return getAttribute(THREAD_CPU_TIME_SUPPORTED);
	}

	@Override
	public boolean isCurrentThreadCpuTimeSupported() {
		return getAttribute(CURRENT_THREAD_CPU_TIME_SUPPORTED);
	}

	@Override
	public boolean isThreadCpuTimeEnabled() {
		return getAttribute(THREAD_CPU_TIME_ENABLED);
	}

	@Override
	public void setThreadCpuTimeEnabled(boolean enable) {
		setAttribute(THREAD_CPU_TIME_ENABLED, enable);
	}

	@Override
	public long[] findMonitorDeadlockedThreads() {
		return invoke(FIND_MONITOR_DEADLOCKED_THREADS);
	}

	@Override
	public void resetPeakThreadCount() {
		invoke(RESET_PEAK_THREAD_COUNT);
	}

	@Override
	public long[] findDeadlockedThreads() {
		return invoke(FIND_DEADLOCKED_THREADS);
	}

	@Override
	public boolean isObjectMonitorUsageSupported() {
		return getAttribute(OBJECT_MONITOR_USAGE_SUPPORTED);
	}

	@Override
	public boolean isSynchronizerUsageSupported() {
		return getAttribute(SYNCHRONIZER_USAGE_SUPPORTED);
	}

	@Override
	public ThreadInfo[] getThreadInfo(long[] ids, boolean lockedMonitors, boolean lockedSynchronizers) {
	    final String longArrayName = long[].class.getName();
	    final String booleanName = boolean.class.getName();
		return from(invoke(GET_THREAD_INFO, new Object[] { ids, lockedMonitors, lockedSynchronizers }, new String[] {  longArrayName, booleanName, booleanName }));
	}

	@Override
	public ThreadInfo[] dumpAllThreads(boolean lockedMonitors, boolean lockedSynchronizers) {
	    final String booleanName = boolean.class.getName();
		return from(invoke(DUMP_ALL_THREADS, new Object[] { lockedMonitors, lockedSynchronizers }, new String[] { booleanName, booleanName }));
	}
	
	private ThreadInfo[] from(CompositeData[] datas) {
		ThreadInfo[] threadInfos = new ThreadInfo[datas.length];
		int idx = 0;
		for(CompositeData data : datas) {
			threadInfos[idx] = ThreadInfo.from(data); 
			idx ++;
		}
		
		return threadInfos;
	}

}
