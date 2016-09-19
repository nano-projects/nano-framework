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
package org.nanoframework.extension.concurrent.scheduler;

import org.nanoframework.commons.entity.BaseEntity;

/**
 * 
 * @author yanghe
 * @since 1.3
 */
public class SchedulerStatus extends BaseEntity {
    private static final long serialVersionUID = 5876395587017572488L;

    private Long key;
    private String group;
    private String id;
    private Status status;
    private long executing;
    private long beforeException;
    private long executeException;
    private long afterException;
    private String performCycle;

    public SchedulerStatus() {
    }

    public SchedulerStatus(String group, String id, Status status, SchedulerAnalysis analysis) {
        this.group = group;
        this.id = id;
        this.status = status;
        this.executing = analysis.executing.get();
        this.beforeException = analysis.beforeException.get();
        this.executeException = analysis.executeException.get();
        this.afterException = analysis.afterException.get();
        this.performCycle = analysis.performCycle();
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getExecuting() {
        return executing;
    }

    public void setExecuting(long executing) {
        this.executing = executing;
    }

    public long getBeforeException() {
        return beforeException;
    }

    public void setBeforeException(long beforeException) {
        this.beforeException = beforeException;
    }

    public long getExecuteException() {
        return executeException;
    }

    public void setExecuteException(long executeException) {
        this.executeException = executeException;
    }

    public long getAfterException() {
        return afterException;
    }

    public void setAfterException(long afterException) {
        this.afterException = afterException;
    }

    public String getPerformCycle() {
        return performCycle;
    }

    public void setPerformCycle(String performCycle) {
        this.performCycle = performCycle;
    }

    public enum Status {
        STARTED, STOPPING, STOPPED;
    }
}
