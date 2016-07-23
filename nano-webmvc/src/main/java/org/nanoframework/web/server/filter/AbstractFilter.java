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

import static org.nanoframework.web.server.http.status.ComponentStatus.BIND_PARAM_EXCEPTION_CODE;
import static org.nanoframework.web.server.http.status.ComponentStatus.INVOKE_ERROR_CODE;
import static org.nanoframework.web.server.http.status.ComponentStatus.IO_EXCEPTION_CODE;
import static org.nanoframework.web.server.http.status.ComponentStatus.SERVLET_EXCEPTION;
import static org.nanoframework.web.server.http.status.ComponentStatus.UNKNOWN;
import static org.nanoframework.web.server.http.status.ComponentStatus.UNSUPPORT_REQUEST_METHOD_CODE;
import static org.nanoframework.web.server.http.status.ComponentStatus.UNSUPPORT_REQUEST_METHOD_DESC;

import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanoframework.commons.util.Charsets;
import org.nanoframework.commons.util.ContentType;
import org.nanoframework.commons.util.ObjectUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.core.component.exception.BindRequestParamException;
import org.nanoframework.core.component.exception.ComponentInvokeException;
import org.nanoframework.core.component.stereotype.bind.RequestMapper;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.context.URLContext;
import org.nanoframework.web.server.http.status.Response;
import org.nanoframework.web.server.http.status.ResultMap;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author yanghe
 * @since 1.2
 */
public abstract class AbstractFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding(Charsets.UTF_8.name());
        response.setCharacterEncoding(Charsets.UTF_8.name());

        if (invoke((HttpServletRequest) request, (HttpServletResponse) response)) {
            chain.doFilter(request, response);
        }
    }

    protected abstract boolean invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;

    @Override
    public void destroy() {

    }

    protected boolean validRequestMethod(ServletResponse response, Writer out, RequestMapper mapper, String method) throws IOException {
        if (!mapper.hasMethod(RequestMethod.valueOf(method))) {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            ResultMap resultMap = ResultMap.create(UNSUPPORT_REQUEST_METHOD_CODE,
                    "不支持此请求类型(" + method + ")，仅支持类型(" + StringUtils.join(mapper.getRequestMethodStrs(), " / ") + ')', UNSUPPORT_REQUEST_METHOD_DESC);
            out.write(JSON.toJSONString(resultMap));
            return false;
        }

        return true;
    }

    protected void process(ServletRequest request, ServletResponse response, Writer out, URLContext urlContext, Object ret, Model model)
            throws IOException, ServletException {
        if (ret instanceof View) {
            ((View) ret).redirect(model.get(), (HttpServletRequest) request, (HttpServletResponse) response);
        } else if (ret instanceof String) {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            out.write((String) ret);
        } else if (ret instanceof Response && ret == Response.EMPTY) {
            return;
        } else if (ret != null) {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            /** 跨域JSONP的Ajax请求支持 */
            final String callback;
            if (!ObjectUtils.isEmpty(callback = (String) urlContext.getParameter().get("callback"))) {
                out.write(callback + '(' + JSON.toJSONString(ret, SerializerFeature.WriteDateUseDateFormat) + ')');
            } else {
                out.write(JSON.toJSONString(ret, SerializerFeature.WriteDateUseDateFormat));
            }
        } else {
            response.setContentType(ContentType.APPLICATION_JSON);
            out = response.getWriter();
            out.write(JSON.toJSONString(UNKNOWN));
        }
    }

    protected URLContext create(HttpServletRequest request) throws IOException {
        final Map<String, Object> parameter = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value.length > 0) {
                if (key.endsWith("[]"))
                    parameter.put(key.toLowerCase(), value);
                else
                    parameter.put(key.toLowerCase(), value[0]);

            }
        });

        final String uri = URLDecoder.decode(((HttpServletRequest) request).getRequestURI(), Charsets.UTF_8.name());
        final URLContext urlContext = URLContext.create().setContext(uri).setParameter(parameter);
        final String[] uris = uri.split(";");
        if (uris.length > 1) {
            urlContext.setContext(uris[0]);
            String[] specials = new String[uris.length - 1];
            System.arraycopy(uris, 1, specials, 0, specials.length);
            urlContext.setSpecial(specials);
        }

        return urlContext;
    }

    protected ResultMap error(Throwable e) {
        ResultMap error = null;
        if (e instanceof ComponentInvokeException) {
            error = ResultMap.create(INVOKE_ERROR_CODE, e.getMessage(), "ComponentInvokeException");
        } else if (e instanceof BindRequestParamException) {
            error = ResultMap.create(BIND_PARAM_EXCEPTION_CODE, e.getMessage(), "BindRequestParamException");
        } else if (e instanceof IOException) {
            error = ResultMap.create(IO_EXCEPTION_CODE, e.getMessage(), "IOException");
        } else if (e instanceof ServletException) {
            error = ResultMap.create(SERVLET_EXCEPTION, e.getMessage(), "ServletException");
        } else
            error = UNKNOWN;

        return error;
    }

}
