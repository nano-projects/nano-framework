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

import java.lang.reflect.Constructor;

/**
 *
 * @author yanghe
 * @since 1.0
 */
public class LoggerFactory {
    public static final String LOG4J2_LOGGER = "org.apache.logging.log4j.Logger";
    public static final String LOG4J_LOGGER = "org.apache.log4j.Logger";
    public static final String SLF4J_LOGGRE = "org.slf4j.Logger";
    public static final String APACHE_COMMON_LOGGER = "org.apache.commons.logging.LogFactory";
    public static final String JDK_LOGGER = "java.util.logging.Logger";
    
    public static final String LOG4J_IMPL_CLASS_NAME = "org.nanoframework.commons.support.logging.Log4jImpl";
    public static final String LOG4J2_IMPL_CLASS_NAME = "org.nanoframework.commons.support.logging.Log4j2Impl";
    public static final String SLF4J_IMPL_CLASS_NAME = "org.nanoframework.commons.support.logging.SLF4JImpl";
    public static final String JAKARTA_COMMONS_LOGGING_IMPL_CLASS_NAME = "org.nanoframework.commons.support.logging.JakartaCommonsLoggingImpl";
    public static final String JDK14_LOGGING_IMPL_CLASS_NAME = "org.nanoframework.commons.support.logging.Jdk14LoggingImpl";
    public static final String NO_LOGGING_IMPL_CLASS_NAME = "org.nanoframework.commons.support.logging.NoLoggingImpl";
    private static Constructor<?> LOGGER_CONSTRUCTOR;

    static {
        // 优先选择log4j2,而非Apache Common Logging. 因为后者无法设置真实Log调用者的信息
        tryImplementation(LOG4J2_LOGGER, LOG4J2_IMPL_CLASS_NAME);
        tryImplementation(LOG4J_LOGGER, LOG4J_IMPL_CLASS_NAME);
        tryImplementation(SLF4J_LOGGRE, SLF4J_IMPL_CLASS_NAME);
        tryImplementation(APACHE_COMMON_LOGGER, JAKARTA_COMMONS_LOGGING_IMPL_CLASS_NAME);
        tryImplementation(JDK_LOGGER, JDK14_LOGGING_IMPL_CLASS_NAME);
        tryNoLoggingImplementation();
        
    }

    private static synchronized void tryImplementation(final String testClassName, final String implClassName) {
        if (LOGGER_CONSTRUCTOR != null) {
            return;
        }

        try {
            Resources.classForName(testClassName);
            final Class<?> implClass = Resources.classForName(implClassName);
            LOGGER_CONSTRUCTOR = implClass.getConstructor(new Class[] { String.class });

            final Class<?> declareClass = LOGGER_CONSTRUCTOR.getDeclaringClass();
            if (!Logger.class.isAssignableFrom(declareClass)) {
                LOGGER_CONSTRUCTOR = null;
            }

            try {
                if (null != LOGGER_CONSTRUCTOR) {
                    LOGGER_CONSTRUCTOR.newInstance(LoggerFactory.class.getName());
                }
            } catch (final Throwable t) {
                LOGGER_CONSTRUCTOR = null;
            }
        } catch (Throwable t) {
            // skip
        }
    }
    
    public static synchronized void tryNoLoggingImplementation() {
        if (LOGGER_CONSTRUCTOR == null) {
            try {
                Class<?> implClass = Resources.classForName(NO_LOGGING_IMPL_CLASS_NAME);
                LOGGER_CONSTRUCTOR = implClass.getConstructor(String.class);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
    
    public static Logger getLogger(final Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(final String loggerName) {
        try {
            return (Logger) LOGGER_CONSTRUCTOR.newInstance(loggerName);
        } catch (final Throwable cause) {
            throw new LoggerException("Error creating logger for logger '" + loggerName + "'.  Cause: " + cause, cause);
        }
    }

    public static synchronized void selectJakartaCommonsLogging() {
        try {
            Resources.classForName(APACHE_COMMON_LOGGER);
            Class<?> implClass = Resources.classForName(JAKARTA_COMMONS_LOGGING_IMPL_CLASS_NAME);
            LOGGER_CONSTRUCTOR = implClass.getConstructor(new Class[] { String.class });
        } catch (final Throwable t) {
            //ignore
        }
    }
    
    public static synchronized void selectJdk14Logging() {
        try {
            Resources.classForName(JDK_LOGGER);
            Class<?> implClass = Resources.classForName(JDK14_LOGGING_IMPL_CLASS_NAME);
            LOGGER_CONSTRUCTOR = implClass.getConstructor(new Class[] { String.class });
        } catch (final Throwable t) {
            //ignore
        }
    }
    
    public static synchronized void selectLog4j2Logging() {
        try {
            Resources.classForName(LOG4J2_LOGGER);
            Class<?> implClass = Resources.classForName(LOG4J2_IMPL_CLASS_NAME);
            LOGGER_CONSTRUCTOR = implClass.getConstructor(new Class[] { String.class });
        } catch (final Throwable t) {
            //ignore
        }
    }
    
    public static synchronized void selectLog4jLogging() {
        try {
            Resources.classForName(LOG4J2_LOGGER);
            Class<?> implClass = Resources.classForName(LOG4J_IMPL_CLASS_NAME);
            LOGGER_CONSTRUCTOR = implClass.getConstructor(new Class[] { String.class });
        } catch (final Throwable t) {
            //ignore
        }
    }
    
    public static synchronized void selectSLF4JLogging() {
        try {
            Resources.classForName(SLF4J_LOGGRE);
            Class<?> implClass = Resources.classForName(SLF4J_IMPL_CLASS_NAME);
            LOGGER_CONSTRUCTOR = implClass.getConstructor(new Class[] { String.class });
        } catch (final Throwable t) {
            //ignore
        }
    }
    
    public static synchronized void selectNoLogging() {
        try {
            Class<?> implClass = Resources.classForName(NO_LOGGING_IMPL_CLASS_NAME);
            LOGGER_CONSTRUCTOR = implClass.getConstructor(new Class[] { String.class });
        } catch (final Throwable t) {
            //ignore
        }
    }
    
}
