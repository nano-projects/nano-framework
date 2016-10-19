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
package org.nanoframework.core.component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nanoframework.commons.format.ClassCast;
import org.nanoframework.commons.loader.LoaderException;
import org.nanoframework.commons.loader.PropertiesLoader;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.exception.BindRequestParamException;
import org.nanoframework.core.component.exception.ComponentInvokeException;
import org.nanoframework.core.component.scan.ClassScanner;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.RequestParam;
import org.nanoframework.core.component.stereotype.bind.Routes;
import org.nanoframework.core.component.stereotype.bind.ValueConstants;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.core.globals.Globals;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

/**
 * 组件操作类.
 * @author yanghe
 * @since 1.0.0
 */
public final class Components {
    private static final Logger LOGGER = LoggerFactory.getLogger(Components.class);
    private static boolean isLoaded;
    
    private Components() {
        
    }

    /**
     * 加载组件服务，并且装载至组件服务映射表中.
     * @throws LoaderException 加载异常类
     * @throws IOException IO异常类
     */
    public static final void load() throws LoaderException, IOException {
        if (isLoaded) {
            throw new LoaderException("Component已经加载，这里不再进行重复的加载，如需重新加载请调用reload方法");
        }

        PropertiesLoader.PROPERTIES.values().stream()
        .filter(item -> StringUtils.isNotBlank(item.getProperty(ApplicationContext.COMPONENT_BASE_PACKAGE)))
        .forEach(item -> {
            final String[] packageNames = item.getProperty(ApplicationContext.COMPONENT_BASE_PACKAGE).split(",");
            Arrays.asList(packageNames).forEach(packageName -> ClassScanner.scan(packageName));
        });

        final Set<Class<?>> classes = ClassScanner.filter(Component.class);
        LOGGER.info("Component size: {}", classes.size());
        if (classes.size() > 0) {
            for (Class<?> cls : classes) {
                LOGGER.info("Inject Component Class: {}", cls.getName());
                final Object instance = Globals.get(Injector.class).getInstance(cls);
                final Method[] methods = cls.getMethods();
                final String mapping = cls.isAnnotationPresent(RequestMapping.class) ? cls.getAnnotation(RequestMapping.class).value() : "";
                final Map<String, Map<RequestMethod, RequestMapper>> mappers = Routes.route().matchers(instance, methods, RequestMapping.class, mapping);
                mappers.forEach((url, mapper) -> Routes.route().registerRoute(url, mapper));
            }
        }

        isLoaded = true;
    }

    /**
     * 重新加载组件.
     * @throws LoaderException 加载异常类
     * @throws IOException IO异常类
     */
    public static final void reload() throws LoaderException, IOException {
        destroy();
        load();
    }
    
    public static final void destroy() {
        Routes.route().clearRoute();
        ClassScanner.clear();
        isLoaded = false;
    }

    /**
     * 绑定参数，根据调用的方法参数列表的类型对传入的参数进行类型转换.
     * @param method 调用的方法
     * @param params 请求参数列表
     * @param objs 附加参数列表
     * @return 返回绑定结果
     * 
     * @see org.nanoframework.commons.format.ClassCast#cast(Object, String)
     */
    @SuppressWarnings("deprecation")
    public static final Object[] bindParam(final Method method, final Map<String, Object> params, final Object... objs) {
        final Map<String, Object> lowerCaseParams = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(params)) {
            params.forEach((key, value) -> lowerCaseParams.put(key.toLowerCase(), value));
        }
        
        final Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length > 0) {
            final List<Object> values = Lists.newArrayList();
            for (final Parameter parameter : parameters) {
                final Class<?> type = parameter.getType();
                final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    String value = requestParam.value();
                    if (StringUtils.isBlank(value)) {
                        value = requestParam.name();
                    }

                    Object param = lowerCaseParams.get(value.toLowerCase());
                    if (param == null && !StringUtils.equals(requestParam.defaultValue(), ValueConstants.DEFAULT_NONE)) {
                        param = requestParam.defaultValue();
                    }

                    /** 空字符参数现在也将被判定为空，在必填的情况下也将抛出绑定参数异常 by yanghe on 2015-07-01 19:08 */
                    if (requestParam.required() && (param == null || (param instanceof String && StringUtils.isEmpty((String) param)))) {
                        throw new BindRequestParamException("参数:[" + value + "]为必填项，但是获取的参数值为空.");
                    }

                    try {
                        final Object obj = ClassCast.cast(param, type.getName());
                        values.add(obj);
                    } catch (org.nanoframework.commons.exception.ClassCastException e) {
                        LOGGER.error(e.getMessage(), e);
                        throw new BindRequestParamException("类型转换异常: 数据类型 [ " + type.getName() + " ], 值 [ " + param + " ]");
                    }
                } else {
                    final PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                    if (pathVariable != null) {
                        final Object param = lowerCaseParams.get(pathVariable.value().toLowerCase());
                        if (param != null) {
                            try {
                                final Object obj = ClassCast.cast(param, type.getName());
                                values.add(obj);
                            } catch (org.nanoframework.commons.exception.ClassCastException e) {
                                LOGGER.error(e.getMessage(), e);
                                throw new BindRequestParamException("类型转换异常: 数据类型 [ " + type.getName() + " ], 值 [ " + param + " ]");
                            }
                        } else {
                            throw new BindRequestParamException("Restful风格参数:[" + pathVariable.value().toLowerCase() + "]为必填项，但是获取的参数值为空.");
                        }
                    } else if (objs != null && objs.length > 0) {
                        for (Object obj : objs) {
                            if (type.isInstance(obj)) {
                                values.add(obj);
                                break;
                            }
                        }
                    }
                }

            }

            return values.toArray(new Object[values.size()]);
        }

        return null;
    }
    
    /**
     * 组件服务调用.
     * @param mapper 组件映射
     * @return 返回调用结果
     */
    public static Object invoke(final RequestMapper mapper) {
        return invoke(mapper, Maps.newHashMap());
    }
 
    /**
     * 组件服务调用.
     * @param mapper 组件映射
     * @param parameter 参数列表
     * @param objs 附加参数列表
     * @return 返回调用结果
     */
    public static final Object invoke(final RequestMapper mapper, final Map<String, Object> parameter, final Object... objs) {
        if (mapper != null) {
            try {
                final Map<String, String> param = mapper.getParam();
                if (!CollectionUtils.isEmpty(param)) {
                    parameter.putAll(param);
                }
                
                final Object obj = mapper.getInstance();
                final Method method = (Method) mapper.getMethod();
                final Object[] bind = Components.bindParam(method, parameter, objs);
                return method.invoke(obj, bind);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                if (e instanceof ComponentInvokeException) {
                    throw (ComponentInvokeException) e;
                } else {
                    Throwable tmp = e;
                    Throwable cause;
                    while ((cause = tmp.getCause()) != null) {
                        if (cause instanceof ComponentInvokeException) {
                            throw (ComponentInvokeException) cause;
                        } else {
                            tmp = cause;
                        }
                    }

                    throw new ComponentInvokeException(tmp.getMessage(), tmp);
                }
            }

        } else {
            throw new ComponentInvokeException("Not found resources!");
        }
    }

}
