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
 * @deprecated 使用Guava中的ImmutableMap替代此类
 */
@Deprecated
public class MapBuilder<K, V> {
    private Map<K, V> map;

    private MapBuilder() {
        
    }

    public static <K, V> MapBuilder<K, V> create() {
        MapBuilder<K, V> build = new MapBuilder<>();
        build.map = new HashMap<>();
        return build;
    }

    public static <K, V> MapBuilder<K, V> create(Class<? extends Map<K, V>> cls, Object... params) {
        MapBuilder<K, V> build = new MapBuilder<>();
        build.map = ReflectUtils.newInstance(cls, params);
        return build;
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return map;
    }
}
