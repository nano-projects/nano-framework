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
package org.nanoframework.web.server.http.status;

/**
 * 组件服务调用状态码
 * 
 * @author yanghe
 * @date 2015年7月25日 下午9:08:21 
 *
 */
public interface ComponentStatus {

	/** 组件调用异常 */
	final int INVOKE_ERROR_CODE = 9099;
	/** 组件不存在异常 */
	final int NOT_FOUND_CODE = 9098;
	/** 绑定参数异常 */
	final int BIND_PARAM_EXCEPTION_CODE = 9097;
	/** IO异常 */
	final int IO_EXCEPTION_CODE = 9096;
	/** Servlet调用异常 */
	final int SERVLET_EXCEPTION = 9095;
	
	final int UNSUPPORT_REQUEST_METHOD_CODE = 9094;
	final String UNSUPPORT_REQUEST_METHOD_DESC = "Unsupport Request Method";
	
	final ResultMap UNKNOWN = ResultMap.create(9999, "未知的服务器调用异常", "Unknown");
//	final ResultMap INVOKE_ERROR = ResultMap.create(9099, throwable.getMessage(), throwable.getClass().getSimpleName());
	final ResultMap NOT_FOUND = ResultMap.create(NOT_FOUND_CODE, "组件服务不存在", "Not Found");
//	final ResultMap BIND_PARAM_EXCEPTION = ResultMap.create(9097, "", "");
//	final ResultMap IO_EXCEPTION = ResultMap.create(9096, "", "");
//	final ResultMap SERVLET_EXCEPTION = ResultMap.create(9095, "", "");
//	final ResultMap UNSUPPORT_REQUEST_METHOD = ResultMap.create(UNSUPPORT_REQUEST_METHOD_CODE, "不支持此请求类型，支持类型(GET / HEAD / POST / PUT / DELETE / OPTIONS / TRACE)", "Unsupport Request Method");
	
}
