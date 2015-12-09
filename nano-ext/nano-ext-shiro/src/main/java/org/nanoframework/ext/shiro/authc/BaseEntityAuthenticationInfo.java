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
package org.nanoframework.ext.shiro.authc;

import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.nanoframework.commons.entity.BaseEntity;

/**
 * @author yanghe
 * @date 2015年12月9日 上午10:02:41
 */
public class BaseEntityAuthenticationInfo extends SimpleAuthenticationInfo {
	private static final long serialVersionUID = -2579426208588141940L;

	protected BaseEntity info;
	
	public BaseEntityAuthenticationInfo() {
		
	}

	public BaseEntityAuthenticationInfo(Object principal, Object credentials, String realmName, BaseEntity info) {
		super(principal, credentials, realmName);
		this.info = info;
	}

	public BaseEntityAuthenticationInfo(Object principal, Object hashedCredentials, ByteSource credentialsSalt, String realmName, BaseEntity info) {
		super(principal, hashedCredentials, credentialsSalt, realmName);
		this.info = info;
	}

	public BaseEntityAuthenticationInfo(PrincipalCollection principals, Object credentials, BaseEntity info) {
		super(principals, credentials);
		this.info = info;
	}

	public BaseEntityAuthenticationInfo(PrincipalCollection principals, Object hashedCredentials, ByteSource credentialsSalt, BaseEntity info) {
		super(principals, hashedCredentials, credentialsSalt);
		this.info = info;
	}
	
	public BaseEntity getInfo() {
		return info;
	}
	
	public void setInfo(BaseEntity info) {
		this.info = info;
	}
}
