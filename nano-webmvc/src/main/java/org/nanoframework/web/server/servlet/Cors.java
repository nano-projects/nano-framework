/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.web.server.servlet;

/**
 * @author yanghe
 * @date 2016年2月3日 下午8:45:18
 */
public class Cors {
	public static final String ALLOW_ENABLE = "context.cors.allow.enable";
	public static final String ALLOW_ORIGIN = "context.cors.allow.origin";
	public static final String ALLOW_METHODS = "context.cors.allow.methods";
	public static final String ALLOW_CREDENTIALS = "context.cors.allow.credentials";
	public static final String ALLOW_HEADERS = "context.cors.allow.headers";
	public static final String EXPOSE_HEADERS = "context.cors.expose.headers";
	
	public static final String REQUEST_HEADERS_ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
	public static final String REQUEST_HEADERS_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
	public static final String REQUEST_HEADERS_ORIGIN = "Origin";
	
	/** 允许访问的客户端域名，例如：http://web.xxx.com，若为*，则表示从任意域都能访问，即不做任何限制 */
	public static final String RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	
	/** 允许访问的方法名，多个方法名用逗号分割，例如：GET,POST,PUT,DELETE,OPTIONS */
	public static final String RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	
	/** 是否允许请求带有验证信息，若要获取客户端域下的cookie时，需要将其设置为true */
	public static final String RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
	
	/** 允许服务端访问的客户端请求头，多个请求头用逗号分割，例如：Content-Type,Authorization */
	public static final String RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	
	/** 允许客户端访问的服务端响应头，多个响应头用逗号分割 */
	public static final String RESPONSE_HEADERS_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
	
	public static final String ALL_ALLOW = "*";
	
	public final boolean allowEnable;
	public final String[] allowOrigin;
	public final String allowMethod;
	public final String[] allowMethods;
	public final String allowCredentials;
	public final String allowHeader;
	public final String[] allowHeaders;
	public final String exposeHeader;
	public final String[] exposeHeaders;
	
	private static Cors INSTANCE;
	private static final Object LOCK = new Object();
	
	private Cors() {
		allowEnable = Boolean.parseBoolean(System.getProperty(ALLOW_ENABLE, "false"));
		allowOrigin = System.getProperty(ALLOW_ORIGIN, "").toLowerCase().split(",");
		allowMethod = System.getProperty(ALLOW_METHODS, "").toLowerCase();
		allowMethods = allowMethod.split(",");
		allowCredentials = System.getProperty(ALLOW_CREDENTIALS, "").toLowerCase();
		allowHeader = System.getProperty(ALLOW_HEADERS, "").toLowerCase();
		allowHeaders = allowHeader.split(",");
		exposeHeader = System.getProperty(EXPOSE_HEADERS, "").toLowerCase();
		exposeHeaders = exposeHeader.split(",");
		
	}
	
	public static Cors get() {
		if(INSTANCE == null) {
			synchronized (LOCK) {
				if(INSTANCE == null)
					INSTANCE = new Cors();
			}
		}
		
		return INSTANCE;
	}

}
