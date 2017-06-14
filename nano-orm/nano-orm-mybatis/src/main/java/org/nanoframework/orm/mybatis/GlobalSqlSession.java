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
package org.nanoframework.orm.mybatis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.ibatis.session.SqlSessionManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 全局MyBatis数据源管理类<br>
 * 通过Guice Inject后初始化SqlSessionManager<br>
 * 存储方式为 {数据源名称 - SqlSessionManager} 
 * 
 * @author yanghe
 * @since 1.2
 * @see MultiDataSourceModule
 */
public class GlobalSqlSession {
    private static ConcurrentMap<String, SqlSessionManager> globals = Maps.newConcurrentMap();

    private GlobalSqlSession() {
    }

    public static void set(final String type, final SqlSessionManager global) {
        globals.put(type, global);
    }

    public static final SqlSessionManager get(final String type) {
        return globals.get(type);
    }

    public static final SqlSessionManager[] get(final String... types) {
        if (types.length > 0) {
            final List<SqlSessionManager> managers = Lists.newArrayList();
            for (final String type : types) {
                final SqlSessionManager manager = globals.get(type);
                if (manager == null) {
                    throw new IllegalArgumentException("无效的数据源名称: " + type);
                }

                managers.add(manager);
            }

            return managers.toArray(new SqlSessionManager[managers.size()]);
        }

        return null;
    }

    public static final Set<String> keys() {
        return globals.keySet();
    }
}
