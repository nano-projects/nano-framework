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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nanoframework.commons.annatations.Property;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.concurrent.exception.QuartzException;

/**
 * 抽象Task类，对基本操作进行了封装
 * 
 * @author yanghe
 * @date 2015年6月8日 下午5:10:18 
 *
 */
public abstract class BaseQuartz extends Thread implements Runnable {

	protected static Logger LOG = LoggerFactory.getLogger(BaseQuartz.class);

	@Property(name = ID)
	private String id;
	
	@Property(name = SERVICE)
	private ThreadPoolExecutor service;
	
	private boolean close = false;
	
	@Property(name = BEFORE_AFTER_ONLY)
	private boolean beforeAfterOnly = false;
	
	private boolean isRunning = false;
	
	@Property(name = RUN_NUMBER_OF_TIMES)
	private int runNumberOfTimes = 0;
	
	private int nowTimes = 0;
	
	@Property(name = INTERVAL)
	private int interval = 0;
	
	@Property(name = NUM)
	private int num = 0;
	
	@Property(name = TOTAL)
	private int total = 0;
	
	public static final String ID = "id";
	public static final String SERVICE = "service";
	public static final String BEFORE_AFTER_ONLY = "beforeAfterOnly";
	public static final String RUN_NUMBER_OF_TIMES = "runNumberOfTimes";
	public static final String INTERVAL = "interval";
	public static final String NUM = "num";
	public static final String TOTAL = "total";
	
	Object LOCK = new Object();
	AtomicBoolean isLock = new AtomicBoolean(false);
	
	public BaseQuartz() {
		this(null, null, null, null, null);
	}
	
	public BaseQuartz(String id , Integer interval) {
		this(id, null, null, null, interval);
	}
			
	public BaseQuartz(String id, ThreadPoolExecutor service , Integer interval) {
		this(id, service, null, null, interval);
	}
	
	public BaseQuartz(String id, ThreadPoolExecutor service , Boolean beforeAfterOnly , Integer interval) {
		this(id, service, beforeAfterOnly, null, interval);
	}
	
	public BaseQuartz(String id , ThreadPoolExecutor service , Integer runNumberOfTimes , Integer interval) {
		this(id, service, null, runNumberOfTimes, interval);
		
	}
	
	public BaseQuartz(String id, ThreadPoolExecutor service , Boolean beforeAfterOnly , Integer runNumberOfTimes , Integer interval) {
		if(runNumberOfTimes != null && runNumberOfTimes < 0)
			throw new QuartzException("运行次数不能小于0.");
		
		this.id = id;
		this.service = service;
		
		if(beforeAfterOnly != null)
			this.beforeAfterOnly = beforeAfterOnly;
		
		if(runNumberOfTimes != null)
			this.runNumberOfTimes = runNumberOfTimes;
		
		if(interval != null)
			this.interval = interval;
	}
	
	@Override
	public void run() {
		try {
			while(!close && !service.isShutdown()) {
				if(beforeAfterOnly) {
					try {
						Throwable err = null;
						if(!isRunning) 
							try { before(); } catch(Throwable befErr) { err = befErr; }
						
						try { execute(); } catch(Throwable execErr) { err = execErr; }
						
						if(!isRunning) 
							try { after(); } catch(Throwable aftErr) { err = aftErr; }
						
						if(!isRunning)
							isRunning = true;
						
						if(err != null)
							throw err;
						
					} catch(Throwable e) {
						errorProcess(e);
						
					} finally {
						finallyProcess();
						
					}
					
				} else {
					try {
						Throwable err = null;
						try { before(); } catch(Throwable befErr) { err = befErr; }
						try { execute(); } catch(Throwable execErr) { err = execErr; }
						try { after(); } catch(Throwable aftErr) { err = aftErr; }
						
						if(err != null)
							throw err;
						
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
		LOG.error("任务运行异常: " + e.getMessage() , e);
		LOG.error("任务开始进入等待状态: 100ms");
		thisWait(100);
		
	}
	
	/**
	 * 逻辑调用结束后处理阶段
	 */
	private void finallyProcess() {
		if(service == null) 
			throw new QuartzException("ThreadPoolExecutor不能为空");
		
		if(!close && !service.isShutdown()) {
			if(runNumberOfTimes == 0) {
				thisWait(interval);			
				
			} else {
				nowTimes ++;
				if(nowTimes < runNumberOfTimes) {
					thisWait(interval);
					
				} else {
					LOG.debug("任务已完成，现在结束操作");
					close = true;
					
				}
			}
		}
		
	}
	
	/**
	 * 任务等待
	 * @param interval 等待时间
	 */
	protected void thisWait(int interval) {
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

	public String _getId() {
		return id;
	}

	public ExecutorService getService() {
		return service;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	public int getRunNumberOfTimes() {
		return runNumberOfTimes;
	}

	public int getInterval() {
		return interval;
	}
	
	public int getNum() {
		return num;
	}
	
	public int getTotal() {
		return total;
	}

	public boolean isClose() {
		return close;
	}

	public void setClose(boolean close) {
		this.close = close;
	}
	
}
