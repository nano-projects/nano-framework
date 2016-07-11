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

import org.nanoframework.commons.format.DateFormat;
import org.nanoframework.commons.format.Pattern;
import org.nanoframework.commons.support.message.MessageFactory;
import org.nanoframework.commons.support.message.ParameterizedMessageFactory;

/**
 *
 * @author yanghe
 * @since 1.0
 */
public class NoLoggingImpl extends AbstractAnalysisLogger implements Logger {
    private static final String FQCN = NoLoggingImpl.class.getName();
    
    private static final String ERROR_LEVEL = "[ERROR]";
    private static final String WARN_LEVEL =  "[WARN ]";
    private static final String DEBUG_LEVEL = "[DEBUG]";
    private static final String INFO_LEVEL =  "[INFO ]";
    private static final String TRACE_LEVEL = "[TRACE]";
    
    private static final int LEVEL = 0;
    private static final int DATETIME = 1;
    private static final int CLASS_NAME = 2;
    private static final int METHOD_NAME = 3;
    private static final int FILE_NAME = 4;
    private static final int LINE_NUMBER = 5;
    private static final int MESSAGE_ARGS_INDEX = 6;
    
    private MessageFactory messageFactory = ParameterizedMessageFactory.INSTANCE;
    
    public NoLoggingImpl(final String loggerName) {
        setLoggerName(loggerName);
    }
    
    protected String message(final String level, final String message, final Object... args) {
        final StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        final int line = stackLine(stacks);
        
        final Object[] newArgs = new Object[6 + args.length];
        newArgs[LEVEL] = level;
        newArgs[DATETIME] = DateFormat.format(System.currentTimeMillis(), Pattern.DATETIME);
        newArgs[CLASS_NAME] = stacks[line].getClassName();
        newArgs[METHOD_NAME] = stacks[line].getMethodName();
        newArgs[FILE_NAME] = stacks[line].getFileName();
        newArgs[LINE_NUMBER] = stacks[line].getLineNumber();
        for(int idx = 0; idx < args.length; idx ++) {
            newArgs[MESSAGE_ARGS_INDEX + idx] = args[idx];
        }
        
        return messageFactory.newMessage("{} {} {}.{}({}:{}) >>> " + message, newArgs).getFormattedMessage();
    }
    
    protected int stackLine(final StackTraceElement[] stacks) {
        int invokeIndex = 0;
        for(int idx = 0; idx < stacks.length; idx ++) {
            if (FQCN.equals(stacks[idx].getClassName()) && (idx + 1 < stacks.length) && !FQCN.equals(stacks[idx + 1].getClassName())) {
                invokeIndex = idx + 1;
                break;
            }
        }
        
        return invokeIndex;
    }
    
    protected void outputStack(final Throwable cause) {
        if(cause != null) {
            outputStack(cause.getMessage(), cause);
        }
    }
    
    protected void outputStack(final String message, final Throwable cause) {
        outputStack(message, cause, false);
    }
    
    protected void outputStack(final String message, final Throwable cause, final boolean caused) {
        if (cause != null) {
            final StackTraceElement[] stacks = cause.getStackTrace();
            if (caused) {
                System.out.println("Caused by: " + cause.getClass().getName() + ": " + (message == null ? cause.getMessage() : message));
                final StackTraceElement stack = stacks[0];
                System.out.println("\tat " + stack.getClassName() + '.' + stack.getMethodName() + '(' + stack.getFileName() + ':' + stack.getLineNumber() + ')');
                System.out.println("\t... " + (stacks.length - 1) + " more");
            } else {
                System.out.println(cause.getClass().getName() + ": " + (message == null ? cause.getMessage() : message));
                for(StackTraceElement stack : stacks) {
                    System.out.println("\tat " + stack.getClassName() + '.' + stack.getMethodName() + '(' + stack.getFileName() + ':' + stack.getLineNumber() + ')');
                }
            }
            
        }
        
        final Throwable parentCause = cause.getCause();
        if (parentCause != null) {
            outputStack(parentCause.getMessage(), parentCause, true);
        }
    }
    
    @Override
    public boolean isErrorEnabled() {
        return true;
    }
    
    public void error(final String message, final Throwable cause) {
        error(message);
        outputStack(message, cause);
    }

    public void error(final String message) {
        System.out.println(message(ERROR_LEVEL, message));
        incrementError();
    }
    
    @Override
    public void error(final String message, final Object... args) {
        System.out.println(message(ERROR_LEVEL, message, args));
        incrementError();
    }

    @Override
    public void error(final Throwable cause) {
        error(cause.getMessage(), cause);
    }
    
    @Override
    public boolean isWarnEnabled() {
        return true;
    }
    
    public void warn(final String message) {
        System.out.println(message(WARN_LEVEL, message));
        incrementWarn();
    }

    @Override
    public void warn(final String message, final Throwable cause) {
        warn(message);
        outputStack(message, cause);
    }
    
    @Override
    public void warn(final String message, final Object... args) {
        System.out.println(message(WARN_LEVEL, message, args));
        incrementWarn();
    }

    @Override
    public void warn(final Throwable cause) {
        warn(cause.getMessage(), cause);
    }
    
    public boolean isDebugEnabled() {
        return true;
    }

    public void debug(final String message) {
        System.out.println(message(DEBUG_LEVEL, message));
        incrementDebug();
    }

    public void debug(final String message, final Throwable cause) {
        debug(message);
        outputStack(message, cause);
    }
    
    @Override
    public void debug(final String message, final Object... args) {
        System.out.println(message(DEBUG_LEVEL, message, args));
        incrementDebug();
    }

    @Override
    public void debug(final Throwable cause) {
        debug(cause.getMessage(), cause);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(final String message) {
        System.out.println(message(INFO_LEVEL, message));
        incrementInfo();
    }

    @Override
    public void info(final String message, final Object... args) {
        System.out.println(message(INFO_LEVEL, message, args));
        incrementInfo();
    }

    @Override
    public void info(final Throwable cause) {
        info(cause.getMessage(), cause);
    }

    @Override
    public void info(final String message, final Throwable cause) {
        info(message);
        outputStack(message, cause);
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(final String message) {
        System.out.println(message(TRACE_LEVEL, message));
        incrementTrace();
    }

    @Override
    public void trace(final String message, final Object... args) {
        System.out.println(message(TRACE_LEVEL, message, args));
        incrementTrace();
    }

    @Override
    public void trace(final Throwable cause) {
        trace(cause.getMessage(), cause);
    }

    @Override
    public void trace(final String message, final Throwable cause) {
        trace(message);
        outputStack(message, cause);
    }

}
