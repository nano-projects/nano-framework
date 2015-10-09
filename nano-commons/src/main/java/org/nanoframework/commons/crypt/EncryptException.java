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
package org.nanoframework.commons.crypt;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:00:11
 */
public class EncryptException extends RuntimeException {
	private static final long serialVersionUID = 5713797904458714409L;

	public EncryptException() {

	}
	
	public EncryptException(String message) {
		super(message);
		
	}
	
	public EncryptException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "加密异常: " + super.getMessage();
	}
	
}
