/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.extension.concurrent.scheduler;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.nanoframework.extension.concurrent.exception.SchedulerException;
import org.nanoframework.extension.concurrent.scheduler.defaults.etcd.EtcdOrderWatcherScheduler;
import org.nanoframework.extension.concurrent.scheduler.defaults.etcd.EtcdScheduler;
import org.nanoframework.extension.concurrent.scheduler.defaults.etcd.EtcdSchedulerOperate;
import org.nanoframework.extension.concurrent.scheduler.defaults.monitor.LocalJmxMonitorScheduler;
import org.nanoframework.extension.concurrent.queue.BlockingQueueFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

/**
 * 任务工厂
 * @author yanghe
 * @date 2016年3月22日 下午5:15:58
 */
public class SchedulerFactory {
	private static Logger LOG = LoggerFactory.getLogger(SchedulerFactory.class);
	private static SchedulerFactory FACTORY;
	private static final Object LOCK = new Object();
	private final ConcurrentMap<String , BaseScheduler> startedScheduler = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, BaseScheduler> stoppingScheduler = new ConcurrentHashMap<>();
	private final ConcurrentMap<String , BaseScheduler> stoppedScheduler = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Set<BaseScheduler>> group = new ConcurrentHashMap<>();
	public static final SchedulerThreadFactory threadFactory = new SchedulerThreadFactory();
	private static final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
	
	private static boolean isLoaded = false;
	
	public static final String BASE_PACKAGE = "context.scheduler-scan.base-package";
	public static final String AUTO_RUN = "context.scheduler.run.auto";
	public static final String INCLUDES = "context.scheduler.group.includes";
	public static final String EXCLUSIONS = "context.scheduler.group.exclusions";
	public static final String SHUTDOWN_TIMEOUT = "context.scheduler.shutdown.timeout";
	public static final String DEFAULT_SCHEDULER_NAME_PREFIX = "Scheduler-Thread-Pool: ";
	
	private final long shutdownTimeout = Long.parseLong(System.getProperty(SHUTDOWN_TIMEOUT, "60000"));
	
	private static EtcdSchedulerOperate etcdScheduler;
	
	private SchedulerFactory() {
		
	}
	
	public static SchedulerFactory getInstance() {
		if(FACTORY == null) {
			synchronized (LOCK) {
				if(FACTORY == null) {
					FACTORY = new SchedulerFactory();
					StatusMonitorScheduler statusMonitor = FACTORY.new StatusMonitorScheduler();
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
	 * @param scheduler 任务
	 * @return 返回当前任务
	 */
	public BaseScheduler bind(BaseScheduler scheduler) {
		try {
			scheduler.setClose(false);
			startedScheduler.put(scheduler.getConfig().getId(), scheduler);
			return scheduler;
		} finally {
			if(LOG.isInfoEnabled())
				LOG.info("绑定任务: 任务号[ " + scheduler.getConfig().getId() + " ]");
			
		}
	}
	
	/**
	 * 解绑任务
	 * 
	 * @param scheduler 任务
	 * @return 返回当前任务
	 */
	protected BaseScheduler unbind(BaseScheduler scheduler) {
		BaseScheduler removed = startedScheduler.remove(scheduler.getConfig().getId());
		if(removed != null) {
			if(LOG.isDebugEnabled())
				LOG.debug("解绑任务 : 任务号[ " + scheduler.getConfig().getId() + " ], 现存任务数: " + startedScheduler.size());
		}
		
		return scheduler;
	}
	
	/**
	 * 获取现在正在执行的任务数
	 * @return 任务数
	 */
	public int getStartedSchedulerSize() {
		return startedScheduler.size();
	}
	
	/**
	 * 返回所有任务
	 * @return 任务集合
	 */
	public Collection<BaseScheduler> getStartedScheduler() {
		return startedScheduler.values();
	}
	
	public int getStopedSchedulerSize() {
		return stoppedScheduler.size();
	}
	
	public Collection<BaseScheduler> getStoppedQuratz() {
		return stoppedScheduler.values();
	}
	
	public int getStoppingSchedulerSize() {
		return stoppingScheduler.size();
	}
	
	public Collection<BaseScheduler> getStoppingScheduler() {
		return stoppingScheduler.values();
	}
	
	/**
	 * 关闭任务
	 * @param id 任务号
	 */
	public void close(String id) {
		try {
			BaseScheduler scheduler = startedScheduler.get(id);
			close(scheduler);
		} finally {
			if(LOG.isDebugEnabled())
				LOG.debug("关闭任务: 任务号[ " + id + " ]");
		}
	}
	
	public void close(final BaseScheduler scheduler) {
		if(scheduler != null && !scheduler.isClose()) {
			/** Sync to Etcd by stop method */
			etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId());
			
			scheduler.setClose(true);
			stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
			startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
		}
	}
	
	/**
	 * 关闭整组任务
	 * @param groupName
	 */
	public void closeGroup(String groupName) {
		Assert.hasLength(groupName, "groupName must not be null");
		Set<String> ids = new HashSet<String>();
		startedScheduler.forEach((id, scheduler) -> {
			if(groupName.equals(scheduler.getConfig().getGroup())) {
				if(!scheduler.isClose()) {
					/** Sync to Etcd by stop method */
					etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId());
					
					scheduler.setClose(true);
					stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
					ids.add(scheduler.getConfig().getId());
				}
			}
		});
		
		ids.forEach(id -> startedScheduler.remove(id));
	}
	
	/**
	 * 关闭所有任务
	 */
	public void closeAll() {
		if(startedScheduler.size() > 0) {
			LOG.warn("现在关闭所有的任务");
			startedScheduler.keySet().forEach(id -> {
				try {
					BaseScheduler scheduler = startedScheduler.get(id);
					if(scheduler != null && !scheduler.isClose()) {
						/** Sync to Etcd by stop method */
						etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId());
						
						scheduler.setClose(true);
						stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
					}
					
				} finally {
					if(LOG.isDebugEnabled())
						LOG.debug("关闭任务: 任务号[ " + id + " ]");
				}
			});
			
			startedScheduler.clear();
		}
	}
	
	/**
	 * 启动所有缓冲区中的任务并清理任务缓冲区
	 */
	public void startAll() {
		if(stoppedScheduler.size() > 0) {
			stoppedScheduler.forEach((id, scheduler) -> {
				if(LOG.isInfoEnabled())
					LOG.info("Start scheduler [ " + id + " ], class with [ " + scheduler.getClass().getName() + " ]");
				
				bind(scheduler);
				threadFactory.setBaseScheduler(scheduler);
				service.execute(scheduler);
				
				/** Sync to Etcd by start method */
				etcdScheduler.start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId());
			});
			
			stoppedScheduler.clear();
		}
	}
	
	public void startGroup(String groupName) {
		if(stoppedScheduler.size() > 0) {
			Set<String> keys = new HashSet<>();
			stoppedScheduler.forEach((id, scheduler) -> {
				if(groupName.equals(scheduler.getConfig().getGroup())) {
					if(scheduler.isClose()) {
						if(LOG.isInfoEnabled())
							LOG.info("Start scheduler [ " + id + " ], class with [ " + scheduler.getClass().getName() + " ]");
						
						bind(scheduler);
						threadFactory.setBaseScheduler(scheduler);
						service.execute(scheduler);
						keys.add(id);
						
						/** Sync to Etcd by start method */
						etcdScheduler.start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId());
					}
				}
			});
			
			for(String key : keys) stoppedScheduler.remove(key);
		}
	}
	
	public void start(String id) {
		BaseScheduler scheduler = stoppedScheduler.get(id);
		if(scheduler != null && scheduler.isClose()) {
			if(LOG.isInfoEnabled())
				LOG.info("Start scheduler [ " + id + " ], class with [ " + scheduler.getClass().getName() + " ]");
			
			bind(scheduler);
			threadFactory.setBaseScheduler(scheduler);
			service.execute(scheduler);
			stoppedScheduler.remove(id);
			
			/** Sync to Etcd by start method */
			etcdScheduler.start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId());
		}
	}
	
	public void append(String groupName, int size, boolean autoStart) {
		BaseScheduler scheduler = findLast(groupName);
		if(scheduler == null) 
			return ;
		
		for(int idx = 0; idx < size; idx ++) {
			SchedulerConfig config = (SchedulerConfig) scheduler.getConfig().clone();
			int total = config.getTotal();
			config.setTotal(total + 1);
			config.setNum(total);
			config.setId(groupName + "-" + scheduler.getIndex(groupName));
			config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + config.getId());
			BaseScheduler _new = scheduler.clone();
			_new.setClose(true);
			_new.setClosed(true);
			_new.setRemove(false);
			_new.setConfig(config);
			addScheduler(_new);
			if(autoStart)
				start(config.getId());
			else {
				/** Sync to Etcd by start method */
				etcdScheduler.stopped(_new.getConfig().getGroup(), _new.getConfig().getId(), false);
			}
		}
	}
	
	public boolean closed(String id) {
		return stoppedScheduler.containsKey(id);
	}
	
	public boolean started(String id) {
		return startedScheduler.containsKey(id);
	}
	
	public boolean hasClosedGroup(String group) {
		if(stoppedScheduler.size() > 0) {
			for(BaseScheduler scheduler : stoppedScheduler.values()) {
				if(scheduler.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean hasStartedGroup(String group) {
		if(startedScheduler.size() > 0) {
			for(BaseScheduler scheduler : startedScheduler.values()) {
				if(scheduler.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public void addScheduler(BaseScheduler scheduler) {
		Set<BaseScheduler> groupScheduler = group.get(scheduler.getConfig().getGroup());
		if(groupScheduler == null) groupScheduler = Sets.newLinkedHashSet();
		groupScheduler.add(scheduler);
		group.put(scheduler.getConfig().getGroup(), groupScheduler);
		
		if(stoppedScheduler.containsKey(scheduler.getConfig().getId()) || startedScheduler.containsKey(scheduler.getConfig().getId()))
			throw new SchedulerException("exists scheduler in memory");
		
		stoppedScheduler.put(scheduler.getConfig().getId(), scheduler);
		rebalance(scheduler.getConfig().getGroup());
	}
	
	public int removeScheduler(BaseScheduler scheduler, boolean force) {
		Set<BaseScheduler> groupScheduler = group.get(scheduler.getConfig().getGroup());
		boolean remove = false;
		if(groupScheduler.size() > 1 || force) {
			groupScheduler.remove(scheduler);
			scheduler.setRemove(remove = true);
		}
		
		if(scheduler.isClosed()) {
			/** Sync to Etcd by start method */
			etcdScheduler.stopped(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), remove);
		} else
			close(scheduler.getConfig().getId());
		
		rebalance(scheduler.getConfig().getGroup());
		
		return groupScheduler.size();
	}
	
	public int removeScheduler(BaseScheduler scheduler) {
		return removeScheduler(scheduler, false);
	}
	
	public int removeScheduler(String groupName) {
		BaseScheduler scheduler = findLast(groupName);
		if(scheduler != null) {
			return removeScheduler(scheduler);
		}
		
		return 0;
	}
	
	public final void removeGroup(String groupName) {
		while(removeScheduler(groupName) > 1) ;
		closeGroup(groupName);
	}
	
	public int getGroupSize(String groupName) {
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler))
			return groupScheduler.size();
		
		return 0;
	}
	
	public Set<BaseScheduler> getGroupScheduler(String groupName) {
		return group.get(groupName);
	}
	
	public final BaseScheduler find(String id) {
		Assert.hasLength(id, "id must be not empty.");
		String groupName = id.substring(0, id.lastIndexOf("-"));
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler)) {
			for(BaseScheduler scheduler : groupScheduler) {
				if(scheduler.getConfig().getId().equals(id))
					return scheduler;
			}
		}
		
		return null;
	}
	
	public BaseScheduler findLast(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler)) {
			int max = -1;
			for(BaseScheduler scheduler : groupScheduler) {
				if(scheduler.getConfig().getNum() > max)
					max = scheduler.getConfig().getNum();
			}
			
			for(BaseScheduler scheduler : groupScheduler) {
				if(scheduler.getConfig().getNum() == max) {
					return scheduler;
				}
			}
		}
		
		return null;
	}
	
	public void rebalance(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler)) {
			AtomicInteger idx = new AtomicInteger(0);
			groupScheduler.forEach(scheduler -> {
				scheduler.getConfig().setNum(idx.getAndIncrement());
				scheduler.getConfig().setTotal(groupScheduler.size());
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
			throw new LoaderException("Scheduler已经加载，这里不再进行重复的加载，如需重新加载请调用reload方法");
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
		
		Set<Class<?>> componentClasses = ComponentScan.filter(Scheduler.class);
		if(LOG.isInfoEnabled())
			LOG.info("Scheduler size: " + componentClasses.size());
		
		if(componentClasses.size() > 0) {
			if(includes.isEmpty()) 
				includes.add(".");
			
			for(Class<?> clz : componentClasses) {
				if(BaseScheduler.class.isAssignableFrom(clz)) {
					if(LOG.isInfoEnabled())
						LOG.info("Inject Scheduler Class: " + clz.getName());
					
					Scheduler scheduler = clz.getAnnotation(Scheduler.class);
					if(!ObjectCompare.isInListByRegEx(clz.getClass().getSimpleName(), includes) || ObjectCompare.isInListByRegEx(clz.getClass().getSimpleName(), exclusions)) {
						LOG.warn("过滤任务组: " + clz.getClass().getSimpleName() + ", 类名 [ " + clz.getName()+ " ]");
						continue ;
					}
					
					String parallelProperty = scheduler.parallelProperty();
					int parallel = 0;
					String cron = "";
					for(Properties properties : PropertiesLoader.PROPERTIES.values()) {
						String value;
						if(StringUtils.isNotBlank(value = properties.getProperty(parallelProperty))) {
							/** 采用最后设置的属性作为最终结果 */
							try {
								parallel = Integer.parseInt(value);
							} catch(NumberFormatException e) { 
								throw new SchedulerException("并行度属性设置错误, 属性名: [ " + parallelProperty + " ], 属性值: [ " + value + " ]");
							}
						}
						
						if(StringUtils.isNotBlank(value = properties.getProperty(scheduler.cronProperty())))
							cron = value;
					}
					
					parallel = scheduler.coreParallel() ? RuntimeUtil.AVAILABLE_PROCESSORS : parallel > 0 ? parallel : scheduler.parallel();
					if(parallel < 0)
						parallel = 0;
					
					if(StringUtils.isBlank(cron))
						cron = scheduler.cron();
					
					for(int p = 0; p < parallel; p ++) {
						BaseScheduler baseScheduler = (BaseScheduler) Globals.get(Injector.class).getInstance(clz);
						SchedulerConfig config = new SchedulerConfig();
						config.setId(clz.getClass().getSimpleName() + "-" + baseScheduler.getIndex(clz.getClass().getSimpleName()));
						config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + config.getId());
						config.setGroup(clz.getClass().getSimpleName());
						config.setService(service);
						config.setBeforeAfterOnly(scheduler.beforeAfterOnly());
						config.setRunNumberOfTimes(scheduler.runNumberOfTimes());
						config.setInterval(scheduler.interval());
						config.setNum(p);
						config.setTotal(parallel);
						if(StringUtils.isNotBlank(cron))
							try { config.setCron(new CronExpression(cron)); } catch(ParseException e) { throw new SchedulerException(e.getMessage(), e); }
					
						config.setDaemon(scheduler.daemon());
						config.setLazy(scheduler.lazy());
						baseScheduler.setConfig(config);
						
						if(getInstance().stoppedScheduler.containsKey(config.getId())) {
							throw new SchedulerException("\n\t任务调度重复: " + config.getId() + ", 组件类: {'" + clz.getName() + "', '" + getInstance().stoppedScheduler.get(config.getId()).getClass().getName() +"'}");
							
						}
						
						getInstance().stoppedScheduler.put(config.getId(), baseScheduler);
						Set<BaseScheduler> groupScheduler = getInstance().group.get(baseScheduler.getConfig().getGroup());
						if(groupScheduler == null) groupScheduler = Sets.newLinkedHashSet();
						groupScheduler.add(baseScheduler);
						getInstance().group.put(config.getGroup(), groupScheduler);
					}
				} else 
					throw new SchedulerException("必须继承: [ "+BaseScheduler.class.getName()+" ]");
				
			}
			
			/** Create and start Etcd Scheduler */
			createEtcdScheduler(componentClasses);
		}
		
		isLoaded = true;
	}
	
	private static final void createEtcdScheduler(Set<Class<?>> componentClasses) {
		try {
			boolean enable = Boolean.parseBoolean(System.getProperty(EtcdScheduler.ETCD_ENABLE, "false"));
			if(enable) {
				EtcdScheduler scheduler = new EtcdScheduler(componentClasses);
				etcdScheduler = scheduler;
				scheduler.getConfig().getService().execute(scheduler);
				scheduler.syncBaseDirTTL();
				scheduler.syncInfo();
				scheduler.syncClass();
				
				/** Start Order Scheduler */
				EtcdOrderWatcherScheduler etcdOrderScheduler = new EtcdOrderWatcherScheduler(scheduler.getEtcd());
				etcdOrderScheduler.getConfig().getService().execute(etcdOrderScheduler);
				
				if(LocalJmxMonitorScheduler.JMX_ENABLE) {
					LocalJmxMonitorScheduler jmxScheduler = new LocalJmxMonitorScheduler(scheduler.getEtcd());
					jmxScheduler.getConfig().getService().execute(jmxScheduler);
				}
			} else 
				etcdScheduler = EtcdSchedulerOperate.EMPTY;
			
		} catch(SchedulerException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 重新加载调度任务
	 */
	public static final void reload() {
		getInstance().stoppedScheduler.clear();
		getInstance().closeAll();
		service.execute(() -> {
			try { while(SchedulerFactory.getInstance().getStartedSchedulerSize() > 0) Thread.sleep(100L); } catch(InterruptedException e) { }
			if(LOG.isInfoEnabled())
				LOG.info("所有任务已经全部关闭");
			
			try {
				load();
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LOG.error(e.getMessage(), e);
			}
			
		});
	}
	
	protected class StatusMonitorScheduler extends BaseScheduler {
		private final ConcurrentMap<String, BaseScheduler> closed;
		
		public StatusMonitorScheduler() {
			SchedulerConfig config = new SchedulerConfig();
			config.setId("StatusMonitorScheduler-0");
			config.setName("StatusMonitorScheduler");
			config.setGroup("StatusMonitorScheduler");
			threadFactory.setBaseScheduler(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			try { config.setCron(new CronExpression("* * * * * ?")); } catch(ParseException e) {}
			config.setTotal(1);
			config.setDaemon(true);
			setConfig(config);
			setClose(false);
			closed = new ConcurrentHashMap<>();
		}
		
		@Override
		public void before() throws SchedulerException {
			stoppingScheduler.forEach((id, scheduler) -> {
				if(scheduler.isClosed())
					closed.put(id, scheduler);
			});
		}

		@Override
		public void execute() throws SchedulerException {
			closed.forEach((id, scheduler) -> {
				if(!scheduler.isRemove())
					stoppedScheduler.put(id, scheduler);
				
				stoppingScheduler.remove(id, scheduler);
				
				/** Sync to Etcd by stopped method */
				etcdScheduler.stopped(scheduler.getConfig().getGroup(), id, scheduler.isRemove());
			});
		}

		@Override
		public void after() throws SchedulerException {
			closed.clear();
		}

		@Override
		public void destroy() throws SchedulerException {
			
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
			closeAll();
			List<BaseScheduler> schedulers = Lists.newArrayList();
			schedulers.addAll(getStartedScheduler());
			schedulers.addAll(getStoppingScheduler());
			for(BaseScheduler scheduler : schedulers) {
				scheduler.thisNotify();
			}
			
			while((getStartedSchedulerSize() > 0 || getStoppingSchedulerSize() > 0) && System.currentTimeMillis() - time < shutdownTimeout) {
				try { Thread.sleep(100L); } catch(InterruptedException e) { }
				for(BaseScheduler scheduler : schedulers) {
					scheduler.thisNotify();
				}
			}
			
 			LOG.info("停止任务调度完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		}
		
	}
}
