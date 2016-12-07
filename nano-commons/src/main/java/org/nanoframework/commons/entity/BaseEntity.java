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
package org.nanoframework.commons.entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nanoframework.commons.format.ClassCast;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 基础实体类，实体类功能扩展辅助类.
 * 
 * @author yanghe
 * @since 1.0
 */
public abstract class BaseEntity implements Cloneable, Serializable {
    private static final long serialVersionUID = 3188627488044889912L;
    private static final transient List<String> FILTER_FIELD_NAMES = Lists.newArrayList("names", "cls", "methods", "fields");

    protected final transient Map<String, Method> methods = paramMethods();
    protected final transient Map<String, Field> fields = paramFields();
    private transient String[] names;

    /**
     * 获取所有属性名.
     * @return 返回属性数组
     */
    public String[] attributeNames() {
        if (names != null) {
            return names;
        }

        names = fields.keySet().toArray(new String[fields.size()]);
        return names;
    }

    /**
     * 根据属性名获取该属性的值.
     * @param <T> 参数类型
     * @param fieldName 属性名
     * @return 返回该属性的值
     */
    @SuppressWarnings("unchecked")
    public <T> T attributeValue(final String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("属性名不能为空");
        }

        try {
            if (fields.containsKey(fieldName)) {
                final Field field = fields.get(fieldName);
                final String fieldGetName = parGetName(field.getName());
                if (hasMethodName(fieldGetName)) {
                    return (T) methods.get(fieldGetName).invoke(this);
                }
            } else {
                throw new NoSuchFieldException("无效的属性名称: " + fieldName);
            }
        } catch (final Throwable e) {
            throw new EntityException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 根据属性名获取该属性的值.
     * @param <T> 参数类型
     * @param fieldName 属性名
     * @param defaultValue 默认值，当field获取的值为null时选用defaulValue的值
     * @return 返回该属性的值
     */
    public <T> T attributeValue(final String fieldName, final T defaultValue) {
        final T value = attributeValue(fieldName);
        return value == null ? (T) defaultValue : value;
    }

    /**
     * 设置属性值, 默认不区分大小写.
     * @param fieldName 属性名
     * @param value 属性值
     */
    public void setAttributeValue(final String fieldName, final Object value) {
        setAttributeValue(fieldName, value, false);
    }

    /**
     * 设置属性值.
     * @param fieldName 属性名
     * @param value 属性值
     * @param isCase 区分大小写，true时区分大小写，默认false
     */
    public void setAttributeValue(final String fieldName, final Object value, final boolean isCase) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("属性名不能为空");
        }
        
        try {
            if (fields.containsKey(fieldName)) {
                final Field field = fields.get(fieldName);
                if ((!isCase && fieldName.toUpperCase().equals(field.getName().toUpperCase())) || (isCase && fieldName.equals(field.getName()))) {
                    final String fieldSetName = parSetName(field.getName());
                    if (hasMethodName(fieldSetName)) {
                        final String typeName = field.getType().getName();
                        methods.get(fieldSetName).invoke(this, ClassCast.cast(value, typeName));
                    }
                }
            } else {
                throw new NoSuchFieldException("无效的属性名: " + fieldName);
            }
        } catch (final Throwable e) {
            throw new EntityException(e.getMessage(), e);
        }
    }

    /**
     * 检查是否有方法.
     * @param methods 方法集
     * @param methodName 方法名
     * @return boolean 
     */
    private boolean hasMethodName(final String methodName) {
        return methods.containsKey(methodName);
    }

    /**
     * get+属性名.
     * @param fieldName 属性名
     * @return String
     */
    protected String parGetName(final String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return null;
        }

        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * set+属性名.
     * @param fieldName 属性名
     * @return String
     */
    protected String parSetName(final String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return null;
        }

        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 将实体类转换成Map.
     * @return Map
     */
    public Map<String, Object> beanToMap() {
        final Map<String, Object> beanToMap = Maps.newHashMap();
        for (String key : attributeNames()) {
            final Object value = attributeValue(key);
            if (value != null) {
                beanToMap.put(key, value);
            }
        }

        return beanToMap;
    }

    /**
     * 将Map对象转换成实体类对象.
     * @param <T> 参数类型
     * @param beanMap 符合实体规范的Map
     * @param beanType 实体类
     * @return 转换后的实体类
     */
    public static <T extends BaseEntity> T mapToBean(Map<String, Object> beanMap, Class<T> beanType) {
        if (beanType == null) {
            throw new EntityException("beanType不能为空");
        }
        
        if (beanMap == null) {
            return null;
        }

        try {
            final T bean = beanType.newInstance();
            beanMap.forEach((key, value) -> bean.setAttributeValue(key, value));
            return bean;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new EntityException(e.getMessage(), e);
        }
    }

    /**
     * 将Map对象集合转换成实体类对象集合.
     * @param <T> 参数类型
     * @param beanMaps 符合实体规范的Map集合
     * @param beanType 实体类
     * @return 转换后的实体类集合
     */
    public static <T extends BaseEntity> List<T> mapToBeans(final List<Map<String, Object>> beanMaps, final Class<T> beanType) {
        if (CollectionUtils.isEmpty(beanMaps)) {
            return Collections.emptyList();
        }

        final List<T> beans = new ArrayList<>(beanMaps.size());
        for (Map<String, Object> beanMap : beanMaps) {
            beans.add(mapToBean(beanMap, beanType));
        }

        return beans;
    }

    /**
     * 获取实体类所有方法.
     * @return 实体类方法列表
     */
    protected Map<String, Method> paramMethods() {
        final List<Method> methods = allMethods(Lists.newArrayList(), this.getClass());
        final Map<String, Method> methodMap = Maps.newLinkedHashMap();
        for (Method method : methods) {
            if (Modifier.isFinal(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            methodMap.put(method.getName(), method);
        }

        return methodMap;
    }

    /**
     * 递归获取当前类及父类中的所有方法.
     * @param allMethods 实体类方法集合
     * @param clazz 当前类或父类
     * @return 新实体类方法集合
     */
    protected List<Method> allMethods(final List<Method> allMethods, final Class<?> clazz) {
        allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        if (clazz.getSuperclass() == null) {
            return allMethods;
        }

        return allMethods(allMethods, clazz.getSuperclass());
    }

    /**
     * 获取实体类的所有属性.
     * @return 实体类属性列表
     */
    protected Map<String, Field> paramFields() {
        final List<Field> fields = allFields(Lists.newArrayList(), this.getClass());
        final Map<String, Field> fieldMap = Maps.newLinkedHashMap();
        for (Field field : fields) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (filterField(field)) {
                continue;
            }

            fieldMap.put(field.getName(), field);
        }

        return fieldMap;
    }

    /**
     * 递归获取当前类及父类中的所有属性.
     * @param allFields 实体类属性集合
     * @param clazz 当前类或父类
     * @return 新实体类属性集合
     */
    protected List<Field> allFields(final List<Field> allFields, final Class<?> clazz) {
        allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() == null) {
            return allFields;
        }

        return allFields(allFields, clazz.getSuperclass());
    }
    
    protected boolean filterField(final Field field) {
        return FILTER_FIELD_NAMES.contains(field.getName());
    }
    
    public Collection<Method> methods() {
        return methods.values();
    }
    
    public Collection<Field> fields() {
        return fields.values();
    }
    
    @Override
    public BaseEntity clone() {
        try {
            return (BaseEntity) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new EntityException("Clone Not Supported Exception: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}