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
package org.nanoframework.core.context;

import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class URLContextTest {

    @Test
    public void contextTest() {
        final String uri = "/v1/test?param=123";
        final URLContext context = URLContext.formatURL(uri);
        Assert.assertEquals(context.getContext(), context.getNoRootContext());
        Assert.assertEquals(context.getParameter().isEmpty(), false);
        Assert.assertEquals(context.getParameter().get("param"), "123");
        
        System.setProperty(ApplicationContext.CONTEXT_ROOT, "/core");
        final String hasRootURI = "/core/v1/test?param=123";
        final URLContext hasRootContext = URLContext.formatURL(hasRootURI);
        Assert.assertEquals(hasRootContext.getContext(), "/core/v1/test");
        Assert.assertEquals(hasRootContext.getNoRootContext(), "/v1/test");
    }
    
}
