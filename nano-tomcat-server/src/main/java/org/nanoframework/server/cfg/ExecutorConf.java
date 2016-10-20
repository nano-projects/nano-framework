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
package org.nanoframework.server.cfg;

import org.apache.catalina.core.StandardThreadExecutor;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class ExecutorConf extends AbstractConf {
    public static final String TOMCAT_EXECUTOR = "context.tomcat.executor";
    
    private static final long serialVersionUID = 4483458534269995105L;

    static {
        final ExecutorConf conf = new ExecutorConf();
        conf.name = "tomcatThreadPool";
        conf.daemon = Boolean.TRUE;
        conf.namePrefix = "tomcat-exec-";
        conf.maxThreads = 200;
        conf.minSpareThreads = 25;
        conf.maxIdleTime = 60_000;
        conf.maxQueueSize = Integer.MAX_VALUE;
        conf.prestartminSpareThreads = Boolean.FALSE;
        conf.threadRenewalDelay = 1000L;
        DEF = conf;
    }
    
    private String name;
    private Boolean daemon;
    private String namePrefix;
    private Integer maxThreads;
    private Integer minSpareThreads;
    private Integer maxIdleTime;
    private Integer maxQueueSize;
    private Boolean prestartminSpareThreads;
    private Long threadRenewalDelay;

    private ExecutorConf() {
        
    }
    
    public ExecutorConf(final ExecutorConf conf) {
        this.merge(conf);
    }
    
    public StandardThreadExecutor init() {
        final StandardThreadExecutor executor = new StandardThreadExecutor();
        executor.setName(name);
        executor.setDaemon(daemon);
        executor.setNamePrefix(namePrefix);
        executor.setMaxThreads(maxThreads);
        executor.setMinSpareThreads(minSpareThreads);
        executor.setMaxIdleTime(maxIdleTime);
        executor.setMaxQueueSize(maxQueueSize);
        executor.setPrestartminSpareThreads(prestartminSpareThreads);
        executor.setThreadRenewalDelay(threadRenewalDelay);
        return executor;
    }
    
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean getDaemon() {
        return daemon;
    }

    public void setDaemon(final Boolean daemon) {
        this.daemon = daemon;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(final String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public Integer getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(final Integer maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Integer getMinSpareThreads() {
        return minSpareThreads;
    }

    public void setMinSpareThreads(final Integer minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
    }

    public Integer getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(final Integer maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public Integer getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(final Integer maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public Boolean getPrestartminSpareThreads() {
        return prestartminSpareThreads;
    }

    public void setPrestartminSpareThreads(final Boolean prestartminSpareThreads) {
        this.prestartminSpareThreads = prestartminSpareThreads;
    }

    public Long getThreadRenewalDelay() {
        return threadRenewalDelay;
    }

    public void setThreadRenewalDelay(final Long threadRenewalDelay) {
        this.threadRenewalDelay = threadRenewalDelay;
    }

    /* (non-Javadoc)
     * @see org.nanoframework.server.cfg.AbstractConf#confName()
     */
    @Override
    public String confName() {
        return TOMCAT_EXECUTOR;
    }
}
