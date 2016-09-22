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
package org.nanoframework.concurrent.scheduler.defaults.etcd;

import org.nanoframework.commons.entity.BaseEntity;

/**
 * 
 * @author yanghe
 * @since 1.3
 */
public class EtcdAppInfo extends BaseEntity {
    private static final long serialVersionUID = -5412741101794352940L;

    private String systemId;
    private String appName;
    private String ip;
    private Long startTime;
    private Long uptime;
    private String hostName;
    private String pid;
    private Integer availableProcessors;
    private Boolean jmxEnable;
    private Integer jmxRate;

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Integer getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(Integer availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public Boolean getJmxEnable() {
        return jmxEnable;
    }

    public void setJmxEnable(Boolean jmxEnable) {
        this.jmxEnable = jmxEnable;
    }

    public Integer getJmxRate() {
        return jmxRate;
    }

    public void setJmxRate(Integer jmxRate) {
        this.jmxRate = jmxRate;
    }

}
