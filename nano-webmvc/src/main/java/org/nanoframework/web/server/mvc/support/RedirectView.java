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
package org.nanoframework.web.server.mvc.support;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.util.Constants;
import org.nanoframework.web.server.mvc.View;

/**
 * 视图实现
 * @author yanghe
 * @date 2015年6月23日 下午2:35:26 
 *
 */
public class RedirectView implements View {

	/** 跳转到前端的页面URI，主路径("/")为webRoot */
	private String page;
	
	public RedirectView(String page) {
		this.page = page;
	}
	
	@Override
	public void redirect(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if(response == null || request == null)
			return ;
		
		if(model != null && model.size() > 0) {
			model.forEach((name, o) -> request.setAttribute(name, o));
		}
		
		String encodedRedirectURL = response.encodeRedirectURL(page);
		String root;
		if(!encodedRedirectURL.startsWith(root = System.getProperty(Constants.CONTEXT_ROOT)))
			encodedRedirectURL = root + encodedRedirectURL;
		
		/** HttpServletResponse.sendRedirect */
		response.sendRedirect(encodedRedirectURL);
			
	}

	public String getPage() {
		return page;
	}

}
