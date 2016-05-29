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
package org.nanoframework.extension.shiro.web.filter.authc;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.shiro.Protocol;

/**
 * 
 * @author yanghe
 * @since 1.3.7
 */
public class SSOAuthenticationFilter extends FormAuthenticationFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSOAuthenticationFilter.class);

    @Override
    protected void issueSuccessRedirect(final ServletRequest request, final ServletResponse response) throws Exception {
        if (bindService(request, response)) {
            return;
        }

        super.issueSuccessRedirect(request, response);
    }

    protected boolean bindService(final ServletRequest request, final ServletResponse response) throws IOException {
        final String service = request.getParameter(Protocol.SHIRO.getServiceParameterName());
        if (StringUtils.isNotBlank(service)) {
            final Session session = SecurityUtils.getSubject().getSession();
            final String sessionId = CryptUtil.encrypt((String) session.getId());
            final String redirectService;
            if (!service.contains("?")) {
                redirectService = service + '?' + Protocol.SHIRO.getArtifactParameterName() + '=' + sessionId;
            } else {
                redirectService = service + '&' + Protocol.SHIRO.getArtifactParameterName() + '=' + sessionId;
            }

            LOGGER.debug("Redirect: {}", redirectService);
            ((HttpServletResponse) response).sendRedirect(redirectService);
            return true;
        }

        return false;
    }

}
