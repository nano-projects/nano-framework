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
package org.nanoframework.server;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;

import com.google.common.collect.Maps;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:16:27
 */
public class JettyStartupTest {

    @Before
	public void init() throws InterruptedException {
	    JettyCustomServer.DEFAULT.bootstrap("start");
	    Thread.sleep(3000);
	}
    
    @Test
    public void invoke() throws IOException {
        final RequestMapper mapper = Components.getMapper("/v1/test", RequestMethod.GET);
        Object ret = Components.invoke(mapper, Maps.newHashMap());
        Assert.assertEquals(ret instanceof ResultMap, true);
        Assert.assertEquals(((ResultMap) ret).getInfo(), HttpStatus.OK.info);
    }
	
    @After
    public void destroy() throws InterruptedException {
        JettyCustomServer.DEFAULT.bootstrap("stop");
    }
}
