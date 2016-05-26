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

import javax.validation.constraints.NotNull;

/**
 * 
 * @author yanghe
 * @since 1.3.7
 */
public interface CipherExecutor {

    /**
     * Encrypt the value. Implementations may
     * choose to also sign the final value.
     * @param value the value
     * @return the encrypted value or null
     */
    String encode(@NotNull String value);

    /**
     * Decode the value. Signatures may also be verified.
     * @param value encrypted value
     * @return the decoded value. 
     */
    String decode(@NotNull String value);
}
