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
package org.nanoframework.extension.dubbo;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import org.aopalliance.intercept.MethodInvocation;
import org.nanoframework.core.inject.AbstractMethodInjectInterceptor;

import java.lang.reflect.Method;

/**
 * @author yanghe
 * @since 1.4.1
 * @deprecated 已废除，建议使用注解的方式替代getter/setter的方式
 * @see org.nanoframework.extension.dubbo.inject.DubboReferenceInjector
 */
@Deprecated
public class DubboReferenceInterceptor extends AbstractMethodInjectInterceptor {

    @Override
    protected void inject(final MethodInvocation invocation, final Method method, final Class<?> returnType) throws Throwable {
        final Reference reference = method.getAnnotation(Reference.class);
        final ReferenceConfig<?> refer = new ReferenceConfig<>(reference);
        refer.setCheck(reference.check());
        refer.setInterface(returnType);
        final Object newInstance = ReferenceConfigCache.getCache().get(refer);
        setInstance(invocation.getThis(), method, returnType, newInstance);
    }

}