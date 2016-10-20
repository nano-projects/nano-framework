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
package org.nanoframework.server.component.impl;

import org.nanoframework.server.component.TomcatComponent;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class TomcatComponentImpl implements TomcatComponent {

    @Override
    public String hello() {
        return "hello";
    }
    
    @Override
    public String hello2() {
        return "hello 2";
    }

    @Override
    public String hello2(final String val) {
        return "hello2 " + val;
    }
    
    @Override
    public String hello3(String val) {
        return "hello3 " + val;
    }

    @Override
    public String hello(Integer val, String ad) {
        return "hello " + val + " " + ad;
    }

}
