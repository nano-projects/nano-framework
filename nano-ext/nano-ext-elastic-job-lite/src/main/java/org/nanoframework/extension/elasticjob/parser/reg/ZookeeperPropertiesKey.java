/*
 * Copyright © 2015-2017 the original author or authors.
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
package org.nanoframework.extension.elasticjob.parser.reg;

/**
 * 基于Zookeeper注册中心的属性文件KEY.
 *
 * @author wangtong
 * @since 1.4.11
 */
public class ZookeeperPropertiesKey {

    public static final String ROOT = "ejob.reg.root";

    public static final String REG = "ejob.reg.";

    public static final String SERVER_LISTS = "serverLists";

    public static final String NAMESPACE = "namespace";

    public static final String BASE_SLEEP_TIME_MILLISECOND = "baseSleepTimeMilliseconds";

    public static final String MAX_SLEEP_TIME_MILLISECONDS = "maxSleepTimeMilliseconds";

    public static final String MAX_RETRIES = "maxRetries";

    public static final String SESSION_TIMEOUT_MILLISECONDS = "sessionTimeoutMilliseconds";

    public static final String CONNECTION_TIMEOUT_MILLISECONDS = "connectionTimeoutMilliseconds";

    public static final String DIGEST = "digest";
}
