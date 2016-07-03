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
package org.nanoframework.web.server.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * 
 * @author yanghe
 * @since 1.2
 */
public class CrossOriginFilter implements Filter {
	private static final Logger LOG = LoggerFactory.getLogger(CrossOriginFilter.class);
	private static final String ORIGIN_HEADER = "Origin";
	public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
	public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
	public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
	public static final String ALLOWED_ORIGINS_PARAM = "allowedOrigins";
	public static final String ALLOWED_METHODS_PARAM = "allowedMethods";
	public static final String ALLOWED_HEADERS_PARAM = "allowedHeaders";
	public static final String PREFLIGHT_MAX_AGE_PARAM = "preflightMaxAge";
	public static final String ALLOW_CREDENTIALS_PARAM = "allowCredentials";
	public static final String EXPOSED_HEADERS_PARAM = "exposedHeaders";
	public static final String OLD_CHAIN_PREFLIGHT_PARAM = "forwardPreflight";
	public static final String CHAIN_PREFLIGHT_PARAM = "chainPreflight";
	private static final String ANY_ORIGIN = "*";
	private static final Set<String> SIMPLE_HTTP_METHODS = Sets.newHashSet("GET", "POST", "HEAD");
	private boolean anyOriginAllowed;
	private List<String> allowedOrigins = new ArrayList<>();
	private List<String> allowedMethods = new ArrayList<>();
	private List<String> allowedHeaders = new ArrayList<>();
	private List<String> exposedHeaders = new ArrayList<>();
	private int preflightMaxAge;
	private boolean allowCredentials;
	private boolean chainPreflight;

	public void init(FilterConfig config) throws ServletException {
		String allowedOriginsConfig = config.getInitParameter(ALLOWED_ORIGINS_PARAM);
		if (allowedOriginsConfig == null) allowedOriginsConfig = ANY_ORIGIN;
		
		String[] allowedOrigins = allowedOriginsConfig.split(",");
		for (String allowedOrigin : allowedOrigins) {
			allowedOrigin = allowedOrigin.trim();
			if (allowedOrigin.length() > 0) {
				if (ANY_ORIGIN.equals(allowedOrigin)) {
					this.anyOriginAllowed = true;
					this.allowedOrigins.clear();
					break;
				}

				this.allowedOrigins.add(allowedOrigin);
			}

		}

		String allowedMethodsConfig = config.getInitParameter(ALLOWED_METHODS_PARAM);
		if (allowedMethodsConfig == null) 
			allowedMethodsConfig = "GET,POST,HEAD";
		
		this.allowedMethods.addAll(Arrays.asList(allowedMethodsConfig.split(",")));

		String allowedHeadersConfig = config.getInitParameter(ALLOWED_HEADERS_PARAM);
		if (allowedHeadersConfig == null)
			allowedHeadersConfig = "X-Requested-With,Content-Type,Accept,Origin";
		
		this.allowedHeaders.addAll(Arrays.asList(allowedHeadersConfig.split(",")));

		String preflightMaxAgeConfig = config.getInitParameter(PREFLIGHT_MAX_AGE_PARAM);
		if (preflightMaxAgeConfig == null)
			preflightMaxAgeConfig = "1800";
		
		try {
			this.preflightMaxAge = Integer.parseInt(preflightMaxAgeConfig);
		} catch (NumberFormatException x) {
			LOG.info("Cross-origin filter, could not parse '{}' parameter as integer: {}", new Object[] { PREFLIGHT_MAX_AGE_PARAM, preflightMaxAgeConfig });
		}

		String allowedCredentialsConfig = config.getInitParameter(ALLOW_CREDENTIALS_PARAM);
		if (allowedCredentialsConfig == null)
			allowedCredentialsConfig = "true";
		
		this.allowCredentials = Boolean.parseBoolean(allowedCredentialsConfig);

		String exposedHeadersConfig = config.getInitParameter(EXPOSED_HEADERS_PARAM);
		if (exposedHeadersConfig == null)
			exposedHeadersConfig = "";
		
		this.exposedHeaders.addAll(Arrays.asList(exposedHeadersConfig.split(",")));

		String chainPreflightConfig = config.getInitParameter(OLD_CHAIN_PREFLIGHT_PARAM);
		if (chainPreflightConfig != null)
			LOG.warn("DEPRECATED CONFIGURATION: Use chainPreflight instead of forwardPreflight", new Object[0]);
		else
			chainPreflightConfig = config.getInitParameter(CHAIN_PREFLIGHT_PARAM);
		
		if (chainPreflightConfig == null)
			chainPreflightConfig = "true";
		
		this.chainPreflight = Boolean.parseBoolean(chainPreflightConfig);

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Cross-origin filter configuration: allowedOrigins = ")
					.append(allowedOriginsConfig).append(", ").append(ALLOWED_METHODS_PARAM).append(" = ")
					.append(allowedMethodsConfig).append(", ").append(ALLOWED_HEADERS_PARAM).append(" = ")
					.append(allowedHeadersConfig).append(", ").append(PREFLIGHT_MAX_AGE_PARAM).append(" = ")
					.append(preflightMaxAgeConfig).append(", ").append(ALLOW_CREDENTIALS_PARAM).append(" = ")
					.append(allowedCredentialsConfig).append(", ").append(EXPOSED_HEADERS_PARAM).append(" = ")
					.append(exposedHeadersConfig).append(", ").append(CHAIN_PREFLIGHT_PARAM).append(" = ")
					.append(chainPreflightConfig).toString(), new Object[0]);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		handle((HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		String origin = request.getHeader(ORIGIN_HEADER);

		if ((origin != null) && (isEnabled(request))) {
			if (originMatches(origin)) {
				if (isSimpleRequest(request)) {
					LOG.debug("Cross-origin request to {} is a simple cross-origin request", new Object[] { request.getRequestURI() });
					handleSimpleResponse(request, response, origin);
					
				} else if (isPreflightRequest(request)) {
					LOG.debug("Cross-origin request to {} is a preflight cross-origin request", new Object[] { request.getRequestURI() });
					handlePreflightResponse(request, response, origin);
					
					if (this.chainPreflight) {
						LOG.debug("Preflight cross-origin request to {} forwarded to application", new Object[] { request.getRequestURI() });
					}
					
					return ;
				} else {
					LOG.debug("Cross-origin request to {} is a non-simple cross-origin request", new Object[] { request.getRequestURI() });
					handleSimpleResponse(request, response, origin);
				}
			} else {
				LOG.debug(new StringBuilder().append("Cross-origin request to ").append(request.getRequestURI()).append(" with origin ").append(origin).append(" does not match allowed origins ").append(this.allowedOrigins).toString(), new Object[0]);
			}
		}

		chain.doFilter(request, response);
	}

	protected boolean isEnabled(HttpServletRequest request) {
		for (Enumeration<String> connections = request.getHeaders("Connection"); connections.hasMoreElements();) {
			String connection = (String) connections.nextElement();
			if ("Upgrade".equalsIgnoreCase(connection)) {
				for (Enumeration<String> upgrades = request.getHeaders("Upgrade"); upgrades.hasMoreElements();) {
					String upgrade = (String) upgrades.nextElement();
					if ("WebSocket".equalsIgnoreCase(upgrade))
						return false;
				}
			}
		}
		
		return true;
	}

	private boolean originMatches(String originList) {
		if (this.anyOriginAllowed) {
			return true;
		}
		
		if (originList.trim().length() == 0) {
			return false;
		}
		
		String[] origins = originList.split(" ");
		for (String origin : origins) {
			if (origin.trim().length() != 0) {
				for (String allowedOrigin : this.allowedOrigins) {
					if (allowedOrigin.contains(ANY_ORIGIN)) {
						Matcher matcher = createMatcher(origin, allowedOrigin);
						if (matcher.matches())
							return true;
						
					} else if (allowedOrigin.equals(origin)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private Matcher createMatcher(String origin, String allowedOrigin) {
		String regex = parseAllowedWildcardOriginToRegex(allowedOrigin);
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(origin);
	}

	private String parseAllowedWildcardOriginToRegex(String allowedOrigin) {
		String regex = allowedOrigin.replace(".", "\\.");
		return regex.replace(ANY_ORIGIN, ".*");
	}

	private boolean isSimpleRequest(HttpServletRequest request) {
		String method = request.getMethod();
		if (SIMPLE_HTTP_METHODS.contains(method)) {
			return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null;
		}
		
		return false;
	}

	private boolean isPreflightRequest(HttpServletRequest request) {
		String method = request.getMethod();
		if (!"OPTIONS".equalsIgnoreCase(method))
			return false;
		
		if (request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null)
			return false;
		
		return true;
	}

	private void handleSimpleResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
		response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
		
		if (this.allowCredentials)
			response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
		
		if (!this.exposedHeaders.isEmpty())
			response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, commify(this.exposedHeaders));
	}

	private void handlePreflightResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
		boolean methodAllowed = isMethodAllowed(request);
		if (!methodAllowed)
			return;
		
		boolean headersAllowed = areHeadersAllowed(request);
		
		if (!headersAllowed)
			return;
		
		response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
		
		if (this.allowCredentials)
			response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
		
		if (this.preflightMaxAge > 0)
			response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, String.valueOf(this.preflightMaxAge));
		
		response.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, commify(this.allowedMethods));
		response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, commify(this.allowedHeaders));
	}

	private boolean isMethodAllowed(HttpServletRequest request) {
		String accessControlRequestMethod = request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER);
		LOG.debug("{} is {}", new Object[] { ACCESS_CONTROL_REQUEST_METHOD_HEADER, accessControlRequestMethod });
		boolean result = false;
		if (accessControlRequestMethod != null) {
			result = this.allowedMethods.contains(accessControlRequestMethod);
		}
		
		LOG.debug(new StringBuilder().append("Method {} is").append(result ? "" : " not").append(" among allowed methods {}").toString(), new Object[] { accessControlRequestMethod, this.allowedMethods });
		return result;
	}

	private boolean areHeadersAllowed(HttpServletRequest request) {
		String accessControlRequestHeaders = request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
		LOG.debug("{} is {}", new Object[] { ACCESS_CONTROL_REQUEST_HEADERS_HEADER, accessControlRequestHeaders });
		boolean result = true;
		if (accessControlRequestHeaders != null) {
			String[] headers = accessControlRequestHeaders.split(",");
			for (String header : headers) {
				boolean headerAllowed = false;
				for (String allowedHeader : this.allowedHeaders) {
					if (header.trim().equalsIgnoreCase(allowedHeader.trim())) {
						headerAllowed = true;
						break;
					}
				}
				
				if (!headerAllowed) {
					result = false;
					break;
				}
			}
		}
		
		LOG.debug(new StringBuilder().append("Headers [{}] are").append(result ? "" : " not").append(" among allowed headers {}").toString(), new Object[] { accessControlRequestHeaders, this.allowedHeaders });
		return result;
	}

	private String commify(List<String> strings) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strings.size(); i++) {
			if (i > 0)
				builder.append(',');
			
			String string = (String) strings.get(i);
			builder.append(string);
		}
		
		return builder.toString();
	}

	public void destroy() {
		this.anyOriginAllowed = false;
		this.allowedOrigins.clear();
		this.allowedMethods.clear();
		this.allowedHeaders.clear();
		this.preflightMaxAge = 0;
		this.allowCredentials = false;
	}
}