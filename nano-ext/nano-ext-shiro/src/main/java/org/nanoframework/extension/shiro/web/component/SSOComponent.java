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
package org.nanoframework.extension.shiro.web.component;

import java.util.Map;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.RequestParam;
import org.nanoframework.extension.shiro.web.component.impl.SSOComponentImpl;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.web.server.http.status.ResultMap;
import org.nanoframework.web.server.mvc.View;

import com.google.inject.ImplementedBy;

/**
 *
 * @author yanghe
 * @since 1.3
 */
@Component
@ImplementedBy(SSOComponentImpl.class)
@RequestMapping("/sso/v1")
public interface SSOComponent {

    String DEFAULT_SHIRO_REDIS_CLIENT_NAME = "shiro";
    String DEFAULT_ERROR_RETRY = "3";
    String DEFAULT_SHIRO_CLIENT_SESSION_PREFIX = "SHIRO_CLIENT_SESSION_";
    String DEFAULT_SHIRO_SESSION_LISTENER_PREFIX = "SHIRO_SESSION_LISTENER_";

    String SHIRO_REDIS_CLIENT_PROPERTY = "context.sso.shiro.client.redis.name";
    String SHIRO_CLIENT_SESSION_PREFIX_PROPERTY = "context.sso.shiro.client.session.prefix";
    String ERROR_RETRY_PROPERTY = "context.sso.error.retry";
    String SHIRO_SESSION_LISTENER_PREFIX_PROPERTY = "context.sso.shiro.session.listener.prefix";

    RedisClient SHIRO = GlobalRedisClient.get(System.getProperty(SHIRO_REDIS_CLIENT_PROPERTY, DEFAULT_SHIRO_REDIS_CLIENT_NAME));
    String SHIRO_CLIENT_SESSION_PREFIX = System.getProperty(SHIRO_CLIENT_SESSION_PREFIX_PROPERTY, DEFAULT_SHIRO_CLIENT_SESSION_PREFIX);
    int ERROR_RETRY = Integer.parseInt(System.getProperty(ERROR_RETRY_PROPERTY, DEFAULT_ERROR_RETRY));
    String SHIRO_SESSION_LISTENER_PREFIX = System.getProperty(SHIRO_SESSION_LISTENER_PREFIX_PROPERTY, DEFAULT_SHIRO_SESSION_LISTENER_PREFIX);

    @RequestMapping(value = "/session/{clientSessionId}", method = RequestMethod.GET)
    String getSession(@PathVariable("clientSessionId") String clientSessionId);

    @RequestMapping(value = "/session/{clientSessionId}", method = RequestMethod.POST)
    String registrySession(@PathVariable("clientSessionId") String clientSessionId, @RequestParam("ticket") String serverEncryptSessionId);

    @RequestMapping(value = "/session/{clientSessionId}", method = RequestMethod.DELETE)
    ResultMap removeSession(@PathVariable("clientSessionId") String clientSessionId);
    
    @RequestMapping(value = "/session/{clientSessionId}/attribute", method = RequestMethod.POST)
    ResultMap syncSessionAttribute(@PathVariable("clientSessionId") String clientSessionId, @RequestParam("attribute") String serialAttribute);
    
    @RequestMapping(value = "/session/{clientSessionId}/max.inactive.internal", method = RequestMethod.POST)
    ResultMap syncSessionMaxInactiveInternal(@PathVariable("clientSessionid") String clientSessionId, @RequestParam("max.inactive.internal") Integer maxInactiveInternal);
    
    @RequestMapping(value = "/session/bind", method = RequestMethod.GET)
    View bindSession(@RequestParam("service") String service, @RequestParam("sessionId") String clientSessionId);
    
    @RequestMapping("/login")
    View loginFailure(@RequestParam(value = "shiroLoginFailure", required = false) String shiroLoginFailure,
            @RequestParam(value = "service", required = false) String service);
    
    @RequestMapping(value = "/remote/login", method = RequestMethod.POST)
    Map<String, Object> login(@RequestParam("token") UsernamePasswordToken token, @RequestParam(value = "service", required = false) String service);

    @RequestMapping(value = "/remote/logout", method = RequestMethod.GET)
    ResultMap logout();
    
    @RequestMapping(value = "/remote/logined", method = RequestMethod.GET)
    Map<String, Object> isLogined(@RequestParam(value = "service", required = false) String service);
}
