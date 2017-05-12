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
package org.nanoframework.extension.dubbo;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.scan.ClassScanner;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.Module;

import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.inject.Injector;

/**
 *
 * @author yanghe
 * @since 1.4.1
 */
public class DubboServiceModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboServiceModule.class);
    
    @Override
    public List<Module> load() throws Throwable {
        modules.add(this);
        return modules;
    }
    
    @Override
    public void config(final ServletConfig config) throws Throwable {
        
    }

    @Override
    protected void configure() {
        scanDubboService();
        bindDubboService();
    }
    
    protected void scanDubboService() {
        PropertiesLoader.PROPERTIES.values().stream()
        .filter(item -> StringUtils.isNotBlank(item.getProperty(ApplicationContext.DUBBO_SERVICE_BASE_PACKAGE)))
        .forEach(item -> {
            final String[] packageNames = item.getProperty(ApplicationContext.DUBBO_SERVICE_BASE_PACKAGE).split(",");
            Arrays.asList(packageNames).forEach(packageName -> ClassScanner.scan(packageName));
        });
    }
    
    protected void bindDubboService() {
        final List<ServiceConfig<Object>> serviceConfigs = Lists.newArrayList();
        final Injector injector = Globals.get(Injector.class);
        ClassScanner.filter(Service.class).forEach(cls -> {
            if (cls.isInterface()) {
                LOGGER.warn("Ignore interface API of {}", cls.getName());
                return;
            }
            
            final Class<?>[] itfs = cls.getInterfaces();
            if (ArrayUtils.isEmpty(itfs)) {
                LOGGER.warn("Ignore no interface implement API of {}", cls.getName());
                return;
            }
            
            final Service service = cls.getAnnotation(Service.class);
            final Object instance = injector.getInstance(cls);
            for (Class<?> itf : itfs) {
                final ServiceConfig<Object> serviceConfig = new ServiceConfig<>(service);
                serviceConfig.setInterface(itf);
                serviceConfig.setRef(instance);
                serviceConfigs.add(serviceConfig);
            }
        });
        
        serviceConfigs.forEach(conf -> conf.export());
    }
    
}
