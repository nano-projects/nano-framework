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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
	private boolean closed = true;
	private boolean remove = false;
	private boolean isRunning = false;
	private int nowTimes = 0;
	private Object LOCK = new Object();
	private AtomicBoolean isLock = new AtomicBoolean(false);
	private static Map<String, AtomicLong> index = new HashMap<String, AtomicLong>();
	
	public BaseQuartz() { }
	
	public BaseQuartz(QuartzConfig config) {
		Assert.notNull(config, "QuartzConfig must not be null");
		if(config.getRunNumberOfTimes() != null && config.getRunNumberOfTimes() < 0)
			throw new QuartzException("运行次数不能小于0.");
		
		this.config = config;
	}
	
	@Override
	public void run() {
		try {
			try {
				if(config.getLazy()) {
					long delay = delay();
					LOG.warn("启动延时: " + delay + "ms");
					thisWait(delay);
				}
			} catch(Throwable e) {
				LOG.error("Lazy error: " + e.getMessage());
			}
			
			close = false;
			closed = false;
			remove = false;
			while(!close && !config.getService().isShutdown()) {
				if(config.getBeforeAfterOnly()) {
					try {
						if(!isRunning) 
							try { before(); } catch(Throwable e) {
								LOG.error("任务运行异常(before): " + e.getMessage(), e);
							}
						
						try { execute(); } catch(Throwable e) { 
							LOG.error("任务运行异常(execute): " + e.getMessage(), e);
						}
						
						if(!isRunning) 
							try { after(); } catch(Throwable e) { 
								LOG.error("任务运行异常(after): " + e.getMessage(), e);
							}
						
						if(!isRunning)
							isRunning = true;
						
					} catch(Throwable e) {
						LOG.error("任务运行异常: " + e.getMessage(), e);
						thisWait(100);
						
					} finally {
						finallyProcess();
					}
					
				} else {
					try {
						try { before(); } catch(Throwable e) { 
							LOG.error("任务运行异常(before): " + e.getMessage(), e);
						}
						
						try { execute(); } catch(Throwable e) { 
							LOG.error("任务运行异常(execute): " + e.getMessage(), e);
						}
						try { after(); } catch(Throwable e) { 
							LOG.error("任务运行异常(after): " + e.getMessage(), e);
						}
						
					} catch(Throwable e) {
						LOG.error("任务运行异常: " + e.getMessage(), e);
						thisWait(100);
						
					} finally {
						finallyProcess();
					}
				}
			}
			
		} finally {
			closed = true;
			QuartzFactory.getInstance().unbind(this);
			destroy();
			
		}
	}
	
	/**
	 * 逻辑调用结束后处理阶段
	 */
	private void finallyProcess() {
		if(config.getService() == null) 
			throw new QuartzException("ThreadPoolExecutor不能为空");
		
		if(!close && !config.getService().isShutdown()) {
			long interval = delay();
			if(config.getRunNumberOfTimes() == 0) {
				thisWait(interval);			
				
			} else {
				nowTimes ++;
				if(nowTimes < config.getRunNumberOfTimes()) {
					thisWait(interval);
					
				} else {
					close = true;
					nowTimes = 0;
				}
			}
		}
		
	}
	
	private long delay() {
		long interval = config.getInterval();
		if(config.getCron() != null) {
			long now;
			interval = config.getCron().getNextValidTimeAfter(new Date(now = System.currentTimeMillis())).getTime() - now;
		}
		
		return interval;
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
	
	protected void thisWait() {
		synchronized (LOCK) {
			try { isLock.set(true); LOCK.wait(); } catch(InterruptedException e) { } finally { isLock.set(false); }
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
	
	public boolean isClosed() {
		return closed;
	}

	public void setClose(boolean close) {
		this.close = close;
	}
	
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	public void setRemove(boolean remove) {
		this.remove = remove;
	}
	
	public boolean isRemove() {
		return remove;
	}

	public QuartzConfig getConfig() {
		return config;
	}
	
	public void setConfig(QuartzConfig config) {
		this.config = config;
	}
	
	public long getIndex(String group) {
		AtomicLong idx;
		if((idx = index.get(group)) == null)
			index.put(group, idx = new AtomicLong());
		
		return idx.getAndIncrement();
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
