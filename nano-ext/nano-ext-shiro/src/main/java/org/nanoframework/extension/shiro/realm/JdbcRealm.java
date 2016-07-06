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
package org.nanoframework.extension.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;

/**
 * @author yanghe
 * @since 1.2
 */
public class JdbcRealm extends org.apache.shiro.realm.jdbc.JdbcRealm {
    protected String dataSourceName;
    protected RealmQuery realmQuery;
    
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
		this.dataSource = GlobalJdbcManager.get(dataSourceName).getDataSource();
	}
	
	public void setRealmQuery(RealmQuery realmQuery) {
		this.realmQuery = realmQuery;
		if(StringUtils.isNotBlank(realmQuery.getAuthenticationQuery())) {
			setAuthenticationQuery(realmQuery.getAuthenticationQuery());
		}
		
		if(StringUtils.isNotBlank(realmQuery.getUserRolesQuery())) {
			setUserRolesQuery(realmQuery.getUserRolesQuery());
		}
		
		if(StringUtils.isNotBlank(realmQuery.getPermissionsQuery())) {
			setPermissionsQuery(realmQuery.getPermissionsQuery());
		}
	}
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if(dataSource == null) {
			dataSource = GlobalJdbcManager.get(dataSourceName).getDataSource();
		}
		
		return super.doGetAuthenticationInfo(token);
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if(dataSource == null) {
			dataSource = GlobalJdbcManager.get(dataSourceName).getDataSource();
		}
		
		return super.doGetAuthorizationInfo(principals);
	}
	
	@Override
    protected String getSaltForUser(String username) {
    	return "Nano Framework Extension Shiro Salt for user: [" + username + ']';
    }
	
	public void setSaltStyle(String saltStyle) {
        this.saltStyle = SaltStyle.valueOf(saltStyle);
        if (this.saltStyle == SaltStyle.COLUMN && authenticationQuery.equals(DEFAULT_AUTHENTICATION_QUERY)) {
            authenticationQuery = DEFAULT_SALTED_AUTHENTICATION_QUERY;
        }
    }

	@Override
	public AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
	    return super.getAuthorizationInfo(principals);
	}
}
