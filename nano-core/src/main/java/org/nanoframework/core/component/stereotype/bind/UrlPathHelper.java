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
package org.nanoframework.core.component.stereotype.bind;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.nanoframework.commons.util.Charsets;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class UrlPathHelper {
    public Map<String, String> decodePathVariables(final Map<String, String> vars) {
        Map<String, String> decodedVars = new LinkedHashMap<String, String>(vars.size());
        for (Entry<String, String> entry : vars.entrySet()) {
            decodedVars.put(entry.getKey(), decodeInternal(entry.getValue()));
        }
        return decodedVars;
    }
    
    private String decodeInternal(final String source) {
        try {
            return URLDecoder.decode(source, Charsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
}
