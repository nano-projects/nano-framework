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

import org.nanoframework.core.component.aop.After;
import org.nanoframework.core.component.aop.AfterInterceptor;
import org.nanoframework.core.component.aop.AfterMore;
import org.nanoframework.core.component.aop.AfterMoreInterceptor;
import org.nanoframework.core.component.aop.Before;
import org.nanoframework.core.component.aop.BeforeAndAfter;
import org.nanoframework.core.component.aop.BeforeAndAfterInterceptor;
import org.nanoframework.core.component.aop.BeforeAndAfterMore;
import org.nanoframework.core.component.aop.BeforeAndAfterMoreInterceptor;
import org.nanoframework.core.component.aop.BeforeInterceptor;
import org.nanoframework.core.component.aop.BeforeMore;
import org.nanoframework.core.component.aop.BeforeMoreInterceptor;
import org.nanoframework.core.plugins.Module;

import com.google.inject.matcher.Matchers;

/**
 * @author yanghe
 * @since 1.1
 */
public class AOPModule extends Module {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Before.class), new BeforeInterceptor());
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(After.class), new AfterInterceptor());
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(BeforeAndAfter.class), new BeforeAndAfterInterceptor());

        // Interceptor More
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(BeforeMore.class), new BeforeMoreInterceptor());
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(AfterMore.class), new AfterMoreInterceptor());
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(BeforeAndAfterMore.class), new BeforeAndAfterMoreInterceptor());
    }

    @Override
    public List<Module> load() throws Throwable {
        modules.add(this);
        return modules;
    }

    @Override
    public void config(ServletConfig config) throws Throwable {

    }
}
