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

import java.util.List;

import javax.servlet.ServletConfig;

import org.nanoframework.core.plugins.Module;

import com.alibaba.dubbo.config.annotation.Reference;
import com.google.inject.matcher.Matchers;

/**
 *
 * @author yanghe
 * @since 1.4.1
 */
public class DubboReferenceModule extends Module {
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
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Reference.class), new DubboReferenceInterceptor());
    }
    
}
