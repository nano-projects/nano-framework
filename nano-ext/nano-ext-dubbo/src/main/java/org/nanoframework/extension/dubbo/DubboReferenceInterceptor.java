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
package org.nanoframework.extension.dubbo;

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;

/**
 * 
 *
 * @author yanghe
 * @since 1.4.1
 */
public class DubboReferenceInterceptor implements MethodInterceptor {
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
            final ReferenceConfig<?> refer = createRefer(method, returnType);
            final Object newInstance = ReferenceConfigCache.getCache().get(refer);
            setInstance(invocation.getThis(), method, returnType, newInstance);
            return invocation.proceed();
        } finally {
            lock.unlock();
        }
    }

    protected ReferenceConfig<?> createRefer(final Method method, final Class<?> returnType) {
        final Reference reference = method.getAnnotation(Reference.class);
        final ReferenceConfig<?> refer = new ReferenceConfig<>(reference);
        refer.setCheck(reference.check());
        refer.setInterface(returnType);
        return refer;
    }

    protected void setInstance(final Object proxy, final Method method, final Class<?> returnType, final Object newInstance) throws Throwable {
        final String methodName = method.getName();
        final String setMethodName = "set" + methodName.substring(3);
        final String className = proxy.getClass().getName();
        final Class<?> cls = Class.forName(className.substring(0, className.indexOf("$$EnhancerByGuice")));
        cls.getMethod(setMethodName, returnType).invoke(proxy, newInstance);
    }
}