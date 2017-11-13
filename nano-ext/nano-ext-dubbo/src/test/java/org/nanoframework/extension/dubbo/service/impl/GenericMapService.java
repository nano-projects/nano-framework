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
package org.nanoframework.extension.dubbo.service.impl;

import java.util.Map;

import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.extension.dubbo.service.GenericService;

import com.alibaba.dubbo.config.annotation.Service;

/**
 *
 * @author yanghe
 * @since 1.4.10
 */
@Service(group = "generic.map")
public class GenericMapService implements GenericService<Map<String, String>> {

    @Override
    public Map<String, String> get() {
        return MapBuilder.<String, String>builder().put("key1", "Hello").put("key2", "Generic").build();
    }

}
