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
package org.nanoframework.ext.shiro;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MD5Utils;
import org.nanoframework.commons.util.ZipUtils;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.DefaultPluginLoader;

import junit.framework.Assert;

/**
 * @author yanghe
 * @date 2015年12月8日 下午10:17:33
 */
public class ShiroJdbcAuthTest {
	private Logger LOG = LoggerFactory.getLogger(ShiroJdbcAuthTest.class);
	
	@Before
	public void before() {
		PluginLoader loader = new DefaultPluginLoader();
		ServletConfig config = new ServletConfig() {
			private Map<String, String> map = new HashMap<String, String>() {
				private static final long serialVersionUID = -1228713388845687367L; {
				put("context", "/context.properties");
			}};
			
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
	
	@Test
	public void test0() {
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro-jdbc.ini");
		SecurityManager manager = factory.getInstance();
		SecurityUtils.setSecurityManager(manager);
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken("admin", MD5Utils.getMD5String(MD5Utils.getMD5String(ZipUtils.gzip("123456"))));
		
		try {
			subject.login(token);
		} catch(AuthenticationException e) {
			LOG.error("Authentication Invalid: " + e.getMessage());
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		
		subject.logout();
	}
}
