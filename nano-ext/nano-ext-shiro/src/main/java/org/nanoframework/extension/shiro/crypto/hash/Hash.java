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
package org.nanoframework.extension.shiro.crypto.hash;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.Sha384Hash;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.nanoframework.extension.shiro.util.ByteSource;

/**
 *
 * @see Md2Hash
 * @see Md5Hash
 * @see Sha1Hash
 * @see Sha256Hash
 * @see Sha384Hash
 * @see Sha512Hash
 * @since 0.9
 */
public interface Hash extends ByteSource {

    /**
     *
     * @return the the name of the algorithm used to hash the input source, for example, {@code SHA-256}, {@code MD5}, etc.
     * @since 1.1
     */
    String getAlgorithmName();

    /**
     * Returns a salt used to compute the hash or {@code null} if no salt was used.
     *
     * @return a salt used to compute the hash or {@code null} if no salt was used.
     * @since 1.2
     */
    ByteSource getSalt();

    /**
     * Returns the number of hash iterations used to compute the hash.
     *
     * @return the number of hash iterations used to compute the hash.
     * @since 1.2
     */
    int getIterations();

}
