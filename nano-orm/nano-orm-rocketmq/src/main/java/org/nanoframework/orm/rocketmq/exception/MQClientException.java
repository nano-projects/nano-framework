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
package org.nanoframework.orm.rocketmq.exception;

import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.help.FAQUrl;

/**
 *
 * @author yanghe
 * @since 0.0.1
 */
public class MQClientException extends RuntimeException {
    private static final long serialVersionUID = 2276992135449774109L;
    private final int responseCode;
    private final String errorMessage;

    /**
     *
     * @param errorMessage the errorMessage
     * @param cause the cause
     */
    public MQClientException(final String errorMessage, final Throwable cause) {
        super(FAQUrl.attachDefaultURL(errorMessage), cause);
        this.responseCode = -1;
        this.errorMessage = errorMessage;
    }

    /**
     *
     * @param responseCode the responseCode
     * @param errorMessage the errorMessage
     */
    public MQClientException(final int responseCode, final String errorMessage) {
        super(FAQUrl.attachDefaultURL("CODE: " + UtilAll.responseCode2String(responseCode) + "  DESC: "
            + errorMessage));
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
