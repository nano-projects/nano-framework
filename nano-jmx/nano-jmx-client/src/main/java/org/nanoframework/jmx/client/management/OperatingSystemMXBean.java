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
 * @author yanghe
 * @since 1.2.x
 */
@SuppressWarnings("restriction")
public interface OperatingSystemMXBean extends com.sun.management.OperatingSystemMXBean {

	/**
	 * 获取1秒间隔的cpu使用率，使用率自动除去cpu核心数以获取相对服务器的cpu使用
	 * @return
	 */
	public double cpuRatio();
	
	/**
	 * 获取?毫秒间隔的cpu使用率，使用率自动除去cpu核心数以获取相对服务器的cpu使用
	 * @param time 毫秒
	 * @return
	 */
	public double cpuRatio(long time);
	
	/**
	 * 获取?毫秒间隔的cpu使用率，根据ifAvaProc参数定制使用率是否除去cpu核心数以获取相对服务器的cpu使用
	 * @param time 毫秒
	 * @param ifAvaProc 是否除去获取相对服务器cpu的使用率
	 * @return
	 */
	public double cpuRatio(long time, boolean ifAvaProc);
}
