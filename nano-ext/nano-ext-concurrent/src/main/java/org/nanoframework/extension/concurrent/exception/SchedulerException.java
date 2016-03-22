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
package org.nanoframework.extension.concurrent.exception;

/**
 * 调度任务异常
 * 
 * @author yanghe
 * @date 2015年6月11日 下午2:59:02 
 *
 */
public class SchedulerException extends RuntimeException {
	private static final long serialVersionUID = -2775206802007728986L;

	public SchedulerException() {

	}
	
	public SchedulerException(String message) {
		super(message);
		
	}
	
	public SchedulerException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "调度任务异常: " + super.getMessage();
	}
	
}
