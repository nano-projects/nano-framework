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

import com.alibaba.fastjson.annotation.JSONType;

/**
 * 节点状态.
 * @author yanghe
 * @since 1.4.9
 */
@JSONType(serializeEnumAsJavaBean = true)
public enum WorkerStatus {
    STOP(0, "停止"), START(1, "运行"), UNKNOWN(9, "未知状态");

    private final int code;
    private final String description;

    WorkerStatus(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static WorkerStatus of(final int code) {
        switch (code) {
            case 0:
                return STOP;
            case 1:
                return START;
            default:
                return UNKNOWN;
        }
    }

    public static WorkerStatus of(final String name) {
        switch (name) {
            case "STOP":
                return STOP;
            case "START":
                return START;
            default:
                return UNKNOWN;
        }
    }
}
