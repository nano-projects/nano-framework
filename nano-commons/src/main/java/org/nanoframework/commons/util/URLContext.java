/**
 * Copyright 2015- the original author or authors.
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
package org.nanoframework.commons.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * 请求地址解析后对象，拆解GET请求中的context和参数列表
 * 
 * @author yanghe
 * @date 2015年6月5日 下午10:58:41 
 *
 */
public class URLContext {

	/** URI串 */
	private String context;
	
	/** 带有';'的URI拆分内容 */
	private String[] special;
	
	/** 参数列表 */
	private Map<String, Object> parameter;
	
	private URLContext() {}

	/** 创建URLContext对象 */
	public static URLContext create() {
		return new URLContext();
		
	}
	
	/** 获取URI串 */
	public String getContext() {
		return context;
	}
	
	/** 获取URI串, 去除context root */
	public String getNoRootContext() {
		String root = System.getProperty(Constants.CONTEXT_ROOT);
		return context == null ? "" : StringUtils.isEmpty(root) ? context : context.replace(root, "");
		
	}

	/** 设置URI串 */
	public URLContext setContext(String context) {
		this.context = context;
		return this;
	}
	
	/**
	 * @return the special
	 */
	public String[] getSpecial() {
		return special;
	}
	
	public URLContext setSpecial(String[] special) {
		this.special = special;
		return this;
	}

	/** 获取参数列表 */
	public Map<String, Object> getParameter() {
		return parameter;
	}

	/** 设置参数列表 */
	public URLContext setParameter(Map<String, Object> parameter) {
		this.parameter = parameter;
		return this;
	}
	
	/** 过滤URI */
	public static final boolean filterURI(String uri) {
		if(!uri.startsWith(System.getProperty(Constants.CONTEXT_ROOT)) || ObjectCompare.isInList(uri, System.getProperty(Constants.CONTEXT_FILTER).split(";")))
			return false;
		
		return true;
		
	}
	
}
