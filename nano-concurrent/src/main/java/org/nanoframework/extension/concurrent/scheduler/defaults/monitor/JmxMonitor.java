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
package org.nanoframework.extension.concurrent.scheduler.defaults.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanoframework.commons.entity.BaseEntity;

/**
 * 
 * @author yanghe
 * @since 1.3
 */
public class JmxMonitor extends BaseEntity {
    private static final long serialVersionUID = 743543178402183287L;

    private static final Integer ZERO_INT = Integer.valueOf(0);
    private static final Long ZERO_LONG = Long.valueOf(0);
    private static final Double ZERO_DOUBLE = Double.valueOf(0);

    /** ClassLoading */
    private Integer loadedClassCount = ZERO_INT;
    private Long unloadedClassCount = ZERO_LONG;
    private Long totalLoadedClassCount = ZERO_LONG;

    /** Memory */
    private Map<MemoryUsage, Long> heapMemoryUsage = DEFAULT_MEMORY_STATUS;

    /** OS */
    private Double cpuRatio = ZERO_DOUBLE;
    private Double totalCpuRatio = ZERO_DOUBLE;

    /** Thread */
    private Long totalStartedThreadCount = ZERO_LONG;
    private Integer threadCount = ZERO_INT;
    private Integer daemonThreadCount = ZERO_INT;
    private Integer peakThreadCount = ZERO_INT;

    /** User defined */
    private List<Pointer> tps;

    private static final Map<MemoryUsage, Long> DEFAULT_MEMORY_STATUS = new HashMap<MemoryUsage, Long>() {
        private static final long serialVersionUID = -3324580956350564994L;
        {
            put(MemoryUsage.MAX, ZERO_LONG);
            put(MemoryUsage.USED, ZERO_LONG);
            put(MemoryUsage.INIT, ZERO_LONG);
            put(MemoryUsage.COMMITTED, ZERO_LONG);
            put(MemoryUsage.FREE, ZERO_LONG);
        }
    };

    public enum MemoryUsage {
        INIT, USED, COMMITTED, MAX, FREE;

    }

    public Integer getLoadedClassCount() {
        return loadedClassCount;
    }

    public void setLoadedClassCount(Integer loadedClassCount) {
        this.loadedClassCount = loadedClassCount;
    }

    public Long getUnloadedClassCount() {
        return unloadedClassCount;
    }

    public void setUnloadedClassCount(Long unloadedClassCount) {
        this.unloadedClassCount = unloadedClassCount;
    }

    public Long getTotalLoadedClassCount() {
        return totalLoadedClassCount;
    }

    public void setTotalLoadedClassCount(Long totalLoadedClassCount) {
        this.totalLoadedClassCount = totalLoadedClassCount;
    }

    public Map<MemoryUsage, Long> getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public void setHeapMemoryUsage(Map<MemoryUsage, Long> heapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    public Double getCpuRatio() {
        return cpuRatio;
    }

    public void setCpuRatio(Double cpuRatio) {
        this.cpuRatio = cpuRatio;
    }

    public Double getTotalCpuRatio() {
        return totalCpuRatio;
    }

    public void setTotalCpuRatio(Double totalCpuRatio) {
        this.totalCpuRatio = totalCpuRatio;
    }

    public Long getTotalStartedThreadCount() {
        return totalStartedThreadCount;
    }

    public void setTotalStartedThreadCount(Long totalStartedThreadCount) {
        this.totalStartedThreadCount = totalStartedThreadCount;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Integer getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(Integer daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public Integer getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(Integer peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    public List<Pointer> getTps() {
        return tps;
    }

    public void setTps(List<Pointer> tps) {
        this.tps = tps;
    }

}
