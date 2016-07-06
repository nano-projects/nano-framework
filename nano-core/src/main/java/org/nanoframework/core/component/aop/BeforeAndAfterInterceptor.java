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
package org.nanoframework.core.component.aop;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.nanoframework.core.globals.Globals;

import com.google.inject.Injector;

/**
 * @author yanghe
 * @since 1.0
 */
public class BeforeAndAfterInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        BeforeAndAfter beforeAndAfter = invocation.getMethod().getAnnotation(BeforeAndAfter.class);
        Object instance = Globals.get(Injector.class).getInstance(beforeAndAfter.value());
        Method beforeMethod = beforeAndAfter.value().getMethod(MethodNames.BEFORE, MethodInvocation.class);
        Method afterMethod = beforeAndAfter.value().getMethod(MethodNames.AFTER, MethodInvocation.class, Object.class);

        Object obj = null;
        try {
            beforeMethod.invoke(instance, invocation);
            return obj = invocation.proceed();
        } catch (Throwable e) {
            obj = e;
            throw e;

        } finally {
            afterMethod.invoke(instance, invocation, obj);
        }
    }

}
