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
package org.nanoframework.orm.jdbc;

/**
 * 数据源处理异常
 * 
 * @author yanghe
 * @date 2015年7月21日 下午9:44:00 
 *
 */
public class DataSourceException extends RuntimeException {
	private static final long serialVersionUID = -4463455332449664304L;

	public DataSourceException() {

	}

	public DataSourceException(String message) {
		super(message);

	}

	public DataSourceException(String message, Throwable cause) {
		super(message, cause);

	}
	
	@Override
	public String getMessage() {
		return "数据源配置异常: " + super.getMessage();
	}
}
