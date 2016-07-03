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
package org.nanoframework.web.server.mvc;

import java.util.Map;

/**
 * 模型接口
 * 
 * @author yanghe
 * @date 2015年6月23日 下午2:34:10 
 *
 */
public interface Model {

	/**
	 * 添加属性至映射表
	 * @param attributeName 属性名
	 * @param attributeValue 属性值
	 * @return Model
	 */
	Model addAttribute(String attributeName, Object attributeValue);

	/**
	 * 批量添加属性至映射表
	 * @param attributes 属性集合
	 * @return Model
	 */
	Model addAllAttributes(Map<String, Object> attributes);

	/**
	 * 验证属性是否存在
	 * @param attributeName 属性名
	 * @return
	 */
	boolean containsAttribute(String attributeName);

	/**
	 * 返回模型属性映射表
	 * @return Map<String, Object>
	 */
	Map<String, Object> get();

	
}
