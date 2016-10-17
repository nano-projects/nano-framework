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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.exception.ComponentServiceRepeatException;
import org.nanoframework.core.context.ApplicationContext;

/**
 * 
 * @author yanghe
 * @since 1.2
 */
public class MapperNode extends BaseEntity {
    private static final long serialVersionUID = -7473965211805892195L;
    private String token;
    private String uri;
    private Map<RequestMethod, RequestMapper> mapper = new LinkedHashMap<>();
    private String parentToken;
    private RequestMapper parentMapper;
    private ConcurrentMap<String, MapperNode> leafNodes = new ConcurrentHashMap<>();

    public static final char SLASH = '/';

    public static final MapperNode ROOT = new MapperNode() {
        private static final long serialVersionUID = 5928454777545648552L;
        {
            String context = System.getProperty(ApplicationContext.CONTEXT_ROOT);
            if (StringUtils.isBlank(context)) {
                throw new IllegalArgumentException("无效的context.root属性");
            }

            if (context.startsWith("/")) {
                context = context.substring(1);
            } else {
                throw new IllegalArgumentException("context.root属性必须以'/'开头");
            }

            setToken(context);
            setUri(SLASH + context);
        }
    };

    private MapperNode() {
    }

    public static synchronized void addLeaf(String uri, Map<RequestMethod, RequestMapper> mapper) {
        Assert.hasLength(uri);
        Assert.notNull(mapper);
        MapperNode root = ROOT;
        String[] tokens = uri.split("/");
        StringBuilder builder = new StringBuilder();
        for (int idx = 1; idx < tokens.length; idx++) {
            String token = tokens[idx].trim();
            if (StringUtils.isBlank(token)) {
                throw new IllegalArgumentException("无效的URI: " + uri);
            }

            Map<String, MapperNode> leafNodes = root.getLeafNodes();
            if (leafNodes.get(token) == null) {
                validURI(uri, token, leafNodes.keySet());
                MapperNode node = new MapperNode();
                node.setToken(token);
                builder.append(SLASH).append(token);
                node.setUri(builder.toString());
                node.setParentToken(root.getToken());
                node.setParentMapper(root.getParentMapper());
                if (idx == tokens.length - 1) {
                    node.putMapper(mapper, uri);
                }

                leafNodes.put(token, node);
                root = node;
            } else {
                validURI(uri, token, leafNodes.keySet());
                builder.append(SLASH).append(token);
                root = leafNodes.get(token);
                if (idx == tokens.length - 1) {
                    root.putMapper(mapper, uri);
                }
            }
        }
    }

    private static void validURI(String uri, String token, Set<String> tokens) {
        if (token.startsWith("{") && token.endsWith("}")) {
            for (String tkn : tokens) {
                if (tkn.startsWith("{") && tkn.endsWith("}")) {
                    if (!tkn.equals(token)) {
                        throw new ComponentServiceRepeatException("重复的Restful风格URI定义：" + uri);
                    }
                }
            }
        } else if ((token.startsWith("{") && !token.endsWith("}")) || (!token.startsWith("{") && token.endsWith("}"))) {
            throw new IllegalArgumentException("无效的Restful风格的URI定义：" + uri);
        }
    }

    public static RequestMapper get(String uri, RequestMethod requestMethod) {
        Assert.notNull(uri);
        Assert.notNull(requestMethod);
        MapperNode nowNode = ROOT;
        String[] tokens = uri.split("/");
        StringBuilder builder = new StringBuilder();
        Map<String, String> param = new HashMap<>();
        for (int idx = 1; idx < tokens.length; idx++) {
            String originToken = tokens[idx].trim();
            String token = originToken.toLowerCase();
            if (StringUtils.isBlank(token)) {
                continue;
            }

            if ((builder.toString() + SLASH + token).equals(nowNode.getUri())) {
                builder.append(SLASH).append(token);
                nowNode = nowNode.getLeafNodes().get(token);
            } else {
                boolean isAppend = false;
                MapperNode leafNode = nowNode.getLeafNodes().get(token);
                if (leafNode != null && (builder.toString() + SLASH + token).equals(leafNode.getUri())) {
                    builder.append(SLASH).append(token);
                    nowNode = nowNode.getLeafNodes().get(token);
                    isAppend = true;
                } else {
                    for (String key : nowNode.getLeafNodes().keySet()) {
                        if (key.startsWith("{") && key.endsWith("}")) {
                            builder.append(SLASH).append(key);
                            nowNode = nowNode.getLeafNodes().get(key);
                            param.put(key.replace("{", "").replace("}", ""), originToken);
                            isAppend = true;
                            break;
                        }
                    }
                }

                if (!isAppend) {
                    builder.append(SLASH).append(token);
                }
            }

            if (idx == tokens.length - 1) {
                if (builder.toString().equals(nowNode.getUri())) {
                    Map<RequestMethod, RequestMapper> map = nowNode.getMapper();
                    RequestMapper mapper;
                    if ((mapper = map.get(requestMethod)) != null) {
                        mapper = (RequestMapper) mapper.clone();
                        mapper.setParam(param);
                        return mapper;
                    }
                }
            }
        }

        return null;
    }

    public static void clear() {
        ROOT.getLeafNodes().clear();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<RequestMethod, RequestMapper> getMapper() {
        return mapper;
    }

    public void putMapper(Map<RequestMethod, RequestMapper> mapper, String uri) {
        Assert.notNull(mapper);
        for (Iterator<Entry<RequestMethod, RequestMapper>> iter = mapper.entrySet().iterator(); iter.hasNext();) {
            final Entry<RequestMethod, RequestMapper> entry = iter.next();
            final RequestMethod method = entry.getKey();
            if (this.mapper.containsKey(method)) {
                throw new ComponentServiceRepeatException(
                        "MapperNode.putMapper(RequestMapper, String): 重复的Restful风格URI定义: " + uri + ", method: " + method.name());
            }

            this.mapper.put(method, entry.getValue());
        }
    }

    public String getParentToken() {
        return parentToken;
    }

    public void setParentToken(String parentToken) {
        this.parentToken = parentToken;
    }

    public RequestMapper getParentMapper() {
        return parentMapper;
    }

    public void setParentMapper(RequestMapper parentMapper) {
        this.parentMapper = parentMapper;
    }

    public Map<String, MapperNode> getLeafNodes() {
        return leafNodes;
    }

}
