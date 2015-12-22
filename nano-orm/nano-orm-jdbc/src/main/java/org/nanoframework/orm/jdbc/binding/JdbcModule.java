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
package org.nanoframework.orm.jdbc.binding;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.not;

import java.util.Collections;
import java.util.Map;

import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.JdbcAdapter;
import org.nanoframework.orm.jdbc.JdbcCreater;
import org.nanoframework.orm.jdbc.config.JdbcConfig;

import com.google.inject.AbstractModule;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:09:08
 */
@JdbcCreater
public class JdbcModule extends AbstractModule {

	private Map<String, JdbcConfig> configs;
	private PoolType poolType;
	
	public JdbcModule(Map<String, JdbcConfig> configs, PoolType poolType) {
		this.configs = configs == null ? Collections.emptyMap() : configs;
		this.poolType = poolType == null ? PoolType.DRUID : poolType;
	}
	
	@Override
	protected void configure() {
		JdbcAdapter.newInstance(configs.values(), poolType, this.getClass());
		
		JdbcTransactionalMethodInterceptor interceptor = new JdbcTransactionalMethodInterceptor();
        requestInjection(interceptor);
        bindInterceptor(any(), annotatedWith(JdbcTransactional.class), interceptor);
        bindInterceptor(annotatedWith(JdbcTransactional.class), not(annotatedWith(JdbcTransactional.class)), interceptor);
        
	}

}
