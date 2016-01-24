/*
 * Copyright (c) 2015, Jurriaan Mous and contributors as indicated by the @author tags.
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
package org.nanoframework.extension.etcd.etcd4j.requests;

import org.nanoframework.extension.etcd.client.retry.RetryPolicy;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdStoreStatsResponse;
import org.nanoframework.extension.etcd.etcd4j.transport.EtcdClientImpl;

import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Jurriaan Mous
 * @author Luca Burgazzoli
 *
 * An Etcd Store Stats Request
 */
public class EtcdStoreStatsRequest extends AbstractEtcdRequest<EtcdStoreStatsResponse> {

  /**
   * Constructor
   *
   * @param clientImpl   the client to handle this request
   * @param retryHandler handles retries
   */
  public EtcdStoreStatsRequest(EtcdClientImpl clientImpl, RetryPolicy retryHandler) {
    super("/v2/stats/store", clientImpl, HttpMethod.GET, retryHandler, EtcdStoreStatsResponse.DECODER);
  }

  @Override public EtcdStoreStatsRequest setRetryPolicy(RetryPolicy retryPolicy) {
    super.setRetryPolicy(retryPolicy);
    return this;
  }
}