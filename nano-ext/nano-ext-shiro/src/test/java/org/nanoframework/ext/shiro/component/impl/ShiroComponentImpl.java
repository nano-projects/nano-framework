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
package org.nanoframework.ext.shiro.component.impl;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.nanoframework.ext.shiro.component.ShiroComponent;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;

/**
 * @author yanghe
 * @date 2015年12月9日 下午5:30:31
 */
public class ShiroComponentImpl implements ShiroComponent {

	@Override
	public View login(HttpServletRequest request, Model model) {
		String errorClassName = (String) request.getAttribute("shiroLoginFailure");

        if(UnknownAccountException.class.getName().equals(errorClassName)) {
            model.addAttribute("error", "用户名/密码错误");
        } else if(IncorrectCredentialsException.class.getName().equals(errorClassName)) {
        	model.addAttribute("error", "用户名/密码错误");
        } else if(errorClassName != null) {
        	model.addAttribute("error", "未知错误：" + errorClassName);
        }
		
		return new ForwardView("/pages/login.jsp", true);
	}
	
	@Override
	public Object hello(String name) {
		Subject subject = SecurityUtils.getSubject();
		
		return "Hello world, Shiro " + name + ", " + subject.getPrincipal() + ", Timeout at: " + subject.getSession().getTimeout() 
				+ "ms, Start Time: " + (new Timestamp(subject.getSession().getStartTimestamp().getTime())) 
				+ ", Last Access Time: " + (new Timestamp(subject.getSession().getLastAccessTime().getTime()));
	}

}
