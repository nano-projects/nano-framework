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
package org.nanoframework.web.server.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public interface CookieValueManager {

    /**
     * Build cookie value.
     *
     * @param givenCookieValue the given cookie value
     * @param request the request
     * @return the original cookie value
     */
    String buildCookieValue(@NotNull String givenCookieValue, @NotNull HttpServletRequest request);

    /**
     * Obtain cookie value.
     *
     * @param cookie the cookie
     * @param request the request
     * @return the cookie value or null
     */
    String obtainCookieValue(@NotNull Cookie cookie, @NotNull HttpServletRequest request);
    
    /**
     * Obtain cookie value.
     * 
     * @param name the cookie name
     * @param value the cookie value
     * @param request the request
     * @return the cookie value or null
     */
    String obtainCookieValue(@NotNull String name, @NotNull String value, @NotNull HttpServletRequest request);
}
