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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.nanoframework.core.globals.Globals;

import com.google.common.collect.Maps;
import com.google.inject.Injector;

/**
 * @author yanghe
 * @date 2016年3月16日 下午1:23:21
 */
public class AfterMoreInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        AfterMore afterMore = invocation.getMethod().getAnnotation(AfterMore.class);
        After[] afters = afterMore.value();
        Map<Method, Object> map = Maps.newLinkedHashMap();
        for (After after : afters) {
            Method method = after.value().getMethod(MethodNames.AFTER, MethodInvocation.class, Object.class);
            Object instance = Globals.get(Injector.class).getInstance(after.value());
            map.put(method, instance);
        }

        Object obj = null;
        try {
            return obj = invocation.proceed();
        } catch (Throwable e) {
            obj = e;
            throw e;

        } finally {
            for (Iterator<Entry<Method, Object>> iter = map.entrySet().iterator(); iter.hasNext();) {
                Entry<Method, Object> entry = iter.next();
                entry.getKey().invoke(entry.getValue(), invocation, obj);
            }
        }
    }

}
