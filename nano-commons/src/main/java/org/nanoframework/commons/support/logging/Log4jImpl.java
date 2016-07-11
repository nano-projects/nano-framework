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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.nanoframework.commons.support.message.MessageFactory;
import org.nanoframework.commons.support.message.ParameterizedMessageFactory;

/**
 *
 * @author yanghe
 * @since 1.0
 */
public class Log4jImpl extends AbstractLog4jAnalysisLogger implements org.nanoframework.commons.support.logging.Logger {
    private static final String FQCN = Log4jImpl.class.getName();

    private MessageFactory messageFactory = ParameterizedMessageFactory.INSTANCE;
    
    private Logger logger;

    public Log4jImpl(final Logger logger) {
        super(logger.getName());
        this.logger = logger;
        this.parent = logger;
    }

    public Log4jImpl(final String loggerName) {
        super(loggerName);
        logger = Logger.getLogger(loggerName);
        this.parent = logger;
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    @Override
    public void error(final String message, final Throwable cause) {
        logger.log(FQCN, Level.ERROR, message, cause);
        incrementError();
    }

    @Override
    public void error(final String message) {
        logger.log(FQCN, Level.ERROR, message, null);
        incrementError();
    }
    
    @Override
    public void error(final String message, final Object... args) {
        l7dlog(Level.ERROR, message, args, null);
        incrementError();
    }

    @Override
    public void error(final Throwable cause) {
        l7dlog(Level.ERROR, cause.getMessage(), null, cause);
        incrementError();
    }
    
    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }
    
    @Override
    public void warn(final String message) {
        logger.log(FQCN, Level.WARN, message, null);
        incrementWarn();
    }

    @Override
    public void warn(final String message, final Throwable cause) {
        logger.log(FQCN, Level.WARN, message, cause);
        incrementWarn();
    }
    
    @Override
    public void warn(final String message, final Object... args) {
        l7dlog(Level.WARN, message, args, null);
        incrementWarn();
    }

    @Override
    public void warn(final Throwable cause) {
        l7dlog(Level.WARN, cause.getMessage(), null, cause);
        incrementWarn();
    }
    
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String message) {
        logger.log(FQCN, Level.DEBUG, message, null);
        incrementDebug();
    }

    @Override
    public void debug(final String message, final Throwable cause) {
        logger.log(FQCN, Level.DEBUG, message, cause);
        incrementDebug();
    }
    
    @Override
    public void debug(final String message, final Object... args) {
        l7dlog(Level.DEBUG, message, args, null);
        incrementDebug();
    }

    @Override
    public void debug(final Throwable cause) {
        l7dlog(Level.DEBUG, cause.getMessage(), null, cause);
        incrementDebug();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(final String message) {
        logger.log(FQCN, Level.INFO, message, null);
        incrementInfo();
    }

    @Override
    public void info(final String message, final Object... args) {
        l7dlog(Level.INFO, message, args, null);
        incrementInfo();
    }

    @Override
    public void info(final Throwable cause) {
        l7dlog(Level.INFO, cause.getMessage(), null, cause);
        incrementInfo();
    }

    @Override
    public void info(final String message, final Throwable cause) {
        l7dlog(Level.INFO, message, null, cause);
        incrementInfo();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabledFor(Level.TRACE);
    }

    @Override
    public void trace(final String message) {
        logger.log(FQCN, Level.TRACE, message, null);
        incrementTrace();
    }

    @Override
    public void trace(final String message, final Object... args) {
        l7dlog(Level.INFO, message, args, null);
        incrementTrace();
    }

    @Override
    public void trace(final Throwable cause) {
        logger.log(FQCN, Level.TRACE, cause.getMessage(), cause);
        incrementTrace();
    }

    @Override
    public void trace(final String message, final Throwable cause) {
        logger.log(FQCN, Level.TRACE, message, cause);
        incrementTrace();
    }
    
    public void l7dlog(Priority priority, String message, Object[] args, Throwable t) {
        if (logger.getLoggerRepository().isDisabled(priority.toInt())) {
            return;
        }

        if (priority.isGreaterOrEqual(this.getEffectiveLevel())) {
            forcedLog(FQCN, priority, messageFactory.newMessage(message, args).getFormattedMessage(), t);
        }
    }
    
    @Override
    public String toString() {
        return logger.toString();
    }
}
