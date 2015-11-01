/**
 * Copyright 2015 the original author or authors.
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
package org.nanoframework.extension.concurrent;

import static org.nanoframework.extension.concurrent.quartz.BaseQuartz.OTHER_TIME;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import junit.framework.Assert;

/**
 * @author yanghe
 * @date 2015年11月1日 下午1:32:27
 */
public class CalendarTest {

	private Logger LOG = LoggerFactory.getLogger(CalendarTest.class);
	
	public void maxDayOfMonthTest() {
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setTimeInMillis(System.currentTimeMillis() - 0 * 24 * 60 * 60 * 1000);
		LOG.debug("max day: " + calendar.getActualMaximum(Calendar.DATE));
		LOG.debug("week: " + (calendar.get(Calendar.DAY_OF_WEEK) - 1));
	}
	
	public void cronTest() {
		int count = 1000000;
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setTimeInMillis(System.currentTimeMillis());
		Random random = new Random();
		int base = 0;
		while(count -- > 0) {
			long time = System.currentTimeMillis();
			calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + base + (base += random.nextInt(3600)));
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			String crontab = "* " + month + " " + day + " " + hour + " " + minute + " " + second;
			cronTest(crontab);
			LOG.debug("exec time: " + (System.currentTimeMillis() - time) + "ms\n\n");
//			try { Thread.sleep(3000L); } catch(InterruptedException e) { }
			
		}
	}
	
	@Test
	public void cronTest1() {
//		cronTest("* * * * * 30");
//		cronTest("* * * * 30 30");
//		cronTest("* * * 12 * 30");
//		cronTest("* * 15 * * 30");
//		cronTest("* 6 * * * 30");
//		cronTest("* * * * 30 30");
//		cronTest("* * * 12 30 30");
//		cronTest("* * 15 * 30 30");
//		cronTest("* 6 * * 30 30");
//		cronTest("* * 15 12 30 30");
//		cronTest("* 6 15 12 30 30");
		
		cronTest("* * * 0 * 0");
	}
	
	public void cronTest(String crontab) {
		String[] times = crontab.split(" ");
		String week = times[0];
		String month = times[1];
		String day = times[2];
		String hour = times[3];
		String minute = times[4];
		String second = times[5];
		
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
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
		Timestamp now = new Timestamp(System.currentTimeMillis());
		long interval = time.getTime() - now.getTime();
		if(interval < 0) {
			if(!OTHER_TIME.equals(week) && Integer.valueOf(week) > 0) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 7);
				time.setTime(calendar.getTimeInMillis());
				interval = time.getTime() - now.getTime();
			} else {
				if(interval > -60 * 1000) {
					if(!OTHER_TIME.equals(hour))
						calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 1);
					else if(!OTHER_TIME.equals(day))
						calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
					else if(!OTHER_TIME.equals(month))
						calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
					else 
						calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
					
				} else if(interval > -60 * 60 * 1000) {
					if(!OTHER_TIME.equals(day))
						calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
					else if(!OTHER_TIME.equals(month))
						calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
					else 
						calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 1);
					
				} else if(interval > -60 * 60 * 24 * 1000) {
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
		
		Timestamp _now = new Timestamp((time.getTime() - interval));
		
		LOG.debug("week: " + diffWeek);
		LOG.debug("month: " + diffMonth);
		LOG.debug("day: " + diffDay);
		LOG.debug("hour: " + diffHour);
		LOG.debug("minute: " + diffMinute);
		LOG.debug("second: " + diffSecond);
		LOG.debug("calc time: " + time);
		LOG.debug("diff time: " + _now);
		LOG.debug("crontab:   " + crontab);
		LOG.debug("interval:  " + interval + "ms");
		LOG.debug("calc second: " + incrSecond + ": " + (nowSecond + diffSecond));
		LOG.debug("calc minute: " + incrMinute + ": " + (nowMinute + diffMinute));
		LOG.debug("calc hour: " + incrHour + ": " + (nowHour + diffHour));
		LOG.debug("\n\n");
		
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTime(time);
		
		if(!OTHER_TIME.equals(week) && Integer.valueOf(week) == 0) {
			Assert.assertEquals("not eq: " + crontab + " - " + time, Integer.valueOf(_calendar.get(Calendar.MONTH) + 1), Integer.valueOf(month));
			Assert.assertEquals("not eq: " + crontab + " - " + time, Integer.valueOf(_calendar.get(Calendar.DAY_OF_MONTH)), Integer.valueOf(day));
			Assert.assertEquals("not eq: " + crontab + " - " + time, Integer.valueOf(_calendar.get(Calendar.HOUR_OF_DAY)), Integer.valueOf(hour));
			Assert.assertEquals("not eq: " + crontab + " - " + time, Integer.valueOf(_calendar.get(Calendar.MINUTE)), Integer.valueOf(minute));
			Assert.assertEquals("not eq: " + crontab + " - " + time, Integer.valueOf(_calendar.get(Calendar.SECOND)), Integer.valueOf(second));
		} 
	}
}
