/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.web.server.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.util.ObjectCompare;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.web.server.http.status.HttpStatusCode;
import org.nanoframework.web.server.servlet.Cors;

/**
 * @author yanghe
 * @date 2016年2月3日 下午11:01:46
 */
public abstract class CorsFilter extends AbstractFilter {

	/**
	 * if `allowEnable` is true, then settings allow headers
	 *  
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	protected boolean allowResponse(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(!Cors.get().allowEnable)
			return false;
		
		String currentOrigin = request.getHeader(Cors.REQUEST_HEADERS_ORIGIN);
		if (response.getStatus() < 300 && ObjectCompare.isInList(Cors.ALL_ALLOW, Cors.get().allowOrigin) || 
				ObjectCompare.isInList(currentOrigin.trim(), Cors.get().allowOrigin)) {  
	        response.setHeader(Cors.RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_ORIGIN, currentOrigin);  
	    } else {
	    	response.setStatus(HttpStatusCode.SC_UNAUTHORIZED);
	    	return false;
	    }
		
	    if (response.getStatus() < 300 && StringUtils.isNotBlank(Cors.get().allowMethod)) { 
	    	String requestMethod = request.getHeader(Cors.REQUEST_HEADERS_ACCESS_CONTROL_REQUEST_METHOD);
	    	String[] allowMethods = Cors.get().allowMethods;
	    	if(request.getMethod().equals(RequestMethod.OPTIONS.name()) && 
	    			!ObjectCompare.isInList(Cors.ALL_ALLOW, allowMethods) && 
	    			!ObjectCompare.isInList(requestMethod.trim(), allowMethods)) {
	    		response.setStatus(HttpStatusCode.SC_UNAUTHORIZED);
	    	}
	    		
	        response.setHeader(Cors.RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_METHODS, requestMethod);  
	    }
	    
	    if (response.getStatus() < 300 && StringUtils.isNotBlank(Cors.get().allowCredentials)) { 
	        response.setHeader(Cors.RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_CREDENTIALS, Cors.get().allowCredentials);  
	    }
	    
	    if (response.getStatus() < 300 && StringUtils.isNotBlank(Cors.get().allowHeader)) { 
	    	String requestHeaders = request.getHeader(Cors.REQUEST_HEADERS_ACCESS_CONTROL_REQUEST_HEADERS);
	    	String[] allowHeaders = Cors.get().allowHeaders;
	    	if(StringUtils.isNotBlank(requestHeaders)) {
	    		String[] headers = requestHeaders.split(",");
	    		if(!ObjectCompare.isInList(Cors.ALL_ALLOW, allowHeaders)) {
	        		for(String header : headers) {
	        			if(!ObjectCompare.isInList(header.trim(), allowHeaders)) {
	        				response.setStatus(HttpStatusCode.SC_UNAUTHORIZED);
	        				break;
	        			}
	        		}
	    		}
	    	}
	    	
			response.setHeader(Cors.RESPONSE_HEADERS_ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);  
	    }
	    
	    if (response.getStatus() < 300 && StringUtils.isNotBlank(Cors.get().exposeHeader)) { 
	        response.setHeader(Cors.RESPONSE_HEADERS_ACCESS_CONTROL_EXPOSE_HEADERS, Cors.get().exposeHeader);  
	    }
		
	    if(request.getMethod().equals(RequestMethod.OPTIONS.name())) {
			if(response.getStatus() >= 300)
				return false;
			
			response.setStatus(HttpStatusCode.SC_NO_CONTENT);
			return false;
		}
        
        return true;
	}
	
}
