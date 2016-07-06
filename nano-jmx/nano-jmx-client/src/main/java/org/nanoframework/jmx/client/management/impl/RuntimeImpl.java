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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.management.AbstractMXBean;
import org.nanoframework.jmx.client.management.RuntimeMXBean;

/**
 * 远程系统运行时信息监控
 * 
 * @author yanghe
 * @since 1.1
 */
public class RuntimeImpl extends AbstractMXBean implements RuntimeMXBean {
	public static final String OBJECT_NAME = ManagementFactory.RUNTIME_MXBEAN_NAME;
	public static final String NAME = "Name";
	public static final String VM_NAME = "VmName";
	public static final String VM_VENDOR = "VmVendor";
	public static final String VM_VERSION = "VmVersion";
	public static final String SPEC_NAME = "SpecName";
	public static final String SPEC_VENDOR = "SpecVendor";
	public static final String SPEC_VERSION = "SpecVersion";
	public static final String MANAGEMENT_SPEC_VERSION = "ManagementSpecVersion";
	public static final String CLASS_PATH = "ClassPath";
	public static final String LIBRARY_PATH = "LibraryPath";
	public static final String BOOT_CLASS_PATH_SUPPORTED = "BootClassPathSupported";
	public static final String BOOT_CLASS_PATH = "BootClassPath";
	public static final String INPUT_ARGUMENTS = "InputArguments";
	public static final String UP_TIME = "Uptime";
	public static final String START_TIME = "StartTime";
	public static final String SYSTEM_PROPERTIES = "SystemProperties";
	
	public RuntimeImpl(JmxClient client) {
		init(client, OBJECT_NAME);
	}
	
	public RuntimeImpl(JmxClient client, ObjectName objectName) {
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
	public String getVmName() {
		return getAttribute(VM_NAME);
	}

	@Override
	public String getVmVendor() {
		return getAttribute(VM_VENDOR);
	}

	@Override
	public String getVmVersion() {
		return getAttribute(VM_VERSION);
	}

	@Override
	public String getSpecName() {
		return getAttribute(SPEC_NAME);
	}

	@Override
	public String getSpecVendor() {
		return getAttribute(SPEC_VENDOR);
	}

	@Override
	public String getSpecVersion() {
		return getAttribute(SPEC_VERSION);
	}

	@Override
	public String getManagementSpecVersion() {
		return getAttribute(MANAGEMENT_SPEC_VERSION);
	}

	@Override
	public String getClassPath() {
		return getAttribute(CLASS_PATH);
	}

	@Override
	public String getLibraryPath() {
		return getAttribute(LIBRARY_PATH);
	}

	@Override
	public boolean isBootClassPathSupported() {
		return getAttribute(BOOT_CLASS_PATH_SUPPORTED);
	}

	@Override
	public String getBootClassPath() {
		return getAttribute(BOOT_CLASS_PATH);
	}

	@Override
	public List<String> getInputArguments() {
		return Arrays.asList((String[]) getAttribute(INPUT_ARGUMENTS));
	}

	@Override
	public long getUptime() {
		return getAttribute(UP_TIME);
	}

	@Override
	public long getStartTime() {
		return getAttribute(START_TIME);
	}

	@Override
	public Map<String, String> getSystemProperties() {
		return getAttribute(SYSTEM_PROPERTIES);
	}
}