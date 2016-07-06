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
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.message.MessageFactory;
import org.nanoframework.commons.support.message.ParameterizedMessageFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.StringUtils;

/**
 * @author yanghe
 * @since 1.2
 */
public class DefaultMessageSource implements MessageSource {
    public static final String DEFAULT_PREFIX_MESSAGE = "/messages/messages";
    public static final String PROPERITES_SUFFIX = ".properties";
    private static final ConcurrentMap<Locale, Properties> MESSAGE = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private final MessageFactory messageFactory;
    private Locale locale;

    /**
     * 构建本地语言的I18N对象.
     */
    protected DefaultMessageSource() {
        this(Locale.getDefault());
    }

    /**
     * 构建指定语言的I18N对象.
     * @param locale 语言
     */
    protected DefaultMessageSource(final Locale locale) {
        this.locale = locale == null ? Locale.ROOT : locale;
        messageFactory = ParameterizedMessageFactory.INSTANCE;
        load(this.locale);
    }

    /**
     * 通过语言加载I18N对应的国际化文件.
     * @param locale 语言
     * @return 对应语言的国际化属性文件配置
     */
    protected Properties load(final Locale locale) {
        Properties properties;
        if ((properties = MESSAGE.get(locale)) == null) {
            synchronized (lock) {
                if ((properties = MESSAGE.get(locale)) == null) {
                    properties = PropertiesLoader.load(DEFAULT_PREFIX_MESSAGE + (StringUtils.isEmpty(locale.getLanguage()) ? "" : '_' + locale.getLanguage())
                                    + (StringUtils.isEmpty(locale.getCountry()) ? "" : '_' + locale.getCountry()) + PROPERITES_SUFFIX);
                    MESSAGE.put(locale, properties);
                }
            }
        }

        return properties;
    }

    @Override
    public String getMessage(final String code, final String defaultMessage) {
        try {
            return formatter(MESSAGE.get(locale).getProperty(code));
        } catch (final Throwable e) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(final String code, final String defaultMessage, final Locale locale) {
        try {
            return formatter(load(locale).getProperty(code));
        } catch (final Throwable e) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(final String code, final Object[] args, final String defaultMessage) {
        try {
            return formatter(MESSAGE.get(locale).getProperty(code), args);
        } catch (final Throwable e) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(final String code, final Object[] args, final String defaultMessage, final Locale locale) {
        try {
            return formatter(load(locale).getProperty(code), args);
        } catch (final Throwable e) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(final String code) throws NoSuchMessageException {
        try {
            return formatter(MESSAGE.get(locale).getProperty(code));
        } catch (final Throwable e) {
            throw new NoSuchMessageException(code);
        }
    }

    @Override
    public String getMessage(final String code, final Object[] args) throws NoSuchMessageException {
        try {
            return formatter(MESSAGE.get(locale).getProperty(code), args);
        } catch (final Throwable e) {
            throw new NoSuchMessageException(code);
        }
    }

    @Override
    public String getMessage(final String code, final Locale locale) throws NoSuchMessageException {
        try {
            return formatter(load(locale).getProperty(code));
        } catch (final Throwable e) {
            throw new NoSuchMessageException(code, locale);
        }
    }

    @Override
    public String getMessage(final String code, final Object[] args, final Locale locale) throws NoSuchMessageException {
        try {
            return formatter(load(locale).getProperty(code), args);
        } catch (Throwable e) {
            throw new NoSuchMessageException(code, locale);
        }
    }

    /**
     * 转化国际化消息
     * @param message 国际化消息
     * @param args 格式参数
     * @return 转化后的国际化消息
     */
    protected String formatter(final String message, final Object... args) {
        Assert.notNull(message);
        return messageFactory.newMessage(message, args).getFormattedMessage();
    }
}
