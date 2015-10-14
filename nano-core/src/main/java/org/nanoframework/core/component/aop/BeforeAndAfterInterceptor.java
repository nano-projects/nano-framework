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
		Before before = beforeAndAfter.before();
		Method beforeMethod = before.classType().getMethod(before.methodName(), MethodInvocation.class);
		Object beforeInstance;
		if(before.singleton()) {
			if((beforeInstance = Globals.get(before.classType())) == null) {
				beforeInstance = Globals.get(Injector.class).getInstance(before.classType());
				Globals.set(before.classType(), beforeInstance);
			}
		} else 
			beforeInstance = before.classType().newInstance();
		
		After after = beforeAndAfter.after();
		Method afterMethod = after.classType().getMethod(after.methodName(), MethodInvocation.class);
		Object afterInstance;
		if(after.singleton()) {
			if((afterInstance = Globals.get(after.classType())) == null) {
				afterInstance = Globals.get(Injector.class).getInstance(after.classType());
				Globals.set(after.classType(), afterInstance);
			}
		} else 
			afterInstance = after.classType().newInstance();
		
		try { 
			beforeMethod.invoke(beforeInstance, invocation);
			return invocation.proceed();
		} finally {
			afterMethod.invoke(afterInstance, invocation);
		}
	}

}
