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
package org.nanoframework.core.plugins.defaults.module;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.scan.ClassScanner;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.inject.API;
import org.nanoframework.core.inject.BindException;
import org.nanoframework.core.plugins.Module;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.name.Names;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class APIModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(APIModule.class);
    
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
        scanApi();
        bindApi();
    }
    
    protected void scanApi() {
        PropertiesLoader.PROPERTIES.values().stream()
        .filter(item -> StringUtils.isNotBlank(item.getProperty(ApplicationContext.API_BASE_PACKAGE)))
        .forEach(item -> {
            final String[] packageNames = item.getProperty(ApplicationContext.API_BASE_PACKAGE).split(",");
            Arrays.asList(packageNames).forEach(packageName -> ClassScanner.scan(packageName));
        });
    }
    
    protected void bindApi() {
        final Map<Class, List<Class>> bindMap = Maps.newHashMap();
        ClassScanner.filter(API.class).forEach(cls -> {
            if (cls.isInterface()) {
                LOGGER.warn("Ignore interface API of {}", cls.getName());
                return;
            }
            
            final Class[] itfs = cls.getInterfaces();
            if (ArrayUtils.isEmpty(itfs)) {
                LOGGER.warn("Ignore no interface implement API of {}", cls.getName());
                return;
            }
            
            for (final Class itf : itfs) {
                List<Class> implList = bindMap.get(itf);
                if (implList == null) {
                    implList = Lists.newArrayList();
                    bindMap.put(itf, implList);
                }
                
                implList.add(cls);
            }
        });
        
        bindApi(bindMap);
    }
    
    protected void bindApi(final Map<Class, List<Class>> bindMap) {
        bindMap.forEach((itf, impls) -> {
            if (impls.size() == 1) {
                final Class cls = impls.get(0);
                binder().bind(itf).to(cls);
                LOGGER.debug("Binding {} to {}", itf.getName(), cls.getName());
            } else {
                if (itf.isAnnotationPresent(Component.class)) {
                    throw new BindException("Multiple components can not be bound");
                }
                
                bindApiWithName(itf, impls);
            }
        });
    }
    
    protected void bindApiWithName(final Class itf, final List<Class> impls) {
        impls.forEach(cls -> {
            final String apiName = ((API) cls.getAnnotation(API.class)).value();
            final String name;
            if (StringUtils.isNotBlank(apiName)) {
                name = apiName;
            } else {
                final String clsName = cls.getSimpleName();
                name = clsName.substring(0, 1).toLowerCase() + clsName.substring(1, clsName.length());
            }
            
            binder().bind(itf).annotatedWith(Names.named(name)).to(cls);
            LOGGER.debug("Binding {} to {} with name {}", itf.getName(), cls.getName(), name);
        });
    }

}
