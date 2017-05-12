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

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;

/**
 *
 * @since 0.9
 */
public class Md2Hash extends SimpleHash {
    private static final long serialVersionUID = -4439798446626919134L;

    public static final String ALGORITHM_NAME = "MD2";

    public Md2Hash() {
        super(ALGORITHM_NAME);
    }

    public Md2Hash(Object source) {
        super(ALGORITHM_NAME, source);
    }

    public Md2Hash(Object source, Object salt) {
        super(ALGORITHM_NAME, source, salt);
    }

    public Md2Hash(Object source, Object salt, int hashIterations) {
        super(ALGORITHM_NAME, source, salt, hashIterations);
    }

    public static Md2Hash fromHexString(String hex) {
        Md2Hash hash = new Md2Hash();
        hash.setBytes(Hex.decode(hex));
        return hash;
    }

    public static Md2Hash fromBase64String(String base64) {
        Md2Hash hash = new Md2Hash();
        hash.setBytes(Base64.decode(base64));
        return hash;
    }
}
