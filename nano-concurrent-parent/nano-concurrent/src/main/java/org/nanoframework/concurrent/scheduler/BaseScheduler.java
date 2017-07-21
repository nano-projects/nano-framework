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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.concurrent.scheduler.exception.SchedulerException;
import org.nanoframework.concurrent.scheduler.single.SchedulerAnalysis;

import com.google.common.collect.Maps;

/**
 * 抽象Scheduler超类，对基本操作进行了封装.
 * @author yanghe
 * @since 1.3
 */
public abstract class BaseScheduler implements Runnable, Cloneable {
    /**
     * @deprecated 请更换Logger名，现在使用LOGGER替代LOG的命名，这个静态常量将在后续版本移除
     */
    @Deprecated
    protected static final Logger LOG = LoggerFactory.getLogger(BaseScheduler.class);
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseScheduler.class);

    private SchedulerConfig config;
    private boolean close = true;
    private boolean closed = true;
    private boolean remove = false;
    private boolean isRunning = false;
    private int nowTimes = 0;
    private Object LOCK = new Object();
    private AtomicBoolean isLock = new AtomicBoolean(false);
    private static Map<String, AtomicLong> index = Maps.newHashMap();
    private SchedulerAnalysis analysis = SchedulerAnalysis.newInstance();
    private SchedulerFactory factory = SchedulerFactory.getInstance();

    public BaseScheduler() {
    }

    public BaseScheduler(final SchedulerConfig config) {
        Assert.notNull(config, "SchedulerConfig must not be null");
        if (config.getRunNumberOfTimes() != null && config.getRunNumberOfTimes() < 0) {
            throw new SchedulerException("运行次数不能小于0.");
        }

        this.config = config;
    }

    @Override
    public void run() {
        try {
            try {
                if (config.getLazy()) {
                    long delay = delay();
                    LOGGER.warn("启动延时: " + delay + "ms");
                    thisWait(delay);
                }
            } catch (final Throwable e) {
                LOGGER.error("Lazy error: " + e.getMessage());
            }

            close = false;
            closed = false;
            remove = false;
            while (!close && !config.getService().isShutdown()) {
                final long start = System.currentTimeMillis();
                process();
                try {
                    analysis.executing.incrementAndGet();
                    analysis.putPerformCycle(System.currentTimeMillis() - start);
                } catch (final Throwable e) {
                    LOGGER.error("Analysis perform cycle error: {}", e.getMessage());
                }
            }

        } finally {
            // 保证结束Scheduler后能够正常的切换到Stop列表中，需要先设置close = false
            close = false;
            closed = true;
            factory.close(this);
            destroy();
        }
    }

    public void process() {
        if (config.getBeforeAfterOnly()) {
            try {
                if (!isRunning) {
                    try {
                        before();
                    } catch (final Throwable e) {
                        analysis.beforeException.incrementAndGet();
                        throw e;
                    }
                }

                try {
                    execute();
                } catch (final Throwable e) {
                    analysis.executeException.incrementAndGet();
                    throw e;
                }

                if (!isRunning) {
                    try {
                        after();
                    } catch (final Throwable e) {
                        analysis.afterException.incrementAndGet();
                        throw e;
                    }
                }

                if (!isRunning) {
                    isRunning = true;
                }
            } catch (final Throwable e) {
                LOGGER.error("任务运行异常: " + e.getMessage(), e);
                thisWait(100);
            } finally {
                finallyProcess();
            }

        } else {
            try {
                try {
                    before();
                } catch (final Throwable e) {
                    analysis.beforeException.incrementAndGet();
                    throw e;
                }

                try {
                    execute();
                } catch (final Throwable e) {
                    analysis.executeException.incrementAndGet();
                    throw e;
                }

                try {
                    after();
                } catch (Throwable e) {
                    analysis.afterException.incrementAndGet();
                    throw e;
                }

            } catch (final Throwable e) {
                LOGGER.error("任务运行异常: " + e.getMessage(), e);
                thisWait(100);

            } finally {
                finallyProcess();
            }
        }
    }

    /**
     * 逻辑调用结束后处理阶段
     */
    private void finallyProcess() {
        final ThreadPoolExecutor service = config.getService();
        if (service == null) {
            throw new SchedulerException("ThreadPoolExecutor不能为空");
        }

        if (!close && !service.isShutdown()) {
            final long interval = delay();
            final int runNumberOfTimes = config.getRunNumberOfTimes();
            if (runNumberOfTimes == 0) {
                thisWait(interval);
            } else {
                nowTimes++;
                if (nowTimes < runNumberOfTimes) {
                    thisWait(interval);
                } else {
                    close = true;
                    nowTimes = 0;
                }
            }
        }

    }

    private long delay() {
        final CronExpression cron = config.getCron();
        final long interval = config.getInterval();
        if (cron != null) {
            final long now = System.currentTimeMillis();
            return cron.getNextValidTimeAfter(new Date(now)).getTime() - now;
        }

        return interval;
    }

    /**
     * 任务等待
     * @param interval 等待时间
     */
    protected void thisWait(final long interval) {
        if (interval > 0) {
            synchronized (LOCK) {
                try {
                    isLock.set(true);
                    LOCK.wait(interval);
                } catch (final InterruptedException e) {
                    // ignore
                } finally {
                    isLock.set(false);
                }
            }
        }
    }

    public void thisNotify() {
        if (isLock.get()) {
            synchronized (LOCK) {
                try {
                    LOCK.notify();
                } catch (final Throwable e) {
                    // ignore
                } finally {
                    isLock.set(false);
                }
            }
        }
    }

    protected void thisWait() {
        synchronized (LOCK) {
            try {
                isLock.set(true);
                LOCK.wait();
            } catch (final InterruptedException e) {
                // ignore
            } finally {
                isLock.set(false);
            }
        }
    }

    /**
     * 逻辑执行前操作
     */
    public abstract void before();

    /**
     * 逻辑执行操作
     */
    public abstract void execute();

    /**
     * 逻辑执行后操作
     */
    public abstract void after();

    /**
     * 任务结束后销毁资源操作
     */
    public abstract void destroy();

    public boolean isClose() {
        return close;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClose(final boolean close) {
        this.close = close;
        thisNotify();
    }

    public void setClosed(final boolean closed) {
        this.closed = closed;
    }

    public void setRemove(final boolean remove) {
        this.remove = remove;
    }

    public boolean isRemove() {
        return remove;
    }

    public SchedulerConfig getConfig() {
        return config;
    }

    public void setConfig(final SchedulerConfig config) {
        Assert.notNull(config, "无效的任务调度配置");
        this.config = config;
    }

    public long getIndex(final String group) {
        AtomicLong idx;
        if ((idx = index.get(group)) == null) {
            index.put(group, idx = new AtomicLong());
        }

        return idx.getAndIncrement();
    }

    /**
     * @return the analysis
     */
    public SchedulerAnalysis getAnalysis() {
        return analysis;
    }

    @Override
    public BaseScheduler clone() {
        try {
            final BaseScheduler scheduler = (BaseScheduler) super.clone();
            scheduler.analysis = SchedulerAnalysis.newInstance();
            return scheduler;
        } catch (final CloneNotSupportedException e) {
            throw new SchedulerException(e.getMessage(), e);
        }
    }
}
