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
package org.nanoframework.commons;

import org.nanoframework.commons.crypt.CryptTest;
import org.nanoframework.commons.entity.BaseEntityTest;
import org.nanoframework.commons.format.ClassCastTest;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author yanghe
 * @date 2015年10月8日 下午1:55:50
 */
public class CommonsTestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Nano Framework Commons Test Suite");
        suite.addTest(new JUnit4TestAdapter(CryptTest.class));
        suite.addTest(new JUnit4TestAdapter(BaseEntityTest.class));
        suite.addTest(new JUnit4TestAdapter(ClassCastTest.class));
        return suite;
    }
}