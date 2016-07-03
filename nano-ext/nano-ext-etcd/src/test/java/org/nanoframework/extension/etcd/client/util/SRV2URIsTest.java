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
package org.nanoframework.extension.etcd.client.util;

import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author yanghe
 * @since 1.0
 */
@Ignore
public class SRV2URIsTest {

  @Test
  public void testFromDNSName() throws Exception {
    URI[] uris = SRV2URIs.fromDNSName("etcd4jtest.mousio.org");

    List<URI> toFind = new ArrayList<URI>(Arrays.asList(
        URI.create("http://test1.nl:4001"),
        URI.create("http://test2.nl:4001"),
        URI.create("http://test3.nl:4001")
    ));

    // Order is maybe not the same so walk till all are matched
    for (URI uri : uris) {
      if (toFind.contains(uri)) {
        toFind.remove(uri);
      } else {
        fail(uri + " not found in expected list");
      }
    }

    assertTrue(toFind.isEmpty());
  }
}