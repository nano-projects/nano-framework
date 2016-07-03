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
package org.nanoframework.core.chain;

import org.nanoframework.core.chain.exception.ChainException;

/**
 * @author yanghe
 * @date 2015年12月3日 下午1:17:29
 */
public abstract class AbstractChain implements Chain {

    private Chain chain;

    public Object next(Object object) {
        if (chain != null)
            return chain.execute(object);

        return object;
    }

    @Override
    public Chain getChain() {
        return chain;
    }

    @Override
    public Chain setChain(Chain chain) {
        if (this == chain)
            throw new ChainException("不能对自身设置责任链");

        this.chain = chain;
        return this;
    }

}
