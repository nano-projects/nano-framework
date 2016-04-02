/**
 * Copyright 2015- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.web.server.http.status;

import static org.nanoframework.web.server.http.status.HttpStatusCode.*;

/**
 * Http状态码
 * 
 * @author yanghe
 * @date 2015年7月25日 下午8:30:21 
 *
 */
public enum HttpStatus {

	// --- 1xx Informational ---
	/** <tt>100 Continue</tt> (HTTP/1.1 - RFC 2616) */
    CONTINUE(SC_CONTINUE, "CONTINUE"),
    /** <tt>101 Switching Protocols</tt> (HTTP/1.1 - RFC 2616) */
    SWITCHING_PROTOCOLS(SC_SWITCHING_PROTOCOLS, "SWITCHING_PROTOCOLS"),
    /** <tt>102 Processing</tt> (WebDAV - RFC 2518) */
    PROCESSING(SC_PROCESSING, "PROCESSING"),
    
    // --- 2xx Success ---
    /** <tt>200 OK</tt> (HTTP/1.0 - RFC 1945) */
    OK(SC_OK, "OK"),
    /** <tt>201 Created</tt> (HTTP/1.0 - RFC 1945) */
    CREATED(SC_CREATED, "CREATED"),
    /** <tt>202 Accepted</tt> (HTTP/1.0 - RFC 1945) */
    ACCEPTED(SC_ACCEPTED, "ACCEPTED"),
    /** <tt>203 Non Authoritative Information</tt> (HTTP/1.1 - RFC 2616) */
    NON_AUTHORITATIVE_INFORMATION(SC_NON_AUTHORITATIVE_INFORMATION, "NON_AUTHORITATIVE_INFORMATION"),
    /** <tt>204 No Content</tt> (HTTP/1.0 - RFC 1945) */
    NO_CONTENT(SC_NO_CONTENT, "NO_CONTENT"),
    /** <tt>205 Reset Content</tt> (HTTP/1.1 - RFC 2616) */
    RESET_CONTENT(SC_RESET_CONTENT, "RESET_CONTENT"),
    /** <tt>206 Partial Content</tt> (HTTP/1.1 - RFC 2616) */
    PARTIAL_CONTENT(SC_PARTIAL_CONTENT, "PARTIAL_CONTENT"),
    /**
	 * <tt>207 Multi-Status</tt> (WebDAV - RFC 2518) or <tt>207 Partial Update
	 * OK</tt> (HTTP/1.1 - draft-ietf-http-v11-spec-rev-01?)
	 */
    MULTI_STATUS(SC_MULTI_STATUS, "MULTI_STATUS"),
    
    // --- 3xx Redirection ---
    /** <tt>300 Mutliple Choices</tt> (HTTP/1.1 - RFC 2616) */
    MULTIPLE_CHOICES(SC_MULTIPLE_CHOICES, "MULTIPLE_CHOICES"),
    /** <tt>301 Moved Permanently</tt> (HTTP/1.0 - RFC 1945) */
    MOVED_PERMANENTLY(SC_MOVED_PERMANENTLY, "MOVED_PERMANENTLY"),
    /**
	 * <tt>302 Moved Temporarily</tt> (Sometimes <tt>Found</tt>) (HTTP/1.0 - RFC
	 * 1945)
	 */
    MOVED_TEMPORARILY(SC_MOVED_TEMPORARILY, "MOVED_TEMPORARILY"),
    /** <tt>303 See Other</tt> (HTTP/1.1 - RFC 2616) */
    SEE_OTHER(SC_SEE_OTHER, "SEE_OTHER"),
    /** <tt>304 Not Modified</tt> (HTTP/1.0 - RFC 1945) */
    NOT_MODIFIED(SC_NOT_MODIFIED, "NOT_MODIFIED"),
    /** <tt>305 Use Proxy</tt> (HTTP/1.1 - RFC 2616) */
    USE_PROXY(SC_USE_PROXY, "USE_PROXY"),
    /** <tt>307 Temporary Redirect</tt> (HTTP/1.1 - RFC 2616) */
    TEMPORARY_REDIRECT(SC_TEMPORARY_REDIRECT, "TEMPORARY_REDIRECT"),
    
    // --- 4xx Client Error ---
    /** <tt>400 Bad Request</tt> (HTTP/1.1 - RFC 2616) */
    BAD_REQUEST(SC_BAD_REQUEST, "BAD_REQUEST"),
    /** <tt>401 Unauthorized</tt> (HTTP/1.0 - RFC 1945) */
    UNAUTHORIZED(SC_UNAUTHORIZED, "UNAUTHORIZED"),
    /** <tt>402 Payment Required</tt> (HTTP/1.1 - RFC 2616) */
    PAYMENT_REQUIRED(SC_PAYMENT_REQUIRED, "PAYMENT_REQUIRED"),
    /** <tt>403 Forbidden</tt> (HTTP/1.0 - RFC 1945) */
    FORBIDDEN(SC_FORBIDDEN, "FORBIDDEN"),
    /** <tt>404 Not Found</tt> (HTTP/1.0 - RFC 1945) */
    NOT_FOUND(SC_NOT_FOUND, "NOT_FOUND"),
    /** <tt>405 Method Not Allowed</tt> (HTTP/1.1 - RFC 2616) */
    METHOD_NOT_ALLOWED(SC_METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED"),
    /** <tt>406 Not Acceptable</tt> (HTTP/1.1 - RFC 2616) */
    NOT_ACCEPTABLE(SC_NOT_ACCEPTABLE, "NOT_ACCEPTABLE"),
    /** <tt>407 Proxy Authentication Required</tt> (HTTP/1.1 - RFC 2616) */
    PROXY_AUTHENTICATION_REQUIRED(SC_PROXY_AUTHENTICATION_REQUIRED, "PROXY_AUTHENTICATION_REQUIRED"),
    /** <tt>408 Request Timeout</tt> (HTTP/1.1 - RFC 2616) */
    REQUEST_TIMEOUT(SC_REQUEST_TIMEOUT, "REQUEST_TIMEOUT"),
    /** <tt>409 Conflict</tt> (HTTP/1.1 - RFC 2616) */
    CONFLICT(SC_CONFLICT, "CONFLICT"),
    /** <tt>410 Gone</tt> (HTTP/1.1 - RFC 2616) */
    GONE(SC_GONE, "GONE"),
    /** <tt>411 Length Required</tt> (HTTP/1.1 - RFC 2616) */
    LENGTH_REQUIRED(SC_LENGTH_REQUIRED, "LENGTH_REQUIRED"),
    /** <tt>412 Precondition Failed</tt> (HTTP/1.1 - RFC 2616) */
    PRECONDITION_FAILED(SC_PRECONDITION_FAILED, "PRECONDITION_FAILED"),
    /** <tt>413 Request Entity Too Large</tt> (HTTP/1.1 - RFC 2616) */
    REQUEST_TOO_LONG(SC_REQUEST_TOO_LONG, "REQUEST_TOO_LONG"),
    /** <tt>414 Request-URI Too Long</tt> (HTTP/1.1 - RFC 2616) */
    REQUEST_URI_TOO_LONG(SC_REQUEST_URI_TOO_LONG, "REQUEST_URI_TOO_LONG"),
    /** <tt>415 Unsupported Media Type</tt> (HTTP/1.1 - RFC 2616) */
    UNSUPPORTED_MEDIA_TYPE(SC_UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE"),
    /** <tt>416 Requested Range Not Satisfiable</tt> (HTTP/1.1 - RFC 2616) */
    REQUESTED_RANGE_NOT_SATISFIABLE(SC_REQUESTED_RANGE_NOT_SATISFIABLE, "REQUESTED_RANGE_NOT_SATISFIABLE"),
    /** <tt>417 Expectation Failed</tt> (HTTP/1.1 - RFC 2616) */
    EXPECTATION_FAILED(SC_EXPECTATION_FAILED, "EXPECTATION_FAILED"),
    /**
	 * Static constant for a 419 error.
	 * <tt>419 Insufficient Space on Resource</tt> (WebDAV -
	 * draft-ietf-webdav-protocol-05?) or
	 * <tt>419 Proxy Reauthentication Required</tt> (HTTP/1.1 drafts?)
	 */
    INSUFFICIENT_SPACE_ON_RESOURCE(SC_INSUFFICIENT_SPACE_ON_RESOURCE, "INSUFFICIENT_SPACE_ON_RESOURCE"),
    /**
	 * Static constant for a 420 error. <tt>420 Method Failure</tt> (WebDAV -
	 * draft-ietf-webdav-protocol-05?)
	 */
    METHOD_FAILURE(SC_METHOD_FAILURE, "METHOD_FAILURE"),
    /** <tt>422 Unprocessable Entity</tt> (WebDAV - RFC 2518) */
    UNPROCESSABLE_ENTITY(SC_UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY"),
    /** <tt>423 Locked</tt> (WebDAV - RFC 2518) */
    LOCKED(SC_LOCKED, "LOCKED"),
    /** <tt>424 Failed Dependency</tt> (WebDAV - RFC 2518) */
    FAILED_DEPENDENCY(SC_FAILED_DEPENDENCY, "FAILED_DEPENDENCY"),
    
    // --- 5xx Server Error ---
 	/** <tt>500 Server Error</tt> (HTTP/1.0 - RFC 1945) */
    INTERNAL_SERVER_ERROR(SC_INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    /** <tt>501 Not Implemented</tt> (HTTP/1.0 - RFC 1945) */
    NOT_IMPLEMENTED(SC_NOT_IMPLEMENTED, "NOT_IMPLEMENTED"),
    /** <tt>502 Bad Gateway</tt> (HTTP/1.0 - RFC 1945) */
    BAD_GATEWAY(SC_BAD_GATEWAY, "BAD_GATEWAY"),
    /** <tt>503 Service Unavailable</tt> (HTTP/1.0 - RFC 1945) */
    SERVICE_UNAVAILABLE(SC_SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE"),
    /** <tt>504 Gateway Timeout</tt> (HTTP/1.1 - RFC 2616) */
    GATEWAY_TIMEOUT(SC_GATEWAY_TIMEOUT, "GATEWAY_TIMEOUT"),
    /** <tt>505 HTTP Version Not Supported</tt> (HTTP/1.1 - RFC 2616) */
    HTTP_VERSION_NOT_SUPPORTED(SC_HTTP_VERSION_NOT_SUPPORTED, "HTTP_VERSION_NOT_SUPPORTED"),
    /** <tt>507 Insufficient Storage</tt> (WebDAV - RFC 2518) */
    INSUFFICIENT_STORAGE(SC_INSUFFICIENT_STORAGE, "INSUFFICIENT_STORAGE");
	
    /** 状态码 */
    public final int code;
    
    /** 消息描述 */
    public final String info;
	
	private HttpStatus(int code, String info) {
		this.code = code;
		this.info = info;
	}

	public ResultMap to() {
		return ResultMap.create(this.info, this);
	}
}
