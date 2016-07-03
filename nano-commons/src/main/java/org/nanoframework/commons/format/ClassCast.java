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
package org.nanoframework.commons.format;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 类型转换处理类.
 * @author yanghe
 * @date 2015年6月5日 下午5:03:48 
 */
public class ClassCast {

    public static final String _Integer = "java.lang.Integer";
    public static final String _LInteger = "[Ljava.lang.Integer;";
    public static final String _int = "int";
    public static final String _Long = "java.lang.Long";
    public static final String _LLong = "[Ljava.lang.Long;";
    public static final String _long = "long";
    public static final String _Double = "java.lang.Double";
    public static final String _LDouble = "[Ljava.lang.Double;";
    public static final String _double = "double";
    public static final String _Float = "java.lang.Float";
    public static final String _LFloat = "[Ljava.lang.Float;";
    public static final String _float = "float";
    public static final String _String = "java.lang.String";
    public static final String _LString = "[Ljava.lang.String;";
    public static final String _Date_util = "java.util.Date";
    public static final String _LDate_util = "[Ljava.util.Date;";
    public static final String _Date_sql = "java.sql.Date";
    public static final String _LDate_sql = "[Ljava.sql.Date;";
    public static final String _Timestamp = "java.sql.Timestamp";
    public static final String _LTimestamp = "[Ljava.sql.Timestamp;";
    public static final String _Boolean = "java.lang.Boolean";
    public static final String _LBoolean = "[Ljava.lang.Boolean;";
    public static final String _boolean = "boolean";
    public static final String _BigDecimal = "java.math.BigDecimal";
    public static final String _LBigDecimal = "[Ljava.math.BigDecimal;";

    /**
     * 根据Class进行转换，转换简单数据类型
     * @param value 值
     * @param typeName 类型
     * @return 返回转换后的值
     */
    public static final Object cast(String value, String typeName) {
        if (value == null)
            return null;

        try {
            switch (typeName) {
                case _Integer:
                    return Integer.valueOf(value);

                case _Long:
                    return Long.valueOf(value);

                case _Double:
                    return Double.valueOf(value);

                case _Float:
                    return Float.valueOf(value);

                case _String:
                    return value;

                case _Date_util:
                case _Date_sql:
                    return DateFormat.parse(value, Pattern.DATE);

                case _Timestamp:
                    long time = DateFormat.parse(value, Pattern.TIMESTAMP).getTime();
                    return new Timestamp(time);

                case _int:
                case _long:
                case _double:
                case _float:
                    throw new ClassCastException("只支持对对象数据类型的转换，不支持基本数据类型的转换");

                default:
                    return JSON.parseObject(value, Class.forName(typeName));

            }
        } catch (ClassCastException | ClassNotFoundException | ParseException e) {
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
        if (StringUtils.isEmpty(typeName))
            throw new IllegalArgumentException("类型名不能为空");

        if (value == null)
            return null;

        try {
            switch (typeName) {
                case _Integer:
                    if (value instanceof String) {
                        if (StringUtils.isEmpty((String) value)) {
                            return null;
                        }

                        return Integer.valueOf((String) value);
                    } else if (value instanceof Integer) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).intValue();
                    } else {
                        return Integer.valueOf(String.valueOf(value));
                    }
                case _LInteger:
                    Object[] values = ObjectUtils.toObjectArray(value);
                    Integer[] ints = new Integer[values.length];
                    for (int idx = 0; idx < ints.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            ints[idx] = null;
                        } else {
                            ints[idx] = Integer.valueOf(String.valueOf(values[idx]));
                        }
                    }

                    return ints;
                case _Long:
                    if (value instanceof String) {
                        if (StringUtils.isEmpty((String) value)) {
                            return null;
                        }

                        return Long.valueOf((String) value);
                    } else if (value instanceof Long) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).longValue();
                    } else {
                        return Long.valueOf(String.valueOf(value));
                    }
                case _LLong:
                    values = ObjectUtils.toObjectArray(value);
                    Long[] longs = new Long[values.length];
                    for (int idx = 0; idx < longs.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            longs[idx] = null;
                        } else {
                            longs[idx] = Long.valueOf(String.valueOf(values[idx]));
                        }
                    }

                    return longs;
                case _Double:
                    if (value instanceof String) {
                        if (StringUtils.isEmpty((String) value)) {
                            return null;
                        }

                        return Double.valueOf((String) value);
                    } else if (value instanceof Double) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).doubleValue();
                    } else {
                        return Double.valueOf(String.valueOf(value));
                    }
                case _LDouble:
                    values = ObjectUtils.toObjectArray(value);
                    Double[] doubles = new Double[values.length];
                    for (int idx = 0; idx < doubles.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            doubles[idx] = null;
                        } else {
                            doubles[idx] = Double.valueOf(String.valueOf(values[idx]));
                        }
                    }

                    return doubles;
                case _Float:
                    if (value instanceof String) {
                        if (StringUtils.isEmpty((String) value)) {
                            return null;
                        }

                        return Float.valueOf((String) value);
                    } else if (value instanceof Float) {
                        return value;
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).floatValue();
                    } else {
                        return Float.valueOf(String.valueOf(value));
                    }
                case _LFloat:
                    values = ObjectUtils.toObjectArray(value);
                    Float[] floats = new Float[values.length];
                    for (int idx = 0; idx < floats.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            floats[idx] = null;
                        } else {
                            floats[idx] = Float.valueOf(String.valueOf(values[idx]));
                        }
                    }

                    return floats;
                case _Boolean:
                    if (value instanceof String) {
                        if (StringUtils.isEmpty((String) value)) {
                            return null;
                        }

                        return Boolean.valueOf((String) value);
                    } else if (value instanceof Boolean)
                        return value;
                    else {
                        return Boolean.valueOf(String.valueOf(value));
                    }
                case _LBoolean:
                    values = ObjectUtils.toObjectArray(value);
                    Boolean[] booleans = new Boolean[values.length];
                    for (int idx = 0; idx < booleans.length; idx++) {
                        if (values[idx] == null || "".equals(values[idx])) {
                            booleans[idx] = null;
                        } else {
                            booleans[idx] = Boolean.valueOf(String.valueOf(values[idx]));
                        }
                    }

                    return booleans;
                case _String:
                    return String.valueOf(value);

                case _Date_util:
                case _Date_sql:
                case _Timestamp:
                    if (value instanceof String) {
                        if (StringUtils.isEmpty((String) value)) {
                            return null;
                        }

                        return parseDate((String) value);
                    } else if (value instanceof Date)
                        return value;
                    else {
                        return parseDate(String.valueOf(value));
                    }
                case _int:
                case _long:
                case _double:
                case _float:
                    throw new ClassCastException("只支持对对象数据类型的转换，不支持基本数据类型的转换");

                default:
                    String newType;
                    if (typeName.startsWith("[L") && typeName.endsWith(";")) {
                        newType = typeName.substring(2, typeName.length() - 1);
                    } else {
                        newType = typeName;
                    }

                    Class<?> cls = Class.forName(newType);
                    TypeReference<Object> type = new TypeReference<Object>() {
                        public Type getType() {
                            return cls;
                        };
                    };

                    if (value instanceof String) {
                        if (String.class == cls) {
                            return value;
                        }

                        return JSON.parseObject((String) value, type);
                    } else if (value instanceof String[]) {
                        String[] array = (String[]) value;
                        Object[] objs = (Object[]) Array.newInstance(cls, array.length);
                        int idx = 0;
                        for (String val : array) {
                            if (String.class == cls) {
                                objs[idx] = (String) val;
                            } else {
                                objs[idx] = JSON.parseObject(val, type);
                            }

                            idx++;
                        }

                        return objs;
                    }

                    return value;
            }
        } catch (Throwable e) {
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

        String pattern = null;
        if (date.indexOf(':') > 0) {
            if (date.indexOf('.') > 0) {
                pattern = "yyyy-MM-dd HH:mm:ss.SSS";
            } else {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }
        } else {
            pattern = "yyyy-MM-dd";
        }

        return DateFormat.parse(date, pattern);
    }

    /**
     * 
     * @param date
     * @return date string
     */
    public static String fmtDate(Date date, String pattern) {
        if (null == date) {
            return null;
        }

        return DateFormat.format(date, pattern);
    }

}
