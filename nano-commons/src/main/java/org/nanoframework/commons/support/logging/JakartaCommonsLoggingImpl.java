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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nanoframework.commons.support.message.MessageFactory;
import org.nanoframework.commons.support.message.ParameterizedMessageFactory;
import org.nanoframework.commons.util.Assert;

/**
 *
 * @author yanghe
 * @since 1.0
 */
public class JakartaCommonsLoggingImpl extends AbstractAnalysisLogger implements org.nanoframework.commons.support.logging.Logger {
    private Log logger;
    
    private MessageFactory messageFactory = ParameterizedMessageFactory.INSTANCE;

    public JakartaCommonsLoggingImpl(final Log logger) {
        Assert.notNull(logger);
        this.logger = logger;
        setLoggerName(logger.getClass().getName());
    }

    public JakartaCommonsLoggingImpl(final String loggerName) {
        Assert.hasText(loggerName);
        logger = LogFactory.getLog(loggerName);
        setLoggerName(loggerName);
    }
    
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void error(final String message, final Throwable cause) {
        logger.error(message, cause);
        incrementError();
    }

    public void error(final String message) {
        logger.error(message);
        incrementError();
    }
    
    @Override
    public void error(final String message, final Object... args) {
        logger.error(messageFactory.newMessage(message, args).getFormattedMessage());
        incrementError();
    }

    @Override
    public void error(final Throwable cause) {
        logger.error(cause.getMessage(), cause);
        incrementError();
    }
    
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }
    
    public void warn(final String message) {
        logger.warn(message);
        incrementWarn();
    }

    @Override
    public void warn(final String message, final Throwable cause) {
        logger.warn(message, cause);
        incrementWarn();
    }
    
    @Override
    public void warn(final String message, final Object... args) {
        logger.warn(messageFactory.newMessage(message, args).getFormattedMessage());
        incrementWarn();
    }

    @Override
    public void warn(final Throwable cause) {
        logger.warn(cause.getMessage(), cause);
        incrementWarn();
    }
    
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }
    
    @Override
    public void info(final String message) {
        logger.info(message);
        incrementInfo();
    }
    
    @Override
    public void info(final String message, final Object... args) {
        logger.info(messageFactory.newMessage(message, args).getFormattedMessage());
        incrementInfo();
    }

    @Override
    public void info(final Throwable cause) {
        logger.info(cause.getMessage(), cause);
        incrementInfo();
    }

    @Override
    public void info(final String message, final Throwable cause) {
        logger.info(message, cause);
        incrementInfo();
    }
    
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(final String message) {
        logger.debug(message);
        incrementDebug();
    }

    public void debug(final String message, final Throwable cause) {
        logger.debug(message, cause);
        incrementDebug();
    }

    @Override
    public void debug(final String message, final Object... args) {
        logger.debug(messageFactory.newMessage(message, args).getFormattedMessage());
        incrementDebug();
    }

    @Override
    public void debug(final Throwable cause) {
        logger.debug(cause.getMessage(), cause);
        incrementDebug();
    }
    
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }
    
    @Override
    public void trace(final String message) {
        logger.trace(message);
        incrementTrace();
    }
    
    @Override
    public void trace(final String message, final Object... args) {
        logger.trace(messageFactory.newMessage(message, args).getFormattedMessage());
        incrementTrace();
    }
    
    @Override
    public void trace(final String message, final Throwable cause) {
        logger.trace(message, cause);
        incrementTrace();
    }
    
    @Override
    public void trace(final Throwable cause) {
        logger.trace(cause.getMessage(), cause);
        incrementTrace();
    }

}
