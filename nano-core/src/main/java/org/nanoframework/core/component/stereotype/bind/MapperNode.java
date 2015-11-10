/**
 * Copyright 2015 the original author or authors.
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
package org.nanoframework.core.component.stereotype.bind;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.exception.ComponentServiceRepeatException;

/**
 * 
 * @author yanghe
 * @date 2015年9月23日 下午8:51:22
 * @since 1.2
 */
public class MapperNode extends BaseEntity {
	private String token;
	private String uri;
	private Map<RequestMethod, RequestMapper> mapper = new LinkedHashMap<>();
	private String parentToken;
	private RequestMapper parentMapper;
	private Map<String, MapperNode> leafNodes = new LinkedHashMap<>();
	
	public static final String SLASH = "/";
	public static final MapperNode ROOT = new MapperNode() {{
		String context = System.getProperty(Constants.CONTEXT_ROOT);
		if(StringUtils.isBlank(context))
			throw new IllegalArgumentException("无效的/无法获取context.root属性");
		
		if(context.startsWith(SLASH))
			context = context.substring(1);
		else
			throw new IllegalArgumentException("context.root属性必须以'/'开头");
		
		setToken(context);
		setUri(SLASH + context);
	}};
	
	private MapperNode() { }

	public static void addLeaf(String uri, Map<RequestMethod, RequestMapper> mapper) {
		Assert.hasLength(uri);
		Assert.notNull(mapper);
		MapperNode _node = ROOT;
		String[] tokens = uri.split(SLASH);
		StringBuilder builder = new StringBuilder();
		for(int idx = 1; idx < tokens.length; idx ++) {
			String _token = tokens[idx].trim();
			if(StringUtils.isBlank(_token))
				throw new IllegalArgumentException("无效的URI: " + uri);
			
			Map<String, MapperNode> _leafNodes = _node.getLeafNodes();
			if(_leafNodes.get(_token) == null) {
				validURI(uri, _token, _leafNodes.keySet());
				MapperNode node = new MapperNode();
				node.setToken(_token);
				builder.append(SLASH).append(_token);
				node.setUri(builder.toString());
				node.setParentToken(_node.getToken());
				node.setParentMapper(_node.getParentMapper());
				if(idx == tokens.length - 1) {
					node.putMapper(mapper, uri);
				}
				
				_leafNodes.put(_token, node);
				_node = node;
			} else  {
				validURI(uri, _token, _leafNodes.keySet());
				builder.append(SLASH).append(_token);
				_node = _leafNodes.get(_token);
				if(idx == tokens.length - 1) {
					_node.putMapper(mapper, uri);
				}
			}
		}
	}
	
	private static void validURI(String uri, String token, Set<String> tokens) {
		if(token.startsWith("{") && token.endsWith("}")) {
			for(String _token : tokens) {
				if(_token.startsWith("{") && _token.endsWith("}")) {
					if(!_token.equals(token))
						throw new ComponentServiceRepeatException("重复的Restful风格URI定义：" + uri);
				}
			}
		} else if((token.startsWith("{") && !token.endsWith("}")) || (!token.startsWith("{") && token.endsWith("}"))) {
			throw new IllegalArgumentException("无效的Restful风格的URI定义：" + uri);
		}
		
	}
	
	public static RequestMapper get(String uri, RequestMethod requestMethod) {
		Assert.hasLength(uri);
		Assert.notNull(requestMethod);
		MapperNode _node = ROOT;
		String[] tokens = uri.split(SLASH);
		StringBuilder builder = new StringBuilder();
		Map<String, String> param = new HashMap<>();
		for(int idx = 1; idx < tokens.length; idx ++) {
			String _token_origin = tokens[idx].trim();
			String _token = _token_origin.toLowerCase();
			if(StringUtils.isBlank(_token))
				continue ;
			
			if((builder.toString() + SLASH + _token).equals(_node.getUri())) {
				builder.append(SLASH).append(_token);
				_node = _node.getLeafNodes().get(_token);
			} else {
				boolean isAppend = false;
				MapperNode _tmp = _node.getLeafNodes().get(_token);
				if(_tmp != null && (builder.toString() + SLASH + _token).equals(_tmp.getUri())) {
					builder.append(SLASH).append(_token);
					_node = _node.getLeafNodes().get(_token);
					isAppend = true;
				} else {
					for(String key : _node.getLeafNodes().keySet()) {
						 if(key.startsWith("{") && key.endsWith("}")) {
							builder.append(SLASH).append(key);
							_node = _node.getLeafNodes().get(key);
							param.put(key.replace("{", "").replace("}", ""), _token_origin);
							isAppend = true;
							break;
						}
					}
				}
				
				if(!isAppend)
					builder.append(SLASH).append(_token);
			}
			
			if(idx == tokens.length - 1) {
				if(builder.toString().equals(_node.getUri())) {
					Map<RequestMethod, RequestMapper> map = _node.getMapper();
					RequestMapper mapper;
					if((mapper = map.get(requestMethod)) != null) {
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
	
	public void putMapper(Map<RequestMethod, RequestMapper> _mapper, String uri) {
		Assert.notNull(_mapper);
		Set<RequestMethod> requestMethods = _mapper.keySet();
		for(RequestMethod method : requestMethods) {
			if(mapper.containsKey(method))
				throw new ComponentServiceRepeatException("MapperNode.putMapper(RequestMapper, String): 重复的Restful风格URI定义: " + uri + ", method: " + method.name());
			
			mapper.put(method, _mapper.get(method));
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
