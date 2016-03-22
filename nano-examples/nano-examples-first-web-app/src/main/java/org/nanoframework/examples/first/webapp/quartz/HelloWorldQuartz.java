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
package org.nanoframework.examples.first.webapp.quartz;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.concurrent.exception.QuartzException;
import org.nanoframework.extension.concurrent.quartz.BaseQuartz;
import org.nanoframework.extension.concurrent.quartz.Quartz;

/**
 * @author yanghe
 * @date 2015年12月28日 下午4:00:33
 */
@Deprecated
@Quartz(name = "HelloWorldQuartz", parallel = 1)
public class HelloWorldQuartz extends BaseQuartz {
	private Logger LOG = LoggerFactory.getLogger(HelloWorldQuartz.class);
	
	@Override
	public void before() throws QuartzException {
		
	}

	@Override
	public void execute() throws QuartzException {
		LOG.debug("Hello world, " + System.currentTimeMillis());
		thisWait(5000);
	}

	@Override
	public void after() throws QuartzException {

	}

	@Override
	public void destroy() throws QuartzException {

	}

}
