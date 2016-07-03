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
package org.nanoframework.core;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author yanghe
 * @date 2016年3月16日 下午2:11:03
 */
public class InjectorTest {

    @Test
    public void injectorObjectTest() {
        Injector injector = Guice.createInjector();
        Entity entity1 = injector.getInstance(Entity.class);
        Entity entity2 = injector.getInstance(Entity.class);
        Assert.assertEquals(StringUtils.equals(entity1.uuid, entity2.uuid), false);
    }

    @Test
    public void injectorSingletionObjectTest() {
        Injector injector = Guice.createInjector();
        EntitySingleton entity1 = injector.getInstance(EntitySingleton.class);
        EntitySingleton entity2 = injector.getInstance(EntitySingleton.class);
        Assert.assertEquals(StringUtils.equals(entity1.uuid, entity2.uuid), true);
    }

}
