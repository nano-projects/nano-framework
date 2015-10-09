/**
 * Copyright 2015- the original author or authors.
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
package org.nanoframework.core.globals;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 全局变量，针对一些全局的属性做统一管理
 * 
 * @author yanghe
 * @date 2015年6月26日 上午8:38:08 
 *
 */
public class Globals {
	
	private static ConcurrentMap<Class<?>, Object> globals = new ConcurrentHashMap<>();
	
	private Globals() { }
	
	public static void set(Class<?> clz, Object global) {
		globals.put(clz, global);
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T get(Class<T> clz) {
		return (T) globals.get(clz);
	}
	
}
