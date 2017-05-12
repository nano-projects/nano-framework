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
package org.nanoframework.extension.shiro.client.web.component;

import java.util.Map;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.extension.shiro.client.web.component.impl.AuthenticationComponentImpl;
import org.nanoframework.web.server.http.status.ResultMap;

import com.google.inject.ImplementedBy;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
@Component
@ImplementedBy(AuthenticationComponentImpl.class)
@RequestMapping("/sso/v1")
public interface AuthenticationComponent {
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    Map<String, Object> findUserInfo();
    
    @RequestMapping("/logout")
    ResultMap logout();
}
