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
package org.nanoframework.extension.shiro.client.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public class ServiceUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtils.class);

    /**
     * Constructs a service url from the HttpServletRequest or from the given
     * serviceUrl. Prefers the serviceUrl provided if both a serviceUrl and a
     * serviceName.
     *
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param service the configured service url (this will be used if not null)
     * @param serverNames the server name to  use to construct the service url if the service param is empty.  Note, prior to CAS Client 3.3, this was a single value.
     *           As of 3.3, it can be a space-separated value.  We keep it as a single value, but will convert it to an array internally to get the matching value. This keeps backward compatability with anything using this public
     *           method.
     * @param serviceParameterName the service parameter name to remove (i.e. service)
     * @param artifactParameterName the artifact parameter name to remove (i.e. ticket)
     * @param encode whether to encode the url or not (i.e. Jsession).
     * @return the service url to use.
     */
    public static String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response, final String service,
            final String serverNames, final String serviceParameterName, final String artifactParameterName, final boolean encode) {
        if (StringUtils.isNotBlank(service)) {
            return encode ? response.encodeURL(service) : service;
        }

        final String serverName = findMatchingServerName(request, serverNames);
        final URIBuilder originalRequestUrl = new URIBuilder(request.getRequestURL().toString(), encode);
        originalRequestUrl.setParameters(request.getQueryString());

        URIBuilder builder = null;

        boolean containsScheme = true;
        if (!serverName.startsWith("https://") && !serverName.startsWith("http://")) {
            builder = new URIBuilder(encode);
            builder.setScheme(request.isSecure() ? "https" : "http");
            builder.setHost(serverName);
            containsScheme = false;
        } else {
            builder = new URIBuilder(serverName, encode);
        }

        if (!serverNameContainsPort(containsScheme, serverName) && !requestIsOnStandardPort(request)) {
            builder.setPort(request.getServerPort());
        }

        builder.setEncodedPath(request.getRequestURI());

        final List<String> serviceParameterNames = Arrays.asList(serviceParameterName.split(","));
        if (!serviceParameterNames.isEmpty() && !originalRequestUrl.getQueryParams().isEmpty()) {
            for (final URIBuilder.BasicNameValuePair pair : originalRequestUrl.getQueryParams()) {
                if (!pair.getName().equals(artifactParameterName) && !serviceParameterNames.contains(pair.getName())) {
                    builder.addParameter(pair.getName(), pair.getValue());
                }
            }
        }

        final String result = builder.toString();
        final String returnValue = encode ? response.encodeURL(result) : result;
        LOGGER.debug("serviceUrl generated: {}", returnValue);
        return returnValue;
    }

    protected static String findMatchingServerName(final HttpServletRequest request, final String serverName) {
        final String[] serverNames = serverName.split(" ");

        if (serverNames == null || serverNames.length == 0 || serverNames.length == 1) {
            return serverName;
        }

        final String host = request.getHeader("Host");
        final String xHost = request.getHeader("X-Forwarded-Host");

        final String comparisonHost;
        if (xHost != null && host == "localhost") {
            comparisonHost = xHost;
        } else {
            comparisonHost = host;
        }

        if (comparisonHost == null) {
            return serverName;
        }

        for (final String server : serverNames) {
            final String lowerCaseServer = server.toLowerCase();

            if (lowerCaseServer.contains(comparisonHost)) {
                return server;
            }
        }

        return serverNames[0];
    }

    private static boolean serverNameContainsPort(final boolean containsScheme, final String serverName) {
        if (!containsScheme && serverName.contains(":")) {
            return true;
        }

        final int schemeIndex = serverName.indexOf(':');
        final int portIndex = serverName.lastIndexOf(':');
        return schemeIndex != portIndex;
    }

    private static boolean requestIsOnStandardPort(final HttpServletRequest request) {
        final int serverPort = request.getServerPort();
        return serverPort == 80 || serverPort == 443;
    }

    /**
     * Url encode a value using UTF-8 encoding.
     * 
     * @param value the value to encode.
     * @return the encoded value.
     */
    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs the URL to use to redirect to the CAS server.
     *
     * @param casServerLoginUrl the CAS Server login url.
     * @param serviceParameterName the name of the parameter that defines the service.
     * @param serviceUrl the actual service's url.
     * @param sessionIdParameterName the sessionIdParameterName
     * @param sessionId the sessionId
     * @return the fully constructed redirect url.
     */
    public static String constructRedirectUrl(final String casServerLoginUrl, final String serviceParameterName, final String serviceUrl, final String sessionIdParameterName, final String sessionId) {
        return casServerLoginUrl + (casServerLoginUrl.contains("?") ? '&' : '?') + serviceParameterName + '=' + urlEncode(serviceUrl) + '&' + sessionIdParameterName + '=' + sessionId;
    }
    
    /**
     * Safe method for retrieving a parameter from the request without disrupting the reader UNLESS the parameter
     * actually exists in the query string.
     * <p>
     * Note, this does not work for POST Requests for "logoutRequest".  It works for all other CAS POST requests because the
     * parameter is ALWAYS in the GET request.
     * <p>
     * If we see the "logoutRequest" parameter we MUST treat it as if calling the standard request.getParameter.
     * <p>
     *     Note, that as of 3.3.0, we've made it more generic.
     * </p>
     *
     * @param request the request to check.
     * @param parameter the parameter to look for.
     * @param parameters the parameters
     * @return the value of the parameter.
     */
    public static String safeGetParameter(final HttpServletRequest request, final String parameter, final List<String> parameters) {
        if ("POST".equals(request.getMethod()) && parameters.contains(parameter)) {
            LOGGER.debug("safeGetParameter called on a POST HttpServletRequest for Restricted Parameters. Cannot complete check safely. Reverting to standard behavior for this Parameter");
            return request.getParameter(parameter);
        }
        
        return request.getQueryString() == null || !request.getQueryString().contains(parameter) ? null : request .getParameter(parameter);
    }
    
    public static String safeGetParameter(final HttpServletRequest request, final String parameter) {
        return safeGetParameter(request, parameter, Arrays.asList("logoutRequest"));
    }
}
