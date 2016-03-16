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
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.component.aop.IBefore;

/**
 * @author yanghe
 * @date 2015年10月14日 上午10:35:57
 */
public class Examples2AOP implements IBefore {
	private Logger LOG = LoggerFactory.getLogger(Examples2AOP.class);
	
	@Override
	public void before(MethodInvocation invocation) {
		LOG.debug("Before invoke by Examples2 AOP.");
	}
}
