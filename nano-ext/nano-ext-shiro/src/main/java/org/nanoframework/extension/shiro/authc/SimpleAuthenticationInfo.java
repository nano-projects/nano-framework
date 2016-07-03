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
package org.nanoframework.extension.shiro.authc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.MergableAuthenticationInfo;
import org.apache.shiro.subject.MutablePrincipalCollection;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.nanoframework.extension.shiro.util.ByteSource;

/**
 *
 * @author yanghe
 * @since 1.2
 */
public class SimpleAuthenticationInfo implements MergableAuthenticationInfo, SaltedAuthenticationInfo {
	private static final long serialVersionUID = -393433691869611317L;

	protected PrincipalCollection principals;
	protected Object credentials;
	protected ByteSource credentialsSalt;

	public SimpleAuthenticationInfo() {

	}

	public SimpleAuthenticationInfo(Object principal, Object credentials, String realmName) {
		this.principals = new SimplePrincipalCollection(principal, realmName);
		this.credentials = credentials;
	}

	public SimpleAuthenticationInfo(Object principal, Object hashedCredentials, ByteSource credentialsSalt, String realmName) {
		this.principals = new SimplePrincipalCollection(principal, realmName);
		this.credentials = hashedCredentials;
		this.credentialsSalt = credentialsSalt;
	}

	public SimpleAuthenticationInfo(PrincipalCollection principals, Object credentials) {
		this.principals = principals;
		this.credentials = credentials;
	}

	public SimpleAuthenticationInfo(PrincipalCollection principals, Object hashedCredentials, ByteSource credentialsSalt) {
		this.principals = new SimplePrincipalCollection(principals);
		this.credentials = hashedCredentials;
		this.credentialsSalt = credentialsSalt;
	}

	public PrincipalCollection getPrincipals() {
		return principals;
	}

	public void setPrincipals(PrincipalCollection principals) {
		this.principals = principals;
	}

	public Object getCredentials() {
		return credentials;
	}

	public void setCredentials(Object credentials) {
		this.credentials = credentials;
	}

	public ByteSource getCredentialsSalt() {
		return credentialsSalt;
	}

	public void setCredentialsSalt(ByteSource salt) {
		this.credentialsSalt = salt;
	}

	@SuppressWarnings("unchecked")
	public void merge(AuthenticationInfo info) {
		if (info == null || info.getPrincipals() == null || info.getPrincipals().isEmpty()) {
			return;
		}

		if (this.principals == null) {
			this.principals = info.getPrincipals();
		} else {
			if (!(this.principals instanceof MutablePrincipalCollection)) {
				this.principals = new SimplePrincipalCollection(this.principals);
			}
			((MutablePrincipalCollection) this.principals).addAll(info.getPrincipals());
		}

		if (this.credentialsSalt == null && info instanceof SaltedAuthenticationInfo) {
			this.credentialsSalt = ((SaltedAuthenticationInfo) info).getCredentialsSalt();
		}

		Object thisCredentials = getCredentials();
		Object otherCredentials = info.getCredentials();

		if (otherCredentials == null) {
			return;
		}

		if (thisCredentials == null) {
			this.credentials = otherCredentials;
			return;
		}

		if (!(thisCredentials instanceof Collection)) {
			Set<Object> newSet = new HashSet<>();
			newSet.add(thisCredentials);
			setCredentials(newSet);
		}

		// At this point, the credentials should be a collection
		Collection<Object> credentialCollection = (Collection<Object>) getCredentials();
		if (otherCredentials instanceof Collection) {
			credentialCollection.addAll((Collection<Object>) otherCredentials);
		} else {
			credentialCollection.add(otherCredentials);
		}
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SimpleAuthenticationInfo))
			return false;

		SimpleAuthenticationInfo that = (SimpleAuthenticationInfo) o;

		// noinspection RedundantIfStatement
		if (principals != null ? !principals.equals(that.principals) : that.principals != null)
			return false;

		return true;
	}

	public int hashCode() {
		return (principals != null ? principals.hashCode() : 0);
	}

	public String toString() {
		return principals.toString();
	}

}
