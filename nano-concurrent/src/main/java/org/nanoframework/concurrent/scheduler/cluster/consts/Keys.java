/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.concurrent.scheduler.cluster.consts;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public final class Keys {
    public static final String CLUSTER_SCHEDULER_ENABLED = "context.cluster.scheduler.enabled";

    public static final String BASE_PACKAGE = "context.cluster.scheduler-scan.base-package";

    public static final String CLUSTER_ID = "context.cluster.scheduler.id";

    public static final String NODE_ID = "context.cluster.scheduler.node.id";

    private Keys() {

    }
}
