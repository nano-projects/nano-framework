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
package org.nanoframework.core;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.plugins.defaults.module.SysAttrModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class AttrInjectTest {
    private static final String HELLO_WORLD = "Hello, World!";
    private static final String VALUE = "10";
    
    static {
        System.setProperty("inject.test", HELLO_WORLD);
        System.setProperty("inject.int", VALUE);
    }

    @Inject
    @Named("inject.test")
    private String attr;
    
    @Inject
    @Named("inject.int")
    private int intValue;
    
    @Test
    public void readInjectAttrTest() {
        Guice.createInjector(new SysAttrModule(), binder -> binder.requestInjection(this));
        LoggerFactory.getLogger(AttrInjectTest.class).debug("Inject System property value: {}", attr);
        Assert.assertEquals(attr, HELLO_WORLD);
        
        LoggerFactory.getLogger(AttrInjectTest.class).debug("Inject System property value: {}", intValue);
        Assert.assertEquals(intValue, Integer.parseInt(VALUE));
    }
    
}
