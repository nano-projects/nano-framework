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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

import org.nanoframework.commons.util.Assert;

import com.google.common.collect.Maps;

/**
 * @author yanghe
 * @since 1.0
 */
public final class DateFormat {
    private static final ConcurrentMap<String, ThreadLocal<SimpleDateFormat>> FORMAT_MAP = Maps.newConcurrentMap();

    private DateFormat() {
        
    }
    
    /**
     * 获取时间转换对象.
     * @param pattern 时间格式
     * @return 时间转换对象
     */
    public static final SimpleDateFormat get(final String pattern) {
        SimpleDateFormat format;
        ThreadLocal<SimpleDateFormat> formatLocal;
        if ((formatLocal = FORMAT_MAP.get(pattern)) == null) {
            formatLocal = new ThreadLocal<>();
            formatLocal.set(format = new SimpleDateFormat(pattern));
            FORMAT_MAP.put(pattern, formatLocal);
        }

        if ((format = formatLocal.get()) == null) {
            format = new SimpleDateFormat(pattern);
            formatLocal.set(format);
        }

        return format;
    }

    /**
     * 根据时间格式枚举获取时间转换对象.
     * @param pattern 时间格式枚举
     * @return 时间转换对象
     */
    public static final SimpleDateFormat get(final Pattern pattern) {
        Assert.notNull(pattern, "pattern must be not null.");
        return get(pattern.get());
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间对象
     * @param pattern 时间格式枚举
     * @return 时间型字符串
     */
    public static final String format(final Date date, final Pattern pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间对象
     * @param pattern 时间格式
     * @return 时间型字符串
     */
    public static final String format(final Date date, final String pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间类型的对象
     * @param pattern 时间格式枚举
     * @return 时间型字符串
     */
    public static final String format(final Object date, final Pattern pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间对象转成时间格式的字符串.
     * @param date 时间类型的对象
     * @param pattern 时间格式字符串
     * @return 时间型字符串
     */
    public static final String format(final Object date, final String pattern) {
        return get(pattern).format(date);
    }

    /**
     * 将时间型字符串转成时间对象.
     * @param <T> 参数类型
     * @param date 时间型字符串
     * @param pattern 时间格式枚举
     * @return 时间对象
     * @throws ParseException 时间转换异常
     */
    @SuppressWarnings("unchecked")
    public static final <T extends Date> T parse(final String date, final Pattern pattern) throws ParseException {
        return (T) get(pattern).parse(date);
    }

    /**
     * 将时间型字符串转成时间对象.
     * @param <T> 参数类型
     * @param date 时间型字符串
     * @param pattern 时间格式字符串
     * @return 时间对象
     * @throws ParseException 时间转换异常
     */
    @SuppressWarnings("unchecked")
    public static final <T extends Date> T parse(final String date, final String pattern) throws ParseException {
        return (T) get(pattern).parse(date);
    }
}
