/*
 * Copyright © 2015-2017 the original author or authors.
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
package org.nanoframework.extension.elasticjob.parser.reg;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.nanoframework.extension.elasticjob.exception.JobException;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.BASE_SLEEP_TIME_MILLISECOND;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.CONNECTION_TIMEOUT_MILLISECONDS;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.DIGEST;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.MAX_RETRIES;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.MAX_SLEEP_TIME_MILLISECONDS;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.NAMESPACE;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.REG;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.SERVER_LISTS;
import static org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey.SESSION_TIMEOUT_MILLISECONDS;

/**
 * 基于Zookeeper注册中心的属性文件解析器.
 *
 * @author wangtong
 * @since 1.4.11
 */
public final class ZookeeperPropertiesParser {
    private static final ZookeeperPropertiesParser INSTANCE = new ZookeeperPropertiesParser();

    private final Map<String, CoordinatorRegistryCenter> zkRegCenterMap = Maps.newConcurrentMap();

    private ZookeeperPropertiesParser() {
    }

    public static ZookeeperPropertiesParser getInstance() {
        return INSTANCE;
    }

    public CoordinatorRegistryCenter buildZookeeperRegistryCenter(final Properties properties, final String regRoot) {
        CoordinatorRegistryCenter registryCenter = zkRegCenterMap.get(regRoot);
        if (registryCenter == null) {
            synchronized (this) {
                registryCenter = zkRegCenterMap.get(regRoot);
                if (registryCenter == null) {
                    final String serverList = properties.getProperty(REG + regRoot + '.' + SERVER_LISTS);
                    final String namespace = properties.getProperty(REG + regRoot + '.' + NAMESPACE);
                    Preconditions.checkArgument(!Strings.isNullOrEmpty(serverList), "serverList can not be empty.");
                    Preconditions.checkArgument(!Strings.isNullOrEmpty(namespace), "namespace can not be empty.");
                    ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(serverList, namespace);
                    addPropertyValueIfNotEmpty(Integer.valueOf(properties.getProperty(REG + regRoot + '.' + BASE_SLEEP_TIME_MILLISECOND, "1000")), BASE_SLEEP_TIME_MILLISECOND, zkConfig);
                    addPropertyValueIfNotEmpty(Integer.valueOf(properties.getProperty(REG + regRoot + '.' + MAX_SLEEP_TIME_MILLISECONDS, "3000")), MAX_SLEEP_TIME_MILLISECONDS, zkConfig);
                    addPropertyValueIfNotEmpty(Integer.valueOf(properties.getProperty(REG + regRoot + '.' + MAX_RETRIES, "3")), MAX_RETRIES, zkConfig);
                    addPropertyValueIfNotEmpty(Integer.valueOf(properties.getProperty(REG + regRoot + '.' + SESSION_TIMEOUT_MILLISECONDS, "0")), SESSION_TIMEOUT_MILLISECONDS, zkConfig);
                    addPropertyValueIfNotEmpty(Integer.valueOf(properties.getProperty(REG + regRoot + '.' + CONNECTION_TIMEOUT_MILLISECONDS, "0")), CONNECTION_TIMEOUT_MILLISECONDS, zkConfig);
                    addPropertyValueIfNotEmpty(properties.getProperty(REG + regRoot + '.' + DIGEST), DIGEST, zkConfig);
                    registryCenter = new ZookeeperRegistryCenter(zkConfig);
                    registryCenter.init();
                    zkRegCenterMap.put(regRoot, registryCenter);
                }
            }
        }
        return registryCenter;
    }

    private void addPropertyValueIfNotEmpty(final Object propertyValue, final String attributeName, final ZookeeperConfiguration configuration) {
        if (propertyValue != null) {
            try {
                final Class<? extends ZookeeperConfiguration> clz = configuration.getClass();
                final Field field = clz.getDeclaredField(attributeName);
                field.setAccessible(true);
                field.set(configuration, propertyValue);
            } catch (Exception e) {
                throw new JobException(e.getMessage(), e);
            }
        }
    }
}
