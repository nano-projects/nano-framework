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
package org.nanoframework.orm.rocketmq.exception;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
public class RocketMQProducerException extends RuntimeException {
    private static final long serialVersionUID = -5836008704632479642L;

    public RocketMQProducerException() {

    }

    public RocketMQProducerException(final String message) {
        super(message);
    }

    public RocketMQProducerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RocketMQProducerException(final Throwable cause) {
        super(cause);
    }
}
