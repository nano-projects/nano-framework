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
package org.nanoframework.orm.jdbc.record.script;

import java.util.List;

import org.nanoframework.commons.entity.BaseEntity;

/**
 *
 * @author yanghe
 * @since 1.3.15
 */
public class SQLScriptBatch extends BaseEntity {
    private static final long serialVersionUID = -5479991226898971698L;

    public final String sql;
    public final List<List<Object>> batchValues;

    private SQLScriptBatch(final String sql, final List<List<Object>> batchValues) {
        this.sql = sql;
        this.batchValues = batchValues;
    }

    public static SQLScriptBatch create(final String sql, final List<List<Object>> batchValues) {
        return new SQLScriptBatch(sql, batchValues);
    }

}
