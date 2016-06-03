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
package org.nanoframework.extension.shiro.web.service.impl;

import java.util.Collection;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.nanoframework.extension.shiro.realm.JdbcRealm;
import org.nanoframework.extension.shiro.web.exception.MultiRealmException;
import org.nanoframework.extension.shiro.web.service.RealmService;

import com.google.inject.Singleton;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
@Singleton
public class RealmServiceImpl implements RealmService {

    @Override
    public AuthorizationInfo getAuthorizationInfo() {
        return getAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }
    
    @Override
    public AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        final Realm realm = getRealm();
        if(realm instanceof JdbcRealm) {
            return ((JdbcRealm) realm).getAuthorizationInfo(principals);
        }
        
        throw new IllegalArgumentException("Realm is not instanceof " + JdbcRealm.class.getName());
    }
    
    protected Realm getRealm() {
        final SecurityManager securityManager = SecurityUtils.getSecurityManager();
        if(securityManager instanceof RealmSecurityManager) {
            Collection<Realm> realms = ((RealmSecurityManager) securityManager).getRealms();
            if(!CollectionUtils.isEmpty(realms) && realms.size() == 1) {
                return realms.iterator().next();
            }
        }
        
        throw new MultiRealmException();
    }
}
