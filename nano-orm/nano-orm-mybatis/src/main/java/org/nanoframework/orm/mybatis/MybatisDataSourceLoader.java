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
import java.util.Properties;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.DataSourceLoader;
import org.nanoframework.orm.ORMType;

import com.google.common.collect.Lists;

/**
 * @author yanghe
 * @since 1.2
 */
public class MybatisDataSourceLoader extends DataSourceLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisDataSourceLoader.class);

    private final long time;
    private final List<DataSourceConfig> dsc = Lists.newArrayList();

    public MybatisDataSourceLoader() {
        time = System.currentTimeMillis();
        load();
        toModule();
    }

    @Override
    public void load() {
        load0(ORMType.MYBATIS);
    }

    @Override
    public void toConfig(final Properties properties) {
        Assert.notNull(properties, "数据源属性文件不能为空");
        final String[] mapperPackageName = properties.getProperty(MAPPER_PACKAGE_NAME, "NULL").split(",");
        final String[] typeAliasPackageName = properties.getProperty(MAPPER_PACKAGE_TYPE_ALIAS, "").split(",");
        final DataSourceConfig config = new DataSourceConfig(mapperPackageName, typeAliasPackageName, properties, poolType(properties));
        dsc.add(config);
        LOGGER.info("创建数据源依赖注入模块, Mapper包路径: " + mapperPackageName + ", 耗时: " + (System.currentTimeMillis() - time) + "ms");
    }

    @Override
    public void toModule() {
        for (final DataSourceConfig config : dsc) {
            modules.add(new MultiDataSourceModule(config));
        }

        modules.add(new MultiTransactionalModule());
    }

}
