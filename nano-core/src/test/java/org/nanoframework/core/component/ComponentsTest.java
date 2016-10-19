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
package org.nanoframework.core.component;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.commons.exception.UnsupportedAccessException;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.core.component.aop.AfterAOP;
import org.nanoframework.core.component.aop.BeforeAOP;
import org.nanoframework.core.component.exception.ComponentInvokeException;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.Routes;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class ComponentsTest extends PluginLoaderInit {
    
    @Test
    public void componentTest() throws LoaderException, IOException {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/test", RequestMethod.GET);
        Assert.assertNotNull(mapper);
        final RequestMethod[] requestMethods = mapper.getRequestMethods();
        final List<RequestMethod> methods = Lists.asList(requestMethods[0], requestMethods);
        Assert.assertEquals(methods.contains(RequestMethod.GET), true);
        Assert.assertEquals(methods.contains(RequestMethod.POST), true);
        Assert.assertEquals(mapper.getInstance() instanceof TestComponent, true);
        
        final Object ret = Components.invoke(mapper, Maps.newHashMap());
        Assert.assertNotNull(ret);
        Assert.assertEquals(ret, "OK");
        
    }
    
    @Test
    public void reloadTest() throws LoaderException, IOException {
        Components.reload();
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/reload", RequestMethod.POST);
        Assert.assertNotNull(mapper);
        final Object reload = Components.invoke(mapper, null);
        Assert.assertEquals(reload, "Reload");
    }
    
    @Test
    public void hasParamTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/param/hello", RequestMethod.GET);
        Assert.assertNotNull(mapper);
        final Object hasParam = Components.invoke(mapper, MapBuilder.<String, Object>create().put("param1", "world").build());
        Assert.assertEquals(hasParam, "hello=world");
    }
    
    @Test
    public void notFoundRouteTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/param/hello", RequestMethod.POST);
        Assert.assertEquals(mapper, null);
    }
    
    @Test
    public void emptyParamTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/param/hello", RequestMethod.GET);
        Assert.assertNotNull(mapper);
        try {
            Components.invoke(mapper, null);
        } catch (final Throwable e) {
            Assert.assertEquals(e instanceof ComponentInvokeException, true);
        }
    }
    
    @Test
    public void beforeAopTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/aop/before", RequestMethod.PUT);
        Assert.assertNotNull(mapper);
        final Object ret = Components.invoke(mapper, MapBuilder.<String, Object>create().put("param", "before").build());
        Assert.assertEquals(ret, "before");
        Assert.assertEquals(BeforeAOP.RESULT, "before");
        BeforeAOP.RESULT = null;
    }
    
    @Test
    public void afterAopTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/aop/after", RequestMethod.PUT);
        Assert.assertNotNull(mapper);
        Components.invoke(mapper, null);
        Assert.assertEquals(AfterAOP.RESULT, "OK");
        AfterAOP.RESULT = null;
    }
    
    @Test
    public void afterAopErrorTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/aop/after/error", RequestMethod.PUT);
        Assert.assertNotNull(mapper);
        try {
            Components.invoke(mapper, null);
        } catch (final Throwable e) {
            Assert.assertEquals(e instanceof ComponentInvokeException, true);
            Assert.assertEquals(e.getCause() instanceof UnsupportedAccessException, true);
        }
        
        Assert.assertEquals(AfterAOP.RESULT instanceof UnsupportedAccessException, true);
        AfterAOP.RESULT = null;
    }
    
    @Test
    public void arrayTest() {
        final RequestMapper mapper = Routes.route().lookupRoute("/v1/array", RequestMethod.PUT);
        final String[] array = { "1", "2", "3" };
        Object value = Components.invoke(mapper, MapBuilder.<String, Object>create().put("ARRAY[]", array).build());
        Assert.assertNotNull(value);
        Assert.assertEquals(JSON.toJSONString(array), value);
    }
}
