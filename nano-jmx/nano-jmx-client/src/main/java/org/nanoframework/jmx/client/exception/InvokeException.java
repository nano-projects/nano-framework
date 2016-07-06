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
package org.nanoframework.jmx.client.exception;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class InvokeException extends MXBeanException {
	private static final long serialVersionUID = 7363552375905030809L;

	public InvokeException() {

	}

	public InvokeException(String message) {
		super(message);
	}
	
	public InvokeException(Throwable cause) {
		super(cause);
	}

	public InvokeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	@Override
	public String getMessage() {
		return "调用方法异常: " + super.getMessage();
	}
}
