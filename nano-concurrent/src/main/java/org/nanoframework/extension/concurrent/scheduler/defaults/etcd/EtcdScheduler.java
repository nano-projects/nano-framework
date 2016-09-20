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
package org.nanoframework.extension.concurrent.scheduler.defaults.etcd;

import static org.nanoframework.core.context.ApplicationContext.Scheduler.ETCD_APP_NAME;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.ETCD_CLIENT_ID;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.ETCD_KEY_TTL;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.ETCD_MAX_RETRY_COUNT;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.ETCD_SCHEDULER_ANALYSIS;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.ETCD_URI;
import static org.nanoframework.core.context.ApplicationContext.Scheduler.ETCD_USER;
import static org.nanoframework.extension.concurrent.scheduler.SchedulerFactory.DEFAULT_SCHEDULER_NAME_PREFIX;
import static org.nanoframework.extension.concurrent.scheduler.SchedulerFactory.THREAD_FACTORY;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.MD5Utils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.concurrent.exception.SchedulerException;
import org.nanoframework.extension.concurrent.scheduler.BaseScheduler;
import org.nanoframework.extension.concurrent.scheduler.SchedulerAnalysis;
import org.nanoframework.extension.concurrent.scheduler.SchedulerConfig;
import org.nanoframework.extension.concurrent.scheduler.SchedulerFactory;
import org.nanoframework.extension.concurrent.scheduler.SchedulerStatus;
import org.nanoframework.extension.concurrent.scheduler.SchedulerStatus.Status;
import org.nanoframework.extension.concurrent.scheduler.defaults.monitor.LocalJmxMonitorScheduler;

import com.google.common.collect.Lists;

import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdKeysResponse;

/**
 * 
 * @author yanghe
 * @since 1.3
 */
public class EtcdScheduler extends BaseScheduler implements EtcdSchedulerOperate {
    public static final String SYSTEM_ID = MD5Utils.md5(UUID.randomUUID().toString() + System.currentTimeMillis() + Math.random());

    public static final String ROOT_RESOURCE = "/machairodus/" + System.getProperty(ETCD_USER, "");
    public static final String DIR = ROOT_RESOURCE + '/' + SYSTEM_ID;
    public static final String CLS_KEY = DIR + "/Scheduler.class";
    public static final String INSTANCE_KEY = DIR + "/Scheduler.list";
    public static final String INFO_KEY = DIR + "/App.info";
    public static final boolean SCHEDULER_ANALYSIS_ENABLE = Boolean.parseBoolean(System.getProperty(ETCD_SCHEDULER_ANALYSIS, "false"));
    private static String APP_NAME;
    
    private final Set<Class<?>> clsSet;
    private final int maxRetryCount = Integer.parseInt(System.getProperty(ETCD_MAX_RETRY_COUNT, "1"));
    private final int timeout = Integer.parseInt(System.getProperty(ETCD_KEY_TTL, "120"));
    private Map<Class<?>, String> clsIndex = new HashMap<Class<?>, String>();
    private Map<String, String> indexMap = new HashMap<String, String>();

    private boolean init = false;
    private EtcdClient etcd;

    public EtcdScheduler(final Set<Class<?>> clsSet) {
        Assert.notNull(clsSet);
        this.clsSet = clsSet;

        final SchedulerConfig config = new SchedulerConfig();
        config.setId("EtcdScheduler-0");
        config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + "EtcdScheduler-0");
        config.setGroup("EtcdScheduler");
        THREAD_FACTORY.setBaseScheduler(this);
        config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, THREAD_FACTORY));
        config.setInterval(60_000L);
        config.setTotal(1);
        config.setDaemon(Boolean.TRUE);
        config.setBeforeAfterOnly(Boolean.TRUE);
        config.setLazy(Boolean.TRUE);
        setConfig(config);
        setClose(false);

        initEtcdClient();
        if (etcd == null) {
            throw new SchedulerException("Can not init Etcd Client");
        }
    }

    @Override
    public void before() {

    }

    @Override
    public void execute() {
        syncBaseDirTTL();
        syncInfo();

        if (SCHEDULER_ANALYSIS_ENABLE)
            syncInstance();
    }

    public void syncBaseDirTTL() {
        try {
            if (!init) {
                etcd.putDir(DIR).ttl(timeout).prevExist(false).send().get();
                init = true;
            } else {
                etcd.putDir(DIR).ttl(timeout).prevExist(true).send().get();
            }
        } catch (final Throwable e) {
            LOGGER.error("Put base dir error: " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().indexOf("Key not found") > -1) {
                reSync();
                return;
            }

            if (e.getMessage() != null && e.getMessage().indexOf("Key already exists") > -1) {
                init = true;
                syncBaseDirTTL();
                return;
            }

            // 异常2秒重试
            thisWait(2000);
            syncBaseDirTTL();
        }
    }

    private void reSync() {
        init = false;
        clsIndex.clear();
        indexMap.clear();

        syncBaseDirTTL();
        syncInfo();
        syncClass();
        syncInstance();
    }

    public void syncInfo() {
        EtcdAppInfo info = new EtcdAppInfo();
        info.setSystemId(SYSTEM_ID);
        info.setAppName(APP_NAME);
        info.setJmxEnable(LocalJmxMonitorScheduler.JMX_ENABLE);
        info.setJmxRate(LocalJmxMonitorScheduler.JMX_RATE);

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        info.setStartTime(runtime.getStartTime());
        info.setUptime(runtime.getUptime());
        String[] rt = runtime.getName().split("@");
        info.setHostName(rt[1]);
        info.setPid(rt[0]);

        info.setAvailableProcessors(ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());

        try {
            info.setIp(Inet4Address.getLocalHost().getHostAddress());
            String value = CryptUtil.encrypt(info.toString(), SYSTEM_ID);
            etcd.put(INFO_KEY, value).send().get();
        } catch (Exception e) {
            LOGGER.error("Send App info error: " + e.getMessage());

            // 异常2秒重试
            thisWait(2000);
            syncBaseDirTTL();
        }
    }

    public void syncClass() {
        if (!CollectionUtils.isEmpty(clsSet)) {
            final Iterator<Class<?>> iter = clsSet.iterator();
            while (iter.hasNext()) {
                try {
                    final Class<?> cls = iter.next();
                    String index;
                    EtcdKeysResponse response;
                    if ((index = clsIndex.get(cls)) != null) {
                        response = etcd.put(CLS_KEY + '/' + index, cls.getName()).prevExist(true).send().get();
                    } else {
                        response = etcd.post(CLS_KEY, cls.getName()).send().get();
                        if (response.node != null) {
                            if ((index = response.node.key.substring(response.node.key.lastIndexOf('/'))) != null) {
                                clsIndex.put(cls, index);
                            }
                        }
                    }

                    LOGGER.debug("Class Sync: " + cls.getName());
                } catch (Exception e) {
                    LOGGER.error("Send to Etcd error: " + e.getMessage());
                }
            }
        }
    }

    public void syncInstance() {
        final Collection<BaseScheduler> started = SchedulerFactory.getInstance().getStartedScheduler();
        final Collection<BaseScheduler> stopping = SchedulerFactory.getInstance().getStoppingScheduler();
        final Collection<BaseScheduler> stopped = SchedulerFactory.getInstance().getStoppedScheduler();

        if (!CollectionUtils.isEmpty(started)) {
            for (BaseScheduler scheduler : started) {
                start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
            }
        }

        if (!CollectionUtils.isEmpty(stopping)) {
            for (BaseScheduler scheduler : stopping) {
                stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
            }
        }

        if (!CollectionUtils.isEmpty(stopped)) {
            for (BaseScheduler scheduler : stopped) {
                stopped(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), false, scheduler.getAnalysis());
            }
        }
    }

    @Override
    public void after() {

    }

    @Override
    public void destroy() {

    }

    private final void initEtcdClient() {
        /** create ETCD client instance */
        final String username = System.getProperty(ETCD_USER, "");
        final String clientId = CryptUtil.decrypt(System.getProperty(ETCD_CLIENT_ID, ""), username);
        APP_NAME = System.getProperty(ETCD_APP_NAME, "");
        final String[] uris = System.getProperty(ETCD_URI, "").split(",");
        if (!StringUtils.isEmpty(username.trim()) && !StringUtils.isEmpty(clientId.trim()) && !StringUtils.isEmpty(APP_NAME.trim())
                && uris.length > 0) {
            final List<URI> uriList = Lists.newArrayList();
            for (String uri : uris) {
                if (StringUtils.isEmpty(uri)) {
                    continue;
                }

                try {
                    uriList.add(URI.create(uri));
                } catch (final Throwable e) {
                    LOGGER.error("Etcd URI Error: " + e.getMessage());
                }
            }

            if (uriList.size() > 0) {
                etcd = new EtcdClient(username, clientId, uriList.toArray(new URI[uriList.size()]));
                etcd.setRetryHandler(new RetryWithExponentialBackOff(20, maxRetryCount, 1000));
            }
        }
    }

    private EtcdKeysResponse put(final String key, final SchedulerStatus status) {
        try {
            String index;
            EtcdKeysResponse response;
            final String value = CryptUtil.encrypt(status.toString(), SYSTEM_ID);
            if ((index = indexMap.get(status.getId())) != null) {
                response = etcd.put(key + '/' + index, value).prevExist(true).send().get();
            } else {
                response = etcd.post(key, value).send().get();
                if (response.node != null) {
                    if ((index = response.node.key.substring(response.node.key.lastIndexOf('/'))) != null) {
                        indexMap.put(status.getId(), index);
                    }
                }
            }

            return response;
        } catch (final Throwable e) {
            LOGGER.error("Put to etcd error: " + e.getMessage());
        }

        return null;
    }

    private EtcdKeysResponse delete(final String key, final SchedulerStatus status) {
        try {
            String index;
            EtcdKeysResponse response = null;
            if ((index = indexMap.get(status.getId())) != null) {
                response = etcd.delete(key + '/' + index).send().get();
                indexMap.remove(status.getId());
            }

            return response;
        } catch (final Throwable e) {
            LOGGER.error("Delete etcd file error: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void start(final String group, final String id, final SchedulerAnalysis analysis) {
        put(INSTANCE_KEY, new SchedulerStatus(group, id, Status.STARTED, analysis));
    }

    @Override
    public void stopping(final String group, final String id, final SchedulerAnalysis analysis) {
        put(INSTANCE_KEY, new SchedulerStatus(group, id, Status.STOPPING, analysis));
    }

    @Override
    public void stopped(final String group, final String id, final boolean isRemove, final SchedulerAnalysis analysis) {
        SchedulerStatus status = new SchedulerStatus(group, id, Status.STOPPED, analysis);
        if (!isRemove) {
            put(INSTANCE_KEY, status);
        } else {
            delete(INSTANCE_KEY, status);
        }
    }

    public EtcdClient getEtcd() {
        return etcd;
    }

    public static String getAppName() {
        return APP_NAME;
    }
}
