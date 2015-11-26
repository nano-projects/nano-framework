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
package org.nanoframework.examples.first.webapp.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.component.aop.IBefore;

/**
 * @author yanghe
 * @date 2015年10月14日 上午10:35:57
 */
public class ExamplesAOP implements IBefore {
	private Logger LOG = LoggerFactory.getLogger(ExamplesAOP.class);
	
	@Override
	public void before(MethodInvocation invocation) {
		if(LOG.isDebugEnabled()) {
			String params = StringUtils.join(invocation.getMethod().getParameters(), ", ");
			String args = StringUtils.join(invocation.getArguments(), ", ");
			LOG.debug("Before invoke method: " + invocation.getThis().getClass().getName() + "." + invocation.getMethod().getName() + "("+ (params == null ? "" : params) +"):: [" + (args == null ? "" : args) + "]");
		}
	}
}
