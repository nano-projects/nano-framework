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

import org.nanoframework.examples.quickstart.component.RestComponent;
import org.nanoframework.examples.quickstart.dao.ElementsDao;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 *
 * @author yanghe
 * @since 1.3.4
 */
public class RestComponentImpl implements RestComponent {
    private static final ResultMap OK = ResultMap.create("OK", HttpStatus.OK);
    private static final Map<String, Object> OK_MAP = OK._getBeanToMap();
    private static final ResultMap FAIL = ResultMap.create("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
    
    @Inject
    private ElementsDao elementsDao;
    
    @Override
    public Map<String, Object> getElements() {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        result.put("value", elementsDao.findAll());
        return result;
    }

    @Override
    public Map<String, Object> getElement(Long id) {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        Element element = elementsDao.findById(id);
        if(element != null) {
            result.put("value", element);
        }
        
        return result;
    }

    @Override
    public ResultMap postElement(Element el) {
        if(el.getId() != null) {
            return ResultMap.create("POST请求 Element Id不可存在.", HttpStatus.BAD_REQUEST);
        }
        
        if(elementsDao.insert(el) > 0) {
            return OK;
        }
        
        return FAIL;
    }

    @Override
    public ResultMap putElement(Element el) {
        if(el.getId() == null) {
            return ResultMap.create("PUT请求 Element Id必须存在", HttpStatus.BAD_REQUEST);
        }
        
        if(elementsDao.update(el) > 0) {
            return OK;
        }
        
        return FAIL;
    }

    @Override
    public ResultMap deleteElement(Long id) {
        if(elementsDao.deleteById(id) > 0) {
            return OK;
        }
        
        return FAIL;
    }

}
