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
package org.nanoframework.commons.io;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.util.ClassUtils;

/**
 *
 * @author yanghe
 * @since 1.3.14
 */
public class ClassPathResourceTest {

    @Test
    public void classPathResourceTest0() throws IOException {
        final String path = "/messages/messages.properties";
        final String noStartPath = path.substring(1);
        final ClassPathResource resource0 = new ClassPathResource(path);
        asserts(resource0, noStartPath);
        
        final ClassPathResource resource1 = new ClassPathResource(noStartPath, this.getClass().getClassLoader());
        asserts(resource1, noStartPath);
        
        final ClassPathResource resource2 = new ClassPathResource(path, this.getClass());
        asserts(resource2, path);
    }
    
    private void asserts(final ClassPathResource resource, final String path) throws IOException {
        Assert.assertEquals(resource.getPath(), path);
        Assert.assertEquals(resource.getClassLoader(), ClassUtils.getDefaultClassLoader());
        Assert.assertNotNull(resource.getInputStream());
        Assert.assertNotNull(resource.getURL());
        final File file = resource.getFile();
        Assert.assertNotNull(file);
        Assert.assertNotNull(resource.getFileForLastModifiedCheck());
        Assert.assertEquals(resource.getFilename(), "messages.properties");
        if (path.startsWith("/")) {
            Assert.assertEquals(resource.getDescription(), "class path resource [/messages/messages.properties]");
        } else {
            Assert.assertEquals(resource.getDescription(), "class path resource [messages/messages.properties]");
        }
        final Resource relativeResource = resource.createRelative("/messages_zh_CN.properties");
        Assert.assertNotNull(relativeResource);
        Assert.assertNotNull(relativeResource.getInputStream());
        Assert.assertNotNull(relativeResource.getURL());
        final File file2 = relativeResource.getFile();
        Assert.assertNotNull(file2);
        Assert.assertEquals(relativeResource.getFilename(), "messages_zh_CN.properties");
        if (path.startsWith("/")) {
            Assert.assertEquals(relativeResource.getDescription(), "class path resource [/messages/messages_zh_CN.properties]");
        } else {
            Assert.assertEquals(relativeResource.getDescription(), "class path resource [messages/messages_zh_CN.properties]");
        }
        
        Assert.assertEquals(resource.equals(relativeResource), false);
        Assert.assertEquals(resource.equals(resource), true);
        Assert.assertEquals(resource.exists(), true);
        
        final Resource notFoundResource = resource.createRelative("/messages_notfound.properties");
        Assert.assertEquals(notFoundResource.exists(), false);
        
        Assert.assertEquals(resource.isReadable(), true);
        Assert.assertEquals(resource.isOpen(), false);
        
    }
}
