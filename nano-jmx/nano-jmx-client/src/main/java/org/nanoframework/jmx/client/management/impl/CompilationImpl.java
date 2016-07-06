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

import javax.management.ObjectName;

import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.management.AbstractMXBean;
import org.nanoframework.jmx.client.management.CompilationMXBean;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class CompilationImpl extends AbstractMXBean implements CompilationMXBean {
	public static final String OBJECT_NAME = ManagementFactory.COMPILATION_MXBEAN_NAME;
	public static final String NAME = "Name";
	public static final String COMPILATION_TIME_MONITORING_SUPPORTED = "CompilationTimeMonitoringSupported";
	public static final String TOTAL_COMPILATION_TIME = "TotalCompilationTime";
	
	public CompilationImpl(JmxClient client) {
		init(client, OBJECT_NAME);
	}
	
	public CompilationImpl(JmxClient client, ObjectName objectName) {
		init(client, objectName);
	}
	
	@Override
	public ObjectName getObjectName() {
		return objectName;
	}

	@Override
	public String getName() {
		return getAttribute(NAME);
	}

	@Override
	public boolean isCompilationTimeMonitoringSupported() {
		return getAttribute(COMPILATION_TIME_MONITORING_SUPPORTED);
	}

	@Override
	public long getTotalCompilationTime() {
		return getAttribute(TOTAL_COMPILATION_TIME);
	}

}
