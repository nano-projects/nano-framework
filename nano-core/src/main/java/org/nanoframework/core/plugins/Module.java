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
package org.nanoframework.core.plugins;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;

import com.google.inject.AbstractModule;

/**
 * @author yanghe
 * @date 2015年10月31日 上午11:02:21
 */
public abstract class Module extends AbstractModule {
    protected List<Module> modules = new ArrayList<>();

    public abstract List<Module> load() throws Throwable;

    public abstract void config(ServletConfig config) throws Throwable;
}
