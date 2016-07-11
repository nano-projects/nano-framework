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

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author yanghe
 * @since 1.3.14
 */
public class NoLoggingTest {
    protected Logger logger;
    
    @Before
    public void selectLogger() {
        LoggerFactory.selectNoLogging();
        logger = LoggerFactory.getLogger(NoLoggingTest.class);
    }
    
    @Test
    public void errorTest() {
        logger.error("Test error 0");
        logger.error(new LoggerException("Test error 1"));
        logger.error("Test error {}", 2);
        logger.error("Test error 3", new LoggerException("Test error 4"));
        
        final Throwable cause0 = new Exception("Level 1");
        final Throwable cause1 = new RuntimeException("Level 2", cause0);
        final Throwable cause2 = new LoggerException("Level 3", cause1);
        logger.error(cause2);
    }
    
    @Test
    public void warnTest() {
        logger.warn("Test warn 0");
        logger.warn(new LoggerException("Test warn 1"));
        logger.warn("Test warn {}", 2);
        logger.warn("Test warn 3", new LoggerException("Test warn 4"));
        
        final Throwable cause0 = new Exception("Level 1");
        final Throwable cause1 = new RuntimeException("Level 2", cause0);
        final Throwable cause2 = new LoggerException("Level 3", cause1);
        logger.warn(cause2);
    }
    
    @Test
    public void infoTest() {
        logger.info("Test info 0");
        logger.info(new LoggerException("Test info 1"));
        logger.info("Test info {}", 2);
        logger.info("Test info 3", new LoggerException("Test info 4"));
        
        final Throwable cause0 = new Exception("Level 1");
        final Throwable cause1 = new RuntimeException("Level 2", cause0);
        final Throwable cause2 = new LoggerException("Level 3", cause1);
        logger.info(cause2);
    }
    
    @Test
    public void debugTest() {
        logger.debug("Test debug 0");
        logger.debug(new LoggerException("Test debug 1"));
        logger.debug("Test debug {}", 2);
        logger.debug("Test debug 3", new LoggerException("Test debug 4"));
        
        final Throwable cause0 = new Exception("Level 1");
        final Throwable cause1 = new RuntimeException("Level 2", cause0);
        final Throwable cause2 = new LoggerException("Level 3", cause1);
        logger.debug(cause2);
    }
    
    @Test
    public void traceTest() {
        logger.trace("Test trace 0");
        logger.trace(new LoggerException("Test trace 1"));
        logger.trace("Test trace {}", 2);
        logger.trace("Test trace 3", new LoggerException("Test trace 4"));
        
        final Throwable cause0 = new Exception("Level 1");
        final Throwable cause1 = new RuntimeException("Level 2", cause0);
        final Throwable cause2 = new LoggerException("Level 3", cause1);
        logger.trace(cause2);
    }
    
    @Test
    public void logCount() {
        System.out.println("Error Invoke count: " + ((AnalysisLoggerMXBean) logger).getErrorCount());
        System.out.println("Warn Invoke count: " + ((AnalysisLoggerMXBean) logger).getWarnCount());
        System.out.println("Info Invoke count: " + ((AnalysisLoggerMXBean) logger).getInfoCount());
        System.out.println("Debug Invoke count: " + ((AnalysisLoggerMXBean) logger).getDebugCount());
        System.out.println("Trace Invoke count: " + ((AnalysisLoggerMXBean) logger).getTraceCount());
    }
}
