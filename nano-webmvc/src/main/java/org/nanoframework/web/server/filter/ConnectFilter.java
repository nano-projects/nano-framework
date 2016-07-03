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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.ObjectCompare;
import org.nanoframework.core.context.ApplicationContext;
import org.nanoframework.web.server.http.status.HttpStatusCode;

/**
 * 请求拦截器 <br>
 * 如果在web.xml中配置了此拦截器，则会获取context.properties属性中的{ context.security.filter }
 * 和{ context.suffix.filter }作为过滤条件，符合此属性中的设置时可以调用后期的服务。
 * 如果配置这2个属性的内容，则拦截所有的http请求。<br>
 * 
 * @author yanghe
 * @date 2015年7月25日 下午8:32:08 
 *
 */
@Deprecated
public class ConnectFilter implements Filter {

	private Logger LOG = LoggerFactory.getLogger(ConnectFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String uri = ((HttpServletRequest) request).getRequestURI();
		String filter = System.getProperty(ApplicationContext.CONTEXT_SECURITY_FILTER);
		String[] filters = StringUtils.isEmpty(filter) ? new String[0] : filter.split(";");
		
		String suffix = System.getProperty(ApplicationContext.CONTEXT_SUFFIX_FILTER);
		String[] suffixs = StringUtils.isEmpty(suffix) ? new String[0] : suffix.split(";");
		try {
			if(ObjectCompare.isInListByRegEx(uri, filters) || ObjectCompare.isInEndWiths(uri, suffixs)) {
				chain.doFilter(request, response);
				return ;
			}
		} catch(Exception e) {
			LOG.error("匹配地址过滤异常: " + e.getMessage());
			((HttpServletResponse) response).sendError(HttpStatusCode.SC_FORBIDDEN, "拒绝请求");
			return ;
		}
		
		((HttpServletResponse) response).sendError(HttpStatusCode.SC_FORBIDDEN, "拒绝请求");
	}

	@Override
	public void destroy() {

	}

}
