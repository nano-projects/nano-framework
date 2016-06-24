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
package org.nanoframework.web.server.stream;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.nanoframework.web.server.filter.HttpRequestFilter.HttpContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 读取HttpServletRequest中的流.
 * @author yanghe
 * @since 1.3.10
 */
public final class ReadStream {

    private ReadStream() {
        
    }
    
    /**
     * 
     * @return Request input stream
     * @throws IOException if I/O error occur
     */
    public static String read() throws IOException {
        final HttpServletRequest request = HttpContext.get(HttpServletRequest.class);
        try (final Scanner scanner = new Scanner(request.getInputStream())) {
            final StringBuilder builder = new StringBuilder();
            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }
            
            return builder.toString();
        }
    }
    
    /**
     * @param <T> Type类型
     * @param type Alibaba FastJSON TypeReference
     * @return T
     * @throws IOException if I/O error occur
     */
    public static <T> T read(final TypeReference<T> type) throws IOException {
        final String stream = read();
        return JSON.parseObject(stream, type);
    }
}
