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
package org.nanoframework.orm.mybatis;

import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionManager;

/**
 * Method interceptor for {@link MultiTransactional} annotation.
 * @author yanghe
 * @since 1.2
 */
public final class MultiTransactionalMethodInterceptor implements MethodInterceptor {

    private static final Class<?>[] CAUSE_TYPES = new Class[] { Throwable.class };

    private static final Class<?>[] MESSAGE_CAUSE_TYPES = new Class[] { String.class, Throwable.class };

    /**
     * This class logger.
     */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * {@inheritDoc}
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Method interceptedMethod = invocation.getMethod();
        MultiTransactional transactional = interceptedMethod.getAnnotation(MultiTransactional.class);

        // The annotation may be present at the class level instead
        if (transactional == null) {
            transactional = interceptedMethod.getDeclaringClass().getAnnotation(MultiTransactional.class);
        }

        final SqlSessionManager[] sqlSessionManager = GlobalSqlSession.get(transactional.envId());
        if (sqlSessionManager == null || sqlSessionManager.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug(format("没有配置数据源名称，不开启事务，直接执行数据库操作"));
            }

            return invocation.proceed();
        }

        final boolean isSessionInherited = isManagedSessionStarted(sqlSessionManager);
        if (!isSessionInherited) {
            startManagedSession(transactional, sqlSessionManager);
        }

        Object object = null;
        boolean needsRollback = transactional.rollbackOnly();
        try {
            object = invocation.proceed();
        } catch (final Throwable t) {
            needsRollback = true;
            throw convertThrowableIfNeeded(invocation, transactional, t);
        } finally {
            if (!isSessionInherited) {
                try {
                    if (needsRollback) {
                        rollback(true, sqlSessionManager);
                    } else {
                        commit(transactional.force(), sqlSessionManager);
                    }
                } finally {
                    close(sqlSessionManager);
                }
            }
        }

        return object;
    }

    private boolean isManagedSessionStarted(final SqlSessionManager[] sqlSessionManager) {
        for (final SqlSessionManager manager : sqlSessionManager) {
            if (!manager.isManagedSessionStarted()) {
                return false;
            }
        }

        return true;
    }

    private void startManagedSession(final MultiTransactional transactional, final SqlSessionManager[] sqlSessionManager) {
        for (final SqlSessionManager manager : sqlSessionManager) {
            if (!manager.isManagedSessionStarted()) {
                manager.startManagedSession(transactional.executorType(), transactional.isolation().getTransactionIsolationLevel());
            }
        }
    }

    private void rollback(final boolean force, final SqlSessionManager[] sqlSessionManager) {
        for (final SqlSessionManager manager : sqlSessionManager) {
            try {
                manager.rollback(true);
            } catch (final Throwable e) {
                // ignore
            }
        }
    }

    private void commit(final boolean force, final SqlSessionManager[] sqlSessionManager) {
        for (final SqlSessionManager manager : sqlSessionManager) {
            manager.commit(true);
        }
    }

    private void close(final SqlSessionManager[] sqlSessionManager) {
        for (final SqlSessionManager manager : sqlSessionManager) {
            try {
                manager.close();
            } catch (final Throwable e) {
                // ignore
            }
        }
    }

    private Throwable convertThrowableIfNeeded(final MethodInvocation invocation, final MultiTransactional transactional, final Throwable t) {
        final Method interceptedMethod = invocation.getMethod();
        // check the caught exception is declared in the invoked method
        for (final Class<?> exceptionClass : interceptedMethod.getExceptionTypes()) {
            if (exceptionClass.isAssignableFrom(t.getClass())) {
                return t;
            }
        }

        // check the caught exception is of same rethrow type
        if (transactional.rethrowExceptionsAs().isAssignableFrom(t.getClass())) {
            return t;
        }

        // rethrow the exception as new exception
        String errorMessage;
        Object[] initargs;
        Class<?>[] initargsType;

        if (transactional.exceptionMessage().length() != 0) {
            errorMessage = format(transactional.exceptionMessage(), invocation.getArguments());
            initargs = new Object[] { errorMessage, t };
            initargsType = MESSAGE_CAUSE_TYPES;
        } else {
            initargs = new Object[] { t };
            initargsType = CAUSE_TYPES;
        }

        final Constructor<? extends Throwable> exceptionConstructor = getMatchingConstructor(transactional.rethrowExceptionsAs(), initargsType);
        Throwable rethrowEx = null;
        if (exceptionConstructor != null) {
            try {
                rethrowEx = exceptionConstructor.newInstance(initargs);
            } catch (final Exception e) {
                errorMessage = format("Impossible to re-throw '%s', it needs the constructor with %s argument(s).",
                        transactional.rethrowExceptionsAs().getName(), Arrays.toString(initargsType));
                log.error(errorMessage, e);
                rethrowEx = new RuntimeException(errorMessage, e);
            }
        } else {
            errorMessage = format("Impossible to re-throw '%s', it needs the constructor with %s or %s argument(s).",
                    transactional.rethrowExceptionsAs().getName(), Arrays.toString(CAUSE_TYPES), Arrays.toString(MESSAGE_CAUSE_TYPES));
            log.error(errorMessage);
            rethrowEx = new RuntimeException(errorMessage);
        }

        return rethrowEx;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> Constructor<E> getMatchingConstructor(final Class<E> type, final Class<?>[] argumentsType) {
        Class<? super E> currentType = type;
        while (Object.class != currentType) {
            for (final Constructor<?> constructor : currentType.getConstructors()) {
                if (Arrays.equals(argumentsType, constructor.getParameterTypes())) {
                    return (Constructor<E>) constructor;
                }
            }
            currentType = currentType.getSuperclass();
        }
        return null;
    }

}
