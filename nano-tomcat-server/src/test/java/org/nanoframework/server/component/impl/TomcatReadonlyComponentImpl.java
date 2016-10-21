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
package org.nanoframework.server.component.impl;

import java.util.Map;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.server.component.TomcatReadonlyComponent;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;
import org.nanoframework.web.server.stream.ReadStream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 *
 * @author yanghe
 * @since 1.4.3
 */
public class TomcatReadonlyComponentImpl implements TomcatReadonlyComponent {

    @Override
    public ResultMap put(final String key, final String spec, final String value) {
        return ResultMap.create(key + spec + value, HttpStatus.OK);
    }

    @Override
    public ResultMap putArray(final String key, final String[] values) {
        return ResultMap.create(key + '=' + JSON.toJSONString(values), HttpStatus.OK);
    }

    @Override
    public ResultMap putJson(final String key) {
        try {
            final Map<String, String[]> map = ReadStream.read(new TypeReference<Map<String, String[]>>() { });
            Assert.notEmpty(map);
            final String[] strArr = map.get("value");
            Assert.notEmpty(strArr);
            Assert.isTrue(strArr.length == 3);
            return ResultMap.create(key + '=' + JSON.toJSONString(strArr), HttpStatus.OK);
        } catch (final Throwable e) {
            return ResultMap.create(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResultMap del(final String key, final String spec, final String value) {
        return ResultMap.create(key + spec + value, HttpStatus.OK);
    }
    
    @Override
    public ResultMap post(final String key, final String spec, final String value) {
        return ResultMap.create(key + spec + value, HttpStatus.OK);
    }
}
