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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.binary.StringUtils;
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
	private long interval = 0;
	
	@Property(name = NUM)
	private int num = 0;
	
	@Property(name = TOTAL)
	private int total = 0;
	
	@Property(name = CRONTAB)
	private String crontab = DEF_CRONTAB;
	
	public static final String ID = "id";
	public static final String SERVICE = "service";
	public static final String BEFORE_AFTER_ONLY = "beforeAfterOnly";
	public static final String RUN_NUMBER_OF_TIMES = "runNumberOfTimes";
	public static final String INTERVAL = "interval";
	public static final String NUM = "num";
	public static final String TOTAL = "total";
	public static final String CRONTAB = "crontab";
	public static final String DEF_CRONTAB = "* * * * * *";
	public static final String OTHER_TIME = "*";
	
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
			long _interval = interval;
			if(!StringUtils.equals(crontab, DEF_CRONTAB)) {
				_interval = calcInterval();
				LOG.debug(getId() + " now to deley: " + (_interval) + "ms");
			}
			
			if(runNumberOfTimes == 0) {
				thisWait(_interval);			
				
			} else {
				nowTimes ++;
				if(nowTimes < runNumberOfTimes) {
					thisWait(_interval);
					
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
	
	private long calcInterval() {
		String[] times = crontab.split(" ");
		String week = times[0];
		String month = times[1];
		String day = times[2];
		String hour = times[3];
		String minute = times[4];
		String second = times[5];
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		int nowWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1 ~ 7, 7 = 周日
		if (calendar.getFirstDayOfWeek() == Calendar.SUNDAY && (nowWeek -= 1) == 0) 
			nowWeek = 7;
		
		int nowMonth = calendar.get(Calendar.MONTH);
		int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
		int maxDay = calendar.getActualMaximum(Calendar.DATE);
		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
		int nowMinute = calendar.get(Calendar.MINUTE);
		int nowSecond = calendar.get(Calendar.SECOND);
		
		int diffWeek = 0;
		int diffMonth = 0;
		int diffDay = 0;
		int diffHour = 0;
		int diffMinute = 0;
		int diffSecond = 0;
		
		if(!OTHER_TIME.equals(week)) {
			int _week = Integer.valueOf(week);
			if(_week >= nowWeek) 
				diffWeek = _week - nowWeek;
			else 
				diffWeek = 7 + _week - nowWeek;
				
		} else {
			/** 如果没有设置周，则使用月和日进行计算 */
			if(!OTHER_TIME.equals(month)) {
				int _month = Integer.valueOf(month);
				if(_month > 0)
					_month --;
				
				if(_month >= nowMonth)
					diffMonth = _month - nowMonth;
				else 
					diffMonth = 12 + _month - nowMonth;
			}
			
			if(!OTHER_TIME.equals(day)) {
				int _day = Integer.valueOf(day);
				if(_day >= nowDay) 
					diffDay = _day - nowDay;
				else 
					diffDay = maxDay + _day - nowDay;
				
			}
		}
		
		if(!OTHER_TIME.equals(hour)) {
			int _hour = Integer.valueOf(hour);
			if(_hour > nowHour)
				diffHour = _hour - nowHour;
			else
				diffHour = 24 + _hour - nowHour;
		}
		
		if(!OTHER_TIME.equals(minute)) {
			int _minute = Integer.valueOf(minute);
			if(_minute > nowMinute)
				diffMinute = _minute - nowMinute;
			else {
				diffMinute = 60 + _minute - nowMinute;
			}
		}
		
		if(!OTHER_TIME.equals(second)) {
			int _second = Integer.valueOf(second);
			if(_second > nowSecond)
				diffSecond = _second - nowSecond;
			else
				diffSecond = 60 + _second - nowSecond;
		}
		
		boolean incrSecond = false;
		if(nowSecond + diffSecond >= 60)
			incrSecond = true;
		
		calendar.set(Calendar.SECOND, nowSecond + diffSecond);
		
		if(incrSecond)
			diffMinute -- ;
		
		boolean incrMinute = false;
		if(nowMinute + diffMinute >= 60)
			incrMinute = true;
		else if(nowMinute + diffMinute == 60 && nowSecond + diffSecond >= 60)
			incrMinute = true;
		
		calendar.set(Calendar.MINUTE, nowMinute + diffMinute);
		
		if(incrMinute)
			diffHour --;
		
		boolean incrHour = false;
		if(nowHour + diffHour > 24)
			incrHour = true;
		else if(nowHour + diffHour == 24 && nowMinute + diffMinute >= 60)
			incrHour = true;
		else if(nowHour + diffHour == 24 && nowMinute + diffMinute == 60 && nowSecond + diffSecond >= 60)
			incrHour = true;
		
		calendar.set(Calendar.HOUR_OF_DAY, nowHour + diffHour);
		
		if(diffWeek > 0) {
			if(incrHour)
				diffWeek --;
			
			calendar.set(Calendar.DAY_OF_MONTH, nowDay + diffWeek);
		} else {
			if(incrHour)
				diffDay --;

			calendar.set(Calendar.DAY_OF_MONTH, nowDay + diffDay);
			
			calendar.set(Calendar.MONTH, nowMonth + diffMonth);
		}
		
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timestamp time = new Timestamp(calendar.getTimeInMillis());
		long nowMillis = System.currentTimeMillis();
		String millis = String.valueOf(nowMillis);
		if("000".equals(millis.substring(millis.length() - 3, millis.length())))
			nowMillis += 1;
		Timestamp now = new Timestamp(nowMillis);
		
		long interval = time.getTime() - now.getTime();
		if(interval < 0) {
			if(!OTHER_TIME.equals(week) && Integer.valueOf(week) > 0) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 7);
				time.setTime(calendar.getTimeInMillis());
				interval = time.getTime() - now.getTime();
			} else {
				if(interval >= -60 * 1000) {
					if(!OTHER_TIME.equals(hour))
						calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 1);
					else if(!OTHER_TIME.equals(day))
						calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
					else if(!OTHER_TIME.equals(month))
						calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
					else 
						calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
					
				} else if(interval >= -60 * 60 * 1000) {
					if(!OTHER_TIME.equals(day))
						calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
					else if(!OTHER_TIME.equals(month))
						calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
					else 
						calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 1);
					
				} else if(interval >= -60 * 60 * 24 * 1000) {
					if(!OTHER_TIME.equals(day))
						calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
					else if(!OTHER_TIME.equals(month))
						calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
					else 
						calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
					
				} else 
					calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
				
				time.setTime(calendar.getTimeInMillis());
				interval = time.getTime() - now.getTime();
			}
		}
		
		return interval;
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

	public long getInterval() {
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
