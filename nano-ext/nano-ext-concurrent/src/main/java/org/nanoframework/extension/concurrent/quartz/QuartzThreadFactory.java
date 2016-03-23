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
package org.nanoframework.extension.concurrent.quartz;

import java.util.concurrent.ThreadFactory;

/**
 * This is a stupid name, now renamed to {@link org.nanoframework.extension.concurrent.scheduler.SchedulerThreadFactory}
 * The next version will be removed
 * 
 * @author yanghe
 * @date 2015年12月03日 下午8:31:13 
 */
@Deprecated
public class QuartzThreadFactory implements ThreadFactory {
	private BaseQuartz baseQuartz;

	@Override
	public Thread newThread(Runnable runnable) {
		if(baseQuartz == null) {
			Thread thread = new Thread(runnable);
			thread.setName("Quartz-Thread-" + System.currentTimeMillis());
			return thread;
		}
		
		Thread thread = new Thread(baseQuartz);
		thread.setName(baseQuartz.getConfig().getName());
		thread.setDaemon(baseQuartz.getConfig().getDaemon());
		return thread;
	}

	public void setBaseQuartz(BaseQuartz baseQuartz) {
		this.baseQuartz = baseQuartz;
	}
}