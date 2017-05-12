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

import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class Status {
    /** OK ResultMap. */
    public static final ResultMap OK = ResultMap.create("OK", HttpStatus.OK);
    /** BAD_REQUEST RequestMap. */
    public static final ResultMap BAD_REQUEST = ResultMap.create("无效的请求", HttpStatus.BAD_REQUEST);
    /** INVALID_AUTH RequestMap. */
    public static final ResultMap INVALID_AUTH = ResultMap.create("权限认证失败", HttpStatus.UNAUTHORIZED);
    /** INVALID_USER_PASS RequestMap. */
    public static final ResultMap INVALID_USER_PASS = ResultMap.create("权限认证失败，无效的用户名或密码", HttpStatus.UNAUTHORIZED);
    /** USER_ERROR RequestMap. */
    public static final ResultMap USER_ERROR = ResultMap.create("权限认证失败，用户名不存在", HttpStatus.UNAUTHORIZED);
    /** PASSWORD_ERROR RequestMap. */
    public static final ResultMap PASSWORD_ERROR = ResultMap.create("权限认证失败，密码错误", HttpStatus.UNAUTHORIZED);
    /** UNAUTH RequestMap. */
    public static final ResultMap UNAUTH = ResultMap.create("未知认证异常", HttpStatus.UNAUTHORIZED);
    /** UNLOGIN RequestMap. */
    public static final ResultMap UNLOGIN = ResultMap.create("未登陆", HttpStatus.UNAUTHORIZED);
    /** REGISTER_ERROR RequestMap. */
    public static final ResultMap REGISTER_ERROR = ResultMap.create("注册用户失败", HttpStatus.BAD_REQUEST);
    /** INTERNAL_SERVER_ERROR RequestMap. */
    public static final ResultMap INTERNAL_SERVER_ERROR = ResultMap.create("服务器端异常", HttpStatus.INTERNAL_SERVER_ERROR);
    
}
