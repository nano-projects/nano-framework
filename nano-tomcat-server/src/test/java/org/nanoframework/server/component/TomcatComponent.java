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
package org.nanoframework.server.component;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.server.component.impl.TomcatComponentImpl;

import com.google.inject.ImplementedBy;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
@Component
@ImplementedBy(TomcatComponentImpl.class)
public interface TomcatComponent {

    @RequestMapping(value = "/hello/{val:\\d+}", method = RequestMethod.POST)
    String hello();
    
    @RequestMapping(value = "/hello/{val:\\d+}", method = RequestMethod.GET)
    String hello2();
    
    @RequestMapping("/HELLO/{val:[A-G]+}")
    String hello2(@PathVariable("val") String val);
    
    @RequestMapping("/HELLO/{val:[E-Z]+}")
    String hello3(@PathVariable("val") String val);
    
    @RequestMapping("/hello/{val:\\d+}/{ad}")
    String hello(@PathVariable("val") Integer val, @PathVariable("ad") String ad);
}
