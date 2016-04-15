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
package org.nanoframework.examples.quickstart.component;

import java.util.Map;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.RequestParam;
import org.nanoframework.examples.quickstart.component.impl.RestComponentImpl;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.web.server.http.status.ResultMap;

import com.google.inject.ImplementedBy;

/**
 *
 * @author yanghe
 * @since 1.3.4
 */
@Component
@ImplementedBy(RestComponentImpl.class)
@RequestMapping("/rest")
public interface RestComponent {
    /**
     * 获取所有Element对象
     * 
     * @return All element
     */
    @RequestMapping(value = "/elements", method = RequestMethod.GET)
    Map<String, Object> getElements();
    
    /**
     * 根据元素Id获取Element对象
     * 
     * @param id the element id
     * @return id of elements
     */
    @RequestMapping(value = "/elements/{id}", method = RequestMethod.GET)
    Map<String, Object> getElement(@PathVariable("id") Long id);
    
    /**
     * 新增Element
     * 
     * @param el Element对象(JSON格式)
     * @return ResultMap
     */
    @RequestMapping(value = "/elements", method = RequestMethod.POST)
    ResultMap postElement(@RequestParam("el") Element el);
    
    /**
     * 更新Element
     * 
     * @param el Element对象(JSON格式)
     * @return ResultMap
     */
    @RequestMapping(value = "/elements", method = RequestMethod.PUT)
    ResultMap putElement(@RequestParam("el") Element el);
    
    /**
     * 删除Element
     * 
     * @param id Element ID
     * @return ResultMap
     */
    @RequestMapping(value = "/elements/{id}", method = RequestMethod.DELETE)
    ResultMap deleteElement(@PathVariable("id") Long id);
    
}
