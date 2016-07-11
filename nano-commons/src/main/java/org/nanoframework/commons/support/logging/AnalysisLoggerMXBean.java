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

/**
 *
 * @author yanghe
 * @since 1.3.14
 */
public interface AnalysisLoggerMXBean {
    String OBJECT_NAME = "org.nanoframework:type=AnalysisLogger";
    
    Set<String> getErrorKeys();
    
    Set<String> getWarnKeys();
    
    Set<String> getInfoKeys();
    
    Set<String> getDebugKeys();
    
    Set<String> getTraceKeys();
    
    long getErrorCount();
    
    long getWarnCount();
    
    long getInfoCount();
    
    long getDebugCount();
    
    long getTraceCount();
    
    void reset();
    
    long getErrorTotal();
    
    long getWarnTotal();
    
    long getInfoTotal();
    
    long getDebugTotal();
    
    long getTraceTotal();
    
    void resetAll();
    
}
