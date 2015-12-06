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

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.extension.concurrent.exception.QuartzException;

/**
 * 抽象Task类，对基本操作进行了封装
 * 
 * @author yanghe
 * @date 2015年6月8日 下午5:10:18 
 *
 */
public abstract class BaseQuartz implements Runnable, Cloneable {
	protected static Logger LOG = LoggerFactory.getLogger(BaseQuartz.class);
	
	private QuartzConfig config;
	private boolean close = true;
	private boolean isRunning = false;
	private int nowTimes = 0;
	private Object LOCK = new Object();
	private AtomicBoolean isLock = new AtomicBoolean(false);
	
	protected BaseQuartz() { }
	
	public BaseQuartz(QuartzConfig config) {
		Assert.notNull(config, "QuartzConfig must not be null");
		this.config = config;
	}
	
	@Override
	public void run() {
		try {
			while(!close && !config.getService().isShutdown()) {
				if(config.getBeforeAfterOnly()) {
					try {
						StringBuilder builder = null;
						if(!isRunning) 
							try { before(); } catch(Throwable befErr) {
								if(builder == null)
									builder = new StringBuilder();
								
								builder.append(befErr.getMessage()).append("\n");
							}
						
						try { execute(); } catch(Throwable execErr) { 
							if(builder == null)
								builder = new StringBuilder();

							builder.append(execErr.getMessage()).append("\n");
						}
						
						if(!isRunning) 
							try { after(); } catch(Throwable aftErr) { 
								if(builder == null)
									builder = new StringBuilder();

								builder.append(aftErr.getMessage()).append("\n");
							}
						
						if(!isRunning)
							isRunning = true;
						
						if(builder != null)
							throw new QuartzException(builder.toString());
						
					} catch(Throwable e) {
						errorProcess(e);
						
					} finally {
						finallyProcess();
						
					}
					
				} else {
					try {
						StringBuilder builder = null;
						try { before(); } catch(Throwable befErr) { 
							if(builder == null)
								builder = new StringBuilder();

							builder.append(befErr.getMessage()).append("\n");
						}
						
						try { execute(); } catch(Throwable execErr) { 
							if(builder == null)
								builder = new StringBuilder();

							builder.append(execErr.getMessage()).append("\n");
						}
						try { after(); } catch(Throwable aftErr) { 
							if(builder == null)
								builder = new StringBuilder();

							builder.append(aftErr.getMessage()).append("\n");
						}
						
						if(builder != null)
							throw new QuartzException(builder.toString());
						
					} catch(Throwable e) {
						errorProcess(e);
						
					} finally {
						finallyProcess();
						
					}
				}
			}
			
		} finally {
			QuartzFactory.getInstance().unbind(this);
			destroy();
			
		}
	}
	
	/**
	 * 异常处理
	 * @param e 异常
	 */
	private void errorProcess(Throwable e) {
		LOG.error("任务运行异常: " + e.getMessage() + ", 任务开始进入等待状态: 100ms", e);
		thisWait(100);
	}
	
	/**
	 * 逻辑调用结束后处理阶段
	 */
	private void finallyProcess() {
		if(config.getService() == null) 
			throw new QuartzException("ThreadPoolExecutor不能为空");
		
		if(!close && !config.getService().isShutdown()) {
			long _interval = config.getInterval();
			if(config.getCron() != null) {
				long now;
				_interval = config.getCron().getNextValidTimeAfter(new Date(now = System.currentTimeMillis())).getTime() - now;
			}
			
			if(config.getRunNumberOfTimes() == 0) {
				thisWait(_interval);			
				
			} else {
				nowTimes ++;
				if(nowTimes < config.getRunNumberOfTimes()) {
					thisWait(_interval);
					
				} else {
					close = true;
				}
			}
		}
		
	}
	
	/**
	 * 任务等待
	 * @param interval 等待时间
	 */
	protected void thisWait(long interval) {
		if(interval > 0) {
			synchronized (LOCK) {
				try { isLock.set(true); LOCK.wait(interval); } catch(InterruptedException e) { } finally { isLock.set(false); }
			}
		}
	}
	
	public void thisNotify() {
		if(isLock.get()) {
			synchronized (LOCK) {
				try { LOCK.notify(); } catch(Exception e) { } finally { isLock.set(false); }
			}
		}
	}
	
	/**
	 * 逻辑执行前操作
	 * @throws QuartzException 任务异常
	 */
	public abstract void before() throws QuartzException;
	
	/**
	 * 逻辑执行操作
	 * @throws QuartzException 任务异常
	 */
	public abstract void execute() throws QuartzException;
	
	/**
	 * 逻辑执行后操作
	 * @throws QuartzException 任务异常
	 */
	public abstract void after() throws QuartzException;
	
	/**
	 * 任务结束后销毁资源操作
	 * @throws QuartzException 任务异常
	 */
	public abstract void destroy() throws QuartzException;

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isClose() {
		return close;
	}

	public void setClose(boolean close) {
		this.close = close;
	}

	public QuartzConfig getConfig() {
		return config;
	}
	
	public void setConfig(QuartzConfig config) {
		this.config = config;
	}
	
	@Override
	public BaseQuartz clone() {
		try {
			return (BaseQuartz) super.clone();
		} catch(CloneNotSupportedException e) {
			throw new QuartzException(e.getMessage(), e);
		}
	}
}
