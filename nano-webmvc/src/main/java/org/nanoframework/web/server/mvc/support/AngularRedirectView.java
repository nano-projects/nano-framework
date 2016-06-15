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
package org.nanoframework.web.server.mvc.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.util.Charsets;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class AngularRedirectView extends RedirectView {

    /**
     * 
     * @param page the angular2 page url
     */
    public AngularRedirectView(String page) {
        super(page);
    }

    @Override
    public void redirect(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (response == null || request == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        if (model != null && model.size() > 0) {
            model.forEach((name, o) -> {
                try {
                    builder.append(';').append(name).append('=').append(URLEncoder.encode(toJSONString(o), Charsets.UTF_8.name()));
                } catch(final UnsupportedEncodingException e) { }
            });
        }

        String encodedRedirectURL = response.encodeRedirectURL(page + builder.toString());

        /** HttpServletResponse.sendRedirect */
        response.sendRedirect(encodedRedirectURL);
    }

}
