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

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private String id;
	
	private ThreadPoolExecutor service;
	
	private boolean close = false;
	
	private boolean beforeAfterOnly = false;
	
	private boolean isRunning = false;
	
	private int runNumberOfTimes = 0;
	
	private int nowTimes = 0;
	
	private long interval = 0;
	
	private int num = 0;
	
	private int total = 0;
	
	private CronExpression cron;
	
	Object LOCK = new Object();
	AtomicBoolean isLock = new AtomicBoolean(false);
	
	public BaseQuartz() {
		
	}
	
	public BaseQuartz(String id , Integer interval) {
		this(id, null, null, null, interval);
	}
	
	public BaseQuartz(String id , String cron) {
		this(id, null, null, null, cron);
	}
			
	public BaseQuartz(String id, ThreadPoolExecutor service , Integer interval) {
		this(id, service, null, null, interval);
	}
	
	public BaseQuartz(String id, ThreadPoolExecutor service , String cron) {
		this(id, service, null, null, cron);
	}
	
	public BaseQuartz(String id, ThreadPoolExecutor service , Boolean beforeAfterOnly , Integer interval) {
		this(id, service, beforeAfterOnly, null, interval);
	}
	
	public BaseQuartz(String id, ThreadPoolExecutor service , Boolean beforeAfterOnly , String cron) {
		this(id, service, beforeAfterOnly, null, cron);
	}
	
	public BaseQuartz(String id , ThreadPoolExecutor service , Integer runNumberOfTimes , Integer interval) {
		this(id, service, null, runNumberOfTimes, interval);
	}
	
	public BaseQuartz(String id , ThreadPoolExecutor service , Integer runNumberOfTimes , String cron) {
		this(id, service, null, runNumberOfTimes, cron);
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
	
	public BaseQuartz(String id, ThreadPoolExecutor service , Boolean beforeAfterOnly , Integer runNumberOfTimes , String cron) {
		if(runNumberOfTimes != null && runNumberOfTimes < 0)
			throw new QuartzException("运行次数不能小于0.");
		
		this.id = id;
		this.service = service;
		
		if(beforeAfterOnly != null)
			this.beforeAfterOnly = beforeAfterOnly;
		
		if(runNumberOfTimes != null)
			this.runNumberOfTimes = runNumberOfTimes;
		
		if(cron != null)
			try { this.cron = new CronExpression(cron); } catch(ParseException e) { throw new QuartzException(e.getMessage(), e); }
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
		LOG.error("任务运行异常: " + e.getMessage() + ", 任务开始进入等待状态: 100ms" , e);
		thisWait(100);
	}
	
	/**
	 * 逻辑调用结束后处理阶段
	 */
	private void finallyProcess() {
		if(service == null) 
			throw new QuartzException("ThreadPoolExecutor不能为空");
		
		if(!close && !service.isShutdown()) {
			long _interval = interval;
			if(cron != null) {
				long now;
				_interval = cron.getNextValidTimeAfter(new Date(now = System.currentTimeMillis())).getTime() - now;
			}
			
			if(runNumberOfTimes == 0) {
				thisWait(_interval);			
				
			} else {
				nowTimes ++;
				if(nowTimes < runNumberOfTimes) {
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

	public String _getId() {
		return id;
	}
	
	protected void setId(String id) {
		this.id = id;
	}

	public ThreadPoolExecutor getService() {
		return service;
	}
	
	protected void setService(ThreadPoolExecutor service) {
		this.service = service;
	}

	protected void setBeforeAfterOnly(boolean beforeAfterOnly) {
		this.beforeAfterOnly = beforeAfterOnly;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	public int getRunNumberOfTimes() {
		return runNumberOfTimes;
	}
	
	protected void setRunNumberOfTimes(int runNumberOfTimes) {
		this.runNumberOfTimes = runNumberOfTimes;
	}

	public long getInterval() {
		return interval;
	}
	
	protected void setInterval(long interval) {
		this.interval = interval;
	}
	
	public int getNum() {
		return num;
	}
	
	protected void setNum(int num) {
		this.num = num;
	}
	
	public int getTotal() {
		return total;
	}
	
	protected void setTotal(int total) {
		this.total = total;
	}

	public boolean isClose() {
		return close;
	}

	public void setClose(boolean close) {
		this.close = close;
	}
	
	public CronExpression getCron() {
		return cron;
	}
	
	protected void setCron(CronExpression cron) {
		this.cron = cron;
	}
}
