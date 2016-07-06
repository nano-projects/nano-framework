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
package org.nanoframework.web.server.mvc.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.web.server.mvc.Model;

/**
 * 模型实现
 * 
 * @author yanghe
 * @since 1.0 
 */
public class RedirectModel implements Model {

	/** 模型属性映射表，此映射表作为返回前端的结果集 */
	private Map<String, Object> attributes = new HashMap<>();
	
	@Override
	public Model addAttribute(String attributeName, Object attributeValue) {
		if(StringUtils.isEmpty(attributeName))
			throw new NullPointerException("attributeName不能为空");
		
		if(attributeValue == null)
			throw new NullPointerException("attributeValue不能为空");
			
		attributes.put(attributeName, attributeValue);
		return this;
	}

	@Override
	public Model addAllAttributes(Map<String, Object> attributes) {
		if(attributes != null && attributes.size() > 0) {
			attributes.forEach((key, value) -> this.attributes.put(key, value));
		}
			
		return this;
	}

	@Override
	public boolean containsAttribute(String attributeName) {
		return attributes.containsKey(attributeName);
	}

	@Override
	public Map<String, Object> get() {
		return attributes;
	}

}
