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
import org.nanoframework.jmx.client.management.ClassLoadingMXBean;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class ClassLoadingImpl extends AbstractMXBean implements ClassLoadingMXBean {
	public static final String OBJECT_NAME = ManagementFactory.CLASS_LOADING_MXBEAN_NAME;
	public static final String TOTAL_LOADED_CLASS_COUNT = "TotalLoadedClassCount";
	public static final String LOADED_CLASS_COUNT = "LoadedClassCount";
	public static final String UNLOADED_CLASS_COUNT = "UnloadedClassCount";
	public static final String VERBOSE = "Verbose";
	
	public ClassLoadingImpl(JmxClient client) {
		init(client, OBJECT_NAME);
	}
	
	public ClassLoadingImpl(JmxClient client, ObjectName objectName) {
		init(client, objectName);
	}
	
	@Override
	public ObjectName getObjectName() {
		return objectName;
	}

	@Override
	public long getTotalLoadedClassCount() {
		return getAttribute(TOTAL_LOADED_CLASS_COUNT);
	}

	@Override
	public int getLoadedClassCount() {
		return getAttribute(LOADED_CLASS_COUNT);
	}

	@Override
	public long getUnloadedClassCount() {
		return getAttribute(UNLOADED_CLASS_COUNT);
	}

	@Override
	public boolean isVerbose() {
		return getAttribute(VERBOSE);
	}

	@Override
	public void setVerbose(boolean value) {
		setAttribute(VERBOSE, value);
	}

}
