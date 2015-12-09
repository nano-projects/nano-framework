/**
 * Copyright 2015 the original author or authors.
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
package org.nanoframework.ext.shiro.component.impl;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.component.exception.ComponentInvokeException;
import org.nanoframework.ext.shiro.component.ShiroComponent;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;
import org.nanoframework.web.server.mvc.support.RedirectView;

/**
 * @author yanghe
 * @date 2015年12月9日 下午5:30:31
 */
public class ShiroComponentImpl implements ShiroComponent {

	private Logger LOG = LoggerFactory.getLogger(ShiroComponentImpl.class);
	
	@Override
	public View login(String username, String password, Model model) {
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		
		try {
			subject.login(token);
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
			throw new ComponentInvokeException("登陆失败");
		}
		
		if(!subject.isAuthenticated()) {
			model.addAttribute("status", 400);
			model.addAttribute("message", "登陆验证失败");
			model.addAttribute("info", "ERROR");
			return new ForwardView("/pages/login.jsp");
		}
		
		subject.getSession().setAttribute("username", username);
		
		return new RedirectView("/index.jsp");
	}
	
	@Override
	public Object hello(String name) {
		Subject subject = SecurityUtils.getSubject();
		return "Hello world, Shiro " + name + ", " + subject.getSession().getAttribute("username");
	}

}
