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
package org.nanoframework.commons.crypt;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

/**
 *
 * @author yanghe
 * @since 1.3.7
 */
public final class NoOpCipherExecutor implements CipherExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpCipherExecutor.class);

    /**
     * Instantiates a new No op cipher executor.
     * Issues a warning on safety.
     */
    public NoOpCipherExecutor() {
        LOGGER.warn("[{}] does no encryption and may NOT be safe in a production environment. "
                + "Consider using other choices, such as [{}] that handle encryption, signing and verification of " + "all appropriate values.",
                this.getClass().getName(), DefaultCipherExecutor.class.getName());
    }

    @Override
    public String encode(final String value) {
        return value;
    }

    @Override
    public String decode(final String value) {
        return value;
    }
}
