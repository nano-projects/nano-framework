/**
 * Copyright 2015- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.commons.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.format.ClassCast;

import com.alibaba.fastjson.JSON;

/**
 * 基础实体类，实体类功能扩展辅助类
 * 
 * @author yanghe
 * @date 2015年6月9日 上午8:46:12 
 *
 */
public abstract class BaseEntity implements Cloneable {
	private String[] names = null;

	/**
	 * 获取所有属性名
	 * 
	 * @return 返回属性数组
	 */
	public String[] _getAttributeNames() {
		if (names != null)
			return names;

		Field[] fields = _getParamFields();
		names = new String[fields.length];
		for (int i = 0, len = fields.length; i < len; i++) {
			names[i] = fields[i].getName();
		}

		return names;
	}

	/**
	 * 根据属性名获取该属性的值
	 * 
	 * @param fieldName 属性名
	 * @return 返回该属性的值
	 */
	@SuppressWarnings("unchecked")
	public <T> T _getAttributeValue(String fieldName) {
		if (StringUtils.isEmpty(fieldName))
			throw new IllegalArgumentException("属性名不能为空");
		
		Class<?> cls = this.getClass();
		Method[] methods = _getParamMethods();
		Field[] fields = _getParamFields();

		try {
			for (Field field : fields) {
				if (fieldName.equals(field.getName())) {
					String fieldGetName = parGetName(field.getName());
					if (!checkGetMet(methods, fieldGetName)) 
						continue;
					
					Method fieldGetMet = cls.getMethod(fieldGetName);
					return (T) fieldGetMet.invoke(this);
				}
			}
		} catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new EntityException(e.getMessage(), e);
		}

		return null;
	}
	
	/**
	 * 根据属性名获取该属性的值
	 * 
	 * @param fieldName 属性名
	 * @Param defaultValue 默认值，当field获取的值为null时选用defaulValue的值
	 * @return 返回该属性的值
	 */
	public <T> T _getAttributeValue(String fieldName, T defaultValue) {
		final T value;
		return (value = _getAttributeValue(fieldName)) == null ? (T) defaultValue : value;
	}
	
	/**
	 * 设置属性值
	 * 
	 * @param fieldName 属性名
	 * @param value 属性值
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public void _setAttributeValue(String fieldName, Object value) {
		if(StringUtils.isEmpty(fieldName))
			throw new IllegalArgumentException("属性名不能为空");
			
		Class<?> cls = this.getClass();
		Method[] methods = _getParamMethods();
		Field[] fields = _getParamFields();

		try {
			for (Field field : fields) {
				if (fieldName.equals(field.getName())) {
					String fieldSetName = parSetName(field.getName());
					if (!checkSetMet(methods, fieldSetName)) 
						continue;
	
					Method fieldSetMet = cls.getMethod(fieldSetName, field.getType());
					String typeName = field.getType().getName();
					fieldSetMet.invoke(this, ClassCast.cast(value, typeName));
					break;
				}
			}
		} catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new EntityException(e.getMessage(), e);
			
		}
	}

	/**
	 * 检查是否有set方法
	 * @param methods 方法集
	 * @param fieldSetMet 方法名
	 * @return boolean
	 */
	private boolean checkSetMet(Method[] methods, String fieldSetMet) {
		for (Method met : methods) {
			if (fieldSetMet.equals(met.getName())) 
				return true;
		}
		
		return false;
	}

	/**
	 * 检查是否有get方法
	 * @param methods 方法集
	 * @param fieldGetMet 方法名
	 * @return boolean 
	 */
	private boolean checkGetMet(Method[] methods, String fieldGetMet) {
		for (Method met : methods) {
			if (fieldGetMet.equals(met.getName())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * get+属性名
	 * @param fieldName 属性名
	 * @return String
	 */
	protected String parGetName(String fieldName) {
		if (StringUtils.isEmpty(fieldName)) 
			return null;
		
		return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}

	/**
	 * set+属性名
	 * @param fieldName 属性名
	 * @return String
	 */
	protected String parSetName(String fieldName) {
		if (StringUtils.isEmpty(fieldName))
			return null;
		
		return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}

	/**
	 * 将实体类转换成Map
	 * @return Map<String, Object>
	 */
	public Map<String, Object> _getBeanToMap() {
		Map<String, Object> beanToMap = new HashMap<>();
		for (String key : _getAttributeNames()) {
			Object value = _getAttributeValue(key);
			if(value != null)
				beanToMap.put(key, value);
		}

		return beanToMap;
	}
	
	/**
	 * 将Map对象转换成实体类对象
	 * 
	 * @param beanMap
	 * @param beanType
	 * @return
	 */
	public static <T extends BaseEntity> T _getMapToBean(Map<String , Object> beanMap , Class<T> beanType) {
		if(beanType == null)
			throw new EntityException("beanType不能为空");
		
		if(beanMap == null)
			return null;
		
		try {
			T bean = beanType.newInstance();
			beanMap.forEach((key, value) -> bean._setAttributeValue(key, value));
			return bean;
		} catch(InstantiationException | IllegalAccessException e) {
			throw new EntityException(e.getMessage(), e);
		}
	}
	
	public static <T extends BaseEntity> List<T> _getMapToBeans(List<Map<String, Object>> beanMaps, Class<T> beanType) {
		if(beanMaps == null || beanMaps.size() == 0)
			return null;
		
		List<T> beans = new ArrayList<>(beanMaps.size());
		for(Map<String, Object> beanMap : beanMaps) {
			beans.add(_getMapToBean(beanMap, beanType));
		}
		
		return beans;
	}
	
	/**
	 * 获取实体类的属性
	 * @return Field[]
	 */
	protected Method[] _getParamMethods() {
		List<Method> methods = _getAllMethods(new ArrayList<>(), this.getClass());
		
		List<Method> methodList = new ArrayList<>();
		for(Method method : methods) {
			if(Modifier.isFinal(method.getModifiers()) || Modifier.isStatic(method.getModifiers()))
				continue;
			
			methodList.add(method);
		}
		
		return methodList.toArray(new Method[methodList.size()]);
	}
	
	protected List<Method> _getAllMethods(List<Method> allMethods, Class<?> clazz) {
		allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

		if (clazz.getSuperclass() == null)
			return allMethods;

		return _getAllMethods(allMethods, clazz.getSuperclass());
	}
	
	/**
	 * 获取实体类的属性
	 * @return Field[]
	 */
	protected Field[] _getParamFields() {
		List<Field> fields = _getAllFields(new ArrayList<>(), this.getClass());
		
		List<Field> filterList = new ArrayList<>();
		for(Field field : fields) {
			if(Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
				continue;
			
			if("names".equals(field.getName()))
				continue ;
			
			filterList.add(field);
		}
		
		return filterList.toArray(new Field[filterList.size()]);
	}
	
	protected List<Field> _getAllFields(List<Field> allFields, Class<?> clazz) {
		allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));

		if (clazz.getSuperclass() == null)
			return allFields;

		return _getAllFields(allFields, clazz.getSuperclass());
	}
	
	@Override
	public BaseEntity clone() throws CloneNotSupportedException {
		return (BaseEntity) super.clone();
	}

	public String toString() {
		return JSON.toJSONString(this);
	}
	
}