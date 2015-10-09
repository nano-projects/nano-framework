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
package org.nanoframework.server.quartz;

import java.sql.Timestamp;

import org.nanoframework.commons.format.ClassCast;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.core.component.exception.ServiceInvokeException;
import org.nanoframework.extension.concurrent.exception.QuartzException;
import org.nanoframework.extension.concurrent.quartz.BaseQuartz;
import org.nanoframework.extension.concurrent.quartz.Quartz;
import org.nanoframework.server.service.JdbcTestService;

import com.google.inject.Inject;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:16:05
 */
@Quartz(name = "TestQuartz", beforeAfterOnly = true, interval = 5000, parallel = 0)
public class TestQuartz extends BaseQuartz {

	@Inject
	private JdbcTestService jdbcTestService;
	
	private int timeRule = (Integer) ClassCast.cast(System.getProperty("recover.time.rule"), Integer.class.getName());
	private long calcTime = timeRule * 60 * 60 * 1000;
	private Timestamp time;
	
	private int limit = (Integer) ClassCast.cast(System.getProperty("recover.exchange.limit"), Integer.class.getName());
	
	@Override
	public void before() throws QuartzException {
		time = new Timestamp(System.currentTimeMillis() - calcTime);
	}

	@Override
	public void execute() throws QuartzException {
		Assert.notNull(timeRule);
		Assert.notNull(limit);
		
		try {
			while(jdbcTestService.exchanged(time, limit) > 0) ;
			
		} catch(ServiceInvokeException e) {
			throw new QuartzException(e.getMessage(), e);
			
		} finally {
			time.setTime(System.currentTimeMillis() - calcTime);
		}
		
	}

	@Override
	public void after() throws QuartzException {
		
	}

	@Override
	public void destroy() throws QuartzException {
		jdbcTestService = null;
		
	}

}
