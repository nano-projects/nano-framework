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
package org.nanoframework.commons.i18n;

import java.util.Locale;

/**
 * @author yanghe
 * @since 1.2
 */
public interface MessageSource {
    /**
     * 本地语言的I18N消息处理对象.
     */
    final MessageSource DEFAULT = new DefaultMessageSource(Locale.getDefault());

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @param defaultMessage 默认消息
     * @return 国际化消息
     */
    String getMessage(String code, String defaultMessage);

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @param defaultMessage 默认消息
     * @param locale 语言
     * @return 国际化消息
     */
    String getMessage(String code, String defaultMessage, Locale locale);

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @param args 参数列表
     * @param defaultMessage 默认消息
     * @return 国际化消息
     */
    String getMessage(String code, Object[] args, String defaultMessage);

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @param args 参数列表
     * @param defaultMessage 默认消息
     * @param locale 语言
     * @return 国际化消息
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @return 国际化消息
     * @throws NoSuchMessageException 未查找到属性编码时抛出
     */
    String getMessage(String code) throws NoSuchMessageException;

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @param args 参数列表
     * @return 国际化消息
     * @throws NoSuchMessageException 未查找到属性编码时抛出
     */
    String getMessage(String code, Object[] args) throws NoSuchMessageException;

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @param locale 语言
     * @return 国际化消息
     * @throws NoSuchMessageException 未查找到属性编码时抛出
     */
    String getMessage(String code, Locale locale) throws NoSuchMessageException;

    /**
     * 获取国际化消息.
     * @param code 属性编码
     * @param args  参数列表
     * @param locale 语言
     * @return 国际化消息
     * @throws NoSuchMessageException 未查找到属性编码时抛出
     */
    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

}
