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
package org.nanoframework.extension.shiro.client.configuration;

/**
 * 
 *
 * @author yanghe
 * @since 1.3.7
 */
public interface ConfigurationKeys {
    ConfigurationKey<String> SERVER_NAME = new ConfigurationKey<String>("serverName", null);
    ConfigurationKey<String> SERVICE = new ConfigurationKey<String>("service");
    ConfigurationKey<Boolean> ENCODE_SERVICE_URL = new ConfigurationKey<Boolean>("encodeServiceUrl", Boolean.TRUE);
    ConfigurationKey<Integer> SERVICE_INVOKE_RETRY = new ConfigurationKey<>("serviceInvokeRetry", 3);
    
    ConfigurationKey<String> IGNORE_PATTERN = new ConfigurationKey<String>("ignorePattern", null);
    ConfigurationKey<String> IGNORE_URL_PATTERN_TYPE = new ConfigurationKey<String>("ignoreUrlPatternType", "REGEX");
    
    ConfigurationKey<String> SHIRO_SESSION_URL = new ConfigurationKey<String>("shiroSessionURL", null);
    ConfigurationKey<String> SHIRO_SESSION_BIND_URL = new ConfigurationKey<String>("shiroSessionBindURL", null);
    
    ConfigurationKey<String> SESSION_ID_NAME = new ConfigurationKey<String>("sessionIdName", "JSESSIONID");
}
