/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.concurrent.scheduler;

import static org.nanoframework.core.context.ApplicationContext.Scheduler.BASE_PACKAGE;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.EXCLUSIONS;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.INCLUDES;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.SHUTDOWN_TIMEOUT;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.nanoframework.concurrent.exception.SchedulerException;
import org.nanoframework.core.component.scan.ClassScanner;
import org.nanoframework.core.globals.Globals;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

/**
 * 任务工厂.
 * 
 * @author yanghe
 * @since 1.3
 */
public class SchedulerFactory {
    public static final String DEFAULT_SCHEDULER_NAME_PREFIX = "Scheduler-Thread-Pool: ";
    public static final SchedulerThreadFactory THREAD_FACTORY = new SchedulerThreadFactory();

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerFactory.class);
    private static SchedulerFactory FACTORY;
    private static final Object LOCK = new Object();
    private static AtomicBoolean LOADED = new AtomicBoolean(false);

    private static final ThreadPoolExecutor SERVICE = (ThreadPoolExecutor) Executors.newCachedThreadPool(THREAD_FACTORY);

    private final ConcurrentMap<String, BaseScheduler> startedScheduler = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BaseScheduler> stoppingScheduler = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BaseScheduler> stoppedScheduler = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<BaseScheduler>> group = new ConcurrentHashMap<>();

    private final long shutdownTimeout = Long.parseLong(System.getProperty(SHUTDOWN_TIMEOUT, "60000"));

    private SchedulerFactory() {

    }

    public static SchedulerFactory getInstance() {
        synchronized (LOCK) {
            if (FACTORY == null) {
                FACTORY = new SchedulerFactory();
                final StatusMonitorScheduler statusMonitor = FACTORY.new StatusMonitorScheduler();
                statusMonitor.getConfig().getService().execute(statusMonitor);
                Runtime.getRuntime().addShutdownHook(new Thread(FACTORY.new ShutdownHook()));
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
    public BaseScheduler bind(final BaseScheduler scheduler) {
        try {
            scheduler.setClose(false);
            startedScheduler.put(scheduler.getConfig().getId(), scheduler);
            return scheduler;
        } finally {
            LOGGER.info("绑定任务: 任务号[ {} ]", scheduler.getConfig().getId());
        }
    }

    /**
     * 解绑任务
     * 
     * @param scheduler 任务
     * @return 返回当前任务
     */
    protected BaseScheduler unbind(final BaseScheduler scheduler) {
        final BaseScheduler removedScheduler = startedScheduler.remove(scheduler.getConfig().getId());
        if (removedScheduler != null) {
            LOGGER.debug("解绑任务 : 任务号[ {} ], 现存任务数: {}", scheduler.getConfig().getId(), startedScheduler.size());
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

    public int getStoppedSchedulerSize() {
        return stoppedScheduler.size();
    }

    public Collection<BaseScheduler> getStoppedScheduler() {
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
    public void close(final String id) {
        try {
            final BaseScheduler scheduler = startedScheduler.get(id);
            close(scheduler);
        } finally {
            LOGGER.info("关闭任务, 任务号: {}", id);
        }
    }

    public void close(final BaseScheduler scheduler) {
        if (scheduler != null && !scheduler.isClose()) {
            scheduler.setClose(true);
            stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
            startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
        }
    }

    /**
     * 关闭整组任务
     * @param groupName the groupName
     */
    public void closeGroup(final String groupName) {
        Assert.hasLength(groupName, "groupName must not be null");
        final Set<String> ids = Sets.newHashSet();
        startedScheduler.forEach((id, scheduler) -> {
            if (groupName.equals(scheduler.getConfig().getGroup())) {
                if (!scheduler.isClose()) {
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
        if (startedScheduler.size() > 0) {
            LOGGER.warn("现在关闭所有的任务");
            startedScheduler.keySet().forEach(id -> {
                try {
                    final BaseScheduler scheduler = startedScheduler.get(id);
                    if (scheduler != null && !scheduler.isClose()) {
                        scheduler.setClose(true);
                        stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
                    }

                } finally {
                    LOGGER.debug("关闭任务, 任务号: {}", id);
                }
            });

            startedScheduler.clear();
        }
    }

    /**
     * 启动所有缓冲区中的任务并清理任务缓冲区
     */
    public void startAll() {
        if (stoppedScheduler.size() > 0) {
            stoppedScheduler.forEach((id, scheduler) -> {
                LOGGER.info("Start scheduler [ {} ], class with [ {} ]", id, scheduler.getClass().getName());

                bind(scheduler);
                THREAD_FACTORY.setBaseScheduler(scheduler);
                SERVICE.execute(scheduler);
            });

            stoppedScheduler.clear();
        }
    }

    public void startGroup(final String groupName) {
        if (stoppedScheduler.size() > 0) {
            final Set<String> keys = Sets.newHashSet();
            stoppedScheduler.forEach((id, scheduler) -> {
                if (groupName.equals(scheduler.getConfig().getGroup())) {
                    if (scheduler.isClose()) {
                        LOGGER.info("Start scheduler [ {} ], class with [ {} ]", id, scheduler.getClass().getName());

                        bind(scheduler);
                        THREAD_FACTORY.setBaseScheduler(scheduler);
                        SERVICE.execute(scheduler);
                        keys.add(id);
                    }
                }
            });

            for (String key : keys) {
                stoppedScheduler.remove(key);
            }
        }
    }

    public void start(final String id) {
        final BaseScheduler scheduler = stoppedScheduler.get(id);
        if (scheduler != null && scheduler.isClose()) {
            LOGGER.info("Start scheduler [ {} ], class with [ {} ]", id, scheduler.getClass().getName());

            bind(scheduler);
            THREAD_FACTORY.setBaseScheduler(scheduler);
            SERVICE.execute(scheduler);
            stoppedScheduler.remove(id);
        }
    }

    public void append(final String groupName, final int size, final boolean autoStart) {
        final BaseScheduler scheduler = findLast(groupName);
        if (scheduler == null) {
            return;
        }

        for (int idx = 0; idx < size; idx++) {
            final SchedulerConfig conf = (SchedulerConfig) scheduler.getConfig().clone();
            int total = conf.getTotal();
            conf.setTotal(total + 1);
            conf.setNum(total);
            conf.setId(groupName + '-' + scheduler.getIndex(groupName));
            conf.setName(DEFAULT_SCHEDULER_NAME_PREFIX + conf.getId());

            final BaseScheduler newScheduler = scheduler.clone();
            newScheduler.setClose(true);
            newScheduler.setClosed(true);
            newScheduler.setRemove(false);
            newScheduler.setConfig(conf);
            addScheduler(newScheduler);
            if (autoStart) {
                start(conf.getId());
            }
        }
    }

    public boolean closed(final String id) {
        return stoppedScheduler.containsKey(id);
    }

    public boolean started(final String id) {
        return startedScheduler.containsKey(id);
    }

    public boolean hasClosedGroup(final String group) {
        if (stoppedScheduler.size() > 0) {
            final Collection<BaseScheduler> schedulers = stoppedScheduler.values();
            for (final BaseScheduler scheduler : schedulers) {
                final SchedulerConfig conf = scheduler.getConfig();
                if (conf.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasStartedGroup(final String group) {
        if (startedScheduler.size() > 0) {
            for (final BaseScheduler scheduler : startedScheduler.values()) {
                final SchedulerConfig conf = scheduler.getConfig();
                if (conf.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addScheduler(final BaseScheduler scheduler) {
        Set<BaseScheduler> groupScheduler = group.get(scheduler.getConfig().getGroup());
        if (groupScheduler == null) {
            groupScheduler = Sets.newLinkedHashSet();
        }

        groupScheduler.add(scheduler);
        group.put(scheduler.getConfig().getGroup(), groupScheduler);

        if (stoppedScheduler.containsKey(scheduler.getConfig().getId()) || startedScheduler.containsKey(scheduler.getConfig().getId())) {
            throw new SchedulerException("exists scheduler in memory");
        }

        stoppedScheduler.put(scheduler.getConfig().getId(), scheduler);
        rebalance(scheduler.getConfig().getGroup());
    }

    public int removeScheduler(final BaseScheduler scheduler, final boolean force) {
        final Set<BaseScheduler> groupScheduler = group.get(scheduler.getConfig().getGroup());
        if (groupScheduler.size() > 1 || force) {
            groupScheduler.remove(scheduler);
            scheduler.setRemove(true);

            // 对已经停止的任务进行删除时，需要主动删除stoppedScheduler
            stoppedScheduler.remove(scheduler.getConfig().getId(), scheduler);
        }

        if (!scheduler.isClosed()) {
            close(scheduler.getConfig().getId());
        }

        rebalance(scheduler.getConfig().getGroup());
        return groupScheduler.size();
    }

    public int removeScheduler(final BaseScheduler scheduler) {
        return removeScheduler(scheduler, false);
    }

    public int removeScheduler(final String groupName) {
        final BaseScheduler scheduler = findLast(groupName);
        if (scheduler != null) {
            return removeScheduler(scheduler);
        }

        return 0;
    }

    public final void removeGroup(final String groupName) {
        while (removeScheduler(groupName) > 1) {
            ;
        }

        closeGroup(groupName);
    }

    public int getGroupSize(final String groupName) {
        final Set<BaseScheduler> groupScheduler = group.get(groupName);
        if (!CollectionUtils.isEmpty(groupScheduler)) {
            return groupScheduler.size();
        }

        return 0;
    }

    public Set<BaseScheduler> getGroupScheduler(final String groupName) {
        return group.get(groupName);
    }

    public final BaseScheduler find(final String id) {
        Assert.hasLength(id, "id must be not empty.");
        final String groupName = id.substring(0, id.lastIndexOf('-'));
        final Set<BaseScheduler> groupScheduler = group.get(groupName);
        if (!CollectionUtils.isEmpty(groupScheduler)) {
            for (final BaseScheduler scheduler : groupScheduler) {
                final SchedulerConfig conf = scheduler.getConfig();
                if (conf.getId().equals(id)) {
                    return scheduler;
                }
            }
        }

        return null;
    }

    public BaseScheduler findLast(final String groupName) {
        Assert.hasLength(groupName);
        final Set<BaseScheduler> groupScheduler = group.get(groupName);
        if (!CollectionUtils.isEmpty(groupScheduler)) {
            int max = -1;
            for (final BaseScheduler scheduler : groupScheduler) {
                final SchedulerConfig conf = scheduler.getConfig();
                if (conf.getNum() > max) {
                    max = scheduler.getConfig().getNum();
                }
            }

            for (final BaseScheduler scheduler : groupScheduler) {
                final SchedulerConfig conf = scheduler.getConfig();
                if (conf.getNum() == max) {
                    return scheduler;
                }
            }
        }

        return null;
    }

    public void rebalance(final String groupName) {
        Assert.hasLength(groupName);
        final Set<BaseScheduler> groupScheduler = group.get(groupName);
        if (!CollectionUtils.isEmpty(groupScheduler)) {
            final AtomicInteger idx = new AtomicInteger(0);
            groupScheduler.forEach(scheduler -> {
                scheduler.getConfig().setNum(idx.getAndIncrement());
                scheduler.getConfig().setTotal(groupScheduler.size());
            });
        }
    }

    /**
     * 加载任务调度
     * @throws IllegalArgumentException 非法的参数列表
     * @throws IllegalAccessException ?
     */
    public static final void load() throws IllegalArgumentException, IllegalAccessException {
        if (LOADED.get()) {
            throw new LoaderException("Scheduler已经加载，这里不再进行重复的加载");
        }

        if (PropertiesLoader.PROPERTIES.size() == 0) {
            throw new LoaderException("没有加载任何的属性文件, 无法加载组件.");
        }

        final Set<String> includes = Sets.newLinkedHashSet();
        final Set<String> exclusions = Sets.newLinkedHashSet();
        PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.get(BASE_PACKAGE) != null).forEach(item -> {
            final String basePacakge = item.getProperty(BASE_PACKAGE);
            ClassScanner.scan(basePacakge);
        });

        PropertiesLoader.PROPERTIES.values().stream().forEach(item -> {
            if (item.containsKey(INCLUDES)) {
                String[] include = item.getProperty(INCLUDES, ".").split(",");
                for (String inc : include) {
                    includes.add(inc);
                }
            }

            if (item.containsKey(EXCLUSIONS)) {
                String[] exclusion = item.getProperty(EXCLUSIONS, "").split(",");
                for (String exc : exclusion) {
                    exclusions.add(exc);
                }
            }
        });

        final Set<Class<?>> schedulerClasses = ClassScanner.filter(Scheduler.class);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scheduler size: {}", schedulerClasses.size());
        }

        if (schedulerClasses.size() > 0) {
            if (includes.isEmpty()) {
                includes.add(".");
            }

            final Injector injector = Globals.get(Injector.class);
            for (final Class<?> clz : schedulerClasses) {
                if (BaseScheduler.class.isAssignableFrom(clz)) {
                    LOGGER.info("Inject Scheduler Class: {}", clz.getName());

                    final Scheduler scheduler = clz.getAnnotation(Scheduler.class);
                    if (!ObjectCompare.isInListByRegEx(clz.getSimpleName(), includes)
                            || ObjectCompare.isInListByRegEx(clz.getSimpleName(), exclusions)) {
                        LOGGER.warn("过滤任务组: {}, 类名 [ {} ]", clz.getSimpleName(), clz.getName());
                        continue;
                    }

                    String parallelProperty = scheduler.parallelProperty();
                    int parallel = 0;
                    String cron = "";
                    for (Properties properties : PropertiesLoader.PROPERTIES.values()) {
                        String value;
                        if (StringUtils.isNotBlank(value = properties.getProperty(parallelProperty))) {
                            /** 采用最后设置的属性作为最终结果 */
                            try {
                                parallel = Integer.parseInt(value);
                            } catch (final NumberFormatException e) {
                                throw new SchedulerException("并行度属性设置错误, 属性名: [ " + parallelProperty + " ], 属性值: [ " + value + " ]");
                            }
                        }

                        if (StringUtils.isNotBlank(value = properties.getProperty(scheduler.cronProperty()))) {
                            cron = value;
                        }
                    }

                    parallel = scheduler.coreParallel() ? RuntimeUtil.AVAILABLE_PROCESSORS : parallel > 0 ? parallel : scheduler.parallel();
                    if (parallel < 0) {
                        parallel = 0;
                    }

                    if (StringUtils.isBlank(cron)) {
                        cron = scheduler.cron();
                    }

                    for (int p = 0; p < parallel; p++) {
                        final BaseScheduler baseScheduler = (BaseScheduler) injector.getInstance(clz);
                        final SchedulerConfig conf = new SchedulerConfig();
                        conf.setId(clz.getSimpleName() + '-' + baseScheduler.getIndex(clz.getSimpleName()));
                        conf.setName(DEFAULT_SCHEDULER_NAME_PREFIX + conf.getId());
                        conf.setGroup(clz.getSimpleName());
                        conf.setService(SERVICE);
                        conf.setBeforeAfterOnly(scheduler.beforeAfterOnly());
                        conf.setRunNumberOfTimes(scheduler.runNumberOfTimes());
                        conf.setInterval(scheduler.interval());
                        conf.setNum(p);
                        conf.setTotal(parallel);
                        if (StringUtils.isNotBlank(cron)) {
                            try {
                                conf.setCron(new CronExpression(cron));
                            } catch (final ParseException e) {
                                throw new SchedulerException(e.getMessage(), e);
                            }
                        }

                        conf.setDaemon(scheduler.daemon());
                        conf.setLazy(scheduler.lazy());
                        conf.setDefined(scheduler.defined());
                        baseScheduler.setConfig(conf);

                        if (getInstance().stoppedScheduler.containsKey(conf.getId())) {
                            throw new SchedulerException("\n\t任务调度重复: " + conf.getId() + ", 组件类: {'" + clz.getName() + "', '"
                                    + getInstance().stoppedScheduler.get(conf.getId()).getClass().getName() + "'}");
                        }

                        getInstance().stoppedScheduler.put(conf.getId(), baseScheduler);
                        Set<BaseScheduler> groupScheduler = getInstance().group.get(baseScheduler.getConfig().getGroup());
                        if (groupScheduler == null) {
                            groupScheduler = Sets.newLinkedHashSet();
                        }
                        groupScheduler.add(baseScheduler);
                        getInstance().group.put(conf.getGroup(), groupScheduler);
                    }
                } else {
                    throw new SchedulerException("必须继承: [ " + BaseScheduler.class.getName() + " ]");
                }
            }
        }

        LOADED.set(true);
    }

    public void destory() {
        final long time = System.currentTimeMillis();
        LOGGER.info("开始停止任务调度");
        closeAll();
        final List<BaseScheduler> schedulers = Lists.newArrayList();
        schedulers.addAll(getStartedScheduler());
        schedulers.addAll(getStoppingScheduler());
        for (final BaseScheduler scheduler : schedulers) {
            scheduler.thisNotify();
        }

        while ((getStartedSchedulerSize() > 0 || getStoppingSchedulerSize() > 0) && System.currentTimeMillis() - time < shutdownTimeout) {
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException e) {
                // ignore
            }

            for (final BaseScheduler scheduler : schedulers) {
                scheduler.thisNotify();
            }
        }

        LOADED.set(false);
        FACTORY.startedScheduler.clear();
        FACTORY.stoppingScheduler.clear();
        FACTORY.stoppedScheduler.clear();
        group.clear();
        LoggerFactory.getLogger(this.getClass()).info("停止任务调度完成, 耗时: {}ms", System.currentTimeMillis() - time);
    }

    protected class StatusMonitorScheduler extends BaseScheduler {
        private final ConcurrentMap<String, BaseScheduler> closed;

        public StatusMonitorScheduler() {
            final SchedulerConfig conf = new SchedulerConfig();
            conf.setId("StatusMonitorScheduler-0");
            conf.setName("StatusMonitorScheduler");
            conf.setGroup("StatusMonitorScheduler");
            THREAD_FACTORY.setBaseScheduler(this);
            conf.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, THREAD_FACTORY));
            conf.setInterval(50L);
            conf.setTotal(1);
            conf.setDaemon(Boolean.TRUE);
            setConfig(conf);
            setClose(false);
            closed = Maps.newConcurrentMap();
        }

        @Override
        public void before() throws SchedulerException {
            stoppingScheduler.forEach((id, scheduler) -> {
                if (scheduler.isClosed()) {
                    closed.put(id, scheduler);
                }
            });
        }

        @Override
        public void execute() throws SchedulerException {
            closed.forEach((id, scheduler) -> {
                if (!scheduler.isRemove()) {
                    stoppedScheduler.put(id, scheduler);
                }

                stoppingScheduler.remove(id, scheduler);
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
            destory();
        }

    }
}
