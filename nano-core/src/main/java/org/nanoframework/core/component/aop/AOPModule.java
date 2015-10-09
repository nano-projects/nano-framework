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
package org.nanoframework.core.component.aop;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * @author yanghe
 * @date 2015年10月8日 下午5:21:03
 */
public class AOPModule extends AbstractModule {

	@Override
	protected void configure() {
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Before.class), new BeforeInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(After.class), new AfterInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(BeforeAndAfter.class), new BeforeAndAfterInterceptor());
	}
}
