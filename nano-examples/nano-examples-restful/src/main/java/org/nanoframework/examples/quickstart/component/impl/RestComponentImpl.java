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
package org.nanoframework.examples.quickstart.component.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.nanoframework.examples.quickstart.component.RestComponent;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;

import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.3.4
 */
public class RestComponentImpl implements RestComponent {
    private static final AtomicLong INDEX = new AtomicLong();
    private static final ConcurrentMap<Long, Element> ELEMENTS = Maps.newConcurrentMap();
    private static final ResultMap OK = ResultMap.create("OK", HttpStatus.OK);
    private static final Map<String, Object> OK_MAP = OK._getBeanToMap();
    
    @Override
    public Map<String, Object> getElements() {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        result.put("value", ELEMENTS.values());
        return result;
    }

    @Override
    public Map<String, Object> getElement(Long id) {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        if(ELEMENTS.containsKey(id)) {
            result.put("value", ELEMENTS.get(id));
        }
        
        return result;
    }

    @Override
    public ResultMap postElement(Element el) {
        if(el.getId() != null) {
            return ResultMap.create("POST请求 Element Id不可存在.", HttpStatus.BAD_REQUEST);
        }
        
        el.setId(INDEX.incrementAndGet());
        ELEMENTS.put(el.getId(), el);
        
        return OK;
    }

    @Override
    public ResultMap putElement(Element el) {
        if(el.getId() == null) {
            return ResultMap.create("PUT请求 Element Id必须存在", HttpStatus.BAD_REQUEST);
        }
        
        ELEMENTS.put(el.getId(), el);
        
        return OK;
    }

    @Override
    public ResultMap deleteElement(Long id) {
        Element el = ELEMENTS.remove(id);
        if(el == null) {
            return ResultMap.create("Element对象不存在", HttpStatus.BAD_REQUEST);
        }
        
        return OK;
    }

}
