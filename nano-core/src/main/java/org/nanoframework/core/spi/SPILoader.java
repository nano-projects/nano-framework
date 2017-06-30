/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.core.spi;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
public class SPILoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPILoader.class);
    private static final String SPI_DIR = "META-INF/nano/spi";
    private static Map<Class<?>, List<SPIMapper>> SPI_MAPPERS = Maps.newHashMap();
    private static AtomicBoolean LOADED = new AtomicBoolean(false);
    private static ReentrantLock LOCK = new ReentrantLock();
    
    public static Set<Class<?>> spis() {
        if (!LOADED.get()) {
            loading();
        }

        return Collections.unmodifiableSet(SPI_MAPPERS.keySet());
    }

    public static Map<Class<?>, List<SPIMapper>> spiMappers() {
        if (!LOADED.get()) {
            loading();
        }

        return Collections.unmodifiableMap(SPI_MAPPERS);
    }

    public static List<SPIMapper> spiMappers(final String spiClsName) {
        if (!LOADED.get()) {
            loading();
        }

        return Collections.unmodifiableList(SPI_MAPPERS.get(spiClsName));
    }

    public static List<SPIMapper> allSpiMappers() {
        if (!LOADED.get()) {
            loading();
        }

        final List<SPIMapper> all = Lists.newArrayList();
        SPI_MAPPERS.values().forEach(spiMappers -> all.addAll(spiMappers));
        return Collections.unmodifiableList(all);
    }

    protected SPILoader() {

    }

    protected static void loading() {
        final ReentrantLock lock = LOCK;
        try {
            lock.lock();
            final SPILoader loader = new SPILoader();
            final Enumeration<URL> resources;
            try {
                resources = loader.getResources();
            } catch (final Throwable e) {
                throw new SPIException("加载资源异常: " + e.getMessage(), e);
            }
    
            final List<File> spiFiles;
            try {
                spiFiles = loader.getSPIFiles(resources);
            } catch (final Throwable e) {
                throw new SPIException("获取SPI资源文件异常: " + e.getMessage(), e);
            }
    
            SPI_MAPPERS.putAll(loader.getSPIMapper(spiFiles));
            LOADED.set(true);
        } finally {
            lock.unlock();
        }
    }

    protected Enumeration<URL> getResources() throws IOException {
        final ClassLoader loader = SPILoader.class.getClassLoader();
        if (loader != null) {
            return loader.getResources(SPI_DIR);
        } else {
            return ClassLoader.getSystemResources(SPI_DIR);
        }
    }

    protected List<File> getSPIFiles(final Enumeration<URL> resources) throws URISyntaxException {
        if (resources != null) {
            final List<File> files = Lists.newArrayList();
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                final File file = new File(url.toURI());
                if (file.exists()) {
                    final File[] spiFiles = file.listFiles(f -> {
                        try {
                            if (f.isDirectory()) {
                                return false;
                            }

                            final Class<?> cls = Class.forName(f.getName());
                            if (cls.isAnnotationPresent(SPI.class)) {
                                return true;
                            }

                            LOGGER.warn("非SPI文件定义: {}", f.getName());
                            return false;
                        } catch (final ClassNotFoundException e) {
                            LOGGER.warn("未找到SPI文件定义: {}", f.getName());
                            return false;
                        }
                    });

                    if (ArrayUtils.isNotEmpty(spiFiles)) {
                        for (final File spiFile : spiFiles) {
                            files.add(spiFile);
                        }
                    }
                }
            }

            return files;
        }

        return Collections.emptyList();
    }

    protected Map<Class<?>, List<SPIMapper>> getSPIMapper(final List<File> spiFiles) {
        if (!CollectionUtils.isEmpty(spiFiles)) {
            Map<Class<?>, List<SPIMapper>> spiMappers = Maps.newHashMap();
            for (final File spiFile : spiFiles) {
                final String spiClsName = spiFile.getName();
                final Class<?> spiCls;
                try {
                    spiCls = Class.forName(spiClsName);
                } catch (final ClassNotFoundException e) {
                    // ignore
                    continue;
                }

                final Properties define = PropertiesLoader.load(spiFile.getAbsolutePath());
                define.keySet().forEach(name -> {
                    final String instanceClsName = define.getProperty((String) name);
                    try {
                        final Class<?> instanceCls = Class.forName(instanceClsName);
                        if (spiCls.isAssignableFrom(instanceCls)) {
                            final SPIMapper spiMapper = SPIMapper.create(spiCls, (String) name, instanceCls);
                            if (!spiMappers.containsKey(spiCls)) {
                                spiMappers.put(spiCls, Lists.newArrayList(spiMapper));
                            } else {
                                spiMappers.get(spiCls).add(spiMapper);
                            }
                        } else {
                            LOGGER.warn("无法加载类: {}, 未实现接口 {}", instanceClsName, spiClsName);
                        }
                    } catch (final ClassNotFoundException e) {
                        LOGGER.warn("未定义SPI实现类: {} = {}", name, instanceClsName);
                    }
                });
            }

            return spiMappers;
        }

        return Collections.emptyMap();
    }
}
