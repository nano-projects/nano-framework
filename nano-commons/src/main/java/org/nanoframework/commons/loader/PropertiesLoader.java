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
package org.nanoframework.commons.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.io.ClassPathResource;
import org.nanoframework.commons.io.Resource;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Charsets;
import org.nanoframework.commons.util.ResourceUtils;

import com.google.common.collect.Maps;

/**
 * 属性文件操作公有类，负责对属性文件进行读写操作.
 * @author yanghe
 * @since 1.0
 */
public class PropertiesLoader {
    /** 属性文件集合/ */
    public static final Map<String, Properties> PROPERTIES = Maps.newHashMap();
    /** 属性配置列表根. */
    public static final String CONTEXT = "context";

    private static Logger LOGGER = LoggerFactory.getLogger(PropertiesLoader.class);

    /**
     * 根据路径加载属性文件.
     * @param path 属性文件路径
     * @return Properties
     */
    public static final Properties load(final String path) {
        try {
            InputStream input = null;
            try {
                final Resource resource = new ClassPathResource(path);
                input = resource.getInputStream();
            } catch (final IOException e) {
                // ignore
            }

            final Properties properties;
            if (input != null) {
                properties = PropertiesLoader.load(input);
            } else {
                properties = PropertiesLoader.load(ResourceUtils.getFile(path));
            }

            return properties;
        } catch (IOException e) {
            throw new LoaderException("加载属性文件异常: " + e.getMessage(), e);
        }
    }

    /**
     * 通过输入流加载属性文件.
     * @param input 文件输入流
     * @return 返回加载后的Properties
     */
    public static final Properties load(final InputStream input) {
        if (input == null) {
            throw new LoaderException("输入流为空");
        }

        try {
            final Properties prop = new Properties();
            prop.load(new InputStreamReader(input, Charsets.UTF_8));
            return prop;
        } catch (final IOException e) {
            throw new LoaderException("加载属性文件异常: " + e.getMessage());
        }
    }

    /**
     * 通过文件加载属性文件.
     * @param file 输入文件
     * @return 返回加载后的Properties
     * @throws LoaderException Loader异常
     * @throws IOException IO异常
     */
    private static final Properties load(final File file) throws LoaderException, IOException {
        if (file == null) {
            throw new LoaderException("文件对象为空");
        }

        final Properties prop = new Properties();
        prop.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
        return prop;
    }

    /**
     * 加载属性文件.
     * @param contextPath 文件相对路径
     * @param stream context属性流
     * @param loadContext 是否加载context
     * @throws LoaderException 加载异常
     * @throws IOException IO异常
     */
    @Deprecated
    public static final void load(final String contextPath, final InputStream stream, final boolean loadContext) throws LoaderException, IOException {
        final Properties prop = load(stream);
        prop.forEach((key, value) -> System.setProperty((String) key, (String) value));
        PROPERTIES.put(contextPath, prop);
        if (loadContext) {
            final String context = prop.getProperty(CONTEXT);
            if (StringUtils.isNotEmpty(context)) {
                final String[] ctxs = context.split(";");
                if (ctxs.length > 0) {
                    for (String ctx : ctxs) {
                        final Properties properties = load(ctx);
                        if (properties != null) {
                            PROPERTIES.put(ctx, properties);
                        } else {
                            LOGGER.error(ctx + ": 无法加载此属性文件!");
                        }
                    }

                }
            }
        }
    }

    /**
     * 加载属性文件.
     * @param contextPath 文件相对路径
     * @param loadContext 是否加载context
     * @throws LoaderException 加载异常
     * @throws IOException IO异常
     */
    public static final void load(final String contextPath, final boolean loadContext) throws LoaderException, IOException {
        final Properties prop = load(contextPath);
        prop.forEach((key, value) -> System.setProperty((String) key, (String) value));
        PROPERTIES.put(contextPath, prop);

        if (loadContext) {
            final String context = prop.getProperty(CONTEXT);
            if (StringUtils.isNotEmpty(context)) {
                final String[] ctxs = context.split(";");
                if (ctxs.length > 0) {
                    for (String ctx : ctxs) {
                        if (StringUtils.isNotBlank(ctx)) {
                            final Properties properties = load(ctx);
                            if (properties != null) {
                                PROPERTIES.put(ctx, properties);
                            } else {
                                LOGGER.error(ctx + ": 无法加载此属性文件!");
                            }
                        }
                    }
                }
            }
        }
    }

}
