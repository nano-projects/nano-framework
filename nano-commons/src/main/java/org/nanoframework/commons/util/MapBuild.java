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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class MapBuild<K, V> {
    private Map<K, V> map;
    
    private MapBuild() {
        
    }
    
    public static <K, V> MapBuild<K, V> create() {
        MapBuild<K, V> build = new MapBuild<>();
        build.map = new HashMap<>();
        return build;
    }
    
    public static <K, V> MapBuild<K, V> create(Class<? extends Map<K, V>> cls, Object... params) {
        MapBuild<K, V> build = new MapBuild<>();
        build.map = ReflectUtils.newInstance(cls, params);
        return build;
    }
    
    public MapBuild<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }
    
    public Map<K, V> build() {
        return map;
    }
}
