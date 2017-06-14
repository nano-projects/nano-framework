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

import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.core.plugins.Module;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;

/**
 * Plugin加载完成后绑定常量.
 * @author yanghe
 * @since 1.3.16
 */
public class BindModule extends Module {
    private static final List<String> BIND_MODULE_CLASS_NAMES = Lists.newArrayList(
        "org.nanoframework.orm.jdbc.binding.BindJdbcManagerModule",
        "org.nanoframework.orm.mybatis.binding.BindJdbcManagerModule", 
        "org.nanoframework.orm.jedis.binding.BindRedisClientModule"
    );

    @Override
    protected void configure() {
        BIND_MODULE_CLASS_NAMES.forEach(clsName -> install(clsName));
    }

    @SuppressWarnings("unchecked")
    protected void install(final String clsName) {
        try {
            final Class<? extends AbstractModule> cls = (Class<? extends AbstractModule>) Class.forName(clsName);
            final AbstractModule module = ReflectUtils.newInstance(cls);
            binder().install(module);
        } catch (final Throwable e) {
            if (!(e instanceof ClassNotFoundException)) {
                throw new BindModuleException(e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Module> load() throws Throwable {
        modules.add(this);
        return modules;
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {

    }

}
