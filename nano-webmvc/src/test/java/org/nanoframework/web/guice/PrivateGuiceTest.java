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
package org.nanoframework.web.guice;

import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yanghe
 * @date 2015年8月19日 上午9:23:37
 */
public class PrivateGuiceTest {

    @Test
    public void test0() {
        Injector injector = Guice.createInjector(new PrivateTestModule());
        TestPrivate test = injector.getInstance(TestPrivateInterceptor.class);
        test.invoke();

    }

    @ImplementedBy(TestPrivateInterceptor.class)
    public interface TestPrivate {
        public void invoke();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface TestAnno {
        String value() default "";
    }

    public static class TestPrivateInterceptor implements TestPrivate {
        @TestAnno
        @Override
        public void invoke() {
            System.out.println("test it");
        }
    }

    public class PrivateTestModule extends PrivateModule {

        @Override
        protected void configure() {
            TestInterceptor interceptor = new TestInterceptor();
            requestInjection(interceptor);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(TestAnno.class), interceptor);

        }

    }

    public class TestInterceptor implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            System.out.println("Run it");
            return invocation.proceed();
        }

    }

}
