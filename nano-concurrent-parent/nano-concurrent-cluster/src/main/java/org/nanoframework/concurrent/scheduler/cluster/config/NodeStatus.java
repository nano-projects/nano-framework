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
package org.nanoframework.concurrent.scheduler.cluster.config;

/**
 * 节点状态.
 * @author yanghe
 * @since 1.4.9
 */
public enum NodeStatus {
    LOOKING(1), LEADER(2), FOLLOWING(3), UNKNOWN(9);

    private final int code;

    NodeStatus(final int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static NodeStatus of(final int code) {
        switch (code) {
            case 1:
                return LOOKING;
            case 2:
                return LEADER;
            case 3:
                return FOLLOWING;
            default:
                return UNKNOWN;
        }
    }

    public static NodeStatus of(final String name) {
        switch (name) {
            case "LOOKING":
                return LOOKING;
            case "LEADER":
                return LEADER;
            case "FOLLOWING":
                return FOLLOWING;
            default:
                return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
