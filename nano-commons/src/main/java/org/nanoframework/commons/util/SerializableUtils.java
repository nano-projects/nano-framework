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
package org.nanoframework.commons.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.nanoframework.commons.exception.SerializationException;

/**
 * @author yanghe
 * @since 1.1
 */
public class SerializableUtils {
    public static <T> String encode(T object) {
        try {
            if (object == null) {
                return null;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            return ZipUtils.gzip(bos.toByteArray());
        } catch (Exception e) {
            throw new SerializationException("序列化对象异常: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T decode(String objectString) {
        try {
            if (StringUtils.isEmpty(objectString)) {
                return null;
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(ZipUtils.gunzipToByte(objectString));
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new SerializationException("反序列化对象异常: " + e.getMessage(), e);
        }
    }
}
