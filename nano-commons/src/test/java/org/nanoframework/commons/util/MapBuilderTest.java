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
package org.nanoframework.commons.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author yanghe
 * @since 1.3.15
 * @deprecated 现在使用Guava ImmutableMap 来代替次实现类
 */
@Deprecated
public class MapBuilderTest {

    @Test
    public void buildTest() {
        final Map<String, Integer> map = MapBuilder.<String, Integer>builder().put("1", 1).put("2", 2).build();
        Assert.assertEquals(map.size(), 2);
        
        final Map<String, Integer> concurrentMap = MapBuilder.<String, Integer>create(ReflectUtils.convert(ConcurrentHashMap.class)).put("1", 1).build();
        Assert.assertEquals(concurrentMap instanceof ConcurrentMap, true);
    }
}
