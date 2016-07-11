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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.nanoframework.commons.util.Assert;

/**
 *
 * @author yanghe
 * @since 1.0
 */
public class Log4j2Impl extends AbstractAnalysisLogger implements org.nanoframework.commons.support.logging.Logger {
    private static final String FQCN = Log4j2Impl.class.getName();
    private ExtendedLogger logger;

    public Log4j2Impl(final Logger logger) {
        Assert.notNull(logger);
        this.logger = (ExtendedLogger) logger;
        setLoggerName(logger.getName());
    }

    public Log4j2Impl(final String loggerName) {
        Assert.hasText(loggerName);
        logger = (ExtendedLogger) LogManager.getLogger(loggerName);
        setLoggerName(loggerName);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(Level.ERROR);
    }

    @Override
    public void error(final String message, final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message, cause);
        incrementError();
    }

    @Override
    public void error(final String message) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message);
        incrementError();
    }

    @Override
    public void error(final String message, final Object... args) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message, args);
        incrementError();
    }

    @Override
    public void error(final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, cause.getMessage(), cause);
        incrementError();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(Level.WARN);
    }

    @Override
    public void warn(final String message) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message);
        incrementWarn();
    }

    @Override
    public void warn(final String message, final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message, cause);
        incrementWarn();
    }

    @Override
    public void warn(final String message, final Object... args) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message, args);
        incrementWarn();
    }

    @Override
    public void warn(final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.WARN, null, cause.getMessage(), cause);
        incrementWarn();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(Level.DEBUG);
    }

    @Override
    public void debug(final String message) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message);
        incrementDebug();
    }

    @Override
    public void debug(final String message, final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message, cause);
        incrementDebug();
    }

    @Override
    public void debug(final String message, final Object... args) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message, args);
        incrementDebug();
    }

    @Override
    public void debug(final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, cause.getMessage(), cause);
        incrementDebug();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(Level.INFO);
    }

    @Override
    public void info(final String message) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message);
        incrementInfo();
    }

    @Override
    public void info(final String message, final Object... args) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message, args);
        incrementInfo();
    }

    @Override
    public void info(final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.INFO, null, cause.getMessage(), cause);
        incrementInfo();
    }

    @Override
    public void info(final String message, final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message, cause);
        incrementInfo();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(Level.TRACE);
    }

    @Override
    public void trace(final String message) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message);
        incrementTrace();
    }

    @Override
    public void trace(final String message, final Object... args) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message, args);
        incrementTrace();
    }

    @Override
    public void trace(final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, cause.getMessage(), cause);
        incrementTrace();
    }

    @Override
    public void trace(final String message, final Throwable cause) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message, cause);
        incrementTrace();
    }

    @Override
    public String toString() {
        return logger.toString();
    }
}
