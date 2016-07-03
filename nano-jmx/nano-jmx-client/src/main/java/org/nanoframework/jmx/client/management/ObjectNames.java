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
package org.nanoframework.jmx.client.management;

/**
 * MemoryManagerMXBean与GarbageCollectorMXBean的name枚举 <br>
 * MemoryManagerMXBean: MetaspaceManager, CodeCacheManager <br>
 * GarbageCollectorMXBean: ParNew, ConcurrentMarkSweep <br>
 * Other为都可用
 * 
 * @author yanghe
 * @date 2015年8月18日 下午10:57:23 
 * @since 1.1
 */
public enum ObjectNames {
	Other("*"), 
	MetaspaceManager("name=Metaspace Manager"), CodeCacheManager("name=CodeCacheManager"), 
	ParNew("name=ParNew"), ConcurrentMarkSweep("name=ConcurrentMarkSweep");

	private String value;

	private ObjectNames(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}