/**
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
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
 * @date 2015年10月8日 下午6:28:26
 */
public class BeforeAndAfterMoreInterceptor implements MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		BeforeAndAfterMore beforeAndAfterMore = invocation.getMethod().getAnnotation(BeforeAndAfterMore.class);
		Map<Method, Object> beforeMap = Maps.newLinkedHashMap();
		Map<Method, Object> afterMap = Maps.newLinkedHashMap();
		for(BeforeAndAfter beforeAndAfter : beforeAndAfterMore.beforeAndAfters()) {
			Method beforeMethod = beforeAndAfter.classType().getMethod(MethodNames.BEFORE, MethodInvocation.class);
			Object beforeInstance = Globals.get(Injector.class).getInstance(beforeAndAfter.classType());
			beforeMap.put(beforeMethod, beforeInstance);
			
			Method afterMethod = beforeAndAfter.classType().getMethod(MethodNames.AFTER, MethodInvocation.class, Object.class);
			Object afterInstance = Globals.get(Injector.class).getInstance(beforeAndAfter.classType());
			afterMap.put(afterMethod, afterInstance);
		}
		
		Object obj = null;
		try { 
			for(Iterator<Entry<Method, Object>> iter = beforeMap.entrySet().iterator(); iter.hasNext(); ) {
				Entry<Method, Object> entry = iter.next();
				entry.getKey().invoke(entry.getValue(), invocation);
			}
			
			return obj = invocation.proceed();
		} finally {
			for(Iterator<Entry<Method, Object>> iter = afterMap.entrySet().iterator(); iter.hasNext(); ) {
				Entry<Method, Object> entry = iter.next();
				entry.getKey().invoke(entry.getValue(), invocation, obj);
			}
		}
	}

}
