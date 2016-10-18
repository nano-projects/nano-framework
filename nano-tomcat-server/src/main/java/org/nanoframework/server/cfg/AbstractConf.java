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
package org.nanoframework.server.cfg;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.entity.BaseEntity;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public abstract class AbstractConf extends BaseEntity {
    private static final long serialVersionUID = 2578839220433405594L;

    protected static BaseEntity DEF;
    
    public void merge(final BaseEntity conf) {
        if (DEF != null) {
            merge0(DEF);
        }
        
        if (conf != null) {
            merge0(conf);
        }
    }
    
    private void merge0(final BaseEntity conf) {
        Arrays.asList(this.attributeNames()).forEach(key -> {
            final Object value = conf.attributeValue(key);
            if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                this.setAttributeValue(key, value);
            }
        });
    }
}
