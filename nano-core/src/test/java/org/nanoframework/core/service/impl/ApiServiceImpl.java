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
package org.nanoframework.core.service.impl;

import org.nanoframework.core.inject.API;
import org.nanoframework.core.service.ApiService;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
@API
public class ApiServiceImpl implements ApiService {

    @Override
    public String invoke() {
        return "Api Invoke";
    }

}
