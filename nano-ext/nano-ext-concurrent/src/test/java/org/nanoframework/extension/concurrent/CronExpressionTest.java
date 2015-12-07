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
package org.nanoframework.extension.concurrent;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;
import org.nanoframework.extension.concurrent.quartz.CronExpression;

/**
 * @author yanghe
 * @date 2015年12月3日 下午5:25:10
 */
public class CronExpressionTest {

	@Test
	public void test0() throws ParseException {
		CronExpression cron = new CronExpression("* * * * * ?");
		Date date = cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
		System.out.println("Valid time after: " + date.toString());
		
		date = cron.getNextValidTimeAfter(date);
		System.out.println("Valid time after: " + date.toString());
		
		date = cron.getNextValidTimeAfter(date);
		System.out.println("Valid time after: " + date.toString());
		
		date = cron.getNextValidTimeAfter(date);
		System.out.println("Valid time after: " + date.toString());
	}
	
}
