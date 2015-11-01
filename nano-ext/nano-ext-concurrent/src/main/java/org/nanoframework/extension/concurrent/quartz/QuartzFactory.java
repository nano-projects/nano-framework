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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.annatations.Property;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.RuntimeUtil;
import org.nanoframework.core.component.scan.ComponentScan;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.extension.concurrent.exception.QuartzException;

import com.google.inject.Injector;

/**
 * 任务工厂
 * @author yanghe
 * @date 2015年6月8日 下午5:24:13 
 */
public class QuartzFactory {

	private static Logger LOG = LoggerFactory.getLogger(QuartzFactory.class);
	
	private static QuartzFactory FACTORY;
	private static Object LOCK = new Object();
	private AtomicInteger quartzSize = new AtomicInteger(0);
	private static ConcurrentMap<String , BaseQuartz> quartzs = new ConcurrentHashMap<>();
	private static ConcurrentMap<String , BaseQuartz> _tmpQuartz = new ConcurrentHashMap<>();
	private static ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	
	private static boolean isLoaded = false;
	
	public static final String BASE_PACKAGE = "context.quartz-scan.base-package";
	
	private QuartzFactory() {
		
	}
	
	public static final QuartzFactory getInstance() {
		if(FACTORY == null) {
			synchronized (LOCK) {
				if(FACTORY == null)
					FACTORY = new QuartzFactory();
				
			}
		}
		
		return FACTORY;
		
	}
	
	/**
	 * 绑定任务
	 * 
	 * @param task 任务
	 * @return 返回当前任务
	 */
	public BaseQuartz bind(BaseQuartz task) {
		try {
			quartzs.put(task._getId(), task);
			quartzSize.incrementAndGet();
			
			return task;
			
		} finally {
			LOG.info("绑定任务: 任务号[ " + task._getId() + " ]");
			
		}
	}
	
	/**
	 * 解绑任务
	 * 
	 * @param task 任务
	 * @return 返回当前任务
	 */
	public BaseQuartz unbind(BaseQuartz task) {
		try {
			quartzs.remove(task._getId());
			quartzSize.decrementAndGet();
			
			return task;
			
		} finally {
			LOG.debug("解绑任务 : 任务号[ " + task._getId() + " ], 现存任务数: " + quartzSize.get());
			
		}
	}
	
	/**
	 * 获取现在正在执行的任务数
	 * @return 任务数
	 */
	public int getQuartzSize() {
		return quartzSize.get();
		
	}
	
	/**
	 * 返回所有任务
	 * @return 任务集合
	 */
	public Collection<BaseQuartz> getQuartzs() {
		return quartzs.values();
		
	}
	
	/**
	 * 关闭任务
	 * @param taskId 任务号
	 */
	public void close(String taskId) {
		try {
			BaseQuartz task = quartzs.get(taskId);
			if(task != null && !task.isClose())
				task.setClose(true);
			
		} finally {
			LOG.debug("关闭任务: 任务号[ " + taskId + " ]");
			
		}
	}
	
	/**
	 * 关闭所有任务
	 */
	public void closeAll() {
		LOG.warn("现在关闭所有的任务");
		quartzs.keySet().forEach(key -> close(key));
		
	}
	
	/**
	 * 加载任务调度
	 * @param injector Guice Injector
	 * @throws IllegalArgumentException 非法的参数列表
	 * @throws IllegalAccessException ?
	 */
	public static final void load() throws IllegalArgumentException, IllegalAccessException {
		if(isLoaded) {
			throw new LoaderException("Quartz已经加载，这里不再进行重复的加载，如需重新加载请调用reload方法");
		}

		if(PropertiesLoader.PROPERTIES.size() == 0) {
			throw new LoaderException("没有加载任何的属性文件, 无法加载组件.");
			
		}
		
		PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.get(BASE_PACKAGE) != null).forEach(item -> {
			ComponentScan.scan(item.getProperty(BASE_PACKAGE));
		});
		
		Set<Class<?>> componentClasses = ComponentScan.filter(Quartz.class);
		LOG.info("Quartz size: " + componentClasses.size());
		
		if(componentClasses.size() > 0) {
			for(Class<?> clz : componentClasses) {
				if(BaseQuartz.class.isAssignableFrom(clz)) {
					LOG.info("Inject Quartz Class: " + clz.getName());
					Quartz quartz = clz.getAnnotation(Quartz.class);
					if(StringUtils.isEmpty(quartz.name())) 
						throw new QuartzException("任务名不能为空, 类名 [ " + clz.getName()+ " ]");
					
					String parallelProperty = quartz.parallelProperty();
					int parallel = 0;
					for(Properties properties : PropertiesLoader.PROPERTIES.values()) {
						if(StringUtils.isNotEmpty(properties.getProperty(parallelProperty))) {
							/** 采用最后设置的属性作为最终结果 */
							String value = properties.getProperty(parallelProperty);
							try {
								parallel = Integer.parseInt(value);
							} catch(NumberFormatException e) { 
								throw new QuartzException("并行度属性设置错误, 属性名: [ " + parallelProperty + " ], 属性值: [ " + value + " ]");
							}
						}
					}
					
					parallel = quartz.coreParallel() ? RuntimeUtil.AVAILABLE_PROCESSORS : parallel > 0 ? parallel : quartz.parallel();
					if(parallel < 0)
						parallel = 0;
					
					for(int p = 0; p < parallel; p ++) {
						Object obj = Globals.get(Injector.class).getInstance(clz);
						Field[] fields = BaseQuartz.class.getDeclaredFields();
						if(fields != null && fields.length > 0) {
							for(Field field : fields) {
								Property property;
								if((property = field.getAnnotation(Property.class)) != null) {
									field.setAccessible(true) ;  
									
									switch(property.name()) {
										case BaseQuartz.ID: 
											field.set(obj, quartz.name() + "-" + p);
											break;
											
										case BaseQuartz.SERVICE: 
											field.set(obj, service);
											break;
											
										case BaseQuartz.BEFORE_AFTER_ONLY: 
											field.set(obj, quartz.beforeAfterOnly());
											break;
											
										case BaseQuartz.RUN_NUMBER_OF_TIMES: 
											field.set(obj, quartz.runNumberOfTimes());
											break;
											
										case BaseQuartz.INTERVAL: 
											field.set(obj, quartz.interval());
											break;
											
										case BaseQuartz.NUM:
											field.set(obj, p);
											break;
											
										case BaseQuartz.TOTAL: 
											field.set(obj, parallel);
											break;
											
										case BaseQuartz.CRONTAB: 
											crontabValid(quartz);
											field.set(obj, quartz.crontab());

										default :
											break;
											
									}
								}
							}
						}
					
						if(_tmpQuartz.containsKey(quartz.name() + "-" + p)) {
							throw new QuartzException("\n\t任务调度重复: " + quartz.name() + "-" + p + ", 组件类: {'" + clz.getName() + "', '" + _tmpQuartz.get(quartz.name() + "-" + p).getClass().getName() +"'}");
							
						}
						
						_tmpQuartz.put(quartz.name() + "-" + p, (BaseQuartz) obj);
						
					}
				} else 
					throw new QuartzException("必须继承: [ "+BaseQuartz.class.getName()+" ]");
				
			}
			
		}
		
		isLoaded = true;
	}
	
	private static final void crontabValid(Quartz quartz) {
		if(quartz.crontab().split(" ").length != 6)
			throw new QuartzException("任务调度crontab参数设置错误，格式必须为: [* * * * * *]");
		
		String[] times = quartz.crontab().split(" ");
		String week = times[0];
		String month = times[1];
		String day = times[2];
		String hour = times[3];
		String minute = times[4];
		String second = times[5];
		
		if(!BaseQuartz.OTHER_TIME.equals(week)) {
			try {
				int _week = Integer.valueOf(week);
				if(_week < 1 || _week > 7)
					throw new QuartzException("时间策略设置错误, 调度任务: " + quartz.name() + "，周的设置范围为: 1~7, 这里设置为: " + week);
			} catch(NumberFormatException e) {
				throw new QuartzException("时间策略设置错误: 周必须为1~7之间的整形");
			}
		}
		
		if(!BaseQuartz.OTHER_TIME.equals(month)) {
			try { 
				int _month = Integer.valueOf(month);
				if(_month < 1 || _month > 12)
					throw new QuartzException("时间策略设置错误, 调度任务: " + quartz.name() + "，月的设置范围为: 1~12, 这里设置为: " + month);
			} catch(NumberFormatException e) {
				throw new QuartzException("时间策略设置错误: 月必须为1~12之间的整形");
			}
		}
		
		if(!BaseQuartz.OTHER_TIME.equals(day)) {
			try { 
				int _day = Integer.valueOf(day);
				if(_day < 1 || _day > 31)
					throw new QuartzException("时间策略设置错误, 调度任务: " + quartz.name() + "，日的设置范围为: 1~31, 这里设置为: " + day);
			} catch(NumberFormatException e) {
				throw new QuartzException("时间策略设置错误: 日必须为1~31之间的整形");
			}
		}
		
		if(!BaseQuartz.OTHER_TIME.equals(hour)) {
			try { 
				int _hour = Integer.valueOf(hour);
				if(_hour < 0 || _hour > 23)
					throw new QuartzException("时间策略设置错误, 调度任务: " + quartz.name() + "，时的设置范围为: 0~23, 这里设置为: " + hour);
			} catch(NumberFormatException e) {
				throw new QuartzException("时间策略设置错误: 时必须为0~23之间的整形");
			}
		}
		
		if(!BaseQuartz.OTHER_TIME.equals(minute)) {
			try { 
				int _minute = Integer.valueOf(minute);
				if(_minute < 0 || _minute > 59)
					throw new QuartzException("时间策略设置错误, 调度任务: " + quartz.name() + "，分的设置范围为: 0~59, 这里设置为: " + minute);
			} catch(NumberFormatException e) {
				throw new QuartzException("时间策略设置错误: 分必须为0~59之间的整形");
			}
		}
		
		if(!BaseQuartz.OTHER_TIME.equals(second)) {
			try { 
				int _second = Integer.valueOf(second);
				if(_second < 0 || _second > 59)
					throw new QuartzException("时间策略设置错误, 调度任务: " + quartz.name() + "，秒的设置范围为: 0~59, 这里设置为: " + second);
			} catch(NumberFormatException e) {
				throw new QuartzException("时间策略设置错误: 秒必须为0~59之间的整形");
			}
		}
	}
	
	/**
	 * 重新加载调度任务
	 * @param injector Guice Injector
	 */
	public static final void reload() {
		_tmpQuartz.clear();
		getInstance().closeAll();
		service.execute(() -> {
			try { while(QuartzFactory.getInstance().getQuartzSize() > 0) Thread.sleep(100L); } catch(InterruptedException e) { }
			LOG.info("所有任务已经全部关闭");
			
			try {
				load();
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LOG.error(e.getMessage(), e);
			}
			
		});
		
	}
	
	/**
	 * 启动所有缓冲区中的任务并清理任务缓冲区
	 */
	public static final void startAll() {
		if(_tmpQuartz.size() > 0) {
			_tmpQuartz.forEach((name, quartz) -> {
				LOG.info("Start quartz [ " + name + " ], class with [ " + quartz.getClass().getName() + " ]");
				getInstance().bind(quartz);
				service.execute(quartz);
				
			});
			
			_tmpQuartz.clear();
			
		}
	}
	
}
