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

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class CollectionBuild<E> {
    private Collection<E> collection;
    
    private CollectionBuild() {
        
    }
    
    public static <E> CollectionBuild<E> create() {
        CollectionBuild<E> build = new CollectionBuild<>();
        build.collection = new ArrayList<>();
        return build;
    }
    
    public static <E> CollectionBuild<E> create(Class<? extends Collection<E>> cls, Object... params) {
        CollectionBuild<E> build = new CollectionBuild<>();
        build.collection = ReflectUtils.newInstance(cls, params);
        return build;
    }
    
    public CollectionBuild<E> add(E element) {
        collection.add(element);
        return this;
    }
    
    public Collection<E> build() {
        return collection;
    }
}
