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
package org.nanoframework.core.stereotype.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.Routes;
import org.nanoframework.core.context.ApplicationContext;

import com.google.common.collect.Maps;

/**
 * @author yanghe
 * @date 2015年9月23日 下午9:29:00
 */
public class RouteTests {

    @Test
    public void registerAndLookupTest() {
        System.setProperty(ApplicationContext.CONTEXT_ROOT, "/jetty");
        final Map<RequestMethod, RequestMapper> mapper = Maps.newHashMap();
        mapper.put(RequestMethod.GET, RequestMapper.create().setInstance(this).setCls(this.getClass()));
        Routes.route().register("/jetty/test/{hello}/get", mapper);
        Routes.route().register("/jetty/test/{hello}", mapper);
        Routes.route().register("/jetty/test/{hello}/save", mapper);
        Routes.route().register("/jetty/test/{hello}/put", mapper);
        Routes.route().register("/jetty/test/{hello}/put/{id}", mapper);
        Routes.route().register("/jetty/test/hello/put/{id:\\d+}/", mapper);
        RequestMapper rm = Routes.route().lookup("/jetty/test/hello/put/123", RequestMethod.GET);
        assertNotNull(rm);
        assertEquals("123", rm.getParam().get("id"));
    }
}
