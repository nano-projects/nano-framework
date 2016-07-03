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
package org.nanoframework.commons.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author yanghe
 * @date 2015年11月7日 下午6:05:00
 */
public class EntityMergeTest {

    @Test
    public void test0() {
        UseEntity use0 = new UseEntity();
        use0.setId("123");

        UseEntity use1 = new UseEntity();
        use1.setName("hello");

        UseEntity use2 = use0._merge(use1);
        Assert.assertEquals("123", use2.getId());
        Assert.assertEquals("hello", use2.getName());
    }

    @Test
    public void test1() {
        UseEntity use0 = new UseEntity();
        use0.setId("123");
        use0.setName("use0 name");

        UseEntity use1 = new UseEntity();
        use1.setName("hello");

        Assert.assertEquals("hello", use0._merge(use1).getName());
        Assert.assertEquals("use0 name", use0._merge(use1, false).getName());
    }
}
