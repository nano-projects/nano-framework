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
package org.nanoframework.core.component.impl;

import org.nanoframework.commons.exception.UnsupportedAccessException;
import org.nanoframework.core.component.TestComponent;
import org.nanoframework.core.component.aop.After;
import org.nanoframework.core.component.aop.AfterAOP;
import org.nanoframework.core.component.aop.Before;
import org.nanoframework.core.component.aop.BeforeAOP;

import com.alibaba.fastjson.JSON;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class TestComponentImpl implements TestComponent {

    @Override
    public String test() {
        return "OK";
    }

    @Override
    public String reload() {
        return "Reload";
    }

    @Override
    public String hasParam(final String param0, final String param1) {
        return param0 + '=' + param1;
    }

    @Before(BeforeAOP.class)
    @Override
    public String beforeAop(final String param) {
        return param;
    }

    @After(AfterAOP.class)
    @Override
    public String afterAop() {
        return "OK";
    }

    @After(AfterAOP.class)
    @Override
    public String afterAopError() {
        throw new UnsupportedAccessException();
    }

    @Override
    public String arrayTest(String[] array) {
        return JSON.toJSONString(array);
    }
}
