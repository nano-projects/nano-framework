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
package org.nanoframework.web.server.filter;

import static org.nanoframework.core.status.ComponentStatus.BIND_PARAM_EXCEPTION_CODE;
import static org.nanoframework.core.status.ComponentStatus.INVOKE_ERROR_CODE;
import static org.nanoframework.core.status.ComponentStatus.IO_EXCEPTION_CODE;
import static org.nanoframework.core.status.ComponentStatus.SERVLET_EXCEPTION;
import static org.nanoframework.core.status.ComponentStatus.UNKNOWN;
import static org.nanoframework.core.status.ComponentStatus.UNSUPPORT_REQUEST_METHOD_CODE;
import static org.nanoframework.core.status.ComponentStatus.UNSUPPORT_REQUEST_METHOD_DESC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.format.StringFormat;
import org.nanoframework.commons.util.Charset;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.commons.util.ContentType;
import org.nanoframework.commons.util.ObjectUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.commons.util.URLContext;
import org.nanoframework.core.component.exception.BindRequestParamException;
import org.nanoframework.core.component.exception.ComponentInvokeException;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.status.ResultMap;
import org.nanoframework.web.server.http.status.Response;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author yanghe
 * @date 2016年2月3日 下午8:15:02
 */
public abstract class AbstractFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding(Charset.UTF8.value());
		response.setCharacterEncoding(Charset.UTF8.value());
		
		if(allowResponse((HttpServletRequest) request, (HttpServletResponse) response, chain) && 
				invoke((HttpServletRequest) request, (HttpServletResponse) response)) {
			chain.doFilter(request, response);
		}
	}
	
	protected abstract boolean invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
	protected abstract boolean allowResponse(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;
	
	@Override
	public void destroy() {

	}
	
	protected boolean validRequestMethod(ServletResponse response, Writer out, RequestMapper mapper, String _method) throws IOException {
		if(!mapper.hasMethod(RequestMethod.valueOf(_method))) {
			response.setContentType(ContentType.APPLICATION_JSON);
			out = response.getWriter();
			ResultMap resultMap = ResultMap.create(UNSUPPORT_REQUEST_METHOD_CODE, "不支持此请求类型("+_method+")，仅支持类型("+StringUtils.join(mapper.getRequestMethodStrs(), " / ")+")", UNSUPPORT_REQUEST_METHOD_DESC);
			out.write(JSON.toJSONString(resultMap));
			return false;
		}
		
		return true;
	}
	
	protected void process(ServletRequest request, ServletResponse response, Writer out, URLContext urlContext, Object ret, Model model) throws IOException, ServletException {
		if(ret instanceof View) {
			((View) ret).redirect(model.get(), (HttpServletRequest) request, (HttpServletResponse) response);
		} else if(ret instanceof String) {
			response.setContentType(ContentType.APPLICATION_JSON);
			out = response.getWriter();
			out.write((String) ret);
		} else if(ret instanceof Response && ret == Response.EMPTY) {
			return ;
		} else if(ret != null) {
			response.setContentType(ContentType.APPLICATION_JSON);
			out = response.getWriter();
			/** 跨域JSONP的Ajax请求支持 */
			Object callback;
			if(!ObjectUtils.isEmpty(callback = urlContext.getParameter().get(Constants.CALLBACK)))
				out.write(callback + "(" + JSON.toJSONString(ret, SerializerFeature.WriteDateUseDateFormat) + ")");
			else 
				out.write(JSON.toJSONString(ret, SerializerFeature.WriteDateUseDateFormat));
			
		} else {
			response.setContentType(ContentType.APPLICATION_JSON);
			out = response.getWriter();
			out.write(JSON.toJSONString(UNKNOWN));
		}
	}
	
	protected URLContext create(HttpServletRequest request) throws IOException {
		Map<String, Object> parameter = new HashMap<>();
		request.getParameterMap().forEach((key, value) -> { 
			if(value.length > 0) {
				if(key.endsWith("[]"))
					parameter.put(key.toLowerCase(), value);
				else 
					parameter.put(key.toLowerCase(), value[0]);
				
			}
		});
		
		/** 增加容器对其他Http请求类型获取参数的支持 */
		String paramString = "";
		switch(RequestMethod.valueOf(request.getMethod())) {
			case PUT:
			case DELETE:
			case HEAD:
			case OPTIONS:
			case TRACE:
			case PATCH:
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) 
					builder.append(line);
				
				paramString = builder.toString();
				break;
			default:
				break;
		}
		
		String uri = URLDecoder.decode(((HttpServletRequest) request).getRequestURI(), Charset.UTF8.value());
		URLContext urlContext;
		if(StringUtils.isNotBlank(paramString)) {
			uri += "?" + paramString;
			urlContext = StringFormat.formatURL(uri);
			urlContext.getParameter().putAll(parameter);
		} else 
			urlContext = URLContext.create().setContext(uri).setParameter(parameter);
		
		String[] uris = uri.split(";");
		if(uris.length > 1) {
			urlContext.setContext(uris[0]);
			String[] specials = new String[uris.length - 1];
			System.arraycopy(uris, 1, specials, 0, specials.length);
			urlContext.setSpecial(specials);
		}
		
		return urlContext;
	}
	
	protected ResultMap error(Throwable e) {
		ResultMap error = null;
		if(e instanceof ComponentInvokeException) {
			error = ResultMap.create(INVOKE_ERROR_CODE, e.getMessage(), "ComponentInvokeException");
		} else if(e instanceof BindRequestParamException) {
			error = ResultMap.create(BIND_PARAM_EXCEPTION_CODE, e.getMessage(), "BindRequestParamException");
		} else if(e instanceof IOException) {
			error = ResultMap.create(IO_EXCEPTION_CODE, e.getMessage(), "IOException");
		} else if(e instanceof ServletException) {
			error = ResultMap.create(SERVLET_EXCEPTION, e.getMessage(), "ServletException");
		} else 
			error = UNKNOWN;
		
		return error;
	}

}
