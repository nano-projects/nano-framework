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
package org.nanoframework.core.component.stereotype.bind;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.AntPathMatcher;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.PathMatcher;
import org.nanoframework.core.component.exception.ComponentServiceRepeatException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class Routes {
    private static final Logger LOGGER = LoggerFactory.getLogger(Routes.class);
    private static final Routes INSTANCE = new Routes();
    private final Map<String, Map<RequestMethod, RequestMapper>> mappers = Maps.newLinkedHashMap();
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private Routes() {

    }

    public static Routes route() {
        return INSTANCE;
    }

    public RequestMapper lookup(final String url, final RequestMethod requestMethod) {
        final Map<RequestMethod, RequestMapper> mappers = this.mappers.get(url);
        if (!CollectionUtils.isEmpty(mappers)) {
            final RequestMapper mapper = mappers.get(requestMethod);
            if (mapper != null) {
                return mapper;
            }
        }

        final List<String> matchingPatterns = Lists.newArrayList();
        this.mappers.keySet().stream().filter(registeredPattern -> pathMatcher.match(registeredPattern, url))
                .forEach(registeredPattern -> matchingPatterns.add(registeredPattern));

        final Comparator<String> patternComparator = pathMatcher.getPatternComparator(url);
        String bestPatternMatch = null;
        if (!matchingPatterns.isEmpty()) {
            Collections.sort(matchingPatterns, patternComparator);
            LOGGER.debug("Matching patterns for request [{}] are {}", url, matchingPatterns);
            bestPatternMatch = matchingPatterns.get(0);
        }

        return lookup(url, requestMethod, bestPatternMatch, matchingPatterns, patternComparator);
    }
    
    protected RequestMapper lookup(final String url, final RequestMethod requestMethod, final String bestPatternMatch, 
            final List<String> matchingPatterns, final Comparator<String> patternComparator) {
        if (bestPatternMatch != null) {
            Map<RequestMethod, RequestMapper> mappers = this.mappers.get(bestPatternMatch);
            if (mappers == null) {
                if (!bestPatternMatch.endsWith("/")) {
                    return null;
                }

                mappers = this.mappers.get(bestPatternMatch.substring(0, bestPatternMatch.length() - 1));
                if (mappers == null) {
                    return null;
                }
            }

            final RequestMapper mapper = mappers.get(requestMethod);
            if (mapper == null) {
                return null;
            }

            final Map<String, String> uriTemplateVariables = Maps.newLinkedHashMap();
            for (final String matchingPattern : matchingPatterns) {
                if (patternComparator.compare(bestPatternMatch, matchingPattern) == 0) {
                    final Map<String, String> vars = pathMatcher.extractUriTemplateVariables(matchingPattern, url);
                    final Map<String, String> decodedVars = urlPathHelper.decodePathVariables(vars);
                    uriTemplateVariables.putAll(decodedVars);
                }
            }

            mapper.setParam(uriTemplateVariables);
            return mapper;
        }
        
        return null;
    }

    public void register(final String url, final Map<RequestMethod, RequestMapper> mappers) {
        if (CollectionUtils.isEmpty(mappers)) {
            return;
        }
        
        mappers.keySet().forEach(requestMethod -> {
            final RequestMapper mapped = lookup(url, requestMethod);
            if (mapped != null) {
                throw new ComponentServiceRepeatException("Duplicate Restful-style URL definition: " + url);
            }
        });
        
        final Map<RequestMethod, RequestMapper> mappedMapper = this.mappers.get(url);
        if (mappedMapper != null) {
            mappers.forEach((requestMethod, mapper) -> {
                if (mappedMapper.containsKey(requestMethod)) {
                    throw new ComponentServiceRepeatException("Duplicate Restful-style URL definition: " + url + " of method [ " + requestMethod + " ]"); 
                } else {
                    mappedMapper.put(requestMethod, mapper);
                }
            });
        } else {
            this.mappers.put(url, mappers);
        }
    }

    public void clear() {
        this.mappers.clear();
    }

    public Map<String, Map<RequestMethod, RequestMapper>> matchers(final Object instance, final Method[] methods,
            final Class<? extends RequestMapping> annotated, final String url) {
        if (ArrayUtils.isEmpty(methods)) {
            return Collections.emptyMap();
        }

        final Map<String, Map<RequestMethod, RequestMapper>> routes = Maps.newHashMap();
        Arrays.asList(methods).stream().filter(method -> filterMethod(method, annotated)).map(method -> routeDefine(instance, method, annotated, url))
                .forEach(routeRes -> routeDefine0(routeRes, routes));

        return routes;
    }

    protected boolean filterMethod(final Method method, final Class<? extends RequestMapping> annotated) {
        if (method.isAnnotationPresent(annotated)) {
            final RequestMapping mapping = method.getAnnotation(annotated);
            if (mapping != null && StringUtils.isNotBlank(mapping.value())) {
                return true;
            }
        }

        return false;
    }

    protected Route routeDefine(final Object instance, final Method method,
            final Class<? extends RequestMapping> annotated, final String url) {
        final RequestMapping mapping = method.getAnnotation(annotated);
        final RequestMapper mapper = RequestMapper.create().setInstance(instance).setCls(instance.getClass()).setMethod(method)
                .setRequestMethods(mapping.method());
        final Map<RequestMethod, RequestMapper> mappers = Maps.newHashMap();
        final RequestMethod[] requestMethods = mapper.getRequestMethods();
        for (final RequestMethod requestMethod : requestMethods) {
            mappers.put(requestMethod, mapper);
        }

        final String route = (url + mapping.value());
        final String newRoute = execRoutePath(route);
        LOGGER.debug("Route define: {}.{}:{} {}", instance.getClass().getName(), method.getName(), newRoute, Lists.newArrayList(requestMethods));
        return new Route(newRoute, mappers);
    }
    
    protected String execRoutePath(final String route) {
        final String[] rtks = route.split("/");
        final StringBuilder routeBuilder = new StringBuilder();
        for (final String rtk : rtks) {
            if (StringUtils.isEmpty(rtk)) {
                continue;
            }
            
            if (rtk.startsWith("{") && rtk.endsWith("}")) {
                routeBuilder.append('/');
                
                final int idx = rtk.indexOf(':');
                if (idx > 0) {
                    routeBuilder.append(StringUtils.lowerCase(rtk.substring(0, idx)));
                    routeBuilder.append(rtk.substring(idx));
                } else {
                    routeBuilder.append(rtk);
                }
            } else if ((rtk.startsWith("{") && !rtk.endsWith("}")) || (!rtk.startsWith("{") && rtk.endsWith("}"))) {
                throw new IllegalArgumentException("Invalid route definition: " + route);
            } else {
                routeBuilder.append('/');
                routeBuilder.append(StringUtils.lowerCase(rtk));
            }
        }
        
        return routeBuilder.toString();
    }

    protected void routeDefine0(final Route route,
            final Map<String, Map<RequestMethod, RequestMapper>> routes) {
        final String routeURL = route.getRoute();
        if (!CollectionUtils.isEmpty(route.getMappers()) && routes.containsKey(routeURL)) {
            final Set<RequestMethod> before = route.getMappers().keySet();
            final Set<RequestMethod> after = routes.get(routeURL).keySet();
            if (!isIntersectionRequestMethod(before, after)) {
                putRoute(route, routes);
            } else {
                throw new ComponentServiceRepeatException(routeURL);
            }
        } else {
            putRoute(route, routes);
        }
    }

    private void putRoute(final Route route,
            final Map<String, Map<RequestMethod, RequestMapper>> routes) {
        final String url = route.getRoute();
        final Map<RequestMethod, RequestMapper> mapper = route.getMappers();
        final Map<RequestMethod, RequestMapper> mappers = routes.get(url);
        if (mappers == null) {
            routes.put(url, mapper);
        } else {
            mappers.putAll(mapper);
            routes.put(url, mappers);
        }
    }

    private boolean isIntersectionRequestMethod(final Set<RequestMethod> before, final Set<RequestMethod> after) {
        Assert.notEmpty(before);
        Assert.notEmpty(after);
        for (final RequestMethod bf : before) {
            for (final RequestMethod af : after) {
                if (bf == af) {
                    return true;
                }
            }
        }

        return false;
    }

    protected static class Route extends BaseEntity {
        private static final long serialVersionUID = 4937587574776102818L;

        private String route;
        private Map<RequestMethod, RequestMapper> mappers;

        public Route(final String route, final Map<RequestMethod, RequestMapper> mappers) {
            this.route = route;
            this.mappers = mappers;
        }
        
        public String getRoute() {
            return route;
        }

        public void setRoute(final String route) {
            this.route = route;
        }

        public Map<RequestMethod, RequestMapper> getMappers() {
            return mappers;
        }

        public void setMappers(final Map<RequestMethod, RequestMapper> mappers) {
            this.mappers = mappers;
        }

    }
}
