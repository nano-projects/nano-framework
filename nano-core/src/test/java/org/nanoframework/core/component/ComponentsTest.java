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
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.core.component.impl.TestComponentImpl;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.DefaultPluginLoader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class ComponentsTest {

    private static PluginLoader PLUGIN_LOADER;
    
    @Before
    public void init() {
        if (PLUGIN_LOADER == null) {
            PLUGIN_LOADER = new DefaultPluginLoader();
            PLUGIN_LOADER.init(new ServletConfig() {
                
                @Override
                public String getServletName() {
                    return null;
                }
                
                @Override
                public ServletContext getServletContext() {
                    return null;
                }
                
                @Override
                public Enumeration<String> getInitParameterNames() {
                    return null;
                }
                
                @Override
                public String getInitParameter(String name) {
                    return null;
                }
            });
        }
    }
    
    @Test
    public void componentTest() throws LoaderException, IOException {
        final RequestMapper mapper = Components.getMapper("/v1/test", RequestMethod.GET);
        Assert.assertNotNull(mapper);
        final RequestMethod[] requestMethods = mapper.getRequestMethods();
        final List<RequestMethod> methods = Lists.asList(requestMethods[0], requestMethods);
        Assert.assertEquals(methods.contains(RequestMethod.GET), true);
        Assert.assertEquals(methods.contains(RequestMethod.POST), true);
        Assert.assertEquals(mapper.getClz(), TestComponentImpl.class);
        Assert.assertEquals(mapper.getObject() instanceof TestComponent, true);
        
        final Object ret = Components.invoke(mapper, Maps.newHashMap());
        Assert.assertNotNull(ret);
        Assert.assertEquals(ret, "OK");
        
        Components.reload();
        final RequestMapper reloadMapper = Components.getMapper("/v1/reload", RequestMethod.POST);
        Assert.assertNotNull(reloadMapper);
        final Object reload = Components.invoke(reloadMapper, null);
        Assert.assertEquals(reload, "Reload");
        
        final RequestMapper hasParamMapper = Components.getMapper("/v1/param/hello", RequestMethod.GET);
        Assert.assertNotNull(hasParamMapper);
        final Object hasParam = Components.invoke(hasParamMapper, MapBuilder.<String, Object>create().put("param1", "world").build());
        Assert.assertEquals(hasParam, "hello=world");
    }
    
}
