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

import org.junit.Test;

/**
 * @author yanghe
 * @since 1.4.11
 */
public class ZipTest {

    @Test
    public void decodeTest() {
        final String value = "1234567890";
        final String gzip = ZipUtils.gzip(value);
        final String val1 = ZipUtils.gunzip(gzip);

        org.junit.Assert.assertEquals(val1, value);

        final String zip = ZipUtils.zip(value);
        final String val2 = ZipUtils.unzip(zip);

        org.junit.Assert.assertEquals(val2, value);
    }
}
