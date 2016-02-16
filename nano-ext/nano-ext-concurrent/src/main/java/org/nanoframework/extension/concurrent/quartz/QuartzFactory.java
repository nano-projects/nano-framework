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
import java.util.HashSet;
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
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ObjectCompare;
import org.nanoframework.commons.util.RuntimeUtil;
import org.nanoframework.core.component.scan.ComponentScan;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.extension.concurrent.exception.QuartzException;
import org.nanoframework.extension.concurrent.queue.BlockingQueueFactory;

import com.google.common.collect.Sets;
import com.google.inject.Injector;

/**
 * 任务工厂
 * @author yanghe
 * @date 2015年6月8日 下午5:24:13 
 */
public class QuartzFactory {
	private static Logger LOG = LoggerFactory.getLogger(QuartzFactory.class);
	private static QuartzFactory FACTORY;
	private static final Object LOCK = new Object();
	private AtomicInteger startedQuartzSize = new AtomicInteger(0);
	private final ConcurrentMap<String , BaseQuartz> startedQuartz = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, BaseQuartz> stoppingQuartz = new ConcurrentHashMap<>();
	private final ConcurrentMap<String , BaseQuartz> stoppedQuartz = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Set<BaseQuartz>> group = new ConcurrentHashMap<>();
	private static final QuartzThreadFactory threadFactory = new QuartzThreadFactory();
	private static final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
	
	private static boolean isLoaded = false;
	
	public static final String BASE_PACKAGE = "context.quartz-scan.base-package";
	public static final String INCLUDES = "context.quartz.group.includes";
	public static final String EXCLUSIONS = "context.quartz.group.exclusions";
	
	private QuartzFactory() {
		
	}
	
	public static QuartzFactory getInstance() {
		if(FACTORY == null) {
			synchronized (LOCK) {
				if(FACTORY == null) {
					FACTORY = new QuartzFactory();
					StatusMonitorQuartz statusMonitor = FACTORY.new StatusMonitorQuartz();
					statusMonitor.getConfig().getService().execute(statusMonitor);
					Runtime.getRuntime().addShutdownHook(new Thread(FACTORY.new ShutdownHook()));
				}
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
			quartz.setClose(false);
			startedQuartz.put(quartz.getConfig().getId(), quartz);
			startedQuartzSize.incrementAndGet();
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
	protected BaseQuartz unbind(BaseQuartz quartz) {
		BaseQuartz removed = startedQuartz.remove(quartz.getConfig().getId());
		if(removed != null) {
			startedQuartzSize.decrementAndGet();
			if(LOG.isDebugEnabled())
				LOG.debug("解绑任务 : 任务号[ " + quartz.getConfig().getId() + " ], 现存任务数: " + startedQuartzSize.get());
		}
		
		return quartz;
	}
	
	/**
	 * 获取现在正在执行的任务数
	 * @return 任务数
	 */
	public int getStartedQuartzSize() {
		return startedQuartzSize.get();
	}
	
	/**
	 * 返回所有任务
	 * @return 任务集合
	 */
	public Collection<BaseQuartz> getStartedQuartzs() {
		return startedQuartz.values();
	}
	
	public int getStopedQuartzSize() {
		return stoppedQuartz.size();
	}
	
	public Collection<BaseQuartz> getStopedQuratz() {
		return stoppedQuartz.values();
	}
	
	public int getStoppingQuartzSize() {
		return stoppingQuartz.size();
	}
	
	public Collection<BaseQuartz> getStoppingQuartz() {
		return stoppingQuartz.values();
	}
	
	/**
	 * 关闭任务
	 * @param id 任务号
	 */
	public void close(String id) {
		try {
			BaseQuartz quartz = startedQuartz.get(id);
			if(quartz != null && !quartz.isClose()) {
				quartz.setClose(true);
				stoppingQuartz.put(quartz.getConfig().getId(), quartz);
				startedQuartz.remove(id, quartz);
			}
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
		Assert.hasLength(groupName, "groupName must not be null");
		Set<String> ids = new HashSet<String>();
		startedQuartz.forEach((id, quartz) -> {
			if(groupName.equals(quartz.getConfig().getGroup())) {
				if(!quartz.isClose()) {
					quartz.setClose(true);
					stoppingQuartz.put(quartz.getConfig().getId(), quartz);
					ids.add(quartz.getConfig().getId());
				}
			}
		});
		
		ids.forEach(id -> startedQuartz.remove(id));
	}
	
	/**
	 * 关闭所有任务
	 */
	public void closeAll() {
		if(startedQuartz.size() > 0) {
			LOG.warn("现在关闭所有的任务");
			startedQuartz.keySet().forEach(id -> {
				try {
					BaseQuartz quartz = startedQuartz.get(id);
					if(quartz != null && !quartz.isClose()) {
						quartz.setClose(true);
						stoppingQuartz.put(quartz.getConfig().getId(), quartz);
					}
					
				} finally {
					if(LOG.isDebugEnabled())
						LOG.debug("关闭任务: 任务号[ " + id + " ]");
				}
			});
			
			startedQuartz.clear();
		}
	}
	
	/**
	 * 启动所有缓冲区中的任务并清理任务缓冲区
	 */
	public void startAll() {
		if(stoppedQuartz.size() > 0) {
			stoppedQuartz.forEach((id, quartz) -> {
				if(LOG.isInfoEnabled())
					LOG.info("Start quartz [ " + id + " ], class with [ " + quartz.getClass().getName() + " ]");
				
				bind(quartz);
				threadFactory.setBaseQuartz(quartz);
				service.execute(quartz);
			});
			
			stoppedQuartz.clear();
		}
	}
	
	public void startGroup(String groupName) {
		if(stoppedQuartz.size() > 0) {
			Set<String> keys = new HashSet<>();
			stoppedQuartz.forEach((id, quartz) -> {
				if(groupName.equals(quartz.getConfig().getGroup())) {
					if(quartz.isClose()) {
						if(LOG.isInfoEnabled())
							LOG.info("Start quartz [ " + id + " ], class with [ " + quartz.getClass().getName() + " ]");
						
						bind(quartz);
						threadFactory.setBaseQuartz(quartz);
						service.execute(quartz);
						keys.add(id);
					}
				}
			});
			
			for(String key : keys) stoppedQuartz.remove(key);
		}
	}
	
	public void start(String id) {
		BaseQuartz quartz = stoppedQuartz.get(id);
		if(quartz != null && quartz.isClose()) {
			if(LOG.isInfoEnabled())
				LOG.info("Start quartz [ " + id + " ], class with [ " + quartz.getClass().getName() + " ]");
			
			bind(quartz);
			threadFactory.setBaseQuartz(quartz);
			service.execute(quartz);
			stoppedQuartz.remove(id);
		}
	}
	
	public boolean closed(String id) {
		return stoppedQuartz.containsKey(id);
	}
	
	public boolean started(String id) {
		return startedQuartz.containsKey(id);
	}
	
	public boolean hasClosedGroup(String group) {
		if(stoppedQuartz.size() > 0) {
			for(BaseQuartz quartz : stoppedQuartz.values()) {
				if(quartz.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean hasStartedGroup(String group) {
		if(startedQuartz.size() > 0) {
			for(BaseQuartz quartz : startedQuartz.values()) {
				if(quartz.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public void addQuartz(BaseQuartz quartz) {
		Set<BaseQuartz> groupQuartz = group.get(quartz.getConfig().getGroup());
		if(groupQuartz == null) groupQuartz = Sets.newLinkedHashSet();
		groupQuartz.add(quartz);
		group.put(quartz.getConfig().getGroup(), groupQuartz);
		
		if(stoppedQuartz.containsKey(quartz.getConfig().getId()) || startedQuartz.containsKey(quartz.getConfig().getId()))
			throw new QuartzException("exists quartz in memory");
		
		stoppedQuartz.put(quartz.getConfig().getId(), quartz);
		rebalance(quartz.getConfig().getGroup());
	}
	
	public void removeQuartz(BaseQuartz quartz, boolean force) {
		Set<BaseQuartz> groupQuartz = group.get(quartz.getConfig().getGroup());
		close(quartz.getConfig().getId());
		
		if(groupQuartz.size() > 1 || force) {
			groupQuartz.remove(quartz);
			quartz.setRemove(true);
		}
		
		rebalance(quartz.getConfig().getGroup());
	}
	
	public void removeQuartz(BaseQuartz quartz) {
		removeQuartz(quartz, false);
	}
	
	public void removeQuartz(String groupName) {
		BaseQuartz quartz = findLast(groupName);
		if(quartz != null) {
			removeQuartz(quartz);
		}
	}
	
	public int getGroupSize(String groupName) {
		Set<BaseQuartz> groupQuartz = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupQuartz))
			return groupQuartz.size();
		
		return 0;
	}
	
	public Set<BaseQuartz> getGroupQuartz(String groupName) {
		return group.get(groupName);
	}
	
	public final BaseQuartz find(String id) {
		Assert.hasLength(id, "id must be not empty.");
		String groupName = id.substring(0, id.lastIndexOf("-"));
		Set<BaseQuartz> groupQuartz = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupQuartz)) {
			for(BaseQuartz quartz : groupQuartz) {
				if(quartz.getConfig().getId().equals(id))
					return quartz;
			}
		}
		
		return null;
	}
	
	public BaseQuartz findLast(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseQuartz> groupQuartz = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupQuartz)) {
			int max = -1;
			for(BaseQuartz quartz : groupQuartz) {
				if(quartz.getConfig().getNum() > max)
					max = quartz.getConfig().getNum();
			}
			
			for(BaseQuartz quartz : groupQuartz) {
				if(quartz.getConfig().getNum() == max) {
					return quartz;
				}
			}
		}
		
		return null;
	}
	
	public void rebalance(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseQuartz> groupQuartz = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupQuartz)) {
			groupQuartz.forEach(quartz -> {
				quartz.getConfig().setTotal(groupQuartz.size());
			});
		}
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
		
		Set<String> includes = Sets.newLinkedHashSet();
		Set<String> exclusions = Sets.newLinkedHashSet();
		PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.get(BASE_PACKAGE) != null).forEach(item -> {
			ComponentScan.scan(item.getProperty(BASE_PACKAGE));
		});
		
		PropertiesLoader.PROPERTIES.values().stream().forEach(item -> {
			if(item.containsKey(INCLUDES)) {
				String[] include = item.getProperty(INCLUDES, ".").split(",");
				for(String inc : include) {
					includes.add(inc);
				}
			}
			
			if(item.containsKey(EXCLUSIONS)) {
				String[] exclusion = item.getProperty(EXCLUSIONS, "").split(",");
				for(String exc : exclusion) {
					exclusions.add(exc);
				}
			}
		});
		
		Set<Class<?>> componentClasses = ComponentScan.filter(Quartz.class);
		if(LOG.isInfoEnabled())
			LOG.info("Quartz size: " + componentClasses.size());
		
		if(componentClasses.size() > 0) {
			if(includes.isEmpty()) 
				includes.add(".");
			
			for(Class<?> clz : componentClasses) {
				if(BaseQuartz.class.isAssignableFrom(clz)) {
					if(LOG.isInfoEnabled())
						LOG.info("Inject Quartz Class: " + clz.getName());
					
					Quartz quartz = clz.getAnnotation(Quartz.class);
					if(StringUtils.isEmpty(quartz.name())) 
						throw new QuartzException("任务名不能为空, 类名 [ " + clz.getName()+ " ]");
					
					if(!ObjectCompare.isInListByRegEx(quartz.name(), includes) || ObjectCompare.isInListByRegEx(quartz.name(), exclusions)) {
						LOG.warn("过滤任务组: " + quartz.name() + ", 类名 [ " + clz.getName()+ " ]");
						continue ;
					}
					
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
						config.setName("Quartz-Thread-Pool: " + config.getId());
						config.setGroup(quartz.name());
						config.setService(service);
						config.setBeforeAfterOnly(quartz.beforeAfterOnly());
						config.setRunNumberOfTimes(quartz.runNumberOfTimes());
						config.setInterval(quartz.interval());
						config.setNum(p);
						config.setTotal(parallel);
						if(StringUtils.isNotBlank(cron))
							try { config.setCron(new CronExpression(cron)); } catch(ParseException e) { throw new QuartzException(e.getMessage(), e); }
					
						config.setDaemon(quartz.daemon());
						config.setLazy(quartz.lazy());
						baseQuartz.setConfig(config);
						
						if(getInstance().stoppedQuartz.containsKey(config.getId())) {
							throw new QuartzException("\n\t任务调度重复: " + config.getId() + ", 组件类: {'" + clz.getName() + "', '" + getInstance().stoppedQuartz.get(config.getId()).getClass().getName() +"'}");
							
						}
						
						getInstance().stoppedQuartz.put(config.getId(), baseQuartz);
						Set<BaseQuartz> groupQuartz = getInstance().group.get(baseQuartz.getConfig().getGroup());
						if(groupQuartz == null) groupQuartz = Sets.newLinkedHashSet();
						groupQuartz.add(baseQuartz);
						getInstance().group.put(config.getGroup(), groupQuartz);
					}
				} else 
					throw new QuartzException("必须继承: [ "+BaseQuartz.class.getName()+" ]");
				
			}
			
		}
		
		isLoaded = true;
	}
	
	/**
	 * 重新加载调度任务
	 */
	public static final void reload() {
		getInstance().stoppedQuartz.clear();
		getInstance().closeAll();
		service.execute(() -> {
			try { while(QuartzFactory.getInstance().getStartedQuartzSize() > 0) Thread.sleep(100L); } catch(InterruptedException e) { }
			if(LOG.isInfoEnabled())
				LOG.info("所有任务已经全部关闭");
			
			try {
				load();
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LOG.error(e.getMessage(), e);
			}
			
		});
	}
	
	protected class StatusMonitorQuartz extends BaseQuartz {
		private final ConcurrentMap<String, BaseQuartz> closed;
		
		public StatusMonitorQuartz() {
			QuartzConfig config = new QuartzConfig();
			config.setId("StatusMonitorQuartz-0");
			config.setName("StatusMonitorQuartz");
			config.setGroup("StatusMonitorQuartz");
			threadFactory.setBaseQuartz(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			try { config.setCron(new CronExpression("* * * * * ?")); } catch(ParseException e) {}
			config.setTotal(1);
			config.setDaemon(true);
			setConfig(config);
			setClose(false);
			closed = new ConcurrentHashMap<>();
		}
		
		@Override
		public void before() throws QuartzException {
			stoppingQuartz.forEach((id, quartz) -> {
				if(quartz.isClosed())
					closed.put(id, quartz);
			});
		}

		@Override
		public void execute() throws QuartzException {
			closed.forEach((id, quartz) -> {
				if(!quartz.isRemove())
					stoppedQuartz.put(id, quartz);
				
				stoppingQuartz.remove(id, quartz);
			});
		}

		@Override
		public void after() throws QuartzException {
			closed.clear();
		}

		@Override
		public void destroy() throws QuartzException {
			
		}
		
	}
	
	protected class ShutdownHook implements Runnable {
		@Override
		public void run() {
			LOG.info("等待队列中的所有元素被执行完成后停止系统");
			while((int) BlockingQueueFactory.howManyElementInQueues() > 0) 
				try { Thread.sleep(10L); } catch(InterruptedException e) { }
			
			LOG.info("队列中的所有元素已被执行完成");
			
			long time = System.currentTimeMillis();
			LOG.info("开始停止任务调度");
			FACTORY.closeAll();
			Collection<BaseQuartz> quartzs = FACTORY.getStoppingQuartz();
			for(BaseQuartz quartz : quartzs) {
				quartz.thisNotify();
			}
			
			while((FACTORY.getStartedQuartzSize() > 0 || FACTORY.getStoppingQuartzSize() > 0) && System.currentTimeMillis() - time < 300000L) 
				try { Thread.sleep(10L); } catch(InterruptedException e) { }
			
 			LOG.info("停止任务调度完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		}
		
	}
}
