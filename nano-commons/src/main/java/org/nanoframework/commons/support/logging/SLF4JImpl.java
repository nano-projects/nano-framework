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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 *
 * @author yanghe
 * @since 1.0
 */
public class SLF4JImpl extends AbstractAnalysisLogger implements org.nanoframework.commons.support.logging.Logger {

    private static final String FQNC = SLF4JImpl.class.getName();
    private static final Logger testLogger = LoggerFactory.getLogger(SLF4JImpl.class);

    static {
        if (!(testLogger instanceof LocationAwareLogger)) {
            throw new UnsupportedOperationException(testLogger.getClass() + " is not a suitable logger");
        }
    }

    private LocationAwareLogger logger;

    public SLF4JImpl(final LocationAwareLogger logger) {
        this.logger = logger;
        setLoggerName(logger.getName());
    }

    public SLF4JImpl(final String loggerName) {
        this.logger = (LocationAwareLogger) LoggerFactory.getLogger(loggerName);
        setLoggerName(loggerName);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(final String message, final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, null, cause);
        incrementError();
    }

    @Override
    public void error(final String message) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, null, null);
        incrementError();
    }
    
    @Override
    public void error(final String message, final Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, args, null);
        incrementError();
    }

    @Override
    public void error(final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, cause.getMessage(), null, cause);
        incrementError();
    }
    
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(final String message) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, message, null, null);
        incrementWarn();
    }

    @Override
    public void warn(final String message, final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, message, null, cause);
        incrementWarn();
    }

    @Override
    public void warn(final String message, final Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, message, args, null);
        incrementWarn();
    }

    @Override
    public void warn(final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.WARN_INT, cause.getMessage(), null, cause);
        incrementWarn();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String message) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, message, null, null);
        incrementInfo();
    }

    @Override
    public void info(final String message, final Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, message, args, null);
        incrementInfo();
    }

    @Override
    public void info(final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, cause.getMessage(), null, cause);
        incrementInfo();
    }

    @Override
    public void info(final String message, final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.INFO_INT, message, null, cause);
        incrementInfo();
    }
    
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    
    @Override
    public void debug(final String message) {
        logger.log(null, FQNC, LocationAwareLogger.DEBUG_INT, message, null, null);
        incrementDebug();
    }

    @Override
    public void debug(final String message, final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.ERROR_INT, message, null, cause);
        incrementDebug();
    }

    @Override
    public void debug(final String message, final Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.DEBUG_INT, message, args, null);
        incrementDebug();
    }

    @Override
    public void debug(final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.DEBUG_INT, cause.getMessage(), null, cause);
        incrementDebug();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(final String message) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, message, null, null);
        incrementWarn();
    }

    @Override
    public void trace(final String message, final Object... args) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, message, args, null);
        incrementWarn();
    }

    @Override
    public void trace(final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, cause.getMessage(), null, cause);
        incrementWarn();
    }

    @Override
    public void trace(final String message, final Throwable cause) {
        logger.log(null, FQNC, LocationAwareLogger.TRACE_INT, message, null, cause);
        incrementWarn();        
    }
    
}
