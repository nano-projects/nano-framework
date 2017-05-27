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

import static com.google.inject.util.Providers.guicify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Provider;
import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.format.ClassCast;
import org.nanoframework.commons.io.ClassPathResource;
import org.nanoframework.commons.io.Resource;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ResourceUtils;
import org.nanoframework.commons.util.StringUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * MyBatis XML模式读取数据源，并通过jdbc属性文件进行配置<br>
 * 将创建的SqlSessionFactory写入至全局管理类.
 * 
 * @author yanghe
 * @since 1.2
 */
public class MultiDataSourceModule extends AbstractModule {
    private String envId;
    private Properties jdbc;
    private String mybatisConfigPath;
    private String[] mapperPackageName;
    private String[] typeAliasPackageName;

    public MultiDataSourceModule(final DataSourceConfig conf) {
        Assert.notNull(this.jdbc = conf.getJdbc());
        Assert.hasLength(this.envId = conf.getEnvId());
        Assert.hasLength(this.mybatisConfigPath = conf.getMybatisConfigPath());
        Assert.notEmpty(this.mapperPackageName = conf.getMapperPackageName());
        this.typeAliasPackageName = conf.getTypeAliasPackageName();
    }

    @Override
    protected void configure() {
        Reader reader = null;
        try {
            InputStream input;
            try {
                final Resource resource = new ClassPathResource(mybatisConfigPath);
                input = resource.getInputStream();
                if (input == null) {
                    input = new FileInputStream(ResourceUtils.getFile(mybatisConfigPath));
                }
            } catch (IOException e) {
                throw new LoaderException("加载文件异常: " + e.getMessage());
            }

            reader = new InputStreamReader(input);
            final SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader, envId, jdbc);
            final SqlSessionManager sessionManager = SqlSessionManager.newInstance(sessionFactory);
            GlobalSqlSession.set(envId, sessionManager);

            final Configuration conf = sessionFactory.getConfiguration();
            registryMapper(conf, sessionManager);
            registryTypeAlias(conf);
            setConfigureSettings(conf);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
    }

    final void registryMapper(final Configuration conf, final SqlSessionManager sessionManager) {
        final MapperRegistry mapperRegistry = conf.getMapperRegistry();
        for (final String pkg : mapperPackageName) {
            final Set<Class<?>> classes = getClasses(StringUtils.trim(pkg));
            if (!CollectionUtils.isEmpty(classes)) {
                for (final Class<?> cls : classes) {
                    if (!mapperRegistry.hasMapper(cls)) {
                        mapperRegistry.addMapper(cls);
                    }
                }
            }
        }

        // bind mappers
        final Collection<Class<?>> mapperClasses = mapperRegistry.getMappers();
        for (final Class<?> mapperType : mapperClasses) {
            bindMapper(mapperType, sessionManager);
        }
    }

    final void registryTypeAlias(final Configuration conf) {
        final TypeAliasRegistry typeAliasRegistry = conf.getTypeAliasRegistry();
        if (ArrayUtils.isNotEmpty(typeAliasPackageName)) {
            for (final String pkg : typeAliasPackageName) {
                typeAliasRegistry.registerAliases(StringUtils.trim(pkg));
            }
        }
    }

    final void setConfigureSettings(final Configuration conf) {
        final String prefix = "mybatis.settings.";
        final Map<String, Method> methods = BaseEntity.paramMethods(conf.getClass());
        final Map<String, Field> fields = BaseEntity.paramFields(conf.getClass());
        jdbc.keySet().stream().filter(key -> StringUtils.startsWith((String) key, prefix)).forEach(key -> {
            try {
                final String keyStr = (String) key;
                final String fieldName = keyStr.replace(prefix, StringUtils.EMPTY);
                final Field field = fields.get(fieldName);
                if (field != null) {
                    final String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    final Method method = methods.get(methodName);
                    if (method != null) {
                        final String value = jdbc.getProperty(keyStr);
                        if (StringUtils.isNotBlank(value)) {
                            final Object castValue = ClassCast.cast(value, field.getType().getName());
                            method.invoke(conf, castValue);
                        }
                    }
                }
            } catch (final Throwable e) {
                // ignore
            }
        });

    }

    /**
     * Set the DataSource Provider type has to be bound.
     *
     * @param dataSourceProviderType the DataSource Provider type
     */
    protected final void bindDataSourceProviderType(final Class<? extends Provider<DataSource>> dataSourceProviderType) {
        Assert.notNull(dataSourceProviderType, "Parameter 'dataSourceProviderType' must be not null");
        bind(DataSource.class).toProvider(dataSourceProviderType).in(Scopes.SINGLETON);
    }

    /**
    *
    * @param <T>
    * @param mapperType
    */
    final <T> void bindMapper(final Class<T> mapperType, final SqlSessionManager sessionManager) {
        bind(mapperType).toProvider(guicify(new MapperProvider<T>(mapperType, sessionManager))).in(Scopes.SINGLETON);
    }

    /**
    * Return a set of all classes contained in the given package.
    *
    * @param packageName the package has to be analyzed.
    * @return a set of all classes contained in the given package.
    */
    final Set<Class<?>> getClasses(final String packageName) {
        return getClasses(new ResolverUtil.IsA(Object.class), packageName);
    }

    /**
    * Return a set of all classes contained in the given package that match with
    * the given test requirement.
    *
    * @param test the class filter on the given package.
    * @param packageName the package has to be analyzed.
    * @return a set of all classes contained in the given package.
    */
    final Set<Class<?>> getClasses(final ResolverUtil.Test test, final String packageName) {
        Assert.notNull(test, "Parameter 'test' must not be null");
        Assert.notNull(packageName, "Parameter 'packageName' must not be null");
        return new ResolverUtil<Object>().find(test, packageName).getClasses();
    }

}
