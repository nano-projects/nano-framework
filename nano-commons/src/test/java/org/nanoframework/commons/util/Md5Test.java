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
package org.nanoframework.commons.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class Md5Test {

    @Test
    public void md5Test() throws IOException {
        final String content = "md5";
        final String encode = "1bc29b36f623ba82aaf6724fd3b16718";
        final File file = new File("md5.txt");
        if (!file.exists()) {
            file.createNewFile();
            file.deleteOnExit();
            
            try (final OutputStream output = new FileOutputStream(file)) {
                output.write(content.getBytes());
                output.flush();
            }
        }
        
        final String fileMd5 = MD5Utils.md5(file);
        Assert.assertEquals(encode, fileMd5);
        
        final String md5 = MD5Utils.md5(content);
        Assert.assertEquals(encode, md5);
        
        Assert.assertEquals(MD5Utils.check(content, encode), true);
    }
}
