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
package org.nanoframework.core.component.scan;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;

import com.google.common.collect.Sets;

/**
 * 扫描组件，并返回符合要求的集合
 * @author yanghe
 * @since 1.0
 */
public class ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassScanner.class);

    private static Set<Class<?>> classes;

    /**
     * 返回所有带有参数中的注解的类
     * @param annotationClass 注解类
     * @return 过滤后的类
     */
    public static Set<Class<?>> filter(final Class<? extends Annotation> annotationClass) {
        if (classes == null) {
            return Collections.emptySet();
        }

        if (classes.size() > 0) {
            final Set<Class<?>> annClasses = Sets.newLinkedHashSet();
            classes.stream().filter(clz -> clz.isAnnotationPresent(annotationClass)).forEach(clz -> annClasses.add(clz));
            return annClasses;

        }

        return Collections.emptySet();
    }

    /**
     * 返回目标类中带有参数中的注解的类
     * @param targetClasses 目标类
     * @param annotationClass 注解类
     * @return 过滤后的类
     */
    public static Set<Class<?>> filter(final Set<Class<?>> targetClasses, final Class<? extends Annotation> annotationClass) {
        if (targetClasses == null) {
            return Collections.emptySet();
        }

        if (targetClasses.size() > 0) {
            final Set<Class<?>> annClasses = Sets.newLinkedHashSet();
            targetClasses.stream().filter(clz -> clz.isAnnotationPresent(annotationClass)).forEach(clz -> annClasses.add(clz));
            return annClasses;

        }

        return Collections.emptySet();
    }

    public static void scan(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            LOGGER.warn("没有设置packageName, 跳过扫描");
            return;
        }

        if (classes == null) {
            classes = Sets.newHashSet();
        }

        classes.addAll(getClasses(packageName));
    }

    public static void clear() {
        if (classes != null) {
            classes.clear();
        }
    }

    /**
     * Return a set of all classes contained in the given package.
     *
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(String packageName) {
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
    private static Set<Class<?>> getClasses(ResolverUtil.Test test, String packageName) {
        Assert.notNull(test, "Parameter 'test' must not be null");
        Assert.notNull(packageName, "Parameter 'packageName' must not be null");
        return new ResolverUtil<Object>().find(test, packageName).getClasses();
    }
}
