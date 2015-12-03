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
package org.nanoframework.core.chain;

import java.lang.reflect.Method;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.core.chain.exception.ChainStepException;
import org.nanoframework.core.chain.exception.ChainStepInvokeException;

/**
 * @author yanghe
 * @date 2015年12月3日 下午2:06:59
 */
public class ChainStep extends BaseEntity {
	private Class<?> cls;
	private Object instance;
	private Method method;
	private Object[] parameter;
	private boolean _isStatic;
	
	public ChainStep() { }
	
	public ChainStep(String className, String methodName, Class<?>[] methodTypes, boolean isStatic) {
		this(className, methodName, methodTypes, null, isStatic);
	}
	
	public ChainStep(String className, String methodName, Class<?>[] methodTypes, Object[] parameter, boolean isStatic) {
		try { this.cls = Class.forName(className); } catch(ClassNotFoundException e) { throw new ChainStepException(e.getMessage(), e); }
		try { this.method = cls.getMethod(methodName, methodTypes); } catch(NoSuchMethodException e) { throw new ChainStepException(e.getMessage(), e); }
		this.parameter = parameter;
		this._isStatic = isStatic;
	}
	
	public ChainStep resetMethod(String methodName, Class<?>[] methodTypes, boolean isStatic) {
		try { this.method = cls.getMethod(methodName, methodTypes); } catch(NoSuchMethodException e) { throw new ChainStepException(e.getMessage(), e); }
		this._isStatic = isStatic;
		return this;
	}
	
	public Object invoke() {
		try {
			if(isStatic()) {
				return method.invoke(cls, parameter);
			} else {
				if(instance == null) {
					synchronized (this) {
						if(instance == null)
							instance = cls.newInstance();
					}
				}
				
				return method.invoke(instance, parameter);
			}
		} catch(Throwable e) {
			throw new ChainStepInvokeException(e.getMessage(), e);
		}
	}

	public Class<?> getCls() {
		return cls;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getParameter() {
		return parameter;
	}
	
	public void setParameter(Object[] parameter) {
		this.parameter = parameter;
	}

	public boolean isStatic() {
		return _isStatic;
	}

}
