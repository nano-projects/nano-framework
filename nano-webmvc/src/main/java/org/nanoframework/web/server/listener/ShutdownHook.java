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
package org.nanoframework.web.server.listener;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * 
 * @author yanghe
 * @date 2015年7月25日 下午8:30:06 
 *
 */
public class ShutdownHook implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(ShutdownHook.class);
	
	@Override
	public void run() {
		try { 
			Class<?> blockingQueueFactory = Class.forName("org.nanoframework.extension.concurrent.queue.BlockingQueueFactory");
			LOG.info("等待队列中的所有元素被执行完成后停止系统");
			while((int) blockingQueueFactory.getMethod("howManyElementInQueues").invoke(blockingQueueFactory) > 0) Thread.sleep(10L); 
			LOG.info("队列中的所有元素已被执行完成");
			
			Class<?> quartzFactory = Class.forName("org.nanoframework.extension.concurrent.quartz.QuartzFactory");
			long time = System.currentTimeMillis();
			LOG.info("开始停止任务调度");
			quartzFactory.getMethod("closeAll").invoke(quartzFactory);
			Collection<?> quartzs = (Collection<?>) quartzFactory.getMethod("getQuartzs").invoke(quartzFactory);
			for(Object item : quartzs) {
				item.getClass().getMethod("thisNotify").invoke(item);
			}
			
			while((int) quartzFactory.getMethod("getQuartzSize").invoke(quartzFactory) > 0 && System.currentTimeMillis() - time < 300000L) Thread.sleep(10L);
			LOG.info("停止任务调度完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		} catch(InterruptedException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			LOG.error("没有加载Concurrent扩展或调用异常: " + e.getMessage());
		}
	}
	
}