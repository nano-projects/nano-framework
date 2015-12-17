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
package org.nanoframework.commons.format;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.entity.BaseEntity;

import com.alibaba.fastjson.JSON;


/**
 * 类型转换处理类
 * @author yanghe
 * @date 2015年6月5日 下午5:03:48 
 *
 */
public class ClassCast {

	public static final String _Integer = "java.lang.Integer";
	public static final String _int = "int";
	public static final String _Long = "java.lang.Long";
	public static final String _long = "long";
	public static final String _Double = "java.lang.Double";
	public static final String _double = "double";
	public static final String _Float = "java.lang.Float";
	public static final String _float = "float";
	public static final String _String = "java.lang.String";
	public static final String _Date_util = "java.util.Date";
	public static final String _Date_sql = "java.sql.Date";
	public static final String _Timestamp = "java.sql.Timestamp";
	public static final String _Boolean = "java.lang.Boolean";
	public static final String _boolean = "boolean";
	public static final String _BigDecimal = "java.math.BigDecimal";
	
	/**
	 * 根据Class进行转换，转换简单数据类型
	 * @param value 值
	 * @param typeName 类型
	 * @return 返回转换后的值
	 */
	public static final Object cast(String value, String typeName) {
		if(value == null)
			return null;
		
		try {
			switch(typeName) {
				case _Integer :
					return new Integer(value);
					
				case _Long : 
					return new Long(value);
					
				case _Double :
					return new Double(value);
					
				case _Float : 
					return new Float(value);
					
				case _String :
					return value;
					
				case _Date_util : 
				case _Date_sql :
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					return format.parse(value);
					
				case _Timestamp : 
					format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					return new Timestamp(format.parse(value).getTime());
					
				case _int : 
				case _long :
				case _double :
				case _float : 
					throw new ClassCastException("只支持对对象数据类型的转换，不支持基本数据类型的转换");
					
				default :
					return JSON.parseObject(value, Class.forName(typeName));
					
			}
		} catch(ClassCastException | ClassNotFoundException | ParseException e) {
			throw new org.nanoframework.commons.exception.ClassCastException(e.getMessage(), e);
			
		}
		
	}
	
	/**
	 * 根据Class进行转换，转换简单数据类型
	 * @param value 值
	 * @param typeName 类型
	 * @return 返回转换后的值
	 */
	public static final Object cast(Object value, String typeName) {
		if(StringUtils.isEmpty(typeName))
			throw new IllegalArgumentException("类型名不能为空");
		
		if(value == null)
			return null;
		
		try {
			Class<?> cls;
			if(value instanceof String && BaseEntity.class.isAssignableFrom(cls = Class.forName(typeName))) {
				return JSON.parseObject((String) value, cls);
			}
		} catch(ClassNotFoundException e) { }
		
		try {
			switch(typeName) {
				case _Integer :
					if(value instanceof String) {
						if(StringUtils.isEmpty((String) value))
							return null;
						
						return new Integer((String) value);
					} else if(value instanceof Integer)
						return value;
					else if(value instanceof BigDecimal) 
						return ((BigDecimal) value).intValue();
					else 
						return new Integer(String.valueOf(value));
						
				case _Long : 
					if(value instanceof String) {
						if(StringUtils.isEmpty((String) value))
							return null;
						
						return new Long((String) value);
					} else if(value instanceof Long)
						return value;
					else if(value instanceof BigDecimal) 
						return ((BigDecimal) value).longValue();
					else 
						return new Long(String.valueOf(value));
					
				case _Double :
					if(value instanceof String) {
						if(StringUtils.isEmpty((String) value))
							return null;
						
						return new Double((String) value);
					} else if(value instanceof Double)
						return value;
					else if(value instanceof BigDecimal) 
						return ((BigDecimal) value).doubleValue();
					else 
						return new Double(String.valueOf(value));
					
				case _Float : 
					if(value instanceof String) {
						if(StringUtils.isEmpty((String) value))
							return null;
						
						return new Float((String) value);
					} else if(value instanceof Float)
						return value;
					else if(value instanceof BigDecimal)
						return ((BigDecimal) value).floatValue();
					else 
						return new Float(String.valueOf(value));
						
				case _Boolean : 
					if(value instanceof String) {
						if(StringUtils.isEmpty((String) value))
							return null;
						
						return new Boolean((String) value);
					} else if(value instanceof Boolean)
						return value;
					else 
						return new Boolean(String.valueOf(value));
					
				case _String : 
					return String.valueOf(value);
					
				case _Date_util :
				case _Date_sql : 
				case _Timestamp : 
					if(value instanceof String) {
						if(StringUtils.isEmpty((String) value))
							return null;
						
						return parseDate((String) value);
					} else if(value instanceof Date)
						return value;
					else 
						return parseDate(String.valueOf(value));
				
				case _int : 
				case _long :
				case _double :
				case _float : 
					throw new ClassCastException("只支持对对象数据类型的转换，不支持基本数据类型的转换");
					
				default :
					return value;
			}
		} catch(Throwable e) {
			throw new org.nanoframework.commons.exception.ClassCastException(e.getMessage(), e);
			
		}
	}
	
	/**
	 * 
	 * @param datestr
	 * @return date
	 * @throws ParseException 
	 */
	public static Date parseDate(String date) throws ParseException {
		if (StringUtils.isEmpty(date))
			return null;

		String parttern = null;
		if (date.indexOf(':') > 0) {
			if(date.indexOf(".") > 0)
				parttern = "yyyy-MM-dd HH:mm:ss.SSS";
			else 
				parttern = "yyyy-MM-dd HH:mm:ss";
			
		} else {
			parttern = "yyyy-MM-dd";
		}
		
		DateFormat format = new SimpleDateFormat(parttern);
		return format.parse(date);
			
	}

	/**
	 * 
	 * @param date
	 * @return date string
	 */
	public static String fmtDate(Date date, String parttern) {
		if (null == date) 
			return null;

		DateFormat format = new SimpleDateFormat(parttern);
		return format.format(date);
		
	}
	
}
