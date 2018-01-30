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
package org.nanoframework.extension.elasticjob;

import static org.nanoframework.core.context.ApplicationContext.JOB_BASE_PACKAGE;
import static org.nanoframework.commons.util.StringUtils.EMPTY;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.CONF;
import static org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey.REG_ROOT;

import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.component.scan.ClassScanner;
import org.nanoframework.extension.elasticjob.exception.JobException;
import org.nanoframework.extension.elasticjob.parser.job.DataflowJobPropertiesParser;
import org.nanoframework.extension.elasticjob.parser.job.common.AbstractJobPropertiesParser;
import org.nanoframework.extension.elasticjob.parser.job.common.JobPropertiesKey;
import org.nanoframework.extension.elasticjob.parser.job.ScriptJobPropertiesParser;
import org.nanoframework.extension.elasticjob.parser.job.SimpleJobPropertiesParser;
import org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesKey;
import org.nanoframework.extension.elasticjob.parser.reg.ZookeeperPropertiesParser;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Job工厂
 *
 * @author wangtong
 * @since 1.4.11
 */
public class JobFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobFactory.class);

    private static final JobFactory INSTANCE = new JobFactory();

    private final Properties properties = new Properties();
    private final Set<String> confRootSet = Sets.newLinkedHashSet();
    private final Set<String> regRootSet = Sets.newLinkedHashSet();

    private final Map<Class<?>, CoordinatorRegistryCenter> regCenterMap = Maps.newConcurrentMap();
    private final Map<Class<?>, LiteJobConfiguration> litJobMap = Maps.newConcurrentMap();
    private final Map<Class<?>, JobEventConfiguration> jobEventMap = Maps.newConcurrentMap();
    private final Map<Class<?>, ElasticJobListener[]> listenerMap = Maps.newConcurrentMap();

    private JobFactory() {
    }

    public static JobFactory getInstance() {
        return INSTANCE;
    }

    public JobFactory initJobConfig(final List<Properties> jobs) {
        if (jobs.size() == 0) {
            throw new LoaderException("The elastic-job property file is not configured, and the component cannot be loaded.");
        }
        for (final Properties propertie : jobs) {
            if (propertie.containsKey(JobPropertiesKey.ROOT)) {
                final String[] confRoot = propertie.getProperty(JobPropertiesKey.ROOT, EMPTY).split(",");
                for (final String root : confRoot) {
                    confRootSet.add(root);
                }
            }
            if (propertie.containsKey(ZookeeperPropertiesKey.ROOT)) {
                final String[] regRoot = propertie.getProperty(ZookeeperPropertiesKey.ROOT, EMPTY).split(",");
                for (final String root : regRoot) {
                    regRootSet.add(root);
                }
            }
            properties.putAll(propertie);
        }
        return this;
    }

    public void load() {
        if (PropertiesLoader.PROPERTIES.size() == 0) {
            throw new LoaderException("No context property files are loaded, and components cannot be loaded.");
        }
        if (properties.size() == 0) {
            throw new LoaderException("The elastic-job property file is not configured, and the component cannot be loaded.");
        }
        if (confRootSet.size() == 0 || regRootSet.size() == 0) {
            throw new LoaderException("ejob.reg.root or ejob.conf.root properties cannot be found");
        }
        PropertiesLoader.PROPERTIES.values().stream().filter(item -> item.get(JOB_BASE_PACKAGE) != null).forEach(item -> {
            final String[] basePacakges = item.getProperty(JOB_BASE_PACKAGE, EMPTY).split(",");
            for (final String basePackage : basePacakges) {
                ClassScanner.scan(basePackage);
            }
        });
        final Set<Class<?>> jobClasses = ClassScanner.filter(ElasticJob.class);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Job size: {}", jobClasses.size());
        }
        if (jobClasses.size() > 0) {
            for (final Class<?> clz : jobClasses) {
                if (AbstractSimpleJob.class.isAssignableFrom(clz) || AbstractDataflowJob.class.isAssignableFrom(clz) ||
                        AbstractScriptJob.class.isAssignableFrom(clz)) {
                    final String confRoot = getJobConfRoot(clz.getAnnotation(ElasticJob.class), clz);
                    if (!confRootSet.contains(confRoot)) {
                        throw new LoaderException("No corresponding confRoot configuration: [ " + clz.getSimpleName() + " ]");
                    }
                    final String regRoot = properties.getProperty(CONF + confRoot + '.' + REG_ROOT);
                    if (!regRootSet.contains(regRoot)) {
                        throw new LoaderException("No corresponding regRoot configuration: [ " + clz.getSimpleName() + " ]");
                    }
                    regCenterMap.put(clz, ZookeeperPropertiesParser.getInstance().buildZookeeperRegistryCenter(properties, regRoot));
                    if (AbstractSimpleJob.class.isAssignableFrom(clz)) {
                        parserToMap(SimpleJobPropertiesParser.getInstance(), clz, confRoot);
                    } else if (AbstractDataflowJob.class.isAssignableFrom(clz)) {
                        parserToMap(DataflowJobPropertiesParser.getInstance(), clz, confRoot);
                    } else {
                        parserToMap(ScriptJobPropertiesParser.getInstance(), clz, confRoot);
                    }
                } else {
                    throw new JobException("[ " + clz.getSimpleName() + " ] must extends: [ " + AbstractSimpleJob.class.getSimpleName() + " || "
                            + AbstractDataflowJob.class.getSimpleName() + " || " + AbstractScriptJob.class.getSimpleName() + " ]");
                }
            }
        }
    }

    public void registerAll() {
        for (Map.Entry<Class<?>, LiteJobConfiguration> entry : litJobMap.entrySet()) {
            final LiteJobConfiguration liteJobConfiguration = entry.getValue();
            final CoordinatorRegistryCenter registryCenter = regCenterMap.get(entry.getKey());
            final JobEventConfiguration jobEventConfiguration = jobEventMap.get(entry.getKey());
            final ElasticJobListener[] listeners = listenerMap.get(entry.getKey());
            if (jobEventConfiguration == null) {
                new JobScheduler(registryCenter, liteJobConfiguration, listeners).init();
            } else {
                new JobScheduler(registryCenter, liteJobConfiguration, jobEventConfiguration, listeners).init();
            }
        }
    }

    private String getJobConfRoot(final ElasticJob job, final Class<?> clz) {
        final String value = job.value();
        if (Strings.isNullOrEmpty(value)) {
            final String simpleName = clz.getSimpleName();
            final char c = simpleName.charAt(0);
            if (Character.isLowerCase(c)) {
                return simpleName;
            }
            final char[] chars = simpleName.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            return new String(chars);
        }
        return value;
    }

    private void parserToMap(final AbstractJobPropertiesParser parser, final Class<?> clz, final String confRoot) {
        litJobMap.put(clz, parser.createLiteJobConfiguration(properties, confRoot, clz));
        listenerMap.put(clz, parser.createJobListeners(properties, confRoot));
        final JobEventConfiguration jobEventConfig = parser.createJobEventConfig(properties, confRoot);
        if (jobEventConfig != null) {
            jobEventMap.put(clz, jobEventConfig);
        }
    }
}
