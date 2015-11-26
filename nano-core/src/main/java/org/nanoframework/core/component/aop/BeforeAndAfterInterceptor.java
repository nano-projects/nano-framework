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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.nanoframework.core.globals.Globals;

import com.google.inject.Injector;

/**
 * @author yanghe
 * @date 2015年10月8日 下午6:28:26
 */
public class BeforeAndAfterInterceptor implements MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		BeforeAndAfter beforeAndAfter = invocation.getMethod().getAnnotation(BeforeAndAfter.class);
		Method beforeMethod = beforeAndAfter.classType().getMethod(beforeAndAfter.beforeMethodName(), MethodInvocation.class);
		Object beforeInstance;
		if(beforeAndAfter.singleton()) {
			if((beforeInstance = Globals.get(beforeAndAfter.classType())) == null) {
				beforeInstance = Globals.get(Injector.class).getInstance(beforeAndAfter.classType());
				Globals.set(beforeAndAfter.classType(), beforeInstance);
			}
		} else 
			beforeInstance = beforeAndAfter.classType().newInstance();
		
		Method afterMethod = beforeAndAfter.classType().getMethod(beforeAndAfter.afterMethodName(), MethodInvocation.class, Object.class);
		Object afterInstance;
		if(beforeAndAfter.singleton()) {
			if((afterInstance = Globals.get(beforeAndAfter.classType())) == null) {
				afterInstance = Globals.get(Injector.class).getInstance(beforeAndAfter.classType());
				Globals.set(beforeAndAfter.classType(), afterInstance);
			}
		} else 
			afterInstance = beforeAndAfter.classType().newInstance();
		
		Object obj = null;
		try { 
			beforeMethod.invoke(beforeInstance, invocation);
			return obj = invocation.proceed();
		} finally {
			afterMethod.invoke(afterInstance, invocation, obj);
		}
	}

}
