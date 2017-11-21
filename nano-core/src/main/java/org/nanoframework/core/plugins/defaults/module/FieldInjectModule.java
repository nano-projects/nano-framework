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
package org.nanoframework.core.plugins.defaults.module;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.nanoframework.commons.util.ReflectUtils;
import org.nanoframework.core.inject.FieldInject;
import org.nanoframework.core.plugins.Module;
import org.nanoframework.core.spi.Order;

import javax.servlet.ServletConfig;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author yanghe
 * @since 1.4.10
 */
@Order(-9999)
public class FieldInjectModule implements Module {

    @Override
    public List<Module> load() throws Throwable {
        return Lists.newArrayList(this);
    }

    @Override
    public void config(final ServletConfig config) throws Throwable {

    }

    @Override
    public void configure(final Binder binder) {
        binder.bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
                final Field[] fields = type.getRawType().getDeclaredFields();
                for (final Field field : fields) {
                    if (field.isAnnotationPresent(FieldInject.class)) {
                        final FieldInject inject = field.getAnnotation(FieldInject.class);
                        encounter.register(ReflectUtils.newInstance(inject.value(), field));
                    }
                }
            }
        });
    }
}
