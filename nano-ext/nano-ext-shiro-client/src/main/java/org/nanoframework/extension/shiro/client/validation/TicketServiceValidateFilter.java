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
package org.nanoframework.extension.shiro.client.validation;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.httpclient.HttpResponse;
import org.nanoframework.extension.shiro.client.AbstractShiroClientFilter;
import org.nanoframework.extension.shiro.client.AuthenticationException;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class TicketServiceValidateFilter extends AbstractShiroClientFilter {

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) resp;

        if (isRequestUrlExcluded(request)) {
            logger.debug("Request is ignored.");
            chain.doFilter(request, response);
            return;
        }

        final String ticket = retrieveTicketFromRequest(request);
        if (StringUtils.isBlank(ticket)) {
            logger.debug("Request is ignored.");
            chain.doFilter(request, response);
            return;
        }

        try {
            final HttpResponse httpResponse = registrySession(request, ticket);
            decodeSession(httpResponse);
            response.sendRedirect(constructServiceUrl(request, response));
        } catch (final AuthenticationException e) {
            responseFailure(request, response);
        }
    }

    protected HttpResponse registrySession(final HttpServletRequest request, final String ticket) {
        final HttpClient httpClient = httpClient();

        Throwable lastError = null;
        for (int retry = 0; retry < serviceInvokeRetry; retry++) {
            try {
                final String sessionURL = sessionURL(request);
                return httpClient.post(sessionURL,
                        MapBuilder.<String, String> create().put(getProtocol().getArtifactParameterName(), ticket).build());

            } catch (final Throwable e) {
                lastError = e;
            }
        }

        if (lastError != null) {
            throw new RegistrySessionException(lastError.getMessage(), lastError);
        } else {
            throw new RegistrySessionException();
        }
    }
}
