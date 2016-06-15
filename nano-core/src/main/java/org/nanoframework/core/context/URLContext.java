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
package org.nanoframework.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.util.ObjectCompare;

/**
 * 请求地址解析后对象，拆解GET请求中的context和参数列表.
 * 
 * @author yanghe
 * @since 1.3.7
 * @date 2015年6月5日 下午10:58:41 
 */
public class URLContext {

	/** URI串. */
	private String context;
	
	/** 带有';'的URI拆分内容. */
	private String[] special;
	
	/** 参数列表. */
	private Map<String, Object> parameter;
	
	private URLContext() {}

	/** 创建URLContext对象. */
	public static URLContext create() {
		return new URLContext();
		
	}
	
	/** 获取URI串. */
	public String getContext() {
		return context;
	}
	
	/** 获取URI串, 去除context root. */
	public String getNoRootContext() {
		final String root = System.getProperty(ApplicationContext.CONTEXT_ROOT);
		return context == null ? "" : StringUtils.isEmpty(root) ? context : context.replaceFirst(root, "");
	}

	/** 设置URI串. */
	public URLContext setContext(final String context) {
		this.context = context;
		return this;
	}
	
	/**
	 * @return the special
	 */
	public String[] getSpecial() {
        return special;
	}
	
	public URLContext setSpecial(final String[] special) {
        this.special = special;
		return this;
	}

	/** 获取参数列表. */
	public Map<String, Object> getParameter() {
		return parameter;
	}

	/** 设置参数列表. */
	public URLContext setParameter(final Map<String, Object> parameter) {
		this.parameter = parameter;
		return this;
	}
	
	/** 过滤URI. */
	public static final boolean filterURI(final String uri) {
		if(!uri.startsWith(System.getProperty(ApplicationContext.CONTEXT_ROOT)) || 
		        ObjectCompare.isInList(uri, System.getProperty(ApplicationContext.CONTEXT_FILTER).split(";"))) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 转换URL, 将context及参数名转位小写，以使其不区分大小写.
	 * @param url 请求URL
	 */
	@SuppressWarnings("unchecked")
	public static final URLContext formatURL(final String url) {
		if (StringUtils.isEmpty(url)) {
			throw new NullPointerException("URL不能为空");
		}
		
		final String[] tokens = url.split("[?]");
		final String context = tokens[0];
		final URLContext urlContext = URLContext.create().setContext(context);

		// 拆分参数列表
		if (tokens.length > 1) {
			final Map<String, Object> keyValue = new HashMap<>();
			final String[] params = tokens[1].split("&");
			if (params.length > 0) {
				boolean hasArray = false;
				for (String param : params) {
					final String[] kv = param.split("=");
					if(kv[0].endsWith("[]")) {
						hasArray = true;
						List<String> values = (List<String>) keyValue.get(kv[0]);
						if(values == null) {
							values = new ArrayList<>();
						}
						
						values.add(kv[1]);
						keyValue.put(kv[0], values);
					} else {
						keyValue.put(kv[0].toLowerCase(), kv[1]);
					}
				}
				
				if(hasArray) {
					keyValue.entrySet().stream().filter(entry -> entry.getKey().endsWith("[]")).forEach(entry -> {
						keyValue.put(entry.getKey(), ((List<String>) entry.getValue()).toArray(new String[0]));
					});
				}
			}

			urlContext.setParameter(keyValue);
		} else {
			urlContext.setParameter(Collections.emptyMap());
		}

		return urlContext;
	}
}
