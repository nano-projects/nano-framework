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
package org.nanoframework.extension.dubbo;

import com.alibaba.dubbo.config.annotation.Reference;
import org.nanoframework.core.inject.FieldInject;
import org.nanoframework.extension.dubbo.inject.DubboReferenceInjector;
import org.nanoframework.extension.dubbo.service.GenericService;

import java.util.Map;

/**
 * @author yanghe
 * @since 1.4.10
 */
public class GenericServiceProxy {
    private static final String SERVER = "localhost:20880";

    @FieldInject(DubboReferenceInjector.class)
    @Reference(check = false, group = "generic.integer", url = "dubbo://" + SERVER + "/org.nanoframework.extension.dubbo.service.GenericService")
    private GenericService<Integer> intService;

    @FieldInject(DubboReferenceInjector.class)
    @Reference(check = false, group = "generic.string", url = "dubbo://" + SERVER + "/org.nanoframework.extension.dubbo.service.GenericService")
    private GenericService<String> stringService;

    @FieldInject(DubboReferenceInjector.class)
    @Reference(check = false, group = "generic.map", url = "dubbo://" + SERVER + "/org.nanoframework.extension.dubbo.service.GenericService")
    private GenericService<Map<String, String>> mapService;

    public Object get(final Class<?> type) {
        switch (type.getSimpleName()) {
            case "Integer":
                return intService.get();
            case "String":
                return stringService.get();
            case "Map":
                return mapService.get();
            default:
                return null;
        }

    }

}
