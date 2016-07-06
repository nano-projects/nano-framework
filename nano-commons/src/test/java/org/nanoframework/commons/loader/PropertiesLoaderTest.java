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
package org.nanoframework.commons.loader;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author yanghe
 * @since 1.3.14
 */
public class PropertiesLoaderTest {
    
    @Test
    public void load0() {
        final String path = "/context.properties";
        final String notFoundPath = "/context_notfound.properties";
        final String classPath = "classpath:context.properties";
        try {
            PropertiesLoader.load(notFoundPath);
        } catch (final Throwable e) {
            Assert.assertEquals(e instanceof LoaderException, true);
        }
        
        Assert.assertNotNull(PropertiesLoader.load(path));
        Assert.assertNotNull(PropertiesLoader.load(classPath));
        
    }
    
    @Test
    public void load1() throws LoaderException, IOException {
        final String path = "/context.properties";
        PropertiesLoader.load(path, true);
    }
}
