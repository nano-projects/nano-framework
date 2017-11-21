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
package org.nanoframework.core.inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yanghe
 * @since 1.4.10
 */
public abstract class AbstractMethodInjectInterceptor implements MethodInterceptor {
    private static final String SET_METHOD_PREFIX = "set";
    private static final String GUICE_ENHANCER_SEGMENT = "$$EnhancerByGuice";

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Object instance = invocation.proceed();
        if (instance != null) {
            return instance;
        }

        final ReentrantLock lock = this.lock;
        try {
            lock.lock();
            instance = invocation.proceed();
            if (instance != null) {
                return instance;
            }

            final Method method = invocation.getMethod();
            final Class<?> returnType = method.getReturnType();
            inject(invocation, method, returnType);
            return invocation.proceed();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 设置依赖注入.
     *
     * @param proxy       代理实现
     * @param method      注入方法
     * @param returnType  方法返回类型
     * @param newInstance 注入实现
     * @throws Throwable 异常
     */
    protected void setInstance(final Object proxy, final Method method, final Class<?> returnType, final Object newInstance) throws Throwable {
        final String methodName = method.getName();
        final String setMethodName = SET_METHOD_PREFIX + methodName.substring(SET_METHOD_PREFIX.length());
        final String className = proxy.getClass().getName();
        final Class<?> cls = Class.forName(className.substring(0, className.indexOf(GUICE_ENHANCER_SEGMENT)));
        cls.getMethod(setMethodName, returnType).invoke(proxy, newInstance);
    }

    /**
     * @param invocation 拦截器切入点
     * @param method     注入方法
     * @param returnType 返回类型
     * @throws Throwable 异常
     */
    protected abstract void inject(MethodInvocation invocation, Method method, Class<?> returnType) throws Throwable;
}
