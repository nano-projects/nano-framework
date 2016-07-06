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
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.management.ObjectName;

import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.management.AbstractMXBean;
import org.nanoframework.jmx.client.management.OperatingSystemMXBean;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class OperatingSystemImpl extends AbstractMXBean implements OperatingSystemMXBean {
	public static final String OBJECT_NAME = ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME;
	public static final String NAME = "Name";
	public static final String ARCH = "Arch";
	public static final String VERSION = "Version";
	public static final String AVAILABLE_PROCESSORS = "AvailableProcessors";
	public static final String SYSTEM_LOAD_AVERAGE = "SystemLoadAverage";
	public static final String COMMITTED_VIRTUAL_MEMORYSIZE = "CommittedVirtualMemorySize";
	public static final String FREE_PHYSICAL_MEMORY_SIZE = "FreePhysicalMemorySize";
	public static final String FREE_SWAP_SPACE_SIZE = "FreeSwapSpaceSize";
	public static final String PROCESS_CPU_LOAD = "ProcessCpuLoad";
	public static final String PROCESS_CPU_TIME = "ProcessCpuTime";
	public static final String SYSTEM_CPU_LOAD = "SystemCpuLoad";
	public static final String TOTAL_PHYSICAL_MEMORY_SIZE = "TotalPhysicalMemorySize";
	public static final String TOTAL_SWAP_SPACE_SIZE = "TotalSwapSpaceSize";
	
	public OperatingSystemImpl(JmxClient client) {
		init(client, OBJECT_NAME);
	}
	
	public OperatingSystemImpl(JmxClient client, ObjectName objectName) {
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
	public String getArch() {
		return getAttribute(ARCH);
	}

	@Override
	public String getVersion() {
		return getAttribute(VERSION);
	}

	@Override
	public int getAvailableProcessors() {
		return getAttribute(AVAILABLE_PROCESSORS);
	}

	@Override
	public double getSystemLoadAverage() {
		return getAttribute(SYSTEM_LOAD_AVERAGE);
	}

	@Override
	public long getCommittedVirtualMemorySize() {
		return getAttribute(COMMITTED_VIRTUAL_MEMORYSIZE);
	}

	@Override
	public long getFreePhysicalMemorySize() {
		return getAttribute(FREE_PHYSICAL_MEMORY_SIZE);
	}

	@Override
	public long getFreeSwapSpaceSize() {
		return getAttribute(FREE_SWAP_SPACE_SIZE);
	}

	@Override
	public double getProcessCpuLoad() {
		return getAttribute(PROCESS_CPU_LOAD);
	}

	@Override
	public long getProcessCpuTime() {
		return getAttribute(PROCESS_CPU_TIME);
	}

	@Override
	public double getSystemCpuLoad() {
		return getAttribute(SYSTEM_CPU_LOAD);
	}

	@Override
	public long getTotalPhysicalMemorySize() {
		return getAttribute(TOTAL_PHYSICAL_MEMORY_SIZE);
	}

	@Override
	public long getTotalSwapSpaceSize() {
		return getAttribute(TOTAL_SWAP_SPACE_SIZE);
	}
	
	public double cpuRatio() {
		return cpuRatio(1000, true);
	}
	
	public double cpuRatio(long time) {
		return cpuRatio(time, true);
	}

	public double cpuRatio(long time, boolean ifAvaProc) {
		Long start = System.currentTimeMillis();  
        long startT = getProcessCpuTime();  
        try { Thread.sleep(time); } catch (InterruptedException e) { }
        Long end = System.currentTimeMillis();  
        long endT = getProcessCpuTime();  
        double ratio = (endT - startT) / 1000000.0 / (end - start);
        if(ifAvaProc)
        	ratio /= getAvailableProcessors();
        
        BigDecimal decimal = new BigDecimal(ratio * 100);
        return decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        
	}
}
