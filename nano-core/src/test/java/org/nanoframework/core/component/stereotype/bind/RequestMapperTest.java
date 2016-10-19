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
package org.nanoframework.core.component.stereotype.bind;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.core.component.PluginLoaderInit;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class RequestMapperTest extends PluginLoaderInit {

    @Test
    public void hasMapperTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/test", RequestMethod.GET);
        Assert.assertEquals(mapper.hasMethod(RequestMethod.POST), true);
    }
    
    @Test
    public void requestMethodStrsTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/test", RequestMethod.GET);
        final String[] methods = mapper.getRequestMethodStrs();
        Assert.assertEquals(methods.length, 2);
        Assert.assertEquals(methods[0], RequestMethod.GET.name());
    }
}
