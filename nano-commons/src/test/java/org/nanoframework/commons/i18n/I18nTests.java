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

import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 * @author yanghe
 * @date 2016年3月5日 下午4:17:18
 */
public class I18nTests {
    private Logger LOG = LoggerFactory.getLogger(I18nTests.class);

    @Test
    public void i18nTest() {
        LOG.debug(MessageSource.DEFAULT.getMessage("message.hello", new Object[] { ".1." }));
        LOG.debug(MessageSource.DEFAULT.getMessage("message.hello", new Object[] { ".2." }, Locale.TRADITIONAL_CHINESE));
        LOG.debug(MessageSource.DEFAULT.getMessage("message.hello", new Object[] { ".3." }, Locale.ROOT));
    }

    @Test
    public void i18nFailedTest() {
        LOG.debug(MessageSource.DEFAULT.getMessage("message.hello", "Not Found I18N Properties", Locale.ENGLISH));
    }
}
