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
package org.nanoframework.orm.jdbc.binding;

import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * 
 * @author yanghe
 * @since 1.2
 */
public final class JdbcTransactionalMethodInterceptor implements MethodInterceptor {

    private static final Class<?>[] CAUSE_TYPES = new Class[]{ Throwable.class };

    private static final Class<?>[] MESSAGE_CAUSE_TYPES = new Class[]{ String.class, Throwable.class };

    /**
     * This class logger.
     */
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method interceptedMethod = invocation.getMethod();
        JdbcTransactional transactional = interceptedMethod.getAnnotation(JdbcTransactional.class);
        
        // The annotation may be present at the class level instead
        if (transactional == null) {
            transactional = interceptedMethod.getDeclaringClass().getAnnotation(JdbcTransactional.class);
        }

        JdbcManager[] jdbcManager = GlobalJdbcManager.get(transactional.envId());
        if(jdbcManager == null || jdbcManager.length == 0)
        	throw new IllegalArgumentException("无法获取到JdbcManager，请检查配置是否正确。");
        
        boolean isSessionInherited = isManagedSessionStarted(jdbcManager);
        if (!isSessionInherited) {
        	 startManagedSession(transactional, jdbcManager);
        }

        Object object = null;
        boolean needsRollback = transactional.rollbackOnly();
        try {
            object = invocation.proceed();
        } catch (Throwable t) {
            needsRollback = true;
            throw convertThrowableIfNeeded(invocation, transactional, t);
        } finally {
            if (!isSessionInherited) {
                try {
                  if (needsRollback) {
                      rollback(jdbcManager);
                  } else {
                      commit(jdbcManager);
                  }
                } finally {
                  close(jdbcManager);
                }
            }
        }

        return object;
    }
    
    private boolean isManagedSessionStarted(JdbcManager[] jdbcManager) {
    	return jdbcManager[0].isManagedSessionStarted();
    }
    
    private void startManagedSession(JdbcTransactional transactional, JdbcManager[] jdbcManager) throws SQLException {
    	for(JdbcManager manager : jdbcManager) {
    		manager.startManagedSession(false);
    	}
    }
    
    private void rollback(JdbcManager[] jdbcManager) {
    	for(JdbcManager manager : jdbcManager) {
    		try { manager.rollback(); } catch(Throwable e) { }
    	}
    }
    
    private void commit(JdbcManager[] jdbcManager) throws SQLException {
    	for(JdbcManager manager : jdbcManager) {
    		manager.commit();
    	}
    }
    
    private void close(JdbcManager[] jdbcManager) {
    	for(JdbcManager manager : jdbcManager) {
    		try { manager.close(); } catch(Throwable e) { LOG.error(e.getMessage(), e); }
    	}
    }
    
    private Throwable convertThrowableIfNeeded(MethodInvocation invocation, JdbcTransactional transactional, Throwable t) {
        Method interceptedMethod = invocation.getMethod();
        
        // check the caught exception is declared in the invoked method
        for (Class<?> exceptionClass : interceptedMethod.getExceptionTypes()) {
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
            initargs = new Object[]{ errorMessage, t };
            initargsType = MESSAGE_CAUSE_TYPES;
        } else {
            initargs = new Object[]{ t };
            initargsType = CAUSE_TYPES;
        }

        Constructor<? extends Throwable> exceptionConstructor = getMatchingConstructor(transactional.rethrowExceptionsAs(), initargsType);
        Throwable rethrowEx = null;
        if (exceptionConstructor != null) {
            try {
                rethrowEx = exceptionConstructor.newInstance(initargs);
            } catch (Exception e) {
                errorMessage = format("Impossible to re-throw '%s', it needs the constructor with %s argument(s).",
                        transactional.rethrowExceptionsAs().getName(),
                        Arrays.toString(initargsType));
                LOG.error(errorMessage, e);
                rethrowEx = new RuntimeException(errorMessage, e);
            }
        } else {
            errorMessage = format("Impossible to re-throw '%s', it needs the constructor with %s or %s argument(s).",
                    transactional.rethrowExceptionsAs().getName(),
                    Arrays.toString(CAUSE_TYPES),
                    Arrays.toString(MESSAGE_CAUSE_TYPES));
            LOG.error(errorMessage);
            rethrowEx = new RuntimeException(errorMessage);
        }

        return rethrowEx;        
    }
    
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> Constructor<E> getMatchingConstructor(Class<E> type, Class<?>[] argumentsType) {
        Class<? super E> currentType = type;
        while (Object.class != currentType) {
            for (Constructor<?> constructor : currentType.getConstructors()) {
                if (Arrays.equals(argumentsType, constructor.getParameterTypes())) {
                    return (Constructor<E>) constructor;
                }
            }
            currentType = currentType.getSuperclass();
        }
        return null;
    }
    
}
