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
package org.nanoframework.commons.support.logging;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Category;

import com.google.common.util.concurrent.AtomicLongMap;

/**
 *
 * @author yanghe
 * @since 1.3.14
 */
public abstract class AbstractLog4jAnalysisLogger extends Category implements AnalysisLoggerMXBean {
    protected static final AtomicLongMap<String> ERROR = AtomicLongMap.create();
    protected static final AtomicLongMap<String> WARN = AtomicLongMap.create();
    protected static final AtomicLongMap<String> INFO = AtomicLongMap.create();
    protected static final AtomicLongMap<String> DEBUG = AtomicLongMap.create();
    protected static final AtomicLongMap<String> TRACE = AtomicLongMap.create();
    
    protected static final String DEFAULT_LOGGER_NAME = "nil";
    
    private String loggerName = DEFAULT_LOGGER_NAME;
    
    protected AbstractLog4jAnalysisLogger(final String loggerName) {
        super(loggerName);
        setLoggerName(loggerName);
    }
    
    @Override
    public Set<String> getErrorKeys() {
        return ERROR.asMap().keySet();
    }
    
    @Override
    public Set<String> getWarnKeys() {
        return WARN.asMap().keySet();
    }
    
    @Override
    public Set<String> getInfoKeys() {
        return INFO.asMap().keySet();
    }
    
    @Override
    public Set<String> getDebugKeys() {
        return DEBUG.asMap().keySet();
    }
    
    @Override
    public Set<String> getTraceKeys() {
        return TRACE.asMap().keySet();
    }
    
    @Override
    public long getErrorCount() {
        return ERROR.get(loggerName);
    }

    @Override
    public long getWarnCount() {
        return WARN.get(loggerName);
    }

    @Override
    public long getInfoCount() {
        return INFO.get(loggerName);
    }

    @Override
    public long getDebugCount() {
        return DEBUG.get(loggerName);
    }
    
    @Override
    public long getTraceCount() {
        return TRACE.get(loggerName);
    }

    @Override
    public void reset() {
        ERROR.put(loggerName, 0);
        WARN.put(loggerName, 0);
        INFO.put(loggerName, 0);
        DEBUG.put(loggerName, 0);
        TRACE.put(loggerName, 0);
    }
    
    @Override
    public long getErrorTotal() {
        return total(ERROR);
    }
    
    @Override
    public long getWarnTotal() {
        return total(WARN);
    }
    
    @Override
    public long getInfoTotal() {
        return total(INFO);
    }
    
    @Override
    public long getDebugTotal() {
        return total(DEBUG);
    }
    
    @Override
    public long getTraceTotal() {
        return total(TRACE);
    }
    
    @Override
    public void resetAll() {
        ERROR.clear();
        WARN.clear();
        INFO.clear();
        DEBUG.clear();
        TRACE.clear();
    }
    
    protected long total(final AtomicLongMap<String> analysis) {
        if (analysis == null || analysis.isEmpty()) {
            return 0;
        }
        
        final AtomicLong total = new AtomicLong(0);
        analysis.asMap().values().forEach(count -> total.addAndGet(count));
        return total.get();
    }
    
    public void incrementError() {
        ERROR.incrementAndGet(loggerName);
    }
    
    public void incrementWarn() {
        WARN.incrementAndGet(loggerName);
    }
    
    public void incrementInfo() {
        INFO.incrementAndGet(loggerName);
    }
    
    public void incrementDebug() {
        DEBUG.incrementAndGet(loggerName);
    }
    
    public void incrementTrace() {
        TRACE.incrementAndGet(loggerName);
    }
    
    public void setLoggerName(final String loggerName) {
        this.loggerName = loggerName;
    }
    
    public String getLoggerName() {
        return loggerName;
    }
}
