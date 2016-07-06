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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 全局Jdbc数据源管理类<br>
 * 通过Guice Inject后初始化JdbcManager<br>
 * 存储方式为 {数据源名称 - JdbcManager} 
 * 
 * @author yanghe
 *@since 1.2
 */
public class GlobalJdbcManager {
	private static ConcurrentMap<String, JdbcManager> globals = new ConcurrentHashMap<>();

	private GlobalJdbcManager() { }

	public static void set(String type, JdbcManager global) {
		globals.put(type, global);

	}

	public static final JdbcManager get(String type) {
		return globals.get(type);
	}
	
	public static final JdbcManager[] get(String... types) {
		if(types.length > 0) {
			List<JdbcManager> managers = new ArrayList<>();
			for(String type : types) {
				JdbcManager manager = globals.get(type);
				if(manager == null) 
					throw new IllegalArgumentException("无效的数据源名称: " + type);
				
				managers.add(manager);
			}
			
			return managers.toArray(new JdbcManager[managers.size()]);
		}
		
		return null;
	}

	public static final Set<String> keys() {
		return globals.keySet();
	}
}
