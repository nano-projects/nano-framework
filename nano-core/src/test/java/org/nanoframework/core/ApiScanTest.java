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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.component.PluginLoaderInit;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.Routes;


/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class ApiScanTest extends PluginLoaderInit {

    @Test
    public void apiTest() throws LoaderException, IOException {
        final Object invoke = Components.invoke(Routes.route().lookupRoute("/invoke", RequestMethod.GET));
        LOGGER.debug("[ /invoke ] response: {}", invoke);
        Assert.assertEquals(invoke, "Api Invoke");
        
        final Object invoke2 = Components.invoke(Routes.route().lookupRoute("/invoke2", RequestMethod.GET));
        LOGGER.debug("[ /invoke2 ] response: {}", invoke2);
        Assert.assertEquals(invoke2, "Api Invoke 2");
    }
}
