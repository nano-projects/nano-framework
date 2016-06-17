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
package org.nanoframework.core.component.stereotype.bind;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.entity.BaseEntity;

/**
 * 组件映射类，存储实例化对象、对象类、方法.
 * 
 * @author yanghe
 * @since 1.0
 * @date 2015年6月5日 下午10:56:30 
 */
public class RequestMapper extends BaseEntity {
    private static final long serialVersionUID = 6571078157462085564L;

    private Object object;
    private Class<?> clz;
    private Method method;

    /** @since 1.2 */
    private RequestMethod[] requestMethods = new RequestMethod[] { RequestMethod.GET, RequestMethod.POST };
    private Map<String, String> param;

    private RequestMapper() {

    }

    public static RequestMapper create() {
        return new RequestMapper();
    }

    public Object getObject() {
        return object;
    }

    public RequestMapper setObject(Object object) {
        this.object = object;
        return this;
    }

    public Class<?> getClz() {
        return clz;
    }

    public RequestMapper setClz(Class<?> clz) {
        this.clz = clz;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public RequestMapper setMethod(Method method) {
        this.method = method;
        return this;
    }

    public RequestMethod[] getRequestMethods() {
        return requestMethods;
    }

    public String[] getRequestMethodStrs() {
        if (ArrayUtils.isNotEmpty(requestMethods)) {
            String[] strings = new String[requestMethods.length];
            int idx = 0;
            for (RequestMethod mtd : requestMethods) {
                strings[idx] = mtd.name();
                idx++;
            }

            return strings;
        }

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public RequestMapper setRequestMethods(RequestMethod[] requestMethods) {
        this.requestMethods = requestMethods;
        return this;
    }

    public boolean hasMethod(RequestMethod method) {
        if (ArrayUtils.isNotEmpty(requestMethods)) {
            for (RequestMethod mtd : requestMethods) {
                if (mtd == method) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public RequestMapper setParam(Map<String, String> param) {
        this.param = param;
        return this;
    }

}
