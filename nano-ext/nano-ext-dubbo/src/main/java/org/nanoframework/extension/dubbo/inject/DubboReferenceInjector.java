/*
 * Copyright 2015-2017 the original author or authors.
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

package org.nanoframework.extension.dubbo.inject;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.google.inject.MembersInjector;

import java.lang.reflect.Field;

/**
 * @author yanghe
 * @since 1.4.10
 */
public class DubboReferenceInjector<T> implements MembersInjector<T> {
    private final Field field;

    /**
     * @param field 依赖注入属性Field
     */
    public DubboReferenceInjector(final Field field) {
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(final T instance) {
        try {
            final Reference reference = field.getAnnotation(Reference.class);
            final ReferenceConfig<?> refer = new ReferenceConfig<>(reference);
            refer.setCheck(reference.check());
            refer.setInterface(field.getType());
            final Object newInstance = ReferenceConfigCache.getCache().get(refer);
            field.set(instance, newInstance);
        } catch (final IllegalAccessException e) {
            throw (Error) new IllegalAccessError(e.getMessage()).initCause(e);
        }
    }
}
