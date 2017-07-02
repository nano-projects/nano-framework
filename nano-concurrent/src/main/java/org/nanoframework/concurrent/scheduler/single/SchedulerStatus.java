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
package org.nanoframework.concurrent.scheduler.single;

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
    private Long executing;
    private Long beforeException;
    private Long executeException;
    private Long afterException;
    private String performCycle;

    public SchedulerStatus() {
    }

    public SchedulerStatus(final String group, final String id, final Status status, final SchedulerAnalysis analysis) {
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

    public void setKey(final Long key) {
        this.key = key;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Long getExecuting() {
        return executing;
    }

    public void setExecuting(final Long executing) {
        this.executing = executing;
    }

    public Long getBeforeException() {
        return beforeException;
    }

    public void setBeforeException(final Long beforeException) {
        this.beforeException = beforeException;
    }

    public Long getExecuteException() {
        return executeException;
    }

    public void setExecuteException(final Long executeException) {
        this.executeException = executeException;
    }

    public Long getAfterException() {
        return afterException;
    }

    public void setAfterException(final Long afterException) {
        this.afterException = afterException;
    }

    public String getPerformCycle() {
        return performCycle;
    }

    public void setPerformCycle(final String performCycle) {
        this.performCycle = performCycle;
    }

    public enum Status {
        STARTED, STOPPING, STOPPED;
    }
}
