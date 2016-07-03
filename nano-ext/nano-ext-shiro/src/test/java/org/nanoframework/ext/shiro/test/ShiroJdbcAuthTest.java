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
package org.nanoframework.ext.shiro.test;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.core.plugins.Configure;
import org.nanoframework.core.plugins.Plugin;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.DefaultPluginLoader;
import org.nanoframework.core.plugins.defaults.plugin.ShiroPlugin;

/**
 * @author yanghe
 * @date 2015年12月8日 下午10:17:33
 */
public class ShiroJdbcAuthTest {
	private Logger LOG = LoggerFactory.getLogger(ShiroJdbcAuthTest.class);
	
	@Ignore
	@Before
	public void before() {
		PluginLoader loader = new DefaultPluginLoader() {
			@Override
			protected void configPlugin(Configure<Plugin> plugins) {
				super.configPlugin(plugins);
				plugins.add(new ShiroPlugin());
			}
		};
		
		ServletConfig config = new ServletConfig() {
			private Map<String, String> map = MapBuilder.<String, String> create()
			        .put("context", "/context.properties")
			        .put("shiro-ini", "classpath:shiro-jdbc.ini")
			        .build();
			
			@Override
			public String getServletName() {
				return null;
			}

			@Override
			public ServletContext getServletContext() {
				return null;
			}

			@Override
			public String getInitParameter(String name) {
				return map.get(name);
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				return null;
			}
			
		};
		
		loader.init(config);
	}
	
	@Ignore
	@Test
	public void test0() {
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken("yanghe", "123456");
		
		try {
			subject.login(token);
			LOG.debug("Session id: " + subject.getSession().getId());
		} catch(AuthenticationException e) {
			LOG.error("Authentication Invalid: " + e.getMessage());
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		Assert.assertEquals(true, subject.hasRole("SYS_ADMIN"));

		try {
			/** 验证permission，不存在时抛出AuthorizationException异常 */
			subject.checkPermission("302");
		} catch(AuthorizationException e) {
			LOG.error("Check permission error: " + e.getMessage());
		}
		
		try {
			/** 验证Role, 有一个不存在时抛出AuthorizationException异常 */
			subject.checkRoles("SYS_ADMIN", "STAFF");
		} catch(AuthorizationException e) {
			LOG.error("Check roles error: " + e.getMessage());
		}
		
		/** 验证permission，返回bool结果 */
		Assert.assertEquals(true, subject.isPermitted("302"));
		
		subject.logout();
	}
}
