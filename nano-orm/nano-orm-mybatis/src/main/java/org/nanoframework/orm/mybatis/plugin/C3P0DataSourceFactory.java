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
package org.nanoframework.orm.mybatis.plugin;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceException;

/**
 * C3P0连接池工厂类.
 * 
 * @author yanghe
 * @since 1.3.6
 */
public class C3P0DataSourceFactory extends AbstractDataSourceFactory {

    public C3P0DataSourceFactory() {
        try {
            Class<?> comboPooledDataSource = Class.forName("com.mchange.v2.c3p0.ComboPooledDataSource");
            this.dataSource = (DataSource) comboPooledDataSource.newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new DataSourceException(e.getMessage(), e);
        }
    }

}
