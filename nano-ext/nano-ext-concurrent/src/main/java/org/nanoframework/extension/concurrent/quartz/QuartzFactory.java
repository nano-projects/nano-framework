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
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
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
	private static final QuartzThreadFactory threadFactory = new QuartzThreadFactory();
	private static ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
	
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
	 * @param quartz 任务
	 * @return 返回当前任务
	 */
	public BaseQuartz bind(BaseQuartz quartz) {
		try {
			quartzs.put(quartz.getConfig().getId(), quartz);
			quartzSize.incrementAndGet();
			
			return quartz;
			
		} finally {
			if(LOG.isInfoEnabled())
				LOG.info("绑定任务: 任务号[ " + quartz.getConfig().getId() + " ]");
			
		}
	}
	
	/**
	 * 解绑任务
	 * 
	 * @param quartz 任务
	 * @return 返回当前任务
	 */
	public BaseQuartz unbind(BaseQuartz quartz) {
		try {
			quartzs.remove(quartz.getConfig().getId());
			quartzSize.decrementAndGet();
			
			return quartz;
			
		} finally {
			if(LOG.isDebugEnabled())
				LOG.debug("解绑任务 : 任务号[ " + quartz.getConfig().getId() + " ], 现存任务数: " + quartzSize.get());
			
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
	 * @param id 任务号
	 */
	public void close(String id) {
		try {
			BaseQuartz quartz = quartzs.get(id);
			if(quartz != null && !quartz.isClose())
				quartz.setClose(true);
			
		} finally {
			if(LOG.isDebugEnabled())
				LOG.debug("关闭任务: 任务号[ " + id + " ]");
			
		}
	}
	
	/**
	 * 关闭整组任务
	 * @param groupName
	 */
	public void closeGroup(String groupName) {
		Assert.hasLength(groupName, "groupName can not be null");
		quartzs.forEach((id, quartz) -> {
			if(groupName.equals(quartz.getConfig().getGroup())) {
				if(!quartz.isClose()) 
					quartz.setClose(true);
			}
		});
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
		if(LOG.isInfoEnabled())
			LOG.info("Quartz size: " + componentClasses.size());
		
		if(componentClasses.size() > 0) {
			for(Class<?> clz : componentClasses) {
				if(BaseQuartz.class.isAssignableFrom(clz)) {
					if(LOG.isInfoEnabled())
						LOG.info("Inject Quartz Class: " + clz.getName());
					
					Quartz quartz = clz.getAnnotation(Quartz.class);
					if(StringUtils.isEmpty(quartz.name())) 
						throw new QuartzException("任务名不能为空, 类名 [ " + clz.getName()+ " ]");
					
					String parallelProperty = quartz.parallelProperty();
					int parallel = 0;
					String cron = "";
					for(Properties properties : PropertiesLoader.PROPERTIES.values()) {
						String value;
						if(StringUtils.isNotBlank(value = properties.getProperty(parallelProperty))) {
							/** 采用最后设置的属性作为最终结果 */
							try {
								parallel = Integer.parseInt(value);
							} catch(NumberFormatException e) { 
								throw new QuartzException("并行度属性设置错误, 属性名: [ " + parallelProperty + " ], 属性值: [ " + value + " ]");
							}
						}
						
						if(StringUtils.isNotBlank(value = properties.getProperty(quartz.cronProperty())))
							cron = value;
					}
					
					parallel = quartz.coreParallel() ? RuntimeUtil.AVAILABLE_PROCESSORS : parallel > 0 ? parallel : quartz.parallel();
					if(parallel < 0)
						parallel = 0;
					
					if(StringUtils.isBlank(cron))
						cron = quartz.cron();
					
					for(int p = 0; p < parallel; p ++) {
						BaseQuartz baseQuartz = (BaseQuartz) Globals.get(Injector.class).getInstance(clz);
						QuartzConfig config = new QuartzConfig();
						config.setId(quartz.name() + "-" + p);
						config.setName("Quartz-Thread-Pool: " + quartz.name() + "-" + p);
						config.setGroup(quartz.name());
						config.setService(service);
						config.setBeforeAfterOnly(quartz.beforeAfterOnly());
						config.setRunNumberOfTimes(quartz.runNumberOfTimes());
						config.setInterval(quartz.interval());
						config.setNum(p);
						config.setTotal(parallel);
						if(StringUtils.isNotBlank(cron))
							try { config.setCron(new CronExpression(cron)); } catch(ParseException e) { throw new QuartzException(e.getMessage(), e); }
					
						baseQuartz.setDaemon(quartz.daemon());
						baseQuartz.setConfig(config);
						
						if(_tmpQuartz.containsKey(quartz.name() + "-" + p)) {
							throw new QuartzException("\n\t任务调度重复: " + quartz.name() + "-" + p + ", 组件类: {'" + clz.getName() + "', '" + _tmpQuartz.get(quartz.name() + "-" + p).getClass().getName() +"'}");
							
						}
						
						_tmpQuartz.put(quartz.name() + "-" + p, baseQuartz);
						
					}
				} else 
					throw new QuartzException("必须继承: [ "+BaseQuartz.class.getName()+" ]");
				
			}
			
		}
		
		isLoaded = true;
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
			if(LOG.isInfoEnabled())
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
				if(LOG.isInfoEnabled())
					LOG.info("Start quartz [ " + name + " ], class with [ " + quartz.getClass().getName() + " ]");
				
				getInstance().bind(quartz);
				threadFactory.setBaseQuartz(quartz);
				service.execute(quartz);
			});
			
			_tmpQuartz.clear();
			
		}
	}
	
	
}
