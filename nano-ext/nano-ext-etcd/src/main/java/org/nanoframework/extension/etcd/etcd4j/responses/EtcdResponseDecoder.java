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
package org.nanoframework.extension.etcd.etcd4j.responses;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;


public interface EtcdResponseDecoder<T> {
  /**
   * @param headers to decode with
   * @param content to decode
   * @return Object of type to which it is decoded to.
   * @throws IOException if content is faulty
   * @throws EtcdException If etcd returns an exception from the server
   */
  T decode(HttpHeaders headers, ByteBuf content)
      throws EtcdException, IOException;
}
