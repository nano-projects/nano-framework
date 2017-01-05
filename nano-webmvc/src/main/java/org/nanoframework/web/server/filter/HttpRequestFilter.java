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
package org.nanoframework.web.server.filter;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.ContentType;
import org.nanoframework.core.component.Components;
import org.nanoframework.core.component.exception.BindRequestParamException;
import org.nanoframework.core.component.exception.ComponentInvokeException;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.Routes;
import org.nanoframework.core.context.URLContext;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.support.RedirectModel;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
/**
 * Http请求拦截器 <br>
 * 此拦截器为NanoFramework框架的请求主入口 <br>
 * 如果调用的URI存在与组件映射表中的话，则可以调用组件的服务。<br>
 * 如果不存在的话，则请求后续的内容.
 * 
 * @author yanghe
 * @since 1.0 
 */
public class HttpRequestFilter extends AbstractFilter {
	private Logger logger = LoggerFactory.getLogger(HttpRequestFilter.class);
	
	@Override
	protected boolean invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		URLContext urlContext = create((HttpServletRequest) request);
		String method = ((HttpServletRequest) request).getMethod();
		RequestMapper mapper = Routes.route().lookup(urlContext.getNoRootContext(), RequestMethod.valueOf(method));
		
		Writer out = null;
		if(mapper != null) {
			try {
				if(!validRequestMethod(response, out, mapper, method)) {
					return false;
				}
				
				Model model = new RedirectModel();
				HttpContext.set(ImmutableMap.<Class<?>, Object> builder()
				        .put(HttpServletRequest.class, request)
		                .put(HttpServletResponse.class, response)
		                .put(Model.class, model)
		                .put(URLContext.class, urlContext)
		                .build());
				
				Object ret = Components.invoke(mapper, urlContext.getParameter(), request, response, model, urlContext);
				process(request, response, out, urlContext, ret, model);
			} catch(ComponentInvokeException | BindRequestParamException | IOException | ServletException e) {
				logger.error(e.getMessage(), e);
				response.setContentType(ContentType.APPLICATION_JSON);
				if(out == null) {
				    out = response.getWriter();
				}
				
				out.write(JSON.toJSONString(error(e)));
			} finally {
				if(out != null) {
					out.flush();
					out.close();
				}
				
				HttpContext.clear();
			}
			
			return false;
		} 
		
		return true;
	}
	
	/**
	 *
	 * @author yanghe
	 * @since 1.3.5
	 */
	public static class HttpContext {
	    private static ThreadLocal<Map<Class<?>, Object>> CONTEXT = new ThreadLocal<>();
	    protected static void set(Map<Class<?>, Object> context) {
	        clear();
	        CONTEXT.set(context);
	    }
	    
	    protected static void clear() {
	        Map<Class<?>, Object> ctx;
	        if((ctx = CONTEXT.get()) != null) {
                ctx.clear();
                CONTEXT.remove();
            }
	    }
	    
	    @SuppressWarnings("unchecked")
        public static <T> T get(Class<T> type) {
	        Assert.notNull(type, "类型不能为空");
	        
	        Map<Class<?>, Object> context = CONTEXT.get();
	        if(context != null) {
	            return (T) context.get(type);
	        }
	        
	        throw new NullPointerException("未设置Class: " + type.getName());
	    }
	}
	
}
