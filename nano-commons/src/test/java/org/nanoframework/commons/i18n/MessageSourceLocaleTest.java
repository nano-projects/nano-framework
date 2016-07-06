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
package org.nanoframework.commons.i18n;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author yanghe
 * @since 1.3.14
 */
public class MessageSourceLocaleTest {

    @Test
    public void getMessage0Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello", "defaultMessage");
        Assert.assertEquals(message, "简体中文{}");
    }
    
    @Test
    public void getMessage1Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello", "defaultMessage", Locale.TAIWAN);
        Assert.assertEquals(message, "繁体中文{}");
    }
    
    @Test
    public void getMessage2Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello.notfound", "defaultMessage");
        Assert.assertEquals(message, "defaultMessage");
    }
    
    @Test
    public void getMessage3Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello.notfound", "defaultMessage", Locale.TAIWAN);
        Assert.assertEquals(message, "defaultMessage");
    }
    
    @Test
    public void getMessage4Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello", new Object[]{"test"}, "defaultMessage");
        Assert.assertEquals(message, "简体中文test");
    }
    
    @Test
    public void getMessage5Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello", new Object[]{"test"}, "defaultMessage", Locale.TAIWAN);
        Assert.assertEquals(message, "繁体中文test");
    }
    
    @Test
    public void getMessage6Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello.notfound", new Object[]{"test"}, "defaultMessage");
        Assert.assertEquals(message, "defaultMessage");
    }
    
    @Test
    public void getMessage7Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello.notfound", new Object[]{"test"}, "defaultMessage", Locale.TAIWAN);
        Assert.assertEquals(message, "defaultMessage");
    }
    
    @Test
    public void getMessage8Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello");
        Assert.assertEquals(message, "简体中文{}");
    }
    
    @Test
    public void getMessage9Test() {
        final MessageSource source = new DefaultMessageSource();
        try {
            source.getMessage("message.hello.notfound");
        } catch (final Throwable e) {
            Assert.assertEquals(e instanceof NoSuchMessageException, true);
        }
    }
    
    @Test
    public void getMessage10Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello", new Object[]{"test"});
        Assert.assertEquals(message, "简体中文test");
    }
    
    @Test
    public void getMessage11Test() {
        final MessageSource source = new DefaultMessageSource();
        try {
            source.getMessage("message.hello.notfound", new Object[]{"test"});
        } catch (final Throwable e) {
            Assert.assertEquals(e instanceof NoSuchMessageException, true);
        }
    }
    
    @Test
    public void getMessage12Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello", Locale.TAIWAN);
        Assert.assertEquals(message, "繁体中文{}");
    }
    
    @Test
    public void getMessage13Test() {
        final MessageSource source = new DefaultMessageSource();
        try {
            source.getMessage("message.hello.notfound", Locale.TAIWAN);
        } catch (final Throwable e) {
            Assert.assertEquals(e instanceof NoSuchMessageException, true);
        }
    }
    
    @Test
    public void getMessage14Test() {
        final MessageSource source = new DefaultMessageSource();
        final String message = source.getMessage("message.hello", new Object[]{"test"}, Locale.TAIWAN);
        Assert.assertEquals(message, "繁体中文test");
    }
    
    @Test
    public void getMessage15Test() {
        final MessageSource source = new DefaultMessageSource();
        try {
            source.getMessage("message.hello.notfound", new Object[]{"test"}, Locale.TAIWAN);
        } catch (final Throwable e) {
            Assert.assertEquals(e instanceof NoSuchMessageException, true);
        }
    }
}
