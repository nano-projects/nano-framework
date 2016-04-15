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
package org.nanoframework.extension.mail;

import static org.nanoframework.extension.mail.AbstractMailSenderFactory.FROM;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.HOST;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.PASSWORD;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.PORT;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.USERNAME;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.VALIDATE;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.DEBUG_ENABLED;

import org.junit.Before;
import org.junit.Test;
import org.nanoframework.extension.mail.defaults.DefaultMailSenderFactory;

import com.google.inject.Guice;

import junit.framework.Assert;

/**
 *
 * @author yanghe
 * @since 1.3.3
 */
public class MailSendTest {
    @Before
    public void initBefore() {
        System.setProperty(HOST, "smtp.exmail.qq.com");
        System.setProperty(PORT, "465");
        System.setProperty(VALIDATE, "true");
        System.setProperty(USERNAME, "test@nanoframework.org");
        System.setProperty(PASSWORD, "RkUxNTM4Mjg4RUNDNzYyQkEyMzQwRDMyOEUyNEI1NTY1");
        System.setProperty(FROM, "test@nanoframework.org");
        System.setProperty(DEBUG_ENABLED, "true");
    }
    
    @Test
    public void sendMailTest() {
        DefaultMailSenderFactory mailSenderFactory = Guice.createInjector().getInstance(DefaultMailSenderFactory.class);
        Assert.assertEquals(mailSenderFactory.sendMail("NanoFramework Compiler", "Package or Depoly NanoFramework", "comicme_yanghe@icloud.com"), true);
    }
}
