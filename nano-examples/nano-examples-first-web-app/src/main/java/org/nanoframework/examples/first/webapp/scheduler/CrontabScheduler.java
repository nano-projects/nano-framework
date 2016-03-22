/**
 * Copyright 2015-2016 the original author or authors.
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
package org.nanoframework.examples.first.webapp.scheduler;

import org.nanoframework.commons.format.DateFormat;
import org.nanoframework.commons.format.Pattern;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.concurrent.scheduler.BaseScheduler;
import org.nanoframework.extension.concurrent.scheduler.Scheduler;

/**
 * @author yanghe
 * @date 2016年03月22日 下午5:01:35
 */
@Scheduler(beforeAfterOnly = true, cron = "*/5 * * * * ?", parallel = 1, daemon = true)
public class CrontabScheduler extends BaseScheduler {

	private Logger LOG = LoggerFactory.getLogger(CrontabScheduler.class);
	
	@Override
	public void before() {

	}

	@Override
	public void execute() {
		LOG.debug("当前时间(秒级控制): " + DateFormat.format(System.currentTimeMillis(), Pattern.TIMESTAMP));
	}

	@Override
	public void after() {

	}

	@Override
	public void destroy() {

	}

}
