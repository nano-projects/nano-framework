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

import org.apache.log4j.Category;
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
public class Log4jImpl extends Category implements org.nanoframework.commons.support.logging.Logger {

    private static final String FQCN = Log4jImpl.class.getName();

    private Logger log;

    private int errorCount;
    private int warnCount;
    private int infoCount;
    private int debugCount;

    private MessageFactory messageFactory = ParameterizedMessageFactory.INSTANCE;

    /**
     * @since 0.2.21
     * @param log the log
     */
    public Log4jImpl(Logger log) {
        super(log.getName());
        this.log = log;
    }

    public Log4jImpl(String loggerName) {
        super(loggerName);
        log = Logger.getLogger(loggerName);
    }

    public Logger getLog() {
        return log;
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public void error(String s, Throwable e) {
        errorCount++;
        log.log(FQCN, Level.ERROR, s, e);
    }

    public void error(String s) {
        errorCount++;
        log.log(FQCN, Level.ERROR, s, null);
    }

    public void debug(String s) {
        debugCount++;
        log.log(FQCN, Level.DEBUG, s, null);
    }

    public void debug(String s, Throwable e) {
        debugCount++;
        log.log(FQCN, Level.DEBUG, s, e);
    }

    public void warn(String s) {
        log.log(FQCN, Level.WARN, s, null);
        warnCount++;
    }

    public void warn(String s, Throwable e) {
        log.log(FQCN, Level.WARN, s, e);
        warnCount++;
    }

    public int getWarnCount() {
        return warnCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void resetStat() {
        errorCount = 0;
        warnCount = 0;
        infoCount = 0;
        debugCount = 0;
    }

    public int getDebugCount() {
        return debugCount;
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public void info(String msg) {
        infoCount++;
        log.log(FQCN, Level.INFO, msg, null);
    }

    public boolean isWarnEnabled() {
        return log.isEnabledFor(Level.WARN);
    }

    public int getInfoCount() {
        return infoCount;
    }

    public String toString() {
        return log.toString();
    }

    @Override
    public void warn(String paramString, Object... paramArrayOfObject) {
        l7dlog(Level.WARN, paramString, paramArrayOfObject, null);
    }

    @Override
    public void warn(Throwable paramThrowable) {
        l7dlog(Level.WARN, null, null, paramThrowable);
    }

    @Override
    public void info(String paramString, Object... paramArrayOfObject) {
        l7dlog(Level.INFO, paramString, paramArrayOfObject, null);
    }

    @Override
    public void info(Throwable paramThrowable) {
        l7dlog(Level.INFO, null, null, paramThrowable);
    }

    @Override
    public void info(String paramString, Throwable paramThrowable) {
        l7dlog(Level.INFO, paramString, null, paramThrowable);
    }

    @Override
    public void debug(String paramString, Object... paramArrayOfObject) {
        l7dlog(Level.DEBUG, paramString, paramArrayOfObject, null);
    }

    @Override
    public void debug(Throwable paramThrowable) {
        l7dlog(Level.DEBUG, null, null, paramThrowable);
    }

    @Override
    public void error(String paramString, Object... paramArrayOfObject) {
        l7dlog(Level.ERROR, paramString, paramArrayOfObject, null);
    }

    @Override
    public void error(Throwable paramThrowable) {
        l7dlog(Level.ERROR, null, null, paramThrowable);
    }

    public void l7dlog(Priority priority, String message, Object[] params, Throwable t) {
        if (log.getLoggerRepository().isDisabled(priority.toInt())) {
            return;
        }

        if (priority.isGreaterOrEqual(this.getEffectiveLevel())) {
            forcedLog(FQCN, priority, messageFactory.newMessage(message, params).getFormattedMessage(), t);
        }
    }
}
